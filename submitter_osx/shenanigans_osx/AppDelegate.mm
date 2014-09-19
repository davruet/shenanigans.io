//
//  AppDelegate.m
//  shenanigans_osx
//
//  Created by dr on 7/23/14.
//  Copyright (c) 2014 Shenanigans.io. All rights reserved.
//

#import "AppDelegate.h"
#import "PcapHelperTool.h"
#import "ProbeGroupNode.h"

#include <ServiceManagement/ServiceManagement.h>
#include <CoreFoundation/CoreFoundation.h>
#include <Security/Security.h>

#include <sstream>
#include <unistd.h>
#include <assert.h>
#include "shenanigans.pb.h"
#include "token.h"
#include "NSURLConnectionWithData.h"

#define MS_BETWEEN_UNIX_AND_Y2K 978307200000

// TODO - add handling of server request (proper display of progress and errors)


@implementation AppDelegate
@synthesize btnSelectDevice;
@synthesize submissionTextView;
@synthesize installLabel;
@synthesize continueFunction;

@synthesize myBrowser;

-(int) doSomething:(int)arg
{
    return 0;
}

-(long) getMillis
{
    CFTimeInterval interval = CFAbsoluteTimeGetCurrent();
    return lrint(interval * 1000) + MS_BETWEEN_UNIX_AND_Y2K;
}

- (void)checkConnection
{
    
    
    //FIXME -make sure that we handle cases where en0 is not the default.
    CWInterface *interface = [CWInterface interfaceWithName:[NSString stringWithUTF8String:INTERFACE]];
    if (![interface powerOn]){
        NSError * error = [self makeError:@"Your WiFi connection appears to be disabled." reason:@"WiFi must be enabled to proceed." suggestion:@"Please turn on WiFi." domain:@"WiFi"];
        [self showFancyError:error continueText:@"Retry..." continueFunc:@selector(checkConnection)];
        return;
    }
  
    io::shenanigans::proto::ServerStatusQuery query;

    query.set_date([self getMillis]);
    query.set_token([persistentID UTF8String]);
    query.set_version(SHENANIGANS_VERSION);
    
    NSLog(@"VERSION: %s\n", SHENANIGANS_VERSION);
    std::string queryStr = query.SerializeAsString();
    [self sendMessage:&queryStr url:[NSString stringWithFormat:@"%@%@", BASE_URL, VERSION_CHECK_PAGE] successCallback:^(NSData * data) {
        NSLog(@"Version check success." );
        io::shenanigans::proto::ServerStatusResponse response;
        bool parsed = response.ParseFromArray([data bytes], (int)[data length]);
        if (!parsed){
            NSError * error = [self makeError:@"Could not start Shenanigans." reason:@"The server replied with a corrupted response." suggestion:@"Try restarting this application later." domain:@"ServerConnect"];
            [self logError:error];
            [_splashLabel setStringValue:@"The server replied with an invalid response. Please let us know about this error, and we'll try to fix it!"];
            
        } else {
            switch (response.statuscode()) {
                case io::shenanigans::proto::ServerStatusResponse_StatusCode_CLIENT_MUST_UPGRADE:
                    {
                        NSError * error = [self makeError:@"This app is out of date." reason:@"You are using an old version of Shenanigans that isn't compatible with the server." suggestion:@"Please download the latest version from https://shenanigans.io/download" domain:@"ServerConnect"];
                        [self logError:error];
                        [self showFancyError:error continueText:nil continueFunc:nil];
                        break;
                    }
                case io::shenanigans::proto::ServerStatusResponse_StatusCode_SERVER_ABANDONED:
                {
                    NSError * error = [self makeError:@"Shenanigans is a zombie." reason:@"Nobody has checked in recently to confirm that everything on the server is still working. Proceed at your own risk." suggestion:@"Nothing you can really do about this, sorry!" domain:@"ServerConnect"];
                    [self logError:error];
                    [self showFancyError:error continueText:@"Proceed anyway..." continueFunc:@selector(continueFromError)];

                    break;
                }
                case io::shenanigans::proto::ServerStatusResponse_StatusCode_SERVER_SLOW:
                {
                    NSError * error = [self makeError:@"The server is currently under heavy load." reason:@"Lots of people are submitting their WiFi fingerprints right now, so please be patient, as your submission might not finish right away. Apologies for the delay!" suggestion:@"Thanks for your patience!" domain:@"ServerConnect"];
                    [self logError:error];
                    [self showFancyError:error continueText:@"Continue..." continueFunc:@selector(continueFromError) ];
                    break;
                }
                case io::shenanigans::proto::ServerStatusResponse_StatusCode_SERVER_ENGULFED_IN_FLAMES:
                {
                    NSError * error = [self makeError:@"The server is too busy." reason:@"Many apologies, but the server is practically engulfed in flames due to all of the activity. Please try again in an hour or so after we've put out the fires." suggestion:@"Please try again later." domain:@"ServerConnect"];
                    [self logError:error];
                    [self showFancyError:error continueText:nil continueFunc:nil];
                    break;
                }
                case io::shenanigans::proto::ServerStatusResponse_StatusCode_READY:
                    [_outerTabView selectNextTabViewItem:self];
                    break;
            }
        }
    } failureCallback:^(NSError * err) {
        NSError * userError = [self makeError:@"Could not start Shenanigans." reason:@"Could not contact the server. You'll need a working internet connection to submit your signature." suggestion:@"Make sure that you are connected to the internet." domain:@"ServerConnect"];
        [self logError:err];
        [self showFancyError:userError continueText:@"Try again" continueFunc:@selector(checkConnection)];
    }];

}



