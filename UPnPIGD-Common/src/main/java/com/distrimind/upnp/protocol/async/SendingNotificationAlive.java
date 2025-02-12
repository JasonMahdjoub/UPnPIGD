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

import com.distrimind.upnp.transport.RouterException;
import com.distrimind.upnp.UpnpService;
import com.distrimind.upnp.model.meta.LocalDevice;
import com.distrimind.upnp.model.types.NotificationSubtype;

import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.upnp.Log;

/**
 * Sending <em>ALIVE</em> notification messages for a registered local device.
 *
 * @author Christian Bauer
 */
public class SendingNotificationAlive extends SendingNotification {

    final private static DMLogger log = Log.getLogger(SendingNotificationAlive.class);

    public SendingNotificationAlive(UpnpService upnpService, LocalDevice<?> device) {
        super(upnpService, device);
    }

    @Override
    protected void execute() throws RouterException {
		if (log.isDebugEnabled()) {
            log.debug("Sending alive messages ("+getBulkRepeat()+" times) for: " + getDevice());
		}
		super.execute();
    }

    @Override
	protected NotificationSubtype getNotificationSubtype() {
        return NotificationSubtype.ALIVE;
    }

}
