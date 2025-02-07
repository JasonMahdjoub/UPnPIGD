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
import com.distrimind.upnp_igd.binding.xml.UDA10DeviceDescriptorBinderImpl;
import com.distrimind.upnp_igd.model.Namespace;
import com.distrimind.upnp_igd.transport.impl.*;
import com.distrimind.upnp_igd.transport.spi.*;

import java.util.concurrent.ExecutorService;

public class DesktopPlatformUpnpServiceConfiguration extends PlatformUpnpServiceConfiguration {

	public DesktopPlatformUpnpServiceConfiguration() {
	}


	@Override
	public DeviceDescriptorBinder createDeviceDescriptorBinderUDA10(NetworkAddressFactory networkAddressFactory) {
		return new UDA10DeviceDescriptorBinderImpl(networkAddressFactory);
	}


	@Override
	public Namespace createNamespace() {
		return new Namespace();
	}

	@Override
	public SOAPActionProcessor createSOAPActionProcessor() {
		return new SOAPActionProcessorImpl();
	}

	@Override
	public GENAEventProcessor createGENAEventProcessor() {
		return new GENAEventProcessorImpl();
	}

	@Override
	public StreamClient<?> createStreamClient(ExecutorService syncProtocolExecutorService, int timeoutSeconds) {
		return new StreamClientImpl(
				new StreamClientConfigurationImpl(
						syncProtocolExecutorService,
						timeoutSeconds
				)
		);
	}

	@Override
	public StreamServer<?> createStreamServer(int streamServerPort) {
		return new StreamServerImpl(
				new StreamServerConfigurationImpl(
						streamServerPort
				)
		);
	}
	@Override
	public int getRegistryMaintenanceIntervalMillis() {
		return 1000;
	}
	@Override
	public NetworkAddressFactory createNetworkAddressFactory(int streamListenPort, int multicastPort) {
		return new NetworkAddressFactoryImpl(streamListenPort, multicastPort);
	}
	@Override
	public Platform getPlatformType() {
		return Platform.DESKTOP;
	}
}