- (void)applicationDidFinishLaunching:(NSNotification *)aNotification

{
    
    // initialize the persistent ID
    [self initPersistentID];
    
    [_btnSplashContinue setHidden:true];
    
    [self checkConnection];
    probeGroupNodes = [[NSMutableArray alloc] init];
    probeGroupNodeDict = [[NSMutableDictionary alloc] init];
    
    [myBrowser setTitle:@"Device MAC" ofColumn:0];
    [myBrowser setTitle:@"Device networks" ofColumn:1];
    
    [btnSelectDevice setEnabled:NO];
    //[_btnSubmitFingerprint setEnabled:NO];
    [submissionTextView setFont:[NSFont userFixedPitchFontOfSize:0.0]];
    
    // initialize sniffing permissions
    [self checkAndFixPermissions];
    
    
    // Add a listener for new probe groups to the probe request sniffer.
    // TODO - make sure that passing the self reference here is safe + correct
    ProbeGroupListener func = [self] (ProbeGroup * group, ProbeReq * req, int status){
        [self probeSeen:group req:req status:status];
    };
    sniffer.addNewGroupListener(func);
    
    [_configureWebView setMainFrameURL:@"http://shenanigans.io/configure.html"];
    
    /* TODO, add IBSS feature- Start in IBSS mode:
    NSError * error = nil;
    CWInterface *en0 = [CWInterface interface];
    NSData *ssid = [@"shenanigans" dataUsingEncoding:NSUTF8StringEncoding];
    NSString *password = @"AFDBA";
    BOOL created = [en0 startIBSSModeWithSSID:ssid security:kCWIBSSModeSecurityWEP40 channel:6 password:password error:&error];
    NSLog(@"Created IBSS: %d", created);
    */
    
}

- (void)checkAndFixPermissions{
    initialBPFCheckResult = [self checkBPFStatus];
    if (initialBPFCheckResult.processHasAccess){
        // no need to change permissions. Proceed to next step.
        //[[self tabView] selectNextTabViewItem:0];
        [installLabel setStringValue:@"Your WiFi hardware is already configured. Click \"Next...\" to continue."];
        [_btnGrantPermission setTitle:@"Next..."];
    } else {
        NSString* detailMessage;
        switch (initialBPFCheckResult.permissionsType){
            case CHECKBPF_ALTERED:
                // FIXME - add another option to continue without changing.
                detailMessage = @"Access to the /dev/bpf* devices has been been changed by another packet sniffing app on your system, and we don't want to mess anything up! If you proceed, the other app may be affected.";
                [installLabel setStringValue:detailMessage];
                break;
            case CHECKBPF_SHENANIGANS:
                detailMessage = @"The /dev/bpf* permissions have been successfully modified, but to access the WiFi hardware you must run this app as a user that's a member of the admin group.";
                [installLabel setStringValue:detailMessage];
                //
            case CHECKBPF_ERROR: {
                NSAlert *alert = [[NSAlert alloc] init];
                [alert addButtonWithTitle:@"OK"];
                [alert setMessageText:@"Couldn't check permissions for WiFi hardware."];
                [alert setInformativeText:@"This app can't proceed. Sorry!!"];
                [alert setAlertStyle:NSWarningAlertStyle];
                
                [alert runModal];
                [NSApp performSelector:@selector(terminate:) withObject:nil afterDelay:0.0];
            }
            case CHECKBPF_STOCK:
                break;
        }
    }

}

