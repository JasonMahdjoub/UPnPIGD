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

import com.distrimind.upnp_igd.binding.xml.DeviceDescriptorBinder;
import com.distrimind.upnp_igd.binding.xml.ServiceDescriptorBinder;
import com.distrimind.upnp_igd.binding.xml.UDA10DeviceDescriptorBinderImpl;
import com.distrimind.upnp_igd.binding.xml.UDA10ServiceDescriptorBinderImpl;
import com.distrimind.upnp_igd.model.Constants;
import com.distrimind.upnp_igd.model.ModelUtil;
import com.distrimind.upnp_igd.model.Namespace;
import com.distrimind.upnp_igd.model.message.UpnpHeaders;
import com.distrimind.upnp_igd.model.meta.RemoteDeviceIdentity;
import com.distrimind.upnp_igd.model.meta.RemoteService;
import com.distrimind.upnp_igd.model.types.ServiceType;
import com.distrimind.upnp_igd.transport.impl.DatagramIOConfigurationImpl;
import com.distrimind.upnp_igd.transport.impl.DatagramIOImpl;
import com.distrimind.upnp_igd.transport.impl.DatagramProcessorImpl;
import com.distrimind.upnp_igd.transport.impl.GENAEventProcessorImpl;
import com.distrimind.upnp_igd.transport.impl.MulticastReceiverConfigurationImpl;
import com.distrimind.upnp_igd.transport.impl.MulticastReceiverImpl;
import com.distrimind.upnp_igd.transport.impl.NetworkAddressFactoryImpl;
import com.distrimind.upnp_igd.transport.impl.SOAPActionProcessorImpl;
import com.distrimind.upnp_igd.transport.impl.StreamClientConfigurationImpl;
import com.distrimind.upnp_igd.transport.impl.StreamClientImpl;
import com.distrimind.upnp_igd.transport.impl.StreamServerConfigurationImpl;
import com.distrimind.upnp_igd.transport.impl.StreamServerImpl;
import com.distrimind.upnp_igd.transport.spi.*;
import com.distrimind.upnp_igd.util.Exceptions;
import jakarta.enterprise.inject.Alternative;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default configuration data of a typical UPnP stack.
 * <p>
 * This configuration utilizes the default network transport implementation found in
 * {@link com.distrimind.upnp_igd.transport.impl}.
 * </p>
 * <p>
 * This configuration utilizes the DOM default descriptor binders found in
 * {@link com.distrimind.upnp_igd.binding.xml}.
 * </p>
 * <p>
 * The thread <code>Executor</code> is an <code>Executors.newCachedThreadPool()</code> with
 * a custom {@link ClingThreadFactory} (it only sets a thread name).
 * </p>
 * <p>
 * Note that this pool is effectively unlimited, so the number of threads will
 * grow (and shrink) as needed - or restricted by your JVM.
 * </p>
 * <p>
 * The default {@link Namespace} is configured without any
 * base path or prefix.
 * </p>
 *
 * @author Christian Bauer
 */
@Alternative
public class DefaultUpnpServiceConfiguration implements UpnpServiceConfiguration {

    private static final Logger log = Logger.getLogger(DefaultUpnpServiceConfiguration.class.getName());

    final private int streamListenPort;

    final private ExecutorService defaultExecutorService;

    final private DatagramProcessor datagramProcessor;
    final private SOAPActionProcessor soapActionProcessor;
    final private GENAEventProcessor genaEventProcessor;

    final private DeviceDescriptorBinder deviceDescriptorBinderUDA10;
    final private ServiceDescriptorBinder serviceDescriptorBinderUDA10;

    final private Namespace namespace;
    final private int multicastPort;
    private NetworkAddressFactory networkAddressFactory;

    /**
     * Defaults to port '0', ephemeral.
     */
    public DefaultUpnpServiceConfiguration() {
        this(NetworkAddressFactoryImpl.DEFAULT_TCP_HTTP_LISTEN_PORT, Constants.UPNP_MULTICAST_PORT);
    }

    public DefaultUpnpServiceConfiguration(int streamListenPort, int multicastPort) {
        this(streamListenPort, multicastPort, true);
    }

