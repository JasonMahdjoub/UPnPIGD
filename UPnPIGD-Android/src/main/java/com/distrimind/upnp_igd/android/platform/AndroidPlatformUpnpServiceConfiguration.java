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

package com.distrimind.upnp_igd.android.platform;

import android.os.Build;
import com.distrimind.upnp_igd.android.AndroidNetworkAddressFactory;
import com.distrimind.upnp_igd.android.transport.impl.undertow.UndertowStreamClientConfigurationImpl;
import com.distrimind.upnp_igd.android.transport.impl.undertow.UndertowStreamClientImpl;
import com.distrimind.upnp_igd.android.transport.impl.undertow.UndertowStreamServerImpl;
import com.distrimind.upnp_igd.android.transport.impl.undertow.Worker;
import com.distrimind.upnp_igd.binding.xml.DeviceDescriptorBinder;
import com.distrimind.upnp_igd.binding.xml.RecoveringUDA10DeviceDescriptorBinderImpl;
import com.distrimind.upnp_igd.model.ModelUtil;
import com.distrimind.upnp_igd.model.Namespace;
import com.distrimind.upnp_igd.model.ServerClientTokens;
import com.distrimind.upnp_igd.platform.Platform;
import com.distrimind.upnp_igd.platform.PlatformUpnpServiceConfiguration;
import com.distrimind.upnp_igd.transport.impl.RecoveringGENAEventProcessorImpl;
import com.distrimind.upnp_igd.transport.impl.RecoveringSOAPActionProcessorImpl;
import com.distrimind.upnp_igd.transport.impl.StreamServerConfigurationImpl;
import com.distrimind.upnp_igd.transport.spi.*;
import org.xnio.XnioWorker;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

/**
 * @author Jason Mahdjoub
 * @since 1.2.0
 */
public class AndroidPlatformUpnpServiceConfiguration extends PlatformUpnpServiceConfiguration {
	@Override
	public ExecutorService createDefaultAndroidExecutorService() throws IOException
	{
		return Worker.createDefaultWorker();
	}

	@Override
	public StreamClient<?> createStreamClient(ExecutorService syncProtocolExecutorService, int timeoutSeconds) {
		XnioWorker worker;
		if (syncProtocolExecutorService instanceof XnioWorker)
			worker=(XnioWorker)syncProtocolExecutorService;
		else
			throw new IllegalArgumentException("syncProtocolExecutorService should be a XnioWorker class and not : "+syncProtocolExecutorService.getClass());

		return new UndertowStreamClientImpl(
				new UndertowStreamClientConfigurationImpl(
						worker
						,timeoutSeconds
				) {
					@Override
					public String getUserAgentValue(int majorVersion, int minorVersion) {
						if (ModelUtil.ANDROID_EMULATOR || ModelUtil.ANDROID_RUNTIME) {
							// TODO: UPNP VIOLATION: Synology NAS requires User-Agent to contain
							// "Android" to return DLNA protocolInfo required to stream to Samsung TV
							// see: http://two-play.com/forums/viewtopic.php?f=6&t=81
							ServerClientTokens tokens = new ServerClientTokens(majorVersion, minorVersion);
							tokens.setOsName("Android");
							tokens.setOsVersion(Build.VERSION.RELEASE);
							return tokens.toString();
						}
						else
							return super.getUserAgentValue(majorVersion, minorVersion);
					}
				}
		);
	}

	@Override
	public StreamServer<?> createStreamServer(int streamServerPort) {
		return new UndertowStreamServerImpl(
				new StreamServerConfigurationImpl(
						streamServerPort
				)
		);
	}

	@Override
	public int getRegistryMaintenanceIntervalMillis() {
		return 3000; // Preserve battery on Android, only run every 3 seconds
	}

	@Override
	public DeviceDescriptorBinder createDeviceDescriptorBinderUDA10(NetworkAddressFactory networkAddressFactory) {
		return new RecoveringUDA10DeviceDescriptorBinderImpl(networkAddressFactory);
	}


	@Override
	public Namespace createNamespace() {
		//return new Namespace("/upnp");
		return new Namespace();
	}

	@Override
	public SOAPActionProcessor createSOAPActionProcessor() {
		return new RecoveringSOAPActionProcessorImpl();
	}

	@Override
	public NetworkAddressFactory createNetworkAddressFactory(int streamListenPort, int multicastPort) {
		return new AndroidNetworkAddressFactory(streamListenPort, multicastPort);
	}
	@Override
	public GENAEventProcessor createGENAEventProcessor() {
		return new RecoveringGENAEventProcessorImpl();
	}

	@Override
	public Platform getPlatformType() {
		return Platform.ANDROID;
	}


}