- (void)initPersistentID {
    NSDictionary *appDefaults = [NSDictionary
                                 dictionaryWithObject:@"" forKey:ID_KEY];
    [[NSUserDefaults standardUserDefaults] registerDefaults:appDefaults];

    NSString * id = [[NSUserDefaults standardUserDefaults] stringForKey:ID_KEY];
    if ([id isEqualToString:@""]) {
        uint8_t buf[ID_SIZE];
        arc4random_buf(&buf, ID_SIZE);
        NSData * data = [NSData dataWithBytes:buf length:ID_SIZE];
        NSString * dataStr = [data base64Encoding];
        
        [[NSUserDefaults standardUserDefaults] setObject:dataStr forKey:ID_KEY];
        [[NSUserDefaults standardUserDefaults] synchronize];
        persistentID = dataStr;
    } else {
        persistentID = id;
    }

}

- (void)probeSeen:(ProbeGroup *)group req:(ProbeReq *)req status:(int)status
{
    switch (status) {
        case STATUS_NEW_GROUP: {
            ProbeGroupNode * node = [[ProbeGroupNode alloc] init:group];
            [probeGroupNodes insertObject:node atIndex:0];
            [probeGroupNodeDict setObject:node forKey:[NSString stringWithUTF8String:group->mac.c_str()]];
            break;
        }
        case STATUS_NEW_SSID:{
            ProbeGroupNode* node = (ProbeGroupNode*)[probeGroupNodeDict objectForKey:[NSString stringWithUTF8String:group->mac.c_str()]];
            if (node){
                [node invalidateChildren];
            } else {
                NSLog(@"Error - received SSID for nonexistant MAC: %s.\n", group->mac.c_str());
            }
            break;
        }
    }
    
    //FIXME - need smart reloading, maybe?
    //[myBrowser reloadColumn:0];
    //NSIndexSet *set = [NSIndexSet indexSetWithIndexesInRange: NSMakeRange(0, [myBrowser ])];
    
    for (NSInteger col = [myBrowser lastColumn]; col >= 0; col--){
        //[myBrowser reloadDataForRowIndexes:set inColumn:col];
        [myBrowser reloadColumn:col];
    }
}

- (BOOL)applicationShouldTerminateAfterLastWindowClosed:(NSApplication *)sender
{
#pragma unused(sender)
    return YES;
}

// FIXME / TODO - factor this out into another class.
- (void)callHelperToChmodBPF:(id)sender error:(NSError **)nsError
{
    OSStatus                    err;
    AuthorizationExternalForm   extForm;
    
    err = AuthorizationCreate(NULL, NULL, 0, &self->_authRef);
    if (err == errAuthorizationSuccess) {
        err = AuthorizationMakeExternalForm(self->_authRef, &extForm);
    }
    if (err == errAuthorizationSuccess) {
        self.authorization = [[NSData alloc] initWithBytes:&extForm length:sizeof(extForm)];
    }
    assert(err == errAuthorizationSuccess);
    
    Boolean             success;
    CFErrorRef          error;
    
    success = SMJobBless(
                         kSMDomainSystemLaunchd,
                         CFSTR("com.davidrueter.shenanigans.PcapHelperTool"),
                         self->_authRef,
                         &error
                         );
    
    if (success) {
        
        // now, execute the chmod bpf commands with the helper.
        NSLog(@"Installed chmod bpf helper tool.\n");
        
        [self callHelper:true];
    } else {
        // Couldn't authenticate. FIXME -- add more informative error message and steps to resolve.
        [self logError:(__bridge NSError *) error];
        *nsError = (__bridge NSError *) error;
        CFRelease(error);
    }

}

