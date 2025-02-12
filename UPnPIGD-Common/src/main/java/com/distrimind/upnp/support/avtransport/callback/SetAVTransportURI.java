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

import com.distrimind.upnp.controlpoint.ActionCallback;
import com.distrimind.upnp.model.action.ActionInvocation;
import com.distrimind.upnp.model.meta.Service;
import com.distrimind.upnp.model.types.UnsignedIntegerFourBytes;

import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.upnp.Log;

/**
 * @author Christian Bauer
 */
public abstract class SetAVTransportURI extends ActionCallback {

    final private static DMLogger log = Log.getLogger(SetAVTransportURI.class);

    public SetAVTransportURI(Service<?, ?, ?> service, String uri) {
        this(new UnsignedIntegerFourBytes(0), service, uri, null);
    }

    public SetAVTransportURI(Service<?, ?, ?> service, String uri, String metadata) {
        this(new UnsignedIntegerFourBytes(0), service, uri, metadata);
    }

    public SetAVTransportURI(UnsignedIntegerFourBytes instanceId, Service<?, ?, ?> service, String uri) {
        this(instanceId, service, uri, null);
    }

    public SetAVTransportURI(UnsignedIntegerFourBytes instanceId, Service<?, ?, ?> service, String uri, String metadata) {
        super(new ActionInvocation<>(service.getAction("SetAVTransportURI")));
		if (log.isDebugEnabled()) {
            log.debug("Creating SetAVTransportURI action for URI: " + uri);
		}
		getActionInvocation().setInput("InstanceID", instanceId);
        getActionInvocation().setInput("CurrentURI", uri);
        getActionInvocation().setInput("CurrentURIMetaData", metadata);
    }

    @Override
    public void success(ActionInvocation<?> invocation) {
        log.debug("Execution successful");
    }
}