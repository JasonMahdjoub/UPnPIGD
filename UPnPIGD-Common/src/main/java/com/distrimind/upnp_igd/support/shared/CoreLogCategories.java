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

package com.distrimind.upnp_igd.support.shared;

import com.distrimind.upnp_igd.swing.logging.LogCategory;
import org.slf4j.event.Level;

import java.util.ArrayList;

/**
 * @author Christian Bauer
 */
public class CoreLogCategories extends ArrayList<LogCategory> {
    private static final long serialVersionUID = 1L;

    public CoreLogCategories() {
        super(10);

        add(new LogCategory("Network", new LogCategory.Group[]{

                new LogCategory.Group(
                        "UDP communication",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.transport.spi.DatagramIO.class.getName(), org.slf4j.event.Level.DEBUG),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.transport.spi.MulticastReceiver.class.getName(), org.slf4j.event.Level.DEBUG),
                        }
                ),

                new LogCategory.Group(
                        "UDP datagram processing and content",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.transport.spi.DatagramProcessor.class.getName(), org.slf4j.event.Level.TRACE)
                        }
                ),

                new LogCategory.Group(
                        "TCP communication",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.transport.spi.UpnpStream.class.getName(), org.slf4j.event.Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.transport.spi.StreamServer.class.getName(), org.slf4j.event.Level.DEBUG),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.transport.spi.StreamClient.class.getName(), org.slf4j.event.Level.DEBUG),
                        }
                ),

                new LogCategory.Group(
                        "SOAP action message processing and content",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.transport.spi.SOAPActionProcessor.class.getName(), org.slf4j.event.Level.TRACE)
                        }
                ),

                new LogCategory.Group(
                        "GENA event message processing and content",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.transport.spi.GENAEventProcessor.class.getName(), org.slf4j.event.Level.TRACE)
                        }
                ),

                new LogCategory.Group(
                        "HTTP header processing",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.model.message.UpnpHeaders.class.getName(), org.slf4j.event.Level.TRACE)
                        }
                ),
        }));


        add(new LogCategory("UPnP Protocol", new LogCategory.Group[]{

                new LogCategory.Group(
                        "Discovery (Notification & Search)",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.ProtocolFactory.class.getName(), org.slf4j.event.Level.TRACE),
                                new LogCategory.LoggerLevel("com.distrimind.upnp_igd.protocol.async", org.slf4j.event.Level.TRACE)
                        }
                ),

                new LogCategory.Group(
                        "Description",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.ProtocolFactory.class.getName(), org.slf4j.event.Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.RetrieveRemoteDescriptors.class.getName(), org.slf4j.event.Level.DEBUG),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.sync.ReceivingRetrieval.class.getName(), org.slf4j.event.Level.DEBUG),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.binding.xml.DeviceDescriptorBinder.class.getName(), org.slf4j.event.Level.DEBUG),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.binding.xml.ServiceDescriptorBinder.class.getName(), org.slf4j.event.Level.DEBUG),
                        }
                ),

                new LogCategory.Group(
                        "Control",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.ProtocolFactory.class.getName(), org.slf4j.event.Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.sync.ReceivingAction.class.getName(), org.slf4j.event.Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.sync.SendingAction.class.getName(), org.slf4j.event.Level.TRACE),
                        }
                ),

                new LogCategory.Group(
                        "GENA ",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel("com.distrimind.upnp_igd.model.gena", org.slf4j.event.Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.ProtocolFactory.class.getName(), org.slf4j.event.Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.sync.ReceivingEvent.class.getName(), org.slf4j.event.Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.sync.ReceivingSubscribe.class.getName(), org.slf4j.event.Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.sync.ReceivingUnsubscribe.class.getName(), org.slf4j.event.Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.sync.SendingEvent.class.getName(), org.slf4j.event.Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.sync.SendingSubscribe.class.getName(), org.slf4j.event.Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.sync.SendingUnsubscribe.class.getName(), org.slf4j.event.Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.protocol.sync.SendingRenewal.class.getName(), org.slf4j.event.Level.TRACE),
                        }
                ),
        }));

        add(new LogCategory("Core", new LogCategory.Group[]{

                new LogCategory.Group(
                        "Router",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.transport.Router.class.getName(), org.slf4j.event.Level.TRACE)
                        }
                ),

                new LogCategory.Group(
                        "Registry",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.registry.Registry.class.getName(), org.slf4j.event.Level.TRACE),
                        }
                ),

                new LogCategory.Group(
                        "Local service binding & invocation",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel("com.distrimind.upnp_igd.binding.annotations", org.slf4j.event.Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.model.meta.LocalService.class.getName(), org.slf4j.event.Level.TRACE),
                                new LogCategory.LoggerLevel("com.distrimind.upnp_igd.model.action", org.slf4j.event.Level.TRACE),
                                new LogCategory.LoggerLevel("com.distrimind.upnp_igd.model.state", org.slf4j.event.Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp_igd.model.DefaultServiceManager.class.getName(), org.slf4j.event.Level.TRACE)
                        }
                ),

                new LogCategory.Group(
                        "Control Point interaction",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel("com.distrimind.upnp_igd.controlpoint", Level.TRACE),
                        }
                ),
        }));

    }

}
