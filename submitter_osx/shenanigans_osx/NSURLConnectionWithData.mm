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
@synthesize certName;

- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response
{

    receivedData = [NSMutableData dataWithCapacity: 0];
    [receivedData setLength:0];
}

- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data
{
    [receivedData appendData:data];
}

- (void)connection:(NSURLConnection *)connection
didFailWithError:(NSError *)error
{
    
    if (requestFailed) requestFailed(error);
    // log
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
    printf("X\n\n\n\n");
    if ([[[challenge protectionSpace] authenticationMethod] isEqualToString: NSURLAuthenticationMethodServerTrust]) {
        SecTrustRef serverTrust = [[challenge protectionSpace] serverTrust];
        (void) SecTrustEvaluate(serverTrust, NULL);
        
        NSData *myCert = nil;
        if (certName != nil){
            myCert = [NSData dataWithContentsOfFile:[[NSBundle mainBundle] pathForResource: certName ofType: @"cer"]];

        }
        if (myCert == nil){
            NSLog(@"Cert not found.");
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

+ (BOOL)allowsAnyHTTPSCertificateForHost:(NSString *)host
{
    return YES;
}
@end


