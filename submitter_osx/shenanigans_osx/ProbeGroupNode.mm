//
//  ProbeGroupNode.m
//  shenanigans_osx
//
//  Created by dr on 7/30/14.
//  Copyright (c) 2014 Shenanigans.io. All rights reserved.
//

#import "ProbeGroupNode.h"

@implementation Node

@dynamic children;
@synthesize displayName;


@end

@implementation ProbeGroupNode


- (id)init:(ProbeGroup *)group {
    if (self = [super init]) {
        probeGroup = group;
        displayName = [NSString stringWithUTF8String:probeGroup->mac.c_str()];
    }
    return self;
}

@synthesize probeGroup;

- (NSString *)description {
    return [NSString stringWithFormat:@"%@ - %@", super.description, displayName];
}



/*
- (NSImage *)icon {
    return [[NSWorkspace sharedWorkspace] iconForFile:[_url path]];
}*/

/*
- (NSColor *)labelColor {
    id value = nil;
    [_url getResourceValue:&value forKey:NSURLLabelColorKey error:NULL];
    return value;
}*/

- (NSArray *)children {
    if (children == nil || _childrenDirty) {
        // This logic keeps the same pointers around, if possible.
        NSMutableDictionary *newChildren = [NSMutableDictionary new];
        
        std::vector<ProbeReq*>* probes = &probeGroup->probeReqs;
        for(std::vector<ProbeReq*>::iterator ptr = probes->begin(); ptr != probes->end(); ptr++) {
            // Use the filename as a key and see if it was around and reuse it, if possible
            ProbeReq *probeReq = *ptr;
            NSString * probeReqKey = [NSString stringWithUTF8String:probeReq->ssid.c_str()];
            
            if (children != nil) {
                ProbeReqNode *oldChild = [children objectForKey:probeReqKey];
                if (oldChild != nil) {
                    [newChildren setObject:oldChild forKey:probeReqKey];
                    continue;
                }
            }
            // We didn't find it, add a new one
            // Wrap the child url with our node
            ProbeReqNode *node = [[ProbeReqNode alloc] init:probeReq];
            [newChildren setObject:node forKey:probeReqKey];
            
        }
        children = newChildren;
        _childrenDirty = NO;
    }
    
    NSArray *result = [children allValues];
    

    result = [result sortedArrayUsingComparator:^(id obj1, id obj2) {
        NSString *objName = [obj1 displayName];
        NSString *obj2Name = [obj2 displayName];
        NSComparisonResult result = [objName compare:obj2Name options:NSNumericSearch | NSCaseInsensitiveSearch | NSWidthInsensitiveSearch | NSForcedOrderingSearch range:NSMakeRange(0, [objName length]) locale:[NSLocale currentLocale]];
        return result;
    }];
    return result;
}

- (void)invalidateChildren {
    _childrenDirty = YES;
}

@end

@implementation ProbeReqNode {

@private
    ProbeGroup *probeGroup;
    NSArray *children;

}

- (id)init:(ProbeReq *)req {
    if (self = [super init]) {
        probeReq = req;
        displayName = [NSString stringWithUTF8String:probeReq->ssid.c_str()];
    }
    return self;
}

@synthesize probeReq;


@end