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
package com.distrimind.upnp_igd.android.test.transport;

import com.distrimind.upnp_igd.UpnpServiceConfiguration;
import com.distrimind.upnp_igd.android.transport.impl.jetty.StreamClientConfigurationImpl;
import com.distrimind.upnp_igd.android.transport.impl.jetty.JettyStreamClientImpl;
import com.distrimind.upnp_igd.transport.spi.StreamClient;

/**
 * @author Christian Bauer
 */
public class JettyServerJDKClientTest extends JettyServerJettyClientTest {

    @Override
    public StreamClient<?> createStreamClient(UpnpServiceConfiguration configuration) {
        return new JettyStreamClientImpl(
            new StreamClientConfigurationImpl(
                configuration.getSyncProtocolExecutorService(),
                3
            )
        );
    }

    // DISABLED, NOT SUPPORTED

    @Override
    public void cancelled() throws Exception {
    }

    @Override
    public void checkAlive() throws Exception {
    }

    @Override
    public void checkAliveExpired() throws Exception {
    }

    @Override
    public void checkAliveCancelled() throws Exception {
    }
}
