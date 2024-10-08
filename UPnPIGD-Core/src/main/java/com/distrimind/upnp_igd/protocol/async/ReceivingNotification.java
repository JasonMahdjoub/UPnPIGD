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

package com.distrimind.upnp_igd.protocol.async;

import com.distrimind.upnp_igd.protocol.ReceivingAsync;
import com.distrimind.upnp_igd.protocol.RetrieveRemoteDescriptors;
import com.distrimind.upnp_igd.transport.RouterException;
import com.distrimind.upnp_igd.UpnpService;
import com.distrimind.upnp_igd.model.ValidationError;
import com.distrimind.upnp_igd.model.ValidationException;
import com.distrimind.upnp_igd.model.message.IncomingDatagramMessage;
import com.distrimind.upnp_igd.model.message.UpnpRequest;
import com.distrimind.upnp_igd.model.message.discovery.IncomingNotificationRequest;
import com.distrimind.upnp_igd.model.meta.RemoteDevice;
import com.distrimind.upnp_igd.model.meta.RemoteDeviceIdentity;
import com.distrimind.upnp_igd.model.types.UDN;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles reception of notification messages.
 * <p>
 * First, the UDN is created from the received message.
 * </p>
 * <p>
 * If an <em>ALIVE</em> message has been received, a new background process will be started
 * running {@link RetrieveRemoteDescriptors}.
 * </p>
 * <p>
 * If a <em>BYEBYE</em> message has been received, the device will be removed from the registry
 * directly.
 * </p>
 * <p>
 * The following was added to the UDA 1.1 spec (in 1.3), clarifying the handling of messages:
 * </p>
 * <p>
 * "If a control point has received at least one 'byebye' message of a root device, embedded device, or
 * service, then the control point can assume that all are no longer available."
 * </p>
 * <p>
 * Of course, they contradict this a little later:
 * </p>
 * <p>
 * "Only when all original advertisements of a root device, embedded device, and services have
 * expired can a control point assume that they are no longer available."
 * </p>
 * <p>
 * This could mean that even if we get 'byeby'e for the root device, we still have to assume that its services
 * are available. That clearly makes no sense at all and I think it's just badly worded and relates to the
 * previous sentence wich says "if you don't get byebye's, rely on the expiration timeout". It does not
 * imply that a service or embedded device lives beyond its root device. It actually reinforces that we are
 * free to ignore anything that happens as long as the root device is not gone with 'byebye' or has expired.
 * In other words: There is no reason at all why SSDP sends dozens of messages for all embedded devices and
 * services. The composite is the root device and the composite defines the lifecycle of all.
 * </p>
 *
 * @author Christian Bauer
 */
public class ReceivingNotification extends ReceivingAsync<IncomingNotificationRequest> {

    final private static Logger log = Logger.getLogger(ReceivingNotification.class.getName());

    public ReceivingNotification(UpnpService upnpService, IncomingDatagramMessage<UpnpRequest> inputMessage) {
        super(upnpService, new IncomingNotificationRequest(inputMessage));
    }

    @Override
	protected void execute() throws RouterException {

        UDN udn = getInputMessage().getUDN();
        if (udn == null) {
			if (log.isLoggable(Level.FINE)) {
				log.fine("Ignoring notification message without UDN: " + getInputMessage());
			}
			return;
        }

        RemoteDeviceIdentity rdIdentity = new RemoteDeviceIdentity(getInputMessage());
		if (log.isLoggable(Level.FINE)) {
			log.fine("Received device notification: " + rdIdentity);
		}

		RemoteDevice rd;
        try {
            rd = new RemoteDevice(rdIdentity);
        } catch (ValidationException ex) {
			if (log.isLoggable(Level.WARNING)) log.warning("Validation errors of device during discovery: " + rdIdentity);
            for (ValidationError validationError : ex.getErrors()) {
				if (log.isLoggable(Level.WARNING)) log.warning(validationError.toString());
            }
            return;
        }

        if (getInputMessage().isAliveMessage()) {

			if (log.isLoggable(Level.FINE)) {
				log.fine("Received device ALIVE advertisement, descriptor location is: " + rdIdentity.getDescriptorURL());
			}

			if (rdIdentity.getDescriptorURL() == null) {
				if (log.isLoggable(Level.FINER)) {
					log.finer("Ignoring message without location URL header: " + getInputMessage());
				}
				return;
            }

            if (rdIdentity.getMaxAgeSeconds() == null) {
				if (log.isLoggable(Level.FINER)) {
					log.finer("Ignoring message without max-age header: " + getInputMessage());
				}
				return;
            }

            if (getUpnpService().getRegistry().update(rdIdentity)) {
				if (log.isLoggable(Level.FINER)) {
					log.finer("Remote device was already known: " + udn);
				}
				return;
            }

            // Unfortunately, we always have to retrieve the descriptor because at this point we
            // have no idea if it's a root or embedded device
            getUpnpService().getConfiguration().getAsyncProtocolExecutor().execute(
                    new RetrieveRemoteDescriptors(getUpnpService(), rd)
            );

        } else if (getInputMessage().isByeByeMessage()) {

            log.fine("Received device BYEBYE advertisement");
            boolean removed = getUpnpService().getRegistry().removeDevice(rd);
            if (removed) {
				if (log.isLoggable(Level.FINE)) {
					log.fine("Removed remote device from registry: " + rd);
				}
			}

        } else {
			if (log.isLoggable(Level.FINER)) {
				log.finer("Ignoring unknown notification message: " + getInputMessage());
			}
		}

    }


}
