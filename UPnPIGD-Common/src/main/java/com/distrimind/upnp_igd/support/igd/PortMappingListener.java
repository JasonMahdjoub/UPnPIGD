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

package com.distrimind.upnp_igd.support.igd;

import com.distrimind.upnp_igd.support.igd.callback.PortMappingAdd;
import com.distrimind.upnp_igd.support.igd.callback.PortMappingDelete;
import com.distrimind.upnp_igd.model.action.ActionInvocation;
import com.distrimind.upnp_igd.model.message.UpnpResponse;
import com.distrimind.upnp_igd.model.meta.Device;
import com.distrimind.upnp_igd.model.meta.Service;
import com.distrimind.upnp_igd.model.types.DeviceType;
import com.distrimind.upnp_igd.model.types.ServiceType;
import com.distrimind.upnp_igd.model.types.UDADeviceType;
import com.distrimind.upnp_igd.model.types.UDAServiceType;
import com.distrimind.upnp_igd.registry.DefaultRegistryListener;
import com.distrimind.upnp_igd.registry.Registry;
import com.distrimind.upnp_igd.support.model.PortMapping;

import java.util.*;
import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.upnp_igd.Log;

/**
 * Maintains UPnP port mappings on an InternetGatewayDevice automatically.
 * <p>
 * This listener will wait for discovered devices which support either
 * {@code WANIPConnection} or the {@code WANPPPConnection} service. As soon as any such
 * service is discovered, the desired port mapping will be created. When the UPnP service
 * is shutting down, all previously established port mappings with all services will
 * be deleted.
 * </p>
 * <p>
 * The following listener maps external WAN TCP port 8123 to internal host 10.0.0.2:
 * </p>
 * <pre>{@code
 * upnpService.getRegistry().addListener(
 *newPortMappingListener(newPortMapping(8123, "10.0.0.2",PortMapping.Protocol.TCP))
 * );}</pre>
 * <p>
 * If all you need from the UPnPIGD UPnP stack is NAT port mapping, use the following idiom:
 * </p>
 * <pre>{@code
 * UpnpService upnpService = new UpnpServiceImpl(
 *     new PortMappingListener(new PortMapping(8123, "10.0.0.2", PortMapping.Protocol.TCP))
 * );
 * <p/>
 * upnpService.getControlPoint().search(new STAllHeader()); // Search for all devices
 * <p/>
 * upnpService.shutdown(); // When you no longer need the port mapping
 * }</pre>
 *
 * @author Christian Bauer
 */
public class PortMappingListener extends DefaultRegistryListener {

    final private static DMLogger log = Log.getLogger(PortMappingListener.class);

	public static final String INTERNET_GATEWAY_DEVICE = "InternetGatewayDevice";
	public static final String WAN_CONNECTION_DEVICE = "WANConnectionDevice";
	public static final String WANIP_CONNECTION = "WANIPConnection";
	public static final String WANPPP_CONNECTION = "WANPPPConnection";

	public static class Types
	{
		private final DeviceType igdDeviceType;
		private final DeviceType connectionDeviceType;
		private final ServiceType ipServiceType;
		private final ServiceType pppServiceType;
		private Types(int version)
		{
			this.igdDeviceType=new UDADeviceType(INTERNET_GATEWAY_DEVICE, version);
			this.connectionDeviceType=new UDADeviceType(WAN_CONNECTION_DEVICE, version);
			this.ipServiceType=new UDAServiceType(WANIP_CONNECTION, version);
			this.pppServiceType=new UDAServiceType(WANPPP_CONNECTION, version);
		}

		public DeviceType getIgdDeviceType() {
			return igdDeviceType;
		}

		public DeviceType getConnectionDeviceType() {
			return connectionDeviceType;
		}

		public ServiceType getIpServiceType() {
			return ipServiceType;
		}

		public ServiceType getPppServiceType() {
			return pppServiceType;
		}
	}
	public static final List<Types> igdTypes=List.of(new Types(2), new Types(1));

    protected List<PortMapping> portMappings;

    // The key of the map is Service and equality is object identity, this is by-design
    protected Map<Service<?, ?, ?>, List<PortMapping>> activePortMappings = new HashMap<>();

    public PortMappingListener(PortMapping portMapping) {
        this(List.of(portMapping));
    }

    public PortMappingListener(List<PortMapping> portMappings) {
        this.portMappings = portMappings;
    }

    @Override
    synchronized public void deviceAdded(Registry registry, Device<?, ?, ?> device) {

        Service<?, ?, ?> connectionService;
        if ((connectionService = discoverConnectionService(device)) == null) return;

		if (log.isDebugEnabled()) {
            log.debug("Activating port mappings on: " + connectionService);
		}

		final List<PortMapping> activeForService = new ArrayList<>();
        for (final PortMapping pm : portMappings) {
			if (!pm.isInternalDataValidForPortAdd())
				continue;
            new PortMappingAdd(connectionService, registry.getUpnpService().getControlPoint(), pm) {

                @Override
                public void success(ActionInvocation<?> invocation) {
					if (log.isDebugEnabled()) {
						log.debug("Port mapping added: " + pm);
					}
					activeForService.add(pm);
                }

                @Override
                public void failure(ActionInvocation<?> invocation, UpnpResponse operation, String defaultMsg) {
                    handleFailureMessage("Failed to add port mapping: " + pm);
                    handleFailureMessage("Reason: " + defaultMsg);
                }
            }.run(); // Synchronous!
        }

        activePortMappings.put(connectionService, activeForService);
    }

    @Override
    synchronized public void deviceRemoved(Registry registry, Device<?, ?, ?> device) {
        for (Service<?, ?, ?> service : device.findServices()) {
            Iterator<Map.Entry<Service<?, ?, ?>, List<PortMapping>>> it = activePortMappings.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Service<?, ?, ?>, List<PortMapping>> activeEntry = it.next();
                if (!activeEntry.getKey().equals(service)) continue;

                if (!activeEntry.getValue().isEmpty())
                    handleFailureMessage("Device disappeared, couldn't delete port mappings: " + activeEntry.getValue().size());

                it.remove();
            }
        }
    }

    @Override
    synchronized public void beforeShutdown(Registry registry) {
        for (Map.Entry<Service<?, ?, ?>, List<PortMapping>> activeEntry : activePortMappings.entrySet()) {

            final Iterator<PortMapping> it = activeEntry.getValue().iterator();
            while (it.hasNext()) {
                final PortMapping pm = it.next();
				if (log.isDebugEnabled()) {
					log.debug("Trying to delete port mapping on IGD: " + pm);
				}
				new PortMappingDelete(activeEntry.getKey(), registry.getUpnpService().getControlPoint(), pm) {

                    @Override
                    public void success(ActionInvocation<?> invocation) {
						if (log.isDebugEnabled()) {
							log.debug("Port mapping deleted: " + pm);
						}
						it.remove();
                    }

                    @Override
                    public void failure(ActionInvocation<?> invocation, UpnpResponse operation, String defaultMsg) {
                        handleFailureMessage("Failed to delete port mapping: " + pm);
                        handleFailureMessage("Reason: " + defaultMsg);
                    }

                }.run(); // Synchronous!
            }
        }
    }

    protected Service<?, ?, ?> discoverConnectionService(Device<?, ?, ?> device) {
		if (igdTypes.stream().noneMatch(t -> t.igdDeviceType.equals(device.getType())))
			return null;

		for (Types t : igdTypes) {
			Collection<? extends Device<?, ?, ?>> connectionDevices = device.findDevices(t.getConnectionDeviceType());
			if (!connectionDevices.isEmpty()) {
				Device<?, ?, ?> connectionDevice = connectionDevices.iterator().next();
				if (log.isDebugEnabled()) {
					log.debug("Using first discovered WAN connection device: " + connectionDevice);
				}
				Service<?, ?, ?> ipConnectionService = connectionDevice.findService(t.getIpServiceType());
				Service<?, ?, ?> pppConnectionService = connectionDevice.findService(t.getPppServiceType());

				if (ipConnectionService == null && pppConnectionService == null) {
					if (log.isDebugEnabled()) {
						log.debug("IGD doesn't support IP or PPP WAN connection service: " + device);
					}
					continue;
				}

				return ipConnectionService != null ? ipConnectionService : pppConnectionService;

			}
		}
		if (log.isDebugEnabled()) {
			log.debug("IGD doesn't support any '" + WAN_CONNECTION_DEVICE + "': " + device);
		}
		return null;
    }

    protected void handleFailureMessage(String s) {
        log.warn(s);
    }

}

