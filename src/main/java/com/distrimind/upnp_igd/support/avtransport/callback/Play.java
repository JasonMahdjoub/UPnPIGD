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

package com.distrimind.upnp_igd.support.avtransport.callback;

import com.distrimind.upnp_igd.controlpoint.ActionCallback;
import com.distrimind.upnp_igd.model.action.ActionInvocation;
import com.distrimind.upnp_igd.model.meta.Service;
import com.distrimind.upnp_igd.model.types.UnsignedIntegerFourBytes;

import java.util.logging.Logger;

/**
 *
 * @author Christian Bauer
 */
public abstract class Play extends ActionCallback {

    private static Logger log = Logger.getLogger(Play.class.getName());

    public Play(Service service) {
        this(new UnsignedIntegerFourBytes(0), service, "1");
    }

    public Play(Service service, String speed) {
        this(new UnsignedIntegerFourBytes(0), service, speed);
    }

    public Play(UnsignedIntegerFourBytes instanceId, Service service) {
        this(instanceId, service, "1");
    }

    public Play(UnsignedIntegerFourBytes instanceId, Service service, String speed) {
        super(new ActionInvocation(service.getAction("Play")));
        getActionInvocation().setInput("InstanceID", instanceId);
        getActionInvocation().setInput("Speed", speed);
    }

    @Override
    public void success(ActionInvocation invocation) {
        log.fine("Execution successful");
    }
}