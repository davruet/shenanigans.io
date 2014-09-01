/*
     File: HelperTool.m
 Abstract: The main object in the helper tool.
  Version: 1.0
 
 Disclaimer: IMPORTANT:  This Apple software is supplied to you by Apple
 Inc. ("Apple") in consideration of your agreement to the following
 terms, and your use, installation, modification or redistribution of
 this Apple software constitutes acceptance of these terms.  If you do
 not agree with these terms, please do not use, install, modify or
 redistribute this Apple software.
 
 In consideration of your agreement to abide by the following terms, and
 subject to these terms, Apple grants you a personal, non-exclusive
 license, under Apple's copyrights in this original Apple software (the
 "Apple Software"), to use, reproduce, modify and redistribute the Apple
 Software, with or without modifications, in source and/or binary forms;
 provided that if you redistribute the Apple Software in its entirety and
 without modifications, you must retain this notice and the following
 text and disclaimers in all such redistributions of the Apple Software.
 Neither the name, trademarks, service marks or logos of Apple Inc. may
 be used to endorse or promote products derived from the Apple Software
 without specific prior written permission from Apple.  Except as
 expressly stated in this notice, no other rights or licenses, express or
 implied, are granted by Apple herein, including but not limited to any
 patent rights that may be infringed by your derivative works or by other
 works in which the Apple Software may be incorporated.
 
 The Apple Software is provided by Apple on an "AS IS" basis.  APPLE
 MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS
 FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND
 OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS.
 
 IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL
 OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION,
 MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED
 AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE),
 STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 
 Copyright (C) 2013 Apple Inc. All Rights Reserved.
 
 */

#import "PcapHelperTool.h"

#include <sys/socket.h>
#include <netinet/in.h>
#include <errno.h>
#include <syslog.h>

@interface HelperTool () <NSXPCListenerDelegate, HelperToolProtocol>

@property (atomic, strong, readwrite) NSXPCListener *    listener;

@end

@implementation HelperTool

- (id)init
{
    
    self = [super init];
    if (self != nil) {
        // Set up our XPC listener to handle requests on our Mach service.
        self->_listener = [[NSXPCListener alloc] initWithMachServiceName:kHelperToolMachServiceName];
        self->_listener.delegate = self;
    }
    return self;
}

- (void)run
{
    // Tell the XPC listener to start processing requests.

    [self.listener resume];
    
    // Run the run loop forever.
    
    [[NSRunLoop currentRunLoop] run];
}

- (BOOL)listener:(NSXPCListener *)listener shouldAcceptNewConnection:(NSXPCConnection *)newConnection
    // Called by our XPC listener when a new connection comes in.  We configure the connection
    // with our protocol and ourselves as the main object.
{
    assert(listener == self.listener);
    #pragma unused(listener)
    assert(newConnection != nil);

    newConnection.exportedInterface = [NSXPCInterface interfaceWithProtocol:@protocol(HelperToolProtocol)];
    newConnection.exportedObject = self;
    [newConnection resume];
    
    return YES;
}


- (void)chmodBPF:(NSData *)authData withReply:(void (^)(NSError *))reply
{
    syslog(6, "Calling shenanigans.");

    NSTask *task = [[NSTask alloc] init];
    
    NSMutableDictionary* details = [NSMutableDictionary dictionary];
    
    NSError* error;
    
    [task setLaunchPath: @"/bin/sh"];

    NSArray *arguments;
    arguments = [NSArray arrayWithObjects: @"-c", @"/usr/bin/chgrp admin /dev/bpf* && /bin/chmod g+rw /dev/bpf* || exit 1", nil];
    
    NSPipe *pipe;
    pipe = [NSPipe pipe];
    [task setStandardOutput: pipe];
    [task setStandardError: pipe];
    [task setArguments: arguments];
    
    NSFileHandle *file;
    file = [pipe fileHandleForReading];
    
    [task launch];
    [task waitUntilExit];
    
    int status = [task terminationStatus];
    NSData *data;
    data = [file readDataToEndOfFile];
    
    NSString* response = [[NSString alloc] initWithData: data
                                          encoding: NSUTF8StringEncoding];
    NSString* logMsg = [NSString stringWithFormat:@"PcapHelperTool output = %@", response];
    syslog(0, "%s", logMsg.UTF8String);

    
    if (status != 0){
        
        NSString* string = [NSString stringWithFormat:@"PcapHelperTool failed: output = %@", response];
        
        syslog (4, "%s", string.UTF8String);
        [details setValue:string forKey:NSLocalizedDescriptionKey];
        error = [NSError errorWithDomain:@"PcapHelperTool" code:1 userInfo:details];
        reply(error);

    }
    
    reply(nil);

}


#pragma mark * HelperToolProtocol implementation

// IMPORTANT: NSXPCConnection can call these methods on any thread.  It turns out that our 
// implementation of these methods is thread safe but if that's not the case for your code 
// you have to implement your own protection (for example, having your own serial queue and 
// dispatching over to it).


- (void)getVersionWithReply:(void(^)(NSString * version))reply
    // Part of the HelperToolProtocol.  Returns the version number of the tool.  Note that never
    // requires authorization.
{
    // We specifically don't check for authorization here.  Everyone is always allowed to get
    // the version of the helper tool.
    reply([[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleVersion"]);
}


@end
