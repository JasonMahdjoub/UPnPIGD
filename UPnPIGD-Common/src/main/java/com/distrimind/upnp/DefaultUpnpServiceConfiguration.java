/*
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.distrimind.upnp;

import com.distrimind.upnp.binding.xml.DeviceDescriptorBinder;
import com.distrimind.upnp.binding.xml.ServiceDescriptorBinder;
import com.distrimind.upnp.model.Constants;
import com.distrimind.upnp.model.ModelUtil;
import com.distrimind.upnp.model.Namespace;
import com.distrimind.upnp.model.message.UpnpHeaders;
import com.distrimind.upnp.model.meta.RemoteDeviceIdentity;
import com.distrimind.upnp.model.meta.RemoteService;
import com.distrimind.upnp.model.types.ServiceType;
import com.distrimind.upnp.platform.Platform;
import com.distrimind.upnp.platform.PlatformUpnpServiceConfiguration;
import com.distrimind.upnp.transport.impl.DatagramProcessorImpl;
import com.distrimind.upnp.transport.impl.NetworkAddressFactoryImpl;
import com.distrimind.upnp.transport.spi.*;
import jakarta.enterprise.inject.Alternative;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import com.distrimind.flexilogxml.log.DMLogger;

/**
 * Default configuration data of a typical UPnP stack.
 * <p>
 * This configuration utilizes the default network transport implementation found in
 * {@link com.distrimind.upnp.transport.impl}.
 * </p>
 * <p>
 * This configuration utilizes the DOM default descriptor binders found in
 * {@link com.distrimind.upnp.binding.xml}.
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

    final private static DMLogger log = Log.getLogger(DefaultUpnpServiceConfiguration.class);

    final private int streamListenPort;

    protected final PlatformUpnpServiceConfiguration platformUpnpServiceConfiguration;
    private final ExecutorService defaultExecutorService;
    private final ExecutorService defaultAndroidExecutorService;
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
    public DefaultUpnpServiceConfiguration() throws IOException {
        this(Platform.getDefault(), NetworkAddressFactoryImpl.DEFAULT_TCP_HTTP_LISTEN_PORT, Constants.UPNP_MULTICAST_PORT);
    }

    public DefaultUpnpServiceConfiguration(int streamListenPort, int multicastPort) throws IOException {
        this(Platform.getDefault(), streamListenPort, multicastPort, true);
    }

    public DefaultUpnpServiceConfiguration(boolean checkRuntime) throws IOException {
        this(Platform.getDefault(), NetworkAddressFactoryImpl.DEFAULT_TCP_HTTP_LISTEN_PORT, Constants.UPNP_MULTICAST_PORT, checkRuntime);
    }

    public DefaultUpnpServiceConfiguration(int streamListenPort, int multicastPort, boolean checkRuntime) throws IOException {
        this(Platform.getDefault(), streamListenPort, multicastPort, checkRuntime);
    }
    /**
     * Defaults to port '0', ephemeral.
     */
    public DefaultUpnpServiceConfiguration(Platform platform) throws IOException {
        this(platform, NetworkAddressFactoryImpl.DEFAULT_TCP_HTTP_LISTEN_PORT, Constants.UPNP_MULTICAST_PORT);
    }

    public DefaultUpnpServiceConfiguration(Platform platform, int streamListenPort, int multicastPort) throws IOException {
        this(platform, streamListenPort, multicastPort, true);
    }

    public DefaultUpnpServiceConfiguration(Platform platform, boolean checkRuntime) throws IOException {
        this(platform, NetworkAddressFactoryImpl.DEFAULT_TCP_HTTP_LISTEN_PORT, Constants.UPNP_MULTICAST_PORT, checkRuntime);
    }

    public DefaultUpnpServiceConfiguration(Platform platform, int streamListenPort, int multicastPort, boolean checkRuntime) throws IOException {
        if (platform==null)
            throw new NullPointerException();
        if (checkRuntime && ((ModelUtil.ANDROID_RUNTIME && platform!=Platform.ANDROID) || (!ModelUtil.ANDROID_RUNTIME && platform==Platform.ANDROID))) {
            throw new Error("Unsupported runtime environment, use com.distrimind.upnp.android.AndroidUpnpServiceConfiguration");
        }

        this.streamListenPort = streamListenPort;
        this.multicastPort=multicastPort;
        platformUpnpServiceConfiguration = platform.getInstance();
        defaultExecutorService=createDefaultExecutorService();
        defaultAndroidExecutorService=platform==Platform.ANDROID?platform.getInstance().createDefaultAndroidExecutorService():defaultExecutorService;
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
    public StreamClient<?> createStreamClient(int timeoutSeconds) {
        return platformUpnpServiceConfiguration.createStreamClient(getDefaultAndroidExecutorService(), timeoutSeconds);
    }

    @Override
    public MulticastReceiver<?> createMulticastReceiver(NetworkAddressFactory networkAddressFactory) {
        return platformUpnpServiceConfiguration.createMulticastReceiver(networkAddressFactory);
    }

    @Override
    public DatagramIO<?> createDatagramIO(NetworkAddressFactory networkAddressFactory) {
        return platformUpnpServiceConfiguration.createDatagramIO(networkAddressFactory);
    }

    @Override
    public StreamServer<?> createStreamServer(NetworkAddressFactory networkAddressFactory) {
        return platformUpnpServiceConfiguration.createStreamServer(networkAddressFactory);
    }
    @Override
    public StreamServer<?> createStreamServer(int streamServerPort) {
        return platformUpnpServiceConfiguration.createStreamServer(streamServerPort);
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
		return platformUpnpServiceConfiguration.isReceivedSubscriptionTimeoutIgnored();
	}

    @Override
    @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
    public UpnpHeaders getDescriptorRetrievalHeaders(RemoteDeviceIdentity identity) {
        return platformUpnpServiceConfiguration.getDescriptorRetrievalHeaders(identity);
    }

    @Override
    @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
    public UpnpHeaders getEventSubscriptionHeaders(RemoteService service) {
        return platformUpnpServiceConfiguration.getEventSubscriptionHeaders(service);
    }

    /**
     * @return Defaults to 1000 milliseconds.
     */
    @Override
    public int getRegistryMaintenanceIntervalMillis() {
        return platformUpnpServiceConfiguration.getRegistryMaintenanceIntervalMillis();
    }

    /**
     * @return Defaults to zero, disabling ALIVE flooding.
     */
    @Override
    public int getAliveIntervalMillis() {
    	return platformUpnpServiceConfiguration.getAliveIntervalMillis();
    }

    @Override
    public Integer getRemoteDeviceMaxAgeSeconds() {
        return platformUpnpServiceConfiguration.getRemoteDeviceMaxAgeSeconds();
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
        log.debug("Shutting down default executor service");
        getDefaultExecutorService().shutdownNow();
    }

    public NetworkAddressFactory getNetworkAddressFactory() {
        if (networkAddressFactory==null)
            networkAddressFactory=createNetworkAddressFactory();
        return networkAddressFactory;
    }

    protected NetworkAddressFactory createNetworkAddressFactory(int streamListenPort, int multicastPort) {
        return platformUpnpServiceConfiguration.createNetworkAddressFactory(streamListenPort, multicastPort);
    }

    protected DatagramProcessor createDatagramProcessor() {
        return new DatagramProcessorImpl();
    }

    protected SOAPActionProcessor createSOAPActionProcessor() {
        return platformUpnpServiceConfiguration.createSOAPActionProcessor();
    }

    protected GENAEventProcessor createGENAEventProcessor() {
        return platformUpnpServiceConfiguration.createGENAEventProcessor();
    }

    protected DeviceDescriptorBinder createDeviceDescriptorBinderUDA10() {
        return platformUpnpServiceConfiguration.createDeviceDescriptorBinderUDA10(getNetworkAddressFactory());
    }

    protected ServiceDescriptorBinder createServiceDescriptorBinderUDA10() {
        return platformUpnpServiceConfiguration.createServiceDescriptorBinderUDA10(getNetworkAddressFactory());
    }

    protected Namespace createNamespace() {
        return platformUpnpServiceConfiguration.createNamespace();
    }

    protected ExecutorService getDefaultExecutorService() {
        return defaultExecutorService;
    }

    protected ExecutorService getDefaultAndroidExecutorService() {
        return defaultAndroidExecutorService;
    }

    protected ExecutorService createDefaultExecutorService() throws IOException {
        return platformUpnpServiceConfiguration.createDefaultExecutorService();
    }
    @Override
    public Platform getPlatformType()
    {
        return platformUpnpServiceConfiguration.getPlatformType();
    }
}