    protected DefaultUpnpServiceConfiguration(boolean checkRuntime) {
        this(NetworkAddressFactoryImpl.DEFAULT_TCP_HTTP_LISTEN_PORT, Constants.UPNP_MULTICAST_PORT, checkRuntime);
    }

    protected DefaultUpnpServiceConfiguration(int streamListenPort, int multicastPort, boolean checkRuntime) {
        if (checkRuntime && ModelUtil.ANDROID_RUNTIME) {
            throw new Error("Unsupported runtime environment, use com.distrimind.upnp_igd.android.AndroidUpnpServiceConfiguration");
        }

        this.streamListenPort = streamListenPort;
        this.multicastPort=multicastPort;
        defaultExecutorService = createDefaultExecutorService();

        datagramProcessor = createDatagramProcessor();
        soapActionProcessor = createSOAPActionProcessor();
        genaEventProcessor = createGENAEventProcessor();

        deviceDescriptorBinderUDA10 = createDeviceDescriptorBinderUDA10();
        serviceDescriptorBinderUDA10 = createServiceDescriptorBinderUDA10();

        namespace = createNamespace();
        networkAddressFactory=null;
    }

    @Override
    public DatagramProcessor getDatagramProcessor() {
        return datagramProcessor;
    }

    @Override
    public SOAPActionProcessor getSoapActionProcessor() {
        return soapActionProcessor;
    }

    @Override
    public GENAEventProcessor getGenaEventProcessor() {
        return genaEventProcessor;
    }

    public int getMulticastPort() {
        return multicastPort;
    }

    @Override
    public StreamClient<?> createStreamClient() {
        return new StreamClientImpl(
            new StreamClientConfigurationImpl(
                getSyncProtocolExecutorService()
            )
        );
    }

    @Override
    public MulticastReceiver<?> createMulticastReceiver(NetworkAddressFactory networkAddressFactory) {
        return new MulticastReceiverImpl(
                new MulticastReceiverConfigurationImpl(
                        networkAddressFactory.getMulticastGroup(),
                        networkAddressFactory.getMulticastPort()
                )
        );
    }

    @Override
    public DatagramIO<?> createDatagramIO(NetworkAddressFactory networkAddressFactory) {
        return new DatagramIOImpl(new DatagramIOConfigurationImpl());
    }

    @Override
    public StreamServer<?> createStreamServer(NetworkAddressFactory networkAddressFactory) {
        return new StreamServerImpl(
                new StreamServerConfigurationImpl(
                        networkAddressFactory.getStreamListenPort()
                )
        );
    }

    @Override
    public Executor getMulticastReceiverExecutor() {
        return getDefaultExecutorService();
    }

    @Override
    public Executor getDatagramIOExecutor() {
        return getDefaultExecutorService();
    }

    @Override
    public ExecutorService getStreamServerExecutorService() {
        return getDefaultExecutorService();
    }

    @Override
    public DeviceDescriptorBinder getDeviceDescriptorBinderUDA10() {
        return deviceDescriptorBinderUDA10;
    }

    @Override
    public ServiceDescriptorBinder getServiceDescriptorBinderUDA10() {
        return serviceDescriptorBinderUDA10;
    }

    @Override
    public ServiceType[] getExclusiveServiceTypes() {
        return new ServiceType[0];
    }

    /**
     * @return Defaults to <code>false</code>.
     */
    @Override
	public boolean isReceivedSubscriptionTimeoutIgnored() {
		return false;
	}

    @Override
    @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
    public UpnpHeaders getDescriptorRetrievalHeaders(RemoteDeviceIdentity identity) {
        return null;
    }

    @Override
    @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
    public UpnpHeaders getEventSubscriptionHeaders(RemoteService service) {
        return null;
    }

    /**
     * @return Defaults to 1000 milliseconds.
     */
    @Override
    public int getRegistryMaintenanceIntervalMillis() {
        return 1000;
    }

    /**
     * @return Defaults to zero, disabling ALIVE flooding.
     */
    @Override
    public int getAliveIntervalMillis() {
    	return 0;
    }

    @Override
    public Integer getRemoteDeviceMaxAgeSeconds() {
        return null;
    }

