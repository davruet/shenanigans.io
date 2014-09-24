//
//  AppDelegate.h
//  shenanigans_osx
//
//  Created by dr on 7/23/14.
//  Copyright (c) 2014 Shenanigans.io. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#import <CoreWLAN/CoreWLAN.h>
#import <WebKit/WebKit.h>

#include "ProbeReqSniffer.h"


#define GROUP_RW_PERMISSION 0660
#define STOCK_BPF_PERMISSION 0600
#define CHECKBPF_ERROR -1
#define CHECKBPF_STOCK 0
#define CHECKBPF_ALTERED 1
#define CHECKBPF_SHENANIGANS 2
#define ID_SIZE 512
#define SHENANIGANS_VERSION "0.1a"
#define REMOTE_BASE_URL @"https://submit.shenanigans.io:8023"
#define CONFIGURE_URL @"https://s3.amazonaws.com/shenanigans.io/configure/configure.html"
#define TEST_BASE_URL @"https://localhost:8023"

#define BASE_URL REMOTE_BASE_URL
#define SUBMIT_PAGE @"/submitFingerprint"
#define VERSION_CHECK_PAGE @"/versionCheck"
#define ID_KEY @"ApplicationID"
#define CERT_NAME @"shenanigans"

#define INTERFACE "en0"

struct BPFCheckResult {
    
    bool processHasAccess;
    int permissionsType;
};

@interface AppDelegate : NSObject <NSApplicationDelegate, NSTabViewDelegate> {

@private
    
    __weak NSTabView *_outerTabView;
    AuthorizationRef    _authRef;
    ProbeReqSniffer sniffer;
    NSMutableArray * probeGroupNodes;
    NSMutableDictionary * probeGroupNodeDict;
    BPFCheckResult initialBPFCheckResult;
    std::vector<ProbeGroup*> selectedProbeGroups;
    NSMutableData *receivedData;
    NSString * persistentID;
    
    __weak NSTextField *_submittingLabel;
    __weak NSImageView *_bigLogo;
    __weak NSButton *btnSelectDevice;
    __weak NSButton *_btnSubmitFingerprint;
    __weak NSButton *_btnGrantPermission;
    __weak NSTextField *installLabel;
    __weak NSTextField *_splashLabel;
    __weak NSTextField *_submitLabel;
    __weak WebView *_configureWebView;
    __weak NSButton *_btnSplashContinue;
    __weak NSBrowser *myBrowser;
    __unsafe_unretained NSTextView *submissionTextView;
}

@property (weak) IBOutlet NSTabView *tabView;

@property (assign) IBOutlet NSWindow *window;

@property (assign) NSTimer *repeatingTimer;

@property (weak) IBOutlet NSBrowser *myBrowser;

@property (atomic, copy,   readwrite) NSData *                  authorization;
@property (atomic, strong, readwrite) NSXPCConnection *         helperToolConnection;
@property (nonatomic, readwrite) void (*continueFunction);

- (void)timerUpdate:(NSTimer*)theTimer;

@property (unsafe_unretained) IBOutlet NSTextView *submissionTextView;


@property (weak) IBOutlet NSTextField *installLabel;
@property (weak) IBOutlet NSButton *btnSelectDevice;

@property (weak) IBOutlet NSButton *btnGrantPermission;
@property (weak) IBOutlet NSButton *btnSubmitFingerprint;
@property (weak) IBOutlet NSImageView *bigLogo;
@property (weak) IBOutlet NSTextField *splashLabel;
@property (weak) IBOutlet NSButton *btnSplashContinue;
@property (weak) IBOutlet NSTabView *outerTabView;
@property (weak) IBOutlet NSTextField *submittingLabel;
@property (weak) IBOutlet NSTextField *submitLabel;
@property (weak) IBOutlet WebView *configureWebView;
@end


