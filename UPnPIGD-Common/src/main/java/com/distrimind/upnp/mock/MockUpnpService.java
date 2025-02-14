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

package com.distrimind.upnp.mock;

import com.distrimind.upnp.controlpoint.ControlPoint;
import com.distrimind.upnp.controlpoint.ControlPointImpl;
import com.distrimind.upnp.model.message.header.UpnpHeader;
import com.distrimind.upnp.model.meta.LocalDevice;
import com.distrimind.upnp.protocol.ProtocolFactory;
import com.distrimind.upnp.protocol.ProtocolFactoryImpl;
import com.distrimind.upnp.protocol.async.SendingNotificationAlive;
import com.distrimind.upnp.protocol.async.SendingSearch;
import com.distrimind.upnp.registry.Registry;
import com.distrimind.upnp.registry.RegistryImpl;
import com.distrimind.upnp.registry.RegistryMaintainer;
import com.distrimind.upnp.transport.RouterException;
import com.distrimind.upnp.transport.spi.NetworkAddressFactory;
import com.distrimind.upnp.UpnpService;
import com.distrimind.upnp.UpnpServiceConfiguration;

import java.io.IOException;

import jakarta.enterprise.inject.Alternative;

/**
 * Simplifies testing of core and non-core modules.
 * <p>
 * It uses the {@link MockUpnpService.MockProtocolFactory}.
 * </p>
 *
 * @author Christian Bauer
 */
@Alternative
public class MockUpnpService implements UpnpService {

    protected final UpnpServiceConfiguration configuration;
    protected final ControlPoint controlPoint;
    protected final ProtocolFactory protocolFactory;
    protected final Registry registry;
    protected final MockRouter router;

    protected final NetworkAddressFactory networkAddressFactory;

    /**
     * Single-thread of execution for the whole UPnP stack, no ALIVE messages or registry maintenance.
     */
    public MockUpnpService() throws IOException {
        this(false, new MockUpnpServiceConfiguration(false, false));
    }

    /**
     * No ALIVE messages.
     */
    public MockUpnpService(MockUpnpServiceConfiguration configuration) throws IOException {
        this(false, configuration);
    }

    /**
     * Single-thread of execution for the whole UPnP stack, except one background registry maintenance thread.
     */
    public MockUpnpService(final boolean sendsAlive, final boolean maintainsRegistry) throws IOException {
        this(sendsAlive, new MockUpnpServiceConfiguration(maintainsRegistry, false));
    }

    public MockUpnpService(final boolean sendsAlive, final boolean maintainsRegistry, final boolean multiThreaded) throws IOException {
        this(sendsAlive, new MockUpnpServiceConfiguration(maintainsRegistry, multiThreaded));
    }

    public MockUpnpService(final boolean sendsAlive, final MockUpnpServiceConfiguration configuration) throws IOException {

        this.configuration = configuration;

        this.protocolFactory = createProtocolFactory(this, sendsAlive);

        this.registry = new RegistryImpl(this) {
            @Override
            protected RegistryMaintainer createRegistryMaintainer() {
                return configuration.isMaintainsRegistry() ? super.createRegistryMaintainer() : null;
            }
        };

        this.networkAddressFactory = this.configuration.createNetworkAddressFactory();

        this.router = createRouter();

        this.controlPoint = new ControlPointImpl(configuration, protocolFactory, registry);
    }

    protected ProtocolFactory createProtocolFactory(UpnpService service, boolean sendsAlive) {
        return new MockProtocolFactory(service, sendsAlive);
    }

    protected MockRouter createRouter() {
        return new MockRouter(getConfiguration(), getProtocolFactory());
    }

    /**
     * This factory customizes several protocols.
     * <p>
     * The {@link SendingNotificationAlive} protocol
     * only sends messages if this feature is enabled when instantiating the factory.
   
     * <p>
     * The {@link SendingSearch} protocol doesn't wait between
     * sending search message bulks, this speeds up testing.
   
     */
    public static class MockProtocolFactory extends ProtocolFactoryImpl {

        private final boolean sendsAlive;

        public MockProtocolFactory(UpnpService upnpService, boolean sendsAlive) {
            super(upnpService);
            this.sendsAlive = sendsAlive;
        }

        @Override
        public <T> SendingNotificationAlive createSendingNotificationAlive(LocalDevice<T> localDevice) {
            return new SendingNotificationAlive(getUpnpService(), localDevice) {
                @Override
                protected void execute() throws RouterException {
                    if (sendsAlive) super.execute();
                }
            };
        }

        @Override
        public SendingSearch createSendingSearch(UpnpHeader<?> searchTarget, int mxSeconds) {
            return new SendingSearch(getUpnpService(), searchTarget, mxSeconds) {
                @Override
                public int getBulkIntervalMilliseconds() {
                    return 0; // Don't wait
                }
            };
        }
    }

    @Override
	public UpnpServiceConfiguration getConfiguration() {
        return configuration;
    }

    @Override
	public ControlPoint getControlPoint() {
        return controlPoint;
    }

    @Override
	public ProtocolFactory getProtocolFactory() {
        return protocolFactory;
    }

    @Override
	public Registry getRegistry() {
        return registry;
    }

    @Override
	public MockRouter getRouter() {
        return router;
    }

    @Override
	public void shutdown() {
        getRegistry().shutdown();
        getConfiguration().shutdown();
    }
}
