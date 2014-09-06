//
//  NSURLConnectionWithData.m
//  shenanigans_osx
//
//  Created by dr on 9/3/14.
//  Copyright (c) 2014 Shenanigans.io. All rights reserved.
//

#import "NSURLConnectionWithData.h"

@implementation NSURLConnectionWithData
@synthesize receivedData;
@synthesize requestSuccess;
@synthesize requestFailed;

- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response
{
    // This method is called when the server has determined that it
    // has enough information to create the NSURLResponse object.
    
    // It can be called multiple times, for example in the case of a
    // redirect, so each time we reset the data.
    receivedData = [NSMutableData dataWithCapacity: 0];
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
    
    if (requestFailed) requestFailed(error);
    // inform the user
    NSLog(@"Connection failed! Error - %@ %@",
          [error localizedDescription],
          [[error userInfo] objectForKey:NSURLErrorFailingURLStringErrorKey]);
}

- (void)connectionDidFinishLoading:(NSURLConnection *)connection
{
    if (requestSuccess) requestSuccess(receivedData);
    NSLog(@"%@\n", [[NSString alloc]initWithData:receivedData encoding:NSUTF8StringEncoding]);
    
    // release receivedData
    receivedData = nil;
}

- (BOOL)connection:(NSURLConnection *)connection canAuthenticateAgainstProtectionSpace:(NSURLProtectionSpace *)protectionSpace {
    return [protectionSpace.authenticationMethod isEqualToString:NSURLAuthenticationMethodServerTrust];
}

- (void)connection:(NSURLConnection *)connection didReceiveAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge {
    if ([[[challenge protectionSpace] authenticationMethod] isEqualToString: NSURLAuthenticationMethodServerTrust]) {
        SecTrustRef serverTrust = [[challenge protectionSpace] serverTrust];
        (void) SecTrustEvaluate(serverTrust, NULL);
        
        NSData *myCert = [NSData dataWithContentsOfFile:[[NSBundle mainBundle] pathForResource: @"shenanigans" ofType: @"cer"]];
        if (myCert == nil){
            NSLog(@"Cert not found.");
            //[[challenge sender] cancelAuthenticationChallenge: challenge];
            [challenge.sender performDefaultHandlingForAuthenticationChallenge:challenge];
            return;
        }
        SecCertificateRef remoteCert = SecTrustGetCertificateAtIndex(serverTrust, 0);
        
        CFDataRef remoteCertData = SecCertificateCopyData(remoteCert);
        
        BOOL match = [myCert isEqualToData: (__bridge NSData *)remoteCertData];
        CFRelease(remoteCertData);
        
        if (match) {
            [[challenge sender] useCredential: [NSURLCredential credentialForTrust: serverTrust] forAuthenticationChallenge:challenge];
        } else {
            [[challenge sender] cancelAuthenticationChallenge: challenge];
        }
    }
}

- (BOOL)shouldTrustProtectionSpace:(NSURLProtectionSpace *)protectionSpace {
    
    return TRUE; // FIXME - implement cert pinning
}


@end


