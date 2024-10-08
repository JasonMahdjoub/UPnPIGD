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
import com.distrimind.upnp_igd.support.model.SeekMode;

import java.util.logging.Logger;

/**
 *
 * @author Christian Bauer
 */
public abstract class Seek extends ActionCallback {

    private static final Logger log = Logger.getLogger(Seek.class.getName());

    public Seek(Service<?, ?, ?> service, String relativeTimeTarget) {
        this(new UnsignedIntegerFourBytes(0), service, SeekMode.REL_TIME, relativeTimeTarget);
    }

    public Seek(UnsignedIntegerFourBytes instanceId, Service<?, ?, ?> service, String relativeTimeTarget) {
        this(instanceId, service, SeekMode.REL_TIME, relativeTimeTarget);
    }

    public Seek(Service<?, ?, ?> service, SeekMode mode, String target) {
        this(new UnsignedIntegerFourBytes(0), service, mode, target);
    }

    public Seek(UnsignedIntegerFourBytes instanceId, Service<?, ?, ?> service, SeekMode mode, String target) {
        super(new ActionInvocation<>(service.getAction("Seek")));
        getActionInvocation().setInput("InstanceID", instanceId);
        getActionInvocation().setInput("Unit", mode.name());
        getActionInvocation().setInput("Target", target);
    }

    @Override
    public void success(ActionInvocation<?> invocation) {
        log.fine("Execution successful");
    }
}