- (void) callHelper:(BOOL)enable

{
    [self connectAndExecuteCommandBlock:^(NSError * connectError) {
        if (connectError != nil) {
            [self logError:connectError];
            [self showError:connectError];
        } else {
            [[self.helperToolConnection remoteObjectProxyWithErrorHandler:^(NSError * proxyError) {
                [self logError:proxyError];
                [self showError:proxyError];
            }] chmodBPF:self.authorization enable:enable withReply:^(NSError * commandError) {
                if (commandError != nil) {
                    [self logError:commandError];
                    [self showError:commandError];
                } else {
                    [self logText:@"chmod success."];
                    [self finishedInstall];
                }
            }];
        }
        
    }];

}

- (NSError *)makeError:(NSString*)msg reason:(NSString*)reason suggestion:(NSString*)suggestion domain:(NSString*)domain;
{
    NSError * error;
    NSDictionary *userInfo = @{
                               NSLocalizedDescriptionKey: NSLocalizedString(msg, nil),
                               NSLocalizedFailureReasonErrorKey: NSLocalizedString(reason, nil),
                               NSLocalizedRecoverySuggestionErrorKey: NSLocalizedString(suggestion, nil)};
    
    error = [NSError errorWithDomain:domain code:2 userInfo:userInfo];
    return error;
}

- (void)finishedInstall
{
    // check again to make sure that permission was correctly set.
    BPFCheckResult bpfCheckResult = [self checkBPFStatus];
    if (bpfCheckResult.processHasAccess){
        [[self tabView] selectNextTabViewItem:self];
    } else {
        NSError * error = [self makeError:@"WiFi hardware could not be configured." reason:@"The helper tool tried to change the /dev/bpf* permissions but did not succeed." suggestion:@"Quit this application, follow the steps in the README for \"Manual BPF Permissions configuration,\" then re-launch this app." domain:@"BPF Permissions check"];
        [self showError:error];
    }
}

- (IBAction)install:(id)sender {
    // Check again just to make sure nothing has changed -- the user may have performed modifications of their own.
    initialBPFCheckResult = [self checkBPFStatus];
    if (!initialBPFCheckResult.processHasAccess){
        NSError * error;
        [self callHelperToChmodBPF:sender error:&error];
        if (error != nil){
            // there was an error. Report.
            [self showError:error];
        }
        
    } else {
        NSLog(@"Skipping install, process already has access.");
        [[self tabView] selectNextTabViewItem:sender];
    }
    
}

-(BPFCheckResult)checkBPFStatus
{
    NSFileManager *fileManager = [NSFileManager defaultManager];
    BPFCheckResult result;
    if ([fileManager changeCurrentDirectoryPath:@"/dev"] == NO){
        // FIXME -- report this
        result.permissionsType = CHECKBPF_ERROR;
        result.processHasAccess = false;
    } else {
        NSError *error;
        NSArray *directoryContents = [fileManager contentsOfDirectoryAtPath:@"/dev"
                                                                      error:&error];
        
        // FIXME - error check for directory listing
        
        NSString *match = @"bpf*";
        NSPredicate *predicate = [NSPredicate predicateWithFormat:@"SELF like %@", match];
        NSArray *results = [directoryContents filteredArrayUsingPredicate:predicate];
        
        result.processHasAccess =  [results count] > 0;
        bool isStock = result.processHasAccess;
        bool isShenanigans = result.processHasAccess;
        
        for (NSString *filePath in results) {
            NSUInteger permissions;
            permissions=[[fileManager attributesOfItemAtPath:filePath error:nil] filePosixPermissions];
            if (permissions != GROUP_RW_PERMISSION){
                isShenanigans = false;
                
            }
            if (permissions != STOCK_BPF_PERMISSION){
                isStock = false;
            }
            if (![fileManager isWritableFileAtPath:filePath] || ![fileManager isReadableFileAtPath:filePath]){
                NSLog(@"File %@ is not readable or writable:", filePath);
                result.processHasAccess = false;
            }
            
        }
        
        
        if (isStock) result.permissionsType = CHECKBPF_STOCK;
        else if (isShenanigans) result.permissionsType = CHECKBPF_SHENANIGANS;
        else result.permissionsType = CHECKBPF_ALTERED;
        
    }
    return result;

}

