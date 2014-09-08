//
//  AppDelegate.h
//  shenanigans_osx
//
//  Created by dr on 7/23/14.
//  Copyright (c) 2014 Shenanigans.io. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#import <CoreWLAN/CoreWLAN.h>

#include "ProbeReqSniffer.h"


#define GROUP_RW_PERMISSION 0660
#define STOCK_BPF_PERMISSION 0600
#define CHECKBPF_ERROR -1
#define CHECKBPF_STOCK 0
#define CHECKBPF_ALTERED 1
#define CHECKBPF_SHENANIGANS 2
#define ID_SIZE 512
#define SHENANIGANS_VERSION "0.1a"

#define ID_KEY @"ApplicationID"
#define CERT_NAME @"shenanigans"

struct BPFCheckResult {
    
    bool processHasAccess;
    int permissionsType;
};

@interface AppDelegate : NSObject <NSApplicationDelegate, NSTabViewDelegate> {

@private
    
    AuthorizationRef    _authRef;
    ProbeReqSniffer sniffer;
    NSMutableArray * probeGroupNodes;
    NSMutableDictionary * probeGroupNodeDict;
    BPFCheckResult bpfCheckResult;
    std::vector<ProbeGroup*> selectedProbeGroups;
    NSMutableData *receivedData;
    NSString * persistentID;
    
    __weak NSButton *btnSelectDevice;
    __weak NSButton *_btnSubmitFingerprint;
    __weak NSButton *_btnGrantPermission;
    __weak NSTextField *installLabel;
    __weak NSBrowser *myBrowser;
    __unsafe_unretained NSTextView *submissionTextView;
}

@property (weak) IBOutlet NSTabView *tabView;

@property (assign) IBOutlet NSWindow *window;

@property (assign) NSTimer *repeatingTimer;

@property (weak) IBOutlet NSBrowser *myBrowser;

@property (atomic, copy,   readwrite) NSData *                  authorization;
@property (atomic, strong, readwrite) NSXPCConnection *         helperToolConnection;

- (void)targetMethod:(NSTimer*)theTimer;

@property (unsafe_unretained) IBOutlet NSTextView *submissionTextView;


@property (weak) IBOutlet NSTextField *installLabel;
@property (weak) IBOutlet NSButton *btnSelectDevice;

@property (weak) IBOutlet NSButton *btnGrantPermission;
@property (weak) IBOutlet NSButton *btnSubmitFingerprint;
@end


