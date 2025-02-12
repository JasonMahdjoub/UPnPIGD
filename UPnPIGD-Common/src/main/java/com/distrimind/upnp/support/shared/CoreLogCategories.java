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

package com.distrimind.upnp.support.shared;

import com.distrimind.upnp.swing.logging.LogCategory;
import com.distrimind.flexilogxml.log.Level;

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
                                new LogCategory.LoggerLevel(com.distrimind.upnp.transport.spi.DatagramIO.class.getName(), Level.DEBUG),
                                new LogCategory.LoggerLevel(com.distrimind.upnp.transport.spi.MulticastReceiver.class.getName(), Level.DEBUG),
                        }
                ),

                new LogCategory.Group(
                        "UDP datagram processing and content",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp.transport.spi.DatagramProcessor.class.getName(), Level.TRACE)
                        }
                ),

                new LogCategory.Group(
                        "TCP communication",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp.transport.spi.UpnpStream.class.getName(), Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp.transport.spi.StreamServer.class.getName(), Level.DEBUG),
                                new LogCategory.LoggerLevel(com.distrimind.upnp.transport.spi.StreamClient.class.getName(), Level.DEBUG),
                        }
                ),

                new LogCategory.Group(
                        "SOAP action message processing and content",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp.transport.spi.SOAPActionProcessor.class.getName(), Level.TRACE)
                        }
                ),

                new LogCategory.Group(
                        "GENA event message processing and content",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp.transport.spi.GENAEventProcessor.class.getName(), Level.TRACE)
                        }
                ),

                new LogCategory.Group(
                        "HTTP header processing",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp.model.message.UpnpHeaders.class.getName(), Level.TRACE)
                        }
                ),
        }));


        add(new LogCategory("UPnP Protocol", new LogCategory.Group[]{

                new LogCategory.Group(
                        "Discovery (Notification & Search)",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp.protocol.ProtocolFactory.class.getName(), Level.TRACE),
                                new LogCategory.LoggerLevel("com.distrimind.upnp.protocol.async", Level.TRACE)
                        }
                ),

                new LogCategory.Group(
                        "Description",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp.protocol.ProtocolFactory.class.getName(), Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp.protocol.RetrieveRemoteDescriptors.class.getName(), Level.DEBUG),
                                new LogCategory.LoggerLevel(com.distrimind.upnp.protocol.sync.ReceivingRetrieval.class.getName(), Level.DEBUG),
                                new LogCategory.LoggerLevel(com.distrimind.upnp.binding.xml.DeviceDescriptorBinder.class.getName(), Level.DEBUG),
                                new LogCategory.LoggerLevel(com.distrimind.upnp.binding.xml.ServiceDescriptorBinder.class.getName(), Level.DEBUG),
                        }
                ),

                new LogCategory.Group(
                        "Control",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp.protocol.ProtocolFactory.class.getName(), Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp.protocol.sync.ReceivingAction.class.getName(), Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp.protocol.sync.SendingAction.class.getName(), Level.TRACE),
                        }
                ),

                new LogCategory.Group(
                        "GENA ",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel("com.distrimind.upnp.model.gena", Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp.protocol.ProtocolFactory.class.getName(), Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp.protocol.sync.ReceivingEvent.class.getName(), Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp.protocol.sync.ReceivingSubscribe.class.getName(), Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp.protocol.sync.ReceivingUnsubscribe.class.getName(), Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp.protocol.sync.SendingEvent.class.getName(), Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp.protocol.sync.SendingSubscribe.class.getName(), Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp.protocol.sync.SendingUnsubscribe.class.getName(), Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp.protocol.sync.SendingRenewal.class.getName(), Level.TRACE),
                        }
                ),
        }));

        add(new LogCategory("Core", new LogCategory.Group[]{

                new LogCategory.Group(
                        "Router",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp.transport.Router.class.getName(), Level.TRACE)
                        }
                ),

                new LogCategory.Group(
                        "Registry",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel(com.distrimind.upnp.registry.Registry.class.getName(), Level.TRACE),
                        }
                ),

                new LogCategory.Group(
                        "Local service binding & invocation",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel("com.distrimind.upnp.binding.annotations", Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp.model.meta.LocalService.class.getName(), Level.TRACE),
                                new LogCategory.LoggerLevel("com.distrimind.upnp.model.action", Level.TRACE),
                                new LogCategory.LoggerLevel("com.distrimind.upnp.model.state", Level.TRACE),
                                new LogCategory.LoggerLevel(com.distrimind.upnp.model.DefaultServiceManager.class.getName(), Level.TRACE)
                        }
                ),

                new LogCategory.Group(
                        "Control Point interaction",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel("com.distrimind.upnp.controlpoint", Level.TRACE),
                        }
                ),
        }));

    }

}