-(void)showFancyError:(NSError *)error continueText:(NSString *)continueText continueFunc:(SEL)continueFunc

{
   
    NSString * combined = [NSString stringWithFormat:@"%@\n%@\n%@", error.localizedDescription, error.localizedFailureReason, error.localizedRecoverySuggestion];
    [self showModalStatus:combined continueText:continueText continueFunc:continueFunc];
}

-(void) showModalStatus:(NSString *)statusMsg continueText:(NSString *)continueText continueFunc:(SEL)continueFunc

{
    continueFunction = continueFunc;
    if (continueFunc)[_btnSplashContinue setAction:continueFunc];
    [_splashLabel setStringValue:statusMsg];
    if (continueText != nil) [_btnSplashContinue setTitle:continueText];
    [_btnSplashContinue setHidden:(continueText == nil)];
    
    NSTabViewItem * errorPane = [_outerTabView tabViewItemAtIndex:0];
    if (_outerTabView.selectedTabViewItem != errorPane){
        [_outerTabView selectTabViewItem:errorPane];
    }

}

-(void)showError:(NSError *)error

{
    NSAlert *alert = [[NSAlert alloc] init];
    [alert addButtonWithTitle:@"OK"];
    if (error.localizedDescription != nil){
        [alert setMessageText:error.localizedDescription];
    } else {
        [alert setMessageText:@"An error occurred -- please try again later."];
    }
    if (error.localizedFailureReason != nil) [alert setInformativeText:error.localizedFailureReason];
    [alert setAlertStyle:NSWarningAlertStyle];
    
    [alert runModal];
    //[alert release];

}

-(void)removeHelperTool

{
    SMJobRemove( kSMDomainSystemLaunchd, CFSTR("com.davidrueter.shenanigans.PcapHelperTool"),
                self->_authRef,
                false,
                NULL);
    AuthorizationFree( self->_authRef, 0 );

}

- (void)connectToHelperTool
// Ensures that we're connected to our helper tool.
{
    assert([NSThread isMainThread]);
    if (self.helperToolConnection == nil) {
        self.helperToolConnection = [[NSXPCConnection alloc] initWithMachServiceName:kHelperToolMachServiceName options:NSXPCConnectionPrivileged];
        self.helperToolConnection.remoteObjectInterface = [NSXPCInterface interfaceWithProtocol:@protocol(HelperToolProtocol)];
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Warc-retain-cycles"
        // We can ignore the retain cycle warning because a) the retain taken by the
        // invalidation handler block is released by us setting it to nil when the block
        // actually runs, and b) the retain taken by the block passed to -addOperationWithBlock:
        // will be released when that operation completes and the operation itself is deallocated
        // (notably self does not have a reference to the NSBlockOperation).
        self.helperToolConnection.invalidationHandler = ^{
            // If the connection gets invalidated then, on the main thread, nil out our
            // reference to it.  This ensures that we attempt to rebuild it the next time around.
            self.helperToolConnection.invalidationHandler = nil;
            [[NSOperationQueue mainQueue] addOperationWithBlock:^{
                self.helperToolConnection = nil;
                NSLog(@"%@",@"Connection invalidated.");
            }];
        };
#pragma clang diagnostic pop
        [self.helperToolConnection resume];
    }
}

- (void)connectAndExecuteCommandBlock:(void(^)(NSError *))commandBlock
// Connects to the helper tool and then executes the supplied command block on the
// main thread, passing it an error indicating if the connection was successful.
{
    assert([NSThread isMainThread]);
    
    // Ensure that there's a helper tool connection in place.
    
    [self connectToHelperTool];
    
    // Run the command block.  Note that we never error in this case because, if there is
    // an error connecting to the helper tool, it will be delivered to the error handler
    // passed to -remoteObjectProxyWithErrorHandler:.  However, I maintain the possibility
    // of an error here to allow for future expansion.
    
    commandBlock(nil);
}

