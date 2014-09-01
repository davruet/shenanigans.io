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


// FIXME - TODO - stop the sniffer.
// TODO - add handling of server request (proper display of progress and errors)


@implementation AppDelegate
@synthesize btnSelectDevice;
@synthesize submissionTextView;
@synthesize installLabel;

@synthesize myBrowser;

-(int) doSomething:(int)arg
{
    return 0;
}


- (void)applicationDidFinishLaunching:(NSNotification *)aNotification
{
    probeGroupNodes = [[NSMutableArray alloc] init];
    probeGroupNodeDict = [[NSMutableDictionary alloc] init];
    
    [myBrowser setTitle:@"Device MAC" ofColumn:0];
    [myBrowser setTitle:@"Device networks" ofColumn:1];
    
    [btnSelectDevice setEnabled:NO];
    [submissionTextView setFont:[NSFont userFixedPitchFontOfSize:0.0]];
    //[myBrowser setAllowsBranchSelection:NO];
    //[myBrowser setAllowsMultipleSelection:YES];
    
    
    bpfCheckResult = [self checkBPFStatus];
    if (bpfCheckResult.processHasAccess){
        // no need to change permissions. Proceed to next step.
        //[[self tabView] selectNextTabViewItem:0];
        [installLabel setStringValue:@"Your WiFi hardware is already configured. Click \"Next...\" to continue."];
    } else {
        NSString* detailMessage;
        switch (bpfCheckResult.permissionsType){
            case CHECKBPF_ALTERED:
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
    
    // Add a listener for new probe groups to the probe request sniffer.
    // FIXME - not sure if passing the self reference here is safe or correct.
    ProbeGroupListener func = [self] (ProbeGroup * group, ProbeReq * req, int status){
        [self probeSeen:group req:req status:status];
    };
    sniffer.addNewGroupListener(func);
    
    /* IBSS snippet
    NSError * error = nil;
    CWInterface *en0 = [CWInterface interface];
    NSData *ssid = [@"shenanigans" dataUsingEncoding:NSUTF8StringEncoding];
    NSString *password = @"AFDBA";
    BOOL created = [en0 startIBSSModeWithSSID:ssid security:kCWIBSSModeSecurityWEP40 channel:6 password:password error:&error];
    NSLog(@"Created IBSS: %d", created);
    */
    
    
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
    //[myBrowser reloadColumn:0];
    
    //NSIndexSet *set = [NSIndexSet indexSetWithIndexesInRange: NSMakeRange(0, [myBrowser ])];
    
    //FIXME - need smart reloading.
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
        
        Poco::Mutex mutex;
        // now, execute the chmod bpf commands with the helper.
        NSLog(@"Installed chmod bpf helper tool.\n");
        
        [self connectAndExecuteCommandBlock:^(NSError * connectError) {
            if (connectError != nil) {
                [self logError:connectError];
                [self showError:connectError];
            } else {
                [[self.helperToolConnection remoteObjectProxyWithErrorHandler:^(NSError * proxyError) {
                    [self logError:proxyError];
                    [self showError:proxyError];
                }] chmodBPF:self.authorization withReply:^(NSError * commandError) {
                    if (commandError != nil) {
                        [self logError:commandError];
                        [self showError:commandError];
                    } else {
                        [self logText:@"chmod success."];
                        [self finishedInstall:sender];
                    }
                }];
            }
            
        }];
        
    } else {
        // Couldn't authenticate. FIXME -- add more informative error message and steps to resolve.
        [self logError:(__bridge NSError *) error];
        *nsError = (__bridge NSError *) error;
        CFRelease(error);
    }

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

- (void)finishedInstall:(id)sender;
{
    // check again to make sure that permission was correctly set.
    bpfCheckResult = [self checkBPFStatus];
    if (bpfCheckResult.processHasAccess){
        [[self tabView] selectNextTabViewItem:sender];
    } else {
        NSError * error = [self makeError:@"WiFi hardware could not be configured." reason:@"The helper tool tried to change the /dev/bpf* permissions but did not succeed." suggestion:@"Quit this application, follow the steps in the README for \"Manual BPF Permissions configuration,\" then re-launch this app." domain:@"BPF Permissions check"];
        [self showError:error];
    }
}

- (IBAction)install:(id)sender {
    bpfCheckResult = [self checkBPFStatus];
    if (!bpfCheckResult.processHasAccess){
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
                printf("File %s is not readable or writable:", filePath.UTF8String);
                result.processHasAccess = false;
            }
            
        }
        
        
        if (isStock) result.permissionsType = CHECKBPF_STOCK;
        else if (isShenanigans) result.permissionsType = CHECKBPF_SHENANIGANS;
        else result.permissionsType = CHECKBPF_ALTERED;
        
    }
    return result;

}

