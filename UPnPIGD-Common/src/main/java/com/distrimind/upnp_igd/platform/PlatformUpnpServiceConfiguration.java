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

package com.distrimind.upnp_igd.platform;

import com.distrimind.upnp_igd.binding.xml.DeviceDescriptorBinder;
import com.distrimind.upnp_igd.binding.xml.ServiceDescriptorBinder;
import com.distrimind.upnp_igd.binding.xml.UDA10ServiceDescriptorBinderImpl;
import com.distrimind.upnp_igd.model.Namespace;
import com.distrimind.upnp_igd.model.message.UpnpHeaders;
import com.distrimind.upnp_igd.model.meta.RemoteDeviceIdentity;
import com.distrimind.upnp_igd.model.meta.RemoteService;
import com.distrimind.upnp_igd.transport.impl.*;
import com.distrimind.upnp_igd.transport.spi.*;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Jason Mahdjoub
 * @since 1.2.0
 */
public abstract class PlatformUpnpServiceConfiguration {

	protected PlatformUpnpServiceConfiguration() {

	}
	public ExecutorService createDefaultExecutorService()
	{
		return new UpnpIGDExecutor();
	}
	public ExecutorService createDefaultAndroidExecutorService() throws IOException
	{
		throw new RuntimeException("Unsupported operation");
	}


	public abstract NetworkAddressFactory createNetworkAddressFactory(int streamListenPort, int multicastPort);



	public abstract StreamClient<?> createStreamClient(ExecutorService syncProtocolExecutorService, int timeoutSeconds);

	public MulticastReceiver<?> createMulticastReceiver(NetworkAddressFactory networkAddressFactory) {
		return new MulticastReceiverImpl(
				new MulticastReceiverConfigurationImpl(
						networkAddressFactory.getMulticastGroup(),
						networkAddressFactory.getMulticastPort()
				)
		);
	}

	public DatagramIO<?> createDatagramIO(NetworkAddressFactory networkAddressFactory) {
		return new DatagramIOImpl(new DatagramIOConfigurationImpl());
	}
	public StreamServer<?> createStreamServer(NetworkAddressFactory networkAddressFactory)
	{
		return createStreamServer(networkAddressFactory.getStreamListenPort());
	}
	public abstract StreamServer<?> createStreamServer(int streamServerPort);

	public abstract int getRegistryMaintenanceIntervalMillis();

	public int getAliveIntervalMillis() {
		return 0;
	}

	public boolean isReceivedSubscriptionTimeoutIgnored() {
		return false;
	}

	public Integer getRemoteDeviceMaxAgeSeconds() {
		return null;
	}
	@SuppressWarnings({"PMD.LooseCoupling", "PMD.ReturnEmptyCollectionRatherThanNull"})
	public UpnpHeaders getDescriptorRetrievalHeaders(RemoteDeviceIdentity identity) {
		return null;
	}
	@SuppressWarnings({"PMD.LooseCoupling", "PMD.ReturnEmptyCollectionRatherThanNull"})
	public UpnpHeaders getEventSubscriptionHeaders(RemoteService service) {
		return null;
	}
	public abstract DeviceDescriptorBinder createDeviceDescriptorBinderUDA10(NetworkAddressFactory networkAddressFactory);

	public ServiceDescriptorBinder createServiceDescriptorBinderUDA10(NetworkAddressFactory networkAddressFactory)
	{
		return new UDA10ServiceDescriptorBinderImpl(networkAddressFactory);
	}

	public NetworkAddressFactory createMockNetworkAddressFactory(int streamListenPort, int multiCastPort) {
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
	public ExecutorService createMockDefaultExecutorService()
	{
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

	public abstract Namespace createNamespace();
	public abstract SOAPActionProcessor createSOAPActionProcessor();
	public abstract GENAEventProcessor createGENAEventProcessor();
	private static final Constructor<? extends PlatformUpnpServiceConfiguration> androidPlaformConstructor;
	private static final Constructor<? extends PlatformUpnpServiceConfiguration> desktopPlaformConstructor;
	static
	{
		Constructor<? extends PlatformUpnpServiceConfiguration> constructor=null;
		try {
			@SuppressWarnings("unchecked") Class<? extends PlatformUpnpServiceConfiguration> c=(Class<? extends PlatformUpnpServiceConfiguration>)Class.forName("com.distrimind.upnp_igd.android.platform.AndroidPlatformUpnpServiceConfiguration");
			constructor=c.getConstructor();
		} catch (ClassNotFoundException | NoSuchMethodException ignored) {

		}
		androidPlaformConstructor=constructor;
		constructor=null;
		try {
			@SuppressWarnings("unchecked") Class<? extends PlatformUpnpServiceConfiguration> c=(Class<? extends PlatformUpnpServiceConfiguration>)Class.forName("com.distrimind.upnp_igd.desktop.platform.DesktopPlatformUpnpServiceConfiguration");
			constructor=c.getConstructor();
		} catch (ClassNotFoundException | NoSuchMethodException ignored) {
		}
		desktopPlaformConstructor=constructor;
	}

	static PlatformUpnpServiceConfiguration getInstance(Platform platform)
	{
		Constructor<? extends PlatformUpnpServiceConfiguration> constructor;
		switch (platform)
		{
			case ANDROID:
				if (androidPlaformConstructor==null)
					throw new RuntimeException("The class com.distrimind.upnp_igd.android.platform.AndroidPlatformUpnpServiceConfiguration was not found. Please import UPnPIGD-Android library.");
				constructor=androidPlaformConstructor;
				break;
			case DESKTOP:
				if (desktopPlaformConstructor==null)
					throw new RuntimeException("com.distrimind.upnp_igd.desktop.platform.DesktopPlatformUpnpServiceConfiguration was not found. Please import UPnPIGD-Desktop library.");
				constructor=desktopPlaformConstructor;
				break;
			default:
				throw new IllegalAccessError();
		}
		try {
			return constructor.newInstance();
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	public abstract Platform getPlatformType();

	@Override
	public String toString() {
		return "PlatformUpnpServiceConfiguration{type="+getPlatformType()+"}";
	}
}