    @Override
    public Executor getAsyncProtocolExecutor() {
        return getDefaultExecutorService();
    }

    @Override
    public ExecutorService getSyncProtocolExecutorService() {
        return getDefaultExecutorService();
    }

    @Override
    public Namespace getNamespace() {
        return namespace;
    }

    @Override
    public Executor getRegistryMaintainerExecutor() {
        return getDefaultExecutorService();
    }

    @Override
    public Executor getRegistryListenerExecutor() {
        return getDefaultExecutorService();
    }

    @Override
    public NetworkAddressFactory createNetworkAddressFactory() {
        return createNetworkAddressFactory(streamListenPort, multicastPort);
    }

    @Override
    public void shutdown() {
        log.fine("Shutting down default executor service");
        getDefaultExecutorService().shutdownNow();
    }
    protected NetworkAddressFactory getNetworkAddressFactory() {
        if (networkAddressFactory==null)
            networkAddressFactory=createNetworkAddressFactory();
        return networkAddressFactory;
    }

    protected NetworkAddressFactory createNetworkAddressFactory(int streamListenPort, int multicastPort) {
        return new NetworkAddressFactoryImpl(streamListenPort, multicastPort);
    }

    protected DatagramProcessor createDatagramProcessor() {
        return new DatagramProcessorImpl();
    }

    protected SOAPActionProcessor createSOAPActionProcessor() {
        return new SOAPActionProcessorImpl();
    }

    protected GENAEventProcessor createGENAEventProcessor() {
        return new GENAEventProcessorImpl();
    }

    protected DeviceDescriptorBinder createDeviceDescriptorBinderUDA10() {
        return new UDA10DeviceDescriptorBinderImpl(getNetworkAddressFactory());
    }

    protected ServiceDescriptorBinder createServiceDescriptorBinderUDA10() {
        return new UDA10ServiceDescriptorBinderImpl(getNetworkAddressFactory());
    }

    protected Namespace createNamespace() {
        return new Namespace();
    }

    protected ExecutorService getDefaultExecutorService() {
        return defaultExecutorService;
    }

    protected ExecutorService createDefaultExecutorService() {
        return new ClingExecutor();
    }

    public static class ClingExecutor extends ThreadPoolExecutor {

        public ClingExecutor() {
            this(new ClingThreadFactory(),
                 new ThreadPoolExecutor.DiscardPolicy() {
                     // The pool is unbounded but rejections will happen during shutdown
                     @Override
                     public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
                         // Log and discard
                         if (log.isLoggable(Level.INFO))
                            log.info("Thread pool rejected execution of " + runnable.getClass());
                         super.rejectedExecution(runnable, threadPoolExecutor);
                     }
                 }
            );
        }

        public ClingExecutor(ThreadFactory threadFactory, RejectedExecutionHandler rejectedHandler) {
            // This is the same as Executors.newCachedThreadPool
            super(0,
                  Integer.MAX_VALUE,
                  60L,
                  TimeUnit.SECONDS,
					new SynchronousQueue<>(),
                  threadFactory,
                  rejectedHandler
            );
        }

        @Override
        protected void afterExecute(Runnable runnable, Throwable throwable) {
            super.afterExecute(runnable, throwable);
            if (throwable != null) {
                Throwable cause = Exceptions.unwrap(throwable);
                if (cause instanceof InterruptedException) {
                    // Ignore this, might happen when we shutdownNow() the executor. We can't
                    // log at this point as the logging system might be stopped already (e.g.
                    // if it's a CDI component).
                    return;
                }
                if (log.isLoggable(Level.WARNING)) {
                    // Log only
                    log.warning("Thread terminated " + runnable + " abruptly with exception: " + throwable);
                    log.warning("Root cause: " + cause);
                }
            }
        }
    }

    // Executors.DefaultThreadFactory is package visibility (...no touching, you unworthy JDK user!)
    public static class ClingThreadFactory implements ThreadFactory {

        protected final ThreadGroup group;
        protected final AtomicInteger threadNumber = new AtomicInteger(1);
        protected final String namePrefix = "cling-";

        public ClingThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(
                    group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0
            );
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);

            return t;
        }
    }

}