-(void)showError:(NSError *)error

{
    NSAlert *alert = [[NSAlert alloc] init];
    [alert addButtonWithTitle:@"OK"];
    [alert setMessageText:error.localizedDescription];
    [alert setInformativeText:error.localizedFailureReason];
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
                printf("Connection invalidated.");
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

- (IBAction)startListening:(id)sender {
    [[self tabView] selectNextTabViewItem:sender];

}

- (void)tabView:(NSTabView *)tabView didSelectTabViewItem:(NSTabViewItem *)tabViewItem{
    if ([[tabViewItem label] isEqualToString:@"Discover Devices"]){
        sniffer.start();
        
        if ([self repeatingTimer]) [self.repeatingTimer invalidate];
        
        NSTimer *timer = [NSTimer scheduledTimerWithTimeInterval:0.5
                                                          target:self selector:@selector(targetMethod:)
                                                        userInfo:nil repeats:YES];
        self.repeatingTimer = timer;
        
    }
    printf("will select item %s", [tabViewItem label].UTF8String );
}


- (void)targetMethod:(NSTimer*)theTimer {
    sniffer.update();
    /*
    std::map<std::string, ProbeGroup*>* groupMap = sniffer.getGroupMap();
    for (std::map<std::string, ProbeGroup*>::iterator it = groupMap->begin(); it != groupMap->end(); ++it){
        ProbeGroup* group = it->second;
        printf("MAC: %s", group->mac.c_str());
    }*/
    

}

// Browser delegate methods
/*
- (id)rootItemForBrowser:(NSBrowser *)browser {
    if (_rootNode == nil) {
        _rootNode = [[FileSystemNode alloc] initWithURL:[NSURL fileURLWithPath:@"/"]];
    }
    return _rootNode;
}*/

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


- (IBAction) browserCellSelected:(id)sender {
    
    NSArray * paths = [myBrowser selectionIndexPaths];
    [btnSelectDevice setEnabled:[paths count] > 0];
    selectedProbeGroups.clear();
    NSMutableString * submissionString = [NSMutableString new];
    for (NSIndexPath * indexPath in paths){
    //NSIndexPath *indexPath = [myBrowser selectionIndexPath];
        id myItem = [myBrowser itemAtIndexPath:indexPath];
        if ([myItem isKindOfClass:[ProbeGroupNode class]]){
            ProbeGroup * group = [myItem probeGroup];
           // printf("Selected: %s\n", group->mac.c_str());
            selectedProbeGroups.push_back(group);
            [submissionString appendString:@"MAC: "];
            [submissionString appendString:[NSString stringWithUTF8String:group->mac.c_str()]];
            [submissionString appendString:@" - SSIDs:\n"];
            std::vector<ProbeReq*>::iterator it;
            for (it = group->probeReqs.begin(); it != group->probeReqs.end(); it++){
                ProbeReq * req = *it;
                [submissionString appendString:[NSString stringWithUTF8String:req->ssid.c_str()]];
                /*if ((it + 1) != group->probeReqs.end()){
                    [submissionString appendString:@", "];
                }*/
                NSData * data = [NSData dataWithBytes:&(req->rawBytes[0]) length:req->rawBytes.size()];
                NSString * dataStr = [data base64EncodedStringWithOptions:NSDataBase64Encoding64CharacterLineLength];
                [submissionString appendString:@"\n"];
                [submissionString appendString:dataStr];
                [submissionString appendString:@"\n"];
                
                
            }
            [submissionString appendString:@"\n"];
            
        }
    }
    [submissionTextView setString:submissionString];
    NSLog(@"%@\n", submissionString);

    //[myItem probeGroup]
    
}
- (IBAction)selectDevice:(id)sender {
    [_tabView selectNextTabViewItem:sender];
}



- (IBAction)submitFingerprint:(id)sender {
    
    std::vector<ProbeGroup*>::iterator it;
    std::vector<ProbeReq*>::iterator reqIt;
    
    io::shenanigans::proto::Submission submission;
    

    for (it = selectedProbeGroups.begin(); it != selectedProbeGroups.end(); it++){
        io::shenanigans::proto::Submission_ProbeGroup *outputGroup = submission.add_group();
        
        
        ProbeGroup * probeGroup = *it;
        std::string tokenInput;
        for (reqIt = probeGroup->probeReqs.begin(); reqIt != probeGroup->probeReqs.end(); reqIt++){
            io::shenanigans::proto::Submission_ProbeGroup_ProbeReq *outputReq = outputGroup->add_req();
            ProbeReq * req = *reqIt;
            //packet_bytes::iterator byteIt;
            /*
            for (byteIt = req->rawBytes.begin(); byteIt != req->rawBytes.end(); byteIt++){
                outputbytes.push_back(*byteIt);
                i++;
            }*/
            outputReq->set_ssid(req->ssid);
            //outputReq->set_reqbytes(std::string(req->rawBytes.begin(), req->rawBytes.end()));
            outputReq->set_reqbytes(&(req->rawBytes[0]), req->rawBytes.size());
            tokenInput.insert(tokenInput.end(), req->rawBytes.begin(), req->rawBytes.end());
            
        }
        outputGroup->set_mac(probeGroup->mac);
        
        std::string *token = computeToken(&tokenInput);
        std::cout << "Token is: " << *token << std::endl;
        outputGroup->set_token(token->c_str(), token->size());
        delete token;
        
    }

    
    NSMutableURLRequest *postRequest = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:@"http://localhost:8000/submitFingerprint"]];
    
    // Set the request's content type to application/x-www-form-urlencoded
    [postRequest setValue:@"application/x-protobuf" forHTTPHeaderField:@"Content-Type"];
    
    // Designate the request a POST request and specify its body data
    [postRequest setHTTPMethod:@"POST"];
    std::string submissionStr = submission.SerializeAsString();
    
    std::cout << "SUBMISSION STRING: '" << submissionStr << "'" << std::endl;
    //NSString *string = [NSString stringWithUTF8String:submissionStr.c_str()];
    NSData *data = [NSData dataWithBytes:submissionStr.c_str() length:submissionStr.length()];
    //NSData *data = [string dataUsingEncoding:NSUTF8StringEncoding];
    NSLog(@"LENGTH:  %lu\n", (unsigned long)[data length]);
    [postRequest setHTTPBody:data];
    
    receivedData = [NSMutableData dataWithCapacity: 0];
    
    NSURLConnection *theConnection=[[NSURLConnection alloc] initWithRequest:postRequest delegate:self];
    if (!theConnection) {
           receivedData = nil;
        NSLog(@"%@\n",@"Connection failed.");
    } else {
         NSLog(@"%@\n",@"Connection is cool.");
    }
}

- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response
{
    // This method is called when the server has determined that it
    // has enough information to create the NSURLResponse object.
    
    // It can be called multiple times, for example in the case of a
    // redirect, so each time we reset the data.
    
    // receivedData is an instance variable declared elsewhere.
    [receivedData setLength:0];
}

- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data
{
    // Append the new data to receivedData.
    // receivedData is an instance variable declared elsewhere.
    [receivedData appendData:data];
}

- (void)connection:(NSURLConnection *)connection
  didFailWithError:(NSError *)error
{

    
    // inform the user
    NSLog(@"Connection failed! Error - %@ %@",
          [error localizedDescription],
          [[error userInfo] objectForKey:NSURLErrorFailingURLStringErrorKey]);
}

- (void)connectionDidFinishLoading:(NSURLConnection *)connection
{
    // do something with the data
    // receivedData is declared as a property elsewhere
    NSLog(@"Succeeded! Received %lu bytes of data",(unsigned long)[receivedData length]);
    NSString * filePath = [self pathForTemporaryFileWithPrefix: @"shenanigans"];
    BOOL written = [receivedData writeToFile:filePath atomically:NO];
    if (!written){
        NSLog(@"Couldn't write to file: '%@'.", filePath);
    } else {
        [[NSWorkspace sharedWorkspace] openFile:filePath withApplication:@"Preview"];
    }
    NSLog(@"%@\n", [[NSString alloc]initWithData:receivedData encoding:NSUTF8StringEncoding]);
    
    // release receivedData
    receivedData = nil;
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
