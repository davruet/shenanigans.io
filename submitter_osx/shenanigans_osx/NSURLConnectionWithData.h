//
//  NSURLConnectionWithData.h
//  shenanigans_osx
//
//  Created by dr on 9/3/14.
//  Copyright (c) 2014 Shenanigans.io. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSURLConnectionWithData : NSURLConnection
@property(nonatomic, strong) NSMutableData *receivedData;
@property(nonatomic, copy) void (^requestSuccess)(NSData * data);
@property(nonatomic, copy) void (^requestFailed)(NSError * error);
@end
