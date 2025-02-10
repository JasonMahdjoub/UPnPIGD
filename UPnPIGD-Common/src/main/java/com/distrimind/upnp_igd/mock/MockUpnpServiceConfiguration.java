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

import com.distrimind.upnp_igd.binding.xml.DeviceDescriptorBinder;
import com.distrimind.upnp_igd.binding.xml.ServiceDescriptorBinder;
import com.distrimind.upnp_igd.platform.Platform;
import com.distrimind.upnp_igd.platform.PlatformUpnpServiceConfiguration;
import com.distrimind.upnp_igd.transport.spi.NetworkAddressFactory;
import com.distrimind.upnp_igd.DefaultUpnpServiceConfiguration;
import com.distrimind.upnp_igd.transport.spi.SOAPActionProcessor;

import jakarta.enterprise.inject.Alternative;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * @author Christian Bauer
 */
@Alternative
public class MockUpnpServiceConfiguration extends DefaultUpnpServiceConfiguration {

    final protected boolean maintainsRegistry;
    final protected boolean multiThreaded;
    private final static PlatformUpnpServiceConfiguration desktopPlatformUpnpServiceConfiguration=Platform.DESKTOP.getInstance();
    /**
     * Does not maintain registry, single threaded execution.
     */
    public MockUpnpServiceConfiguration() throws IOException {
        this(Platform.getDefault(), false, false);
    }

    /**
     * Single threaded execution.
     */
    public MockUpnpServiceConfiguration(boolean maintainsRegistry) throws IOException {
        this(Platform.getDefault(), maintainsRegistry, false);
    }

    public MockUpnpServiceConfiguration(boolean maintainsRegistry, boolean multiThreaded) throws IOException {
        this(Platform.getDefault(), maintainsRegistry, multiThreaded);
    }
    /**
     * Does not maintain registry, single threaded execution.
     */
    public MockUpnpServiceConfiguration(Platform platform) throws IOException {
        this(platform, false, false);
    }

    /**
     * Single threaded execution.
     */
    public MockUpnpServiceConfiguration(Platform platform, boolean maintainsRegistry) throws IOException {
        this(platform, maintainsRegistry, false);
    }

    public MockUpnpServiceConfiguration(Platform platform, boolean maintainsRegistry, boolean multiThreaded) throws IOException {
        super(platform, false);
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
        return platformUpnpServiceConfiguration.createMockNetworkAddressFactory(streamListenPort, multiCastPort);
    }

    @Override
    public Executor getRegistryMaintainerExecutor() {
        if (isMaintainsRegistry()) {
            return runnable -> startThread(runnable);
        }
        return getDefaultExecutorService();
    }

    @Override
    protected ExecutorService getDefaultExecutorService()  {
        if (isMultiThreaded()) {
            return super.getDefaultExecutorService();
        }
        else
            return platformUpnpServiceConfiguration.createMockDefaultExecutorService();
    }

    @Override
    protected SOAPActionProcessor createSOAPActionProcessor() {
        return desktopPlatformUpnpServiceConfiguration.createSOAPActionProcessor();
    }

    @Override
    protected DeviceDescriptorBinder createDeviceDescriptorBinderUDA10() {
        return desktopPlatformUpnpServiceConfiguration.createDeviceDescriptorBinderUDA10(getNetworkAddressFactory());
    }

    @Override
    protected ServiceDescriptorBinder createServiceDescriptorBinderUDA10() {
        return desktopPlatformUpnpServiceConfiguration.createServiceDescriptorBinderUDA10(getNetworkAddressFactory());
    }

    protected PlatformUpnpServiceConfiguration getDesktopPlatformUpnpServiceConfiguration()
    {
        return desktopPlatformUpnpServiceConfiguration;
    }
}
