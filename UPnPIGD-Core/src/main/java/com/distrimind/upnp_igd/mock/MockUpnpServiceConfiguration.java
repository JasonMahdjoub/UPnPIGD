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

package com.distrimind.upnp_igd.mock;

import com.distrimind.upnp_igd.transport.impl.NetworkAddressFactoryImpl;
import com.distrimind.upnp_igd.transport.spi.NetworkAddressFactory;
import com.distrimind.upnp_igd.DefaultUpnpServiceConfiguration;

import jakarta.enterprise.inject.Alternative;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Christian Bauer
 */
@Alternative
public class MockUpnpServiceConfiguration extends DefaultUpnpServiceConfiguration {

    final protected boolean maintainsRegistry;
    final protected boolean multiThreaded;

    /**
     * Does not maintain registry, single threaded execution.
     */
    public MockUpnpServiceConfiguration() {
        this(false, false);
    }

    /**
     * Single threaded execution.
     */
    public MockUpnpServiceConfiguration(boolean maintainsRegistry) {
        this(maintainsRegistry, false);
    }

    public MockUpnpServiceConfiguration(boolean maintainsRegistry, boolean multiThreaded) {
        super(false);
        this.maintainsRegistry = maintainsRegistry;
        this.multiThreaded = multiThreaded;
    }

    public boolean isMaintainsRegistry() {
        return maintainsRegistry;
    }

    public boolean isMultiThreaded() {
        return multiThreaded;
    }

    @Override
    protected NetworkAddressFactory createNetworkAddressFactory(int streamListenPort, int multiCastPort) {
        // We are only interested in 127.0.0.1
        return new NetworkAddressFactoryImpl(streamListenPort, multiCastPort) {
            @Override
            protected boolean isUsableNetworkInterface(NetworkInterface iface) throws Exception {
                return (iface.isLoopback());
            }

            @Override
            protected boolean isUsableAddress(NetworkInterface networkInterface, InetAddress address) {
                return (address.isLoopbackAddress() && address instanceof Inet4Address);
            }

        };
    }

    @Override
    public Executor getRegistryMaintainerExecutor() {
        if (isMaintainsRegistry()) {
            return runnable -> new Thread(runnable).start();
        }
        return getDefaultExecutorService();
    }

    @Override
    protected ExecutorService getDefaultExecutorService() {
        if (isMultiThreaded()) {
            return super.getDefaultExecutorService();
        }
        return new AbstractExecutorService() {

            boolean terminated;

            @Override
			public void shutdown() {
                terminated = true;
            }

            @Override
			public List<Runnable> shutdownNow() {
                shutdown();
                return Collections.emptyList();
            }

            @Override
			public boolean isShutdown() {
                return terminated;
            }

            @Override
			public boolean isTerminated() {
                return terminated;
            }

            @Override
			public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
                shutdown();
                return terminated;
            }

            @Override
			public void execute(Runnable runnable) {
                runnable.run();
            }
        };
    }

}
