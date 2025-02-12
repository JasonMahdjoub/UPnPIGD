/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.distrimind.upnp.support.avtransport.callback;

import com.distrimind.upnp.Log;
import com.distrimind.upnp.controlpoint.ActionCallback;
import com.distrimind.upnp.model.action.ActionInvocation;
import com.distrimind.upnp.model.meta.Service;
import com.distrimind.upnp.model.types.UnsignedIntegerFourBytes;
import com.distrimind.upnp.support.model.PlayMode;

import com.distrimind.flexilogxml.log.DMLogger;

/**
 * @author Christian Bauer
 */
public abstract class SetPlayMode extends ActionCallback {

    final private static DMLogger log = Log.getLogger(SetPlayMode.class);

    public SetPlayMode(Service<?, ?, ?> service, PlayMode playMode) {
        this(new UnsignedIntegerFourBytes(0), service, playMode);
    }

    public SetPlayMode(UnsignedIntegerFourBytes instanceId, Service<?, ?, ?> service, PlayMode playMode) {
        super(new ActionInvocation<>(service.getAction("SetPlayMode")));
        getActionInvocation().setInput("InstanceID", instanceId);
        getActionInvocation().setInput("NewPlayMode", playMode.toString());
    }

    @Override
    public void success(ActionInvocation<?> invocation) {
        log.debug("Execution successful");
    }
}