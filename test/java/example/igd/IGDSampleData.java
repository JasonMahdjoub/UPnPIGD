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
package example.igd;

import com.distrimind.upnp_igd.binding.annotations.UpnpAction;
import com.distrimind.upnp_igd.binding.annotations.UpnpInputArgument;
import com.distrimind.upnp_igd.binding.annotations.AnnotationLocalServiceBinder;
import com.distrimind.upnp_igd.binding.annotations.UpnpOutputArgument;
import com.distrimind.upnp_igd.binding.annotations.UpnpService;
import com.distrimind.upnp_igd.binding.annotations.UpnpServiceId;
import com.distrimind.upnp_igd.binding.annotations.UpnpServiceType;
import com.distrimind.upnp_igd.binding.annotations.UpnpStateVariable;
import com.distrimind.upnp_igd.binding.annotations.UpnpStateVariables;
import com.distrimind.upnp_igd.model.DefaultServiceManager;
import com.distrimind.upnp_igd.model.action.ActionException;
import com.distrimind.upnp_igd.model.meta.DeviceDetails;
import com.distrimind.upnp_igd.model.meta.DeviceIdentity;
import com.distrimind.upnp_igd.model.meta.LocalDevice;
import com.distrimind.upnp_igd.model.meta.LocalService;
import com.distrimind.upnp_igd.model.types.ErrorCode;
import com.distrimind.upnp_igd.model.types.UDADeviceType;
import com.distrimind.upnp_igd.model.types.UDN;
import com.distrimind.upnp_igd.model.types.UnsignedIntegerFourBytes;
import com.distrimind.upnp_igd.model.types.UnsignedIntegerTwoBytes;
import com.distrimind.upnp_igd.support.model.Connection;
import com.distrimind.upnp_igd.support.model.PortMapping;

