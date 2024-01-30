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

package com.distrimind.upnp_igd.controlpoint;

import com.distrimind.upnp_igd.UpnpServiceConfiguration;
import com.distrimind.upnp_igd.controlpoint.event.ExecuteAction;
import com.distrimind.upnp_igd.controlpoint.event.Search;
import com.distrimind.upnp_igd.model.message.header.MXHeader;
import com.distrimind.upnp_igd.model.message.header.STAllHeader;
import com.distrimind.upnp_igd.model.message.header.UpnpHeader;
import com.distrimind.upnp_igd.protocol.ProtocolFactory;
import com.distrimind.upnp_igd.registry.Registry;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * Default implementation.
 * <p>
 * This implementation uses the executor returned by
 * {@link UpnpServiceConfiguration#getSyncProtocolExecutorService()}.
 * </p>
 *
 * @author Christian Bauer
 */
@ApplicationScoped
public class ControlPointImpl implements ControlPoint {

    private static final Logger log = Logger.getLogger(ControlPointImpl.class.getName());

    protected UpnpServiceConfiguration configuration;
    protected ProtocolFactory protocolFactory;
    protected Registry registry;

    protected ControlPointImpl() {
    }

    @Inject
    public ControlPointImpl(UpnpServiceConfiguration configuration, ProtocolFactory protocolFactory, Registry registry) {
        log.fine("Creating ControlPoint: " + getClass().getName());
        
        this.configuration = configuration;
        this.protocolFactory = protocolFactory;
        this.registry = registry;
    }

    public UpnpServiceConfiguration getConfiguration() {
        return configuration;
    }

    public ProtocolFactory getProtocolFactory() {
        return protocolFactory;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void search(@Observes Search search) {
        search(search.getSearchType(), search.getMxSeconds());
    }

    public void search() {
        search(new STAllHeader(), MXHeader.DEFAULT_VALUE);
    }

    public void search(UpnpHeader searchType) {
        search(searchType, MXHeader.DEFAULT_VALUE);
    }

    public void search(int mxSeconds) {
        search(new STAllHeader(), mxSeconds);
    }

    public void search(UpnpHeader searchType, int mxSeconds) {
        log.fine("Sending asynchronous search for: " + searchType.getString());
        getConfiguration().getAsyncProtocolExecutor().execute(
                getProtocolFactory().createSendingSearch(searchType, mxSeconds)
        );
    }

    public void execute(ExecuteAction executeAction) {
        execute(executeAction.getCallback());
    }

    public Future execute(ActionCallback callback) {
        log.fine("Invoking action in background: " + callback);
        callback.setControlPoint(this);
        ExecutorService executor = getConfiguration().getSyncProtocolExecutorService();
        return executor.submit(callback);
    }

    public void execute(SubscriptionCallback callback) {
        log.fine("Invoking subscription in background: " + callback);
        callback.setControlPoint(this);
        getConfiguration().getSyncProtocolExecutorService().execute(callback);
    }
}
