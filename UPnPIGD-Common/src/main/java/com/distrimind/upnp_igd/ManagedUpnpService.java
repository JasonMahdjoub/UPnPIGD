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

package com.distrimind.upnp_igd;

import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.upnp_igd.controlpoint.ControlPoint;
import com.distrimind.upnp_igd.model.meta.LocalDevice;
import com.distrimind.upnp_igd.model.meta.RemoteDevice;
import com.distrimind.upnp_igd.protocol.ProtocolFactory;
import com.distrimind.upnp_igd.registry.Registry;
import com.distrimind.upnp_igd.registry.RegistryListener;
import com.distrimind.upnp_igd.registry.event.*;
import com.distrimind.upnp_igd.transport.DisableRouter;
import com.distrimind.upnp_igd.transport.EnableRouter;
import com.distrimind.upnp_igd.transport.Router;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;


/**
 * Adapter for CDI environments.
 * <p>
 * The CDI container provides injectable instances of UPnPIGD UPnP interfaces, e.g.
 * you can <code>@Inject Registry</code> or <code>@Inject ControlPoint</code>.
 * </p>
 * <p>
 * Furthermore, this adapter also binds UPnPIGD into the CDI eventing system. You
 * can <code>@Observe RemoteDeviceDiscoveryStart</code> etc. events of the
 * registry.
 * </p>
 * <p>
 * Even better, in the future you might be able to listen to GENA UPnP events with
 * the same API - although this will require some magic for subscription...
 * </p>
 * <p>
 * TODO: This is a work in progress.
 * </p>
 *
 * @author Christian Bauer
 */
@ApplicationScoped
public class ManagedUpnpService implements UpnpService {

    final private static DMLogger log = Log.getLogger(ManagedUpnpService.class);

    @Inject
    RegistryListenerAdapter registryListenerAdapter;

    @Inject
    Instance<UpnpServiceConfiguration> configuration;

    @Inject
    Instance<Registry> registryInstance;

    @Inject
    Instance<Router> routerInstance;

    @Inject
    Instance<ProtocolFactory> protocolFactoryInstance;

    @Inject
    Instance<ControlPoint> controlPointInstance;

    @Inject
    Event<EnableRouter> enableRouterEvent;

    @Inject
    Event<DisableRouter> disableRouterEvent;

    @Override
    public UpnpServiceConfiguration getConfiguration() {
        return configuration.get();
    }

    @Override
    public ControlPoint getControlPoint() {
        return controlPointInstance.get();
    }

    @Override
    public ProtocolFactory getProtocolFactory() {
        return protocolFactoryInstance.get();
    }

    @Override
    public Registry getRegistry() {
        return registryInstance.get();
    }

    @Override
    public Router getRouter() {
        return routerInstance.get();
    }

    public void start(@Observes Start start) {
        log.info(">>> Starting managed UPnP service...");

        // First start the registry before we can receive messages through the transport

        getRegistry().addListener(registryListenerAdapter);

        enableRouterEvent.fire(new EnableRouter());

        log.info("<<< Managed UPnP service started successfully");
    }

    @Override
    public void shutdown() {
        shutdown(null);
    }

    public void shutdown(@Observes Shutdown shutdown) {

        // Well, since java.util.logging has its own shutdown hook, this
        // might actually make it into the log or not...
        log.info(">>> Shutting down managed UPnP service...");

        // First stop the registry and announce BYEBYE on the transport
        getRegistry().shutdown();

        disableRouterEvent.fire(new DisableRouter());

        getConfiguration().shutdown();

        log.info("<<< Managed UPnP service shutdown completed");
    }

    @ApplicationScoped
    static class RegistryListenerAdapter implements RegistryListener {

        @Inject
        @Any
        Event<RemoteDeviceDiscovery> remoteDeviceDiscoveryEvent;

        @Inject
        @Any
        Event<FailedRemoteDeviceDiscovery> failedRemoteDeviceDiscoveryEvent;

        @Inject
        @Any
        Event<LocalDeviceDiscovery<?>> localDeviceDiscoveryEvent;

        @Inject
        @Any
        Event<RegistryShutdown> registryShutdownEvent;

        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
            remoteDeviceDiscoveryEvent.select(Phase.ALIVE).fire(
                    new RemoteDeviceDiscovery(device)
            );
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
            failedRemoteDeviceDiscoveryEvent.fire(
                    new FailedRemoteDeviceDiscovery(device, ex)
            );
        }

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            remoteDeviceDiscoveryEvent.select(Phase.COMPLETE).fire(
                    new RemoteDeviceDiscovery(device)
            );
        }

        @Override
        public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
            remoteDeviceDiscoveryEvent.select(Phase.UPDATED).fire(
                    new RemoteDeviceDiscovery(device)
            );
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            remoteDeviceDiscoveryEvent.select(Phase.BYEBYE).fire(
                    new RemoteDeviceDiscovery(device)
            );
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice<?> device) {
            localDeviceDiscoveryEvent.select(Phase.COMPLETE).fire(
                    new LocalDeviceDiscovery<>(device)
            );
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice<?> device) {
            localDeviceDiscoveryEvent.select(Phase.BYEBYE).fire(
                    new LocalDeviceDiscovery<>(device)
            );
        }

        @Override
        public void beforeShutdown(Registry registry) {
            registryShutdownEvent.select(new AnnotationLiteral<>() {
                private static final long serialVersionUID = 1L;
            }).fire(
                    new RegistryShutdown()
            );
        }

        @Override
        public void afterShutdown() {
            registryShutdownEvent.select(new AnnotationLiteral<>() {
                private static final long serialVersionUID = 1L;
            }).fire(
                    new RegistryShutdown()
            );
        }
    }

}