import java.util.Collection;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class IGDSampleData {

    public static <T> LocalService<T> readService(Class<T> serviceClass) throws Exception {
        LocalService<T> service = new AnnotationLocalServiceBinder().read(serviceClass);
        service.setManager(
                new DefaultServiceManager<>(service, serviceClass)
        );
        return service;
    }

    public static <T> LocalDevice<T> createIGDevice(Class<T> serviceClass) throws Exception {
        return createIGDevice(
                null,
				List.of(
						createWANDevice(
								null,
								List.of(
										createWANConnectionDevice(List.of(readService(serviceClass)), null)
								)
						)
				));
    }

    public static <T> LocalDevice<T> createIGDevice(Collection<LocalService<T>> services, List<LocalDevice<T>> embedded) throws Exception {
        return new LocalDevice<>(
                new DeviceIdentity(new UDN("1111")),
                new UDADeviceType("InternetGatewayDevice", 1),
                new DeviceDetails("Example Router"),
                services,
                embedded
        );
    }

    public static <T> LocalDevice<T> createWANDevice(Collection<LocalService<T>> services, List<LocalDevice<T>> embedded) throws Exception {
        return new LocalDevice<>(
                new DeviceIdentity(new UDN("2222")),
                new UDADeviceType("WANDevice", 1),
                new DeviceDetails("Example WAN Device"),
                services,
                embedded
        );
    }

    public static <T> LocalDevice<T> createWANConnectionDevice(Collection<LocalService<T>> services, List<LocalDevice<T>> embedded) throws Exception {
        return new LocalDevice<>(
                new DeviceIdentity(new UDN("3333")),
                new UDADeviceType("WANConnectionDevice", 1),
                new DeviceDetails("Example WAN Connection Device"),
                services,
                embedded
        );
    }

    @UpnpService(
            serviceId = @UpnpServiceId("WANIPConnection"),
            serviceType = @UpnpServiceType("WANIPConnection")
    )
    @UpnpStateVariables({
            @UpnpStateVariable(name = "RemoteHost", datatype = "string", sendEvents = false),
            @UpnpStateVariable(name = "ExternalPort", datatype = "ui2", sendEvents = false),
            @UpnpStateVariable(name = "PortMappingProtocol", datatype = "string", sendEvents = false, allowedValuesEnum = PortMapping.Protocol.class),
            @UpnpStateVariable(name = "InternalPort", datatype = "ui2", sendEvents = false),
            @UpnpStateVariable(name = "InternalClient", datatype = "string", sendEvents = false),
            @UpnpStateVariable(name = "PortMappingEnabled", datatype = "boolean", sendEvents = false),
            @UpnpStateVariable(name = "PortMappingDescription", datatype = "string", sendEvents = false),
            @UpnpStateVariable(name = "PortMappingLeaseDuration", datatype = "ui4", sendEvents = false),
            @UpnpStateVariable(name = "ConnectionStatus", datatype = "string", sendEvents = false),
            @UpnpStateVariable(name = "LastConnectionError", datatype = "string", sendEvents = false),
            @UpnpStateVariable(name = "Uptime", datatype = "ui4", sendEvents = false),
            @UpnpStateVariable(name = "ExternalIPAddress", datatype = "string", sendEvents = false),
            @UpnpStateVariable(name = "PortMappingIndex", datatype = "ui2", sendEvents = false)

    })
    public static class WANIPConnectionService {

        @UpnpAction
        public void addPortMapping(
                @UpnpInputArgument(name = "NewRemoteHost", stateVariable = "RemoteHost") String remoteHost,
                @UpnpInputArgument(name = "NewExternalPort", stateVariable = "ExternalPort") UnsignedIntegerTwoBytes externalPort,
                @UpnpInputArgument(name = "NewProtocol", stateVariable = "PortMappingProtocol") String protocol,
                @UpnpInputArgument(name = "NewInternalPort", stateVariable = "InternalPort") UnsignedIntegerTwoBytes internalPort,
                @UpnpInputArgument(name = "NewInternalClient", stateVariable = "InternalClient") String internalClient,
                @UpnpInputArgument(name = "NewEnabled", stateVariable = "PortMappingEnabled") Boolean enabled,
                @UpnpInputArgument(name = "NewPortMappingDescription", stateVariable = "PortMappingDescription") String description,
                @UpnpInputArgument(name = "NewLeaseDuration", stateVariable = "PortMappingLeaseDuration") UnsignedIntegerFourBytes leaseDuration
        ) throws ActionException {
            try {
                addPortMapping(new PortMapping(
                        enabled,
                        leaseDuration,
                        remoteHost,
                        externalPort,
                        internalPort,
                        internalClient,
                        PortMapping.Protocol.valueOf(protocol),
                        description
                ));
            } catch (Exception ex) {
                throw new ActionException(ErrorCode.ACTION_FAILED, "Can't convert port mapping: " + ex.toString(), ex);
            }
        }

        @UpnpAction
        public void deletePortMapping(
                @UpnpInputArgument(name = "NewRemoteHost", stateVariable = "RemoteHost") String remoteHost,
                @UpnpInputArgument(name = "NewExternalPort", stateVariable = "ExternalPort") UnsignedIntegerTwoBytes externalPort,
                @UpnpInputArgument(name = "NewProtocol", stateVariable = "PortMappingProtocol") String protocol
        ) throws ActionException {
            try {
                deletePortMapping(new PortMapping(
                        remoteHost,
                        externalPort,
                        PortMapping.Protocol.valueOf(protocol)
                ));
            } catch (Exception ex) {
                throw new ActionException(ErrorCode.ACTION_FAILED, "Can't convert port mapping: " + ex.toString(), ex);
            }
        }

        @UpnpAction(out = {
                @UpnpOutputArgument(name = "NewRemoteHost", stateVariable = "RemoteHost", getterName = "getRemoteHost"),
                @UpnpOutputArgument(name = "NewExternalPort", stateVariable = "ExternalPort", getterName = "getExternalPort"),
                @UpnpOutputArgument(name = "NewProtocol", stateVariable = "PortMappingProtocol", getterName = "getProtocol"),
                @UpnpOutputArgument(name = "NewInternalPort", stateVariable = "InternalPort", getterName = "getInternalPort"),
                @UpnpOutputArgument(name = "NewInternalClient", stateVariable = "InternalClient", getterName = "getInternalClient"),
                @UpnpOutputArgument(name = "NewEnabled", stateVariable = "PortMappingEnabled", getterName = "isEnabled"),
                @UpnpOutputArgument(name = "NewPortMappingDescription", stateVariable = "PortMappingDescription", getterName = "getDescription"),
                @UpnpOutputArgument(name = "NewLeaseDuration", stateVariable = "PortMappingLeaseDuration", getterName = "getLeaseDurationSeconds")
        })
        public PortMapping getGenericPortMappingEntry(
                @UpnpInputArgument(name = "NewPortMappingIndex", stateVariable = "PortMappingIndex") UnsignedIntegerTwoBytes index
        ) throws ActionException {
            return null;
        }

        protected void addPortMapping(PortMapping portMapping) {
        }

        protected void deletePortMapping(PortMapping portMapping) {
        }

        @UpnpAction(out = {
                @UpnpOutputArgument(name = "NewConnectionStatus", stateVariable = "ConnectionStatus", getterName = "getStatus"),
                @UpnpOutputArgument(name = "NewLastConnectionError", stateVariable = "LastConnectionError", getterName = "getLastError"),
                @UpnpOutputArgument(name = "NewUptime", stateVariable = "Uptime", getterName = "getUptime")
        })
        public Connection.StatusInfo getStatusInfo() {
            return null;
        }

        @UpnpAction(out = {
                @UpnpOutputArgument(name = "NewExternalIPAddress", stateVariable = "ExternalIPAddress")
        })
        public String getExternalIPAddress() {
            return null;
        }

    }

}
