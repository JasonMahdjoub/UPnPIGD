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

package com.distrimind.upnp.protocol.async;

import com.distrimind.upnp.protocol.ReceivingAsync;
import com.distrimind.upnp.protocol.RetrieveRemoteDescriptors;
import com.distrimind.upnp.transport.RouterException;
import com.distrimind.upnp.UpnpService;
import com.distrimind.upnp.model.ValidationError;
import com.distrimind.upnp.model.ValidationException;
import com.distrimind.upnp.model.message.IncomingDatagramMessage;
import com.distrimind.upnp.model.message.UpnpResponse;
import com.distrimind.upnp.model.message.discovery.IncomingSearchResponse;
import com.distrimind.upnp.model.meta.RemoteDevice;
import com.distrimind.upnp.model.meta.RemoteDeviceIdentity;
import com.distrimind.upnp.model.types.UDN;

import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.upnp.Log;

/**
 * Handles reception of search response messages.
 * <p>
 * This protocol implementation is basically the same as
 * the {@link ReceivingNotification} protocol for
 * an <em>ALIVE</em> message.
 * </p>
 *
 * @author Christian Bauer
 */
public class ReceivingSearchResponse extends ReceivingAsync<IncomingSearchResponse> {

    final private static DMLogger log = Log.getLogger(ReceivingSearchResponse.class);

    public ReceivingSearchResponse(UpnpService upnpService, IncomingDatagramMessage<UpnpResponse> inputMessage) {
        super(upnpService, new IncomingSearchResponse(inputMessage));
    }

    @Override
	protected void execute() throws RouterException {

        if (!getInputMessage().isSearchResponseMessage()) {
			if (log.isDebugEnabled()) {
				log.debug("Ignoring invalid search response message: " + getInputMessage());
			}
			return;
        }

        UDN udn = getInputMessage().getRootDeviceUDN();
        if (udn == null) {
			if (log.isDebugEnabled()) {
				log.debug("Ignoring search response message without UDN: " + getInputMessage());
			}
			return;
        }

        RemoteDeviceIdentity rdIdentity = new RemoteDeviceIdentity(getInputMessage());
		if (log.isDebugEnabled()) {
            log.debug("Received device search response: " + rdIdentity);
		}

		if (getUpnpService().getRegistry().update(rdIdentity)) {
			if (log.isDebugEnabled()) {
				log.debug("Remote device was already known: " + udn);
			}
			return;
        }

        RemoteDevice rd;
        try {
            rd = new RemoteDevice(rdIdentity);
        } catch (ValidationException ex) {
			if (log.isWarnEnabled()) log.warn("Validation errors of device during discovery: " + rdIdentity);
            for (ValidationError validationError : ex.getErrors()) {
				if (log.isWarnEnabled()) log.warn(validationError.toString());
            }
            return;
        }

        if (rdIdentity.getDescriptorURL() == null) {
			if (log.isTraceEnabled()) {
				log.trace("Ignoring message without location URL header: " + getInputMessage());
			}
			return;
        }

        if (rdIdentity.getMaxAgeSeconds() == null) {
			if (log.isTraceEnabled()) {
				log.trace("Ignoring message without max-age header: " + getInputMessage());
			}
			return;
        }

        // Unfortunately, we always have to retrieve the descriptor because at this point we
        // have no idea if it's a root or embedded device
        getUpnpService().getConfiguration().getAsyncProtocolExecutor().execute(
                new RetrieveRemoteDescriptors(getUpnpService(), rd)
        );

    }

}