- (IBAction)nextTab:(id)sender {
    [[self tabView] selectNextTabViewItem:sender];
    
}

- (IBAction)continueFromError {
    [_outerTabView selectNextTabViewItem:self];
}


- (void)tabView:(NSTabView *)tabView didSelectTabViewItem:(NSTabViewItem *)tabViewItem{
    // Start sniffing before the user adds the network - adding the network triggers a probe request.
    if ([[tabViewItem label] isEqualToString:@"Configure Device"] || [[tabViewItem label] isEqualToString:@"Select Device"]){
        if (!sniffer.isRunning()){
            sniffer.start(INTERFACE);
            
            // start the UI redraw / sniffer update timer.
            if ([self repeatingTimer]) [self.repeatingTimer invalidate];
            
            NSTimer *timer = [NSTimer scheduledTimerWithTimeInterval:0.5
                                                            target:self selector:@selector(timerUpdate:)
                                                            userInfo:nil repeats:YES];
            self.repeatingTimer = timer;
            NSLog(@"%@", @"Started sniffer and timer.");
        }
    } else {
        
        if ([self repeatingTimer]) {
            [[self repeatingTimer] invalidate];
            self.repeatingTimer = nil;
            NSLog(@"%@", @"Stopped timer.");

        }
        if (sniffer.isRunning()){
            sniffer.stop();
            NSLog(@"%@", @"Stopped Sniffer.");

        }
    }
    
}


- (void)timerUpdate:(NSTimer*)theTimer {
    sniffer.update();
}

// Browser delegate methods


- (NSIndexSet *)browser:(NSBrowser *)browser selectionIndexesForProposedSelection:(NSIndexSet *)proposedSelectionIndexes inColumn:(NSInteger)column
{
    /*
    NSMutableIndexSet * set = [[NSMutableIndexSet alloc] init];
    [proposedSelectionIndexes enumerateIndexesUsingBlock:^(NSUInteger idx, BOOL *stop) {
       [proposedSelectionIndexes ]
    }];
    return set;*/
    return [proposedSelectionIndexes indexesPassingTest:^BOOL(NSUInteger idx, BOOL *stop) {
        return YES;
    }];
}

- (NSInteger)browser:(NSBrowser *)browser numberOfChildrenOfItem:(id)item {
    if (item){
        Node *node = (Node *)item;
        return [[node children] count];
    } else {
        return [probeGroupNodes count];
    }
}

- (id)browser:(NSBrowser *)browser child:(NSInteger)index ofItem:(id)item {
    if (item){
        Node *node = (Node *)item;
        return [node.children objectAtIndex:index];
    } else {
        return [probeGroupNodes objectAtIndex:index];
    }
}

- (BOOL)browser:(NSBrowser *)browser isLeafItem:(id)item {
    if (!item) return [probeGroupNodes count] == 0;
    return [item isKindOfClass:[ProbeReqNode class]];
}

- (id)browser:(NSBrowser *)browser objectValueForItem:(id)item {
    if (!item) return @"";
    return ((Node *)item).displayName;
}


- (BOOL)tabView:(NSTabView *)tabView shouldSelectTabViewItem:(NSTabViewItem *)tabViewItem {
    if ([[tabViewItem identifier] intValue] == 3){
        return [self getSelectedDeviceCount] > 0;
    } else {
        return YES;
    }
}

- (int) getSelectedDeviceCount {
    NSArray * paths = [myBrowser selectionIndexPaths];
    return (int)[paths count];
}

- (IBAction) browserCellSelected:(id)sender {
    
    NSArray * paths = [myBrowser selectionIndexPaths];
    [btnSelectDevice setEnabled:([self getSelectedDeviceCount] > 0)];
    /*NSTabViewItem *submitItem = [_tabView tabViewItemAtIndex:3];
    [_btnSubmitFingerprint setEnabled:[paths count] > 0];*/
    selectedProbeGroups.clear();
    NSMutableString * submissionString = [NSMutableString new];
    for (NSIndexPath * indexPath in paths){
        id myItem = [myBrowser itemAtIndexPath:indexPath];
        ProbeGroup * group;
        if ([myItem isKindOfClass:[ProbeGroupNode class]]){
            group = [myItem probeGroup];
        } else if ([myItem isKindOfClass:[ProbeReqNode class]]){
            group = [myItem parentGroup];
        } else {
            // something else was put in, we don't know what it is.
            NSLog(@"%@", @"Invalid node found.");
            continue;
        }
        selectedProbeGroups.push_back(group);
        [submissionString appendString:@"MAC: "];
        [submissionString appendString:[NSString stringWithUTF8String:group->mac.c_str()]];
        [submissionString appendString:@" - SSIDs:\n"];
        std::vector<ProbeReq*>::iterator it;
        for (it = group->probeReqs.begin(); it != group->probeReqs.end(); it++){
            ProbeReq * req = *it;
            [submissionString appendString:[NSString stringWithUTF8String:req->ssid.c_str()]];

            NSData * data = [NSData dataWithBytes:&(req->rawBytes[0]) length:req->rawBytes.size()];

            NSString * dataStr = [data base64Encoding];
            //NSString * dataStr = [data base64EncodedStringWithOptions:NSDataBase64Encoding64CharacterLineLength];
            [submissionString appendString:@"\n"];
            [submissionString appendString:dataStr];
            [submissionString appendString:@"\n"];
            
            
        }
        [submissionString appendString:@"\n"];
            
        
    }
    [submissionTextView setString:submissionString];

    
}

// end browser delegate

- (IBAction)selectDevice:(id)sender {
    [_tabView selectNextTabViewItem:sender];
}


- (void)sendMessage:(std::string *)message url:(NSString *)url successCallback:(void(^)(NSData *))success failureCallback:(void(^)(NSError *))failure{
    NSLog(@"Sending message: %s\n", message->c_str());
    NSData *data = [NSData dataWithBytes:message->c_str() length:message->length()];
    
    NSMutableURLRequest *postRequest = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:url]];
    
    [postRequest setValue:@"application/x-protobuf" forHTTPHeaderField:@"Content-Type"];
    [postRequest setHTTPMethod:@"POST"];
    [postRequest setHTTPBody:data];
    
    NSURLConnectionWithData *conn = [NSURLConnectionWithData alloc];
    
    conn.requestSuccess =  success;
    conn.requestFailed = failure;
    conn.certName = CERT_NAME;
    
    conn = [conn initWithRequest:postRequest delegate:conn];
    
    
}

- (BOOL) validateConsent

{
    std::vector<ProbeGroup *>::iterator iterator;
    for (iterator = selectedProbeGroups.begin(); iterator != selectedProbeGroups.end(); iterator++){
        ProbeGroup * group = *iterator;
        if (!group->consent) return NO;
    }
    return YES;
}

- (void)showSelectDevice

{
    [_outerTabView selectTabViewItemAtIndex:1];
    [_tabView selectTabViewItemAtIndex:2];
}


