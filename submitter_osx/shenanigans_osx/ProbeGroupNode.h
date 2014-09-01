//
//  ProbeGroupNode.h
//  shenanigans_osx
//
//  Created by dr on 7/30/14.
//  Copyright (c) 2014 Shenanigans.io. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#include "ProbeGroup.h"

@interface Node : NSObject {

@protected
    NSMutableDictionary *children;
    NSString *displayName;
}

@property(readonly, retain) NSArray *children;
@property(readonly, copy) NSString *displayName;

@end

@interface ProbeGroupNode : Node {

@private
    ProbeGroup *probeGroup;
    BOOL _childrenDirty;
}

- (id)init:(ProbeGroup *)group;
- (void)invalidateChildren;

@property(readonly) ProbeGroup *probeGroup;
//@property(readonly, retain) NSImage *icon;
//@property(readonly, retain) NSColor *labelColor;




@end

@interface ProbeReqNode : Node {

@private
    ProbeReq *probeReq;
}

@property(readonly) ProbeReq *probeReq;

- (id)init:(ProbeReq *)req;


@end
