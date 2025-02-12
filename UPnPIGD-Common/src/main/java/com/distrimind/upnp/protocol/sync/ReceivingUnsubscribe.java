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

package com.distrimind.upnp.protocol.sync;

import com.distrimind.upnp.protocol.ReceivingSync;
import com.distrimind.upnp.transport.RouterException;
import com.distrimind.upnp.UpnpService;
import com.distrimind.upnp.model.gena.LocalGENASubscription;
import com.distrimind.upnp.model.message.StreamRequestMessage;
import com.distrimind.upnp.model.message.StreamResponseMessage;
import com.distrimind.upnp.model.message.UpnpResponse;
import com.distrimind.upnp.model.message.gena.IncomingUnsubscribeRequestMessage;
import com.distrimind.upnp.model.resource.ServiceEventSubscriptionResource;

import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.upnp.Log;

/**
 * Handles reception of GENA event unsubscribe messages.
 *
 * @author Christian Bauer
 */
public class ReceivingUnsubscribe extends ReceivingSync<StreamRequestMessage, StreamResponseMessage> {

    final private static DMLogger log = Log.getLogger(ReceivingUnsubscribe.class);

    public ReceivingUnsubscribe(UpnpService upnpService, StreamRequestMessage inputMessage) {
        super(upnpService, inputMessage);
    }

    @Override
	protected StreamResponseMessage executeSync() throws RouterException {

        ServiceEventSubscriptionResource<?> resource =
                getUpnpService().getRegistry().getResource(
                        ServiceEventSubscriptionResource.class,
                        getInputMessage().getUri()
        );

        if (resource == null) {
			if (log.isDebugEnabled()) {
				log.debug("No local resource found: " + getInputMessage());
			}
			return null;
        }

		if (log.isDebugEnabled()) {
            log.debug("Found local event subscription matching relative request URI: " + getInputMessage().getUri());
		}

		IncomingUnsubscribeRequestMessage requestMessage =
                new IncomingUnsubscribeRequestMessage(getInputMessage(), resource.getModel());

        // Error conditions UDA 1.0 section 4.1.3
        if (requestMessage.getSubscriptionId() != null &&
                (requestMessage.hasNotificationHeader() || requestMessage.hasCallbackHeader())) {
			if (log.isDebugEnabled()) {
				log.debug("Subscription ID and NT or Callback in unsubcribe request: " + getInputMessage());
			}
			return new StreamResponseMessage(UpnpResponse.Status.BAD_REQUEST);
        }

        LocalGENASubscription<?> subscription =
                getUpnpService().getRegistry().getLocalSubscription(requestMessage.getSubscriptionId());

        if (subscription == null) {
			if (log.isDebugEnabled()) {
				log.debug("Invalid subscription ID for unsubscribe request: " + getInputMessage());
			}
			return new StreamResponseMessage(UpnpResponse.Status.PRECONDITION_FAILED);
        }

		if (log.isDebugEnabled()) {
            log.debug("Unregistering subscription: " + subscription);
		}
		if (getUpnpService().getRegistry().removeLocalSubscription(subscription)) {
            subscription.end(null); // No reason, just an unsubscribed
        } else {
            log.debug("Subscription was already removed from registry");
        }

        return new StreamResponseMessage(UpnpResponse.Status.OK);
    }
}