- (IBAction)submitFingerprint:(id)sender {
    if (![self validateConsent]){
        NSError * error = [self makeError:@"One or more of the selected devices has not given consent by adding a network named \"shenanigans.\"" reason:@"You can only submit fingerprints of consenting devices." suggestion:nil domain:@"ConsentFailure"];
        [self showFancyError:error continueText:@"Change selection..." continueFunc:@selector(showSelectDevice)];
        return;
    }
    //[_outerTabView selectNextTabViewItem:sender];
    [self showModalStatus:@"Submitting fingerprints and downloading Certificate of De-identification..." continueText:nil continueFunc:nil];
    dispatch_async(dispatch_get_global_queue( DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        std::vector<ProbeGroup*>::iterator it;
        std::vector<ProbeReq*>::iterator reqIt;
        
        io::shenanigans::proto::Submission submission;
        submission.set_token([persistentID UTF8String]);
        
        for (it = selectedProbeGroups.begin(); it != selectedProbeGroups.end(); it++){
            io::shenanigans::proto::Submission_ProbeGroup *outputGroup = submission.add_group();
            
            
            ProbeGroup * probeGroup = *it;
            std::string tokenInput;
            for (reqIt = probeGroup->probeReqs.begin(); reqIt != probeGroup->probeReqs.end(); reqIt++){
                io::shenanigans::proto::Submission_ProbeGroup_ProbeReq *outputReq = outputGroup->add_req();
                ProbeReq * req = *reqIt;
                outputReq->set_ssid(req->ssid);
                
                outputReq->set_reqbytes(&(req->rawBytes[0]), req->rawBytes.size());
                tokenInput.insert(tokenInput.end(), req->rawBytes.begin(), req->rawBytes.end());
                
            }
            outputGroup->set_mac(probeGroup->mac);
            
            std::string *token = computeToken(&tokenInput);
            std::cout << "Token is: " << *token << std::endl;
            outputGroup->set_token(token->c_str(), token->size());
            delete token;
            
        }
        submission.set_date([self getMillis]);
        submission.set_token([persistentID UTF8String]);
        dispatch_async(dispatch_get_main_queue(), ^{
            std::string message = submission.SerializeAsString();
            [self sendMessage:&message url:[NSString stringWithFormat:@"%@%@", BASE_URL, SUBMIT_PAGE] successCallback:^(NSData * data){
                
                NSLog(@"Received %lu bytes of data",(unsigned long)[data length]);
                NSString * filePath = [self pathForTemporaryFileWithPrefix: @"shenanigans"];
                BOOL written = [data writeToFile:filePath atomically:NO];
                if (!written){
                    NSLog(@"Couldn't write to file: '%@'.", filePath);
                } else {
                     // FIXME - if preview has been closed, and we submit another item, we get a crash: malloc: *** error for object 0x600000019580: Freeing already free'd pointer
                    BOOL opened = [[NSWorkspace sharedWorkspace] openFile:filePath withApplication:@"Preview" ];
                    if (!opened){ // FIXME - test this more and handle more error cases.
                        NSError * error = [self makeError:@"Couldn't create a valid certificate." reason:@"The server responded with an invalid document." suggestion:@"Contact a site administrator" domain:@"ServerConnect"];
                        [self showFancyError:error continueText:@"Try again..." continueFunc:@selector(continueFromError)];


                    } else {
                        [self showModalStatus:@"Your WiFi fingerprints are now in the Shenanigans database. Thank you for your participation. Soon, you'll be everywhere!" continueText:nil continueFunc:nil]; // FIXME - add quit
                    }
                    
                }
            }
              failureCallback:^(NSError * error){
                  [self showFancyError:error continueText:@"Try again..." continueFunc:@selector(continueFromError)];
                
              }];

        });
        
    });
}



- (void)logText:(NSString *)text
// Logs the specified text to the text view.
{
    assert(text != nil);
    NSLog(@"%@", text);
   
}

- (void)logWithFormat:(NSString *)format, ...
// Logs the formatted text to the text view.
{
    va_list ap;
    
    // any thread
    assert(format != nil);
    
    va_start(ap, format);
    [self logText:[[NSString alloc] initWithFormat:format arguments:ap]];
    va_end(ap);
}

- (void)logError:(NSError *)error
// Logs the error to the text view.
{
    // any thread
    assert(error != nil);
    [self logWithFormat:@"error %@ / %d\n", [error domain], (int) [error code]];
}

- (NSString *)pathForTemporaryFileWithPrefix:(NSString *)prefix
{
    NSString *  result;
    CFUUIDRef   uuid;
    CFStringRef uuidStr;
    
    uuid = CFUUIDCreate(NULL);
    assert(uuid != NULL);
    
    uuidStr = CFUUIDCreateString(NULL, uuid);
    assert(uuidStr != NULL);
    
    result = [NSTemporaryDirectory() stringByAppendingPathComponent:[NSString stringWithFormat:@"%@-%@", prefix, uuidStr]];
    assert(result != nil);
    
    CFRelease(uuidStr);
    CFRelease(uuid);
    
    return result;
}


@end
