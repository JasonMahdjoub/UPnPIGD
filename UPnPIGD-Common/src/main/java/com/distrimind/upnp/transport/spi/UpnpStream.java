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

package com.distrimind.upnp.transport.spi;

import com.distrimind.upnp.transport.Router;
import com.distrimind.upnp.model.message.StreamRequestMessage;
import com.distrimind.upnp.model.message.StreamResponseMessage;
import com.distrimind.upnp.model.message.UpnpResponse;
import com.distrimind.upnp.protocol.ProtocolCreationException;
import com.distrimind.upnp.protocol.ProtocolFactory;
import com.distrimind.upnp.protocol.ReceivingSync;
import com.distrimind.upnp.util.Exceptions;

import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.upnp.Log;

/**
 * A runnable representation of a single HTTP request/response procedure.
 * <p>
 * Instantiated by the {@link StreamServer}, executed by the
 * {@link Router}. See the pseudo-code example
 * in the documentation of {@link StreamServer}. An implementation's
 * <code>run()</code> method has to call the {@link #process(StreamRequestMessage)},
 * {@link #responseSent(StreamResponseMessage)} and
 * {@link #responseException(Throwable)} methods.
 * </p>
 * <p>
 * An implementation does not have to be thread-safe.
 * </p>
 * @author Christian Bauer
 */
public abstract class UpnpStream implements Runnable {

    final private static DMLogger log = Log.getLogger(UpnpStream.class);

    protected final ProtocolFactory protocolFactory;
    protected ReceivingSync<?, ?> syncProtocol;

    protected UpnpStream(ProtocolFactory protocolFactory) {
        this.protocolFactory = protocolFactory;
    }

    public ProtocolFactory getProtocolFactory() {
        return protocolFactory;
    }

    /**
     * Selects a UPnP protocol, runs it within the calling thread, returns the response.
     * <p>
     * This method will return <code>null</code> if the UPnP protocol returned <code>null</code>.
     * The HTTP response in this case is always <em>404 NOT FOUND</em>. Any other (HTTP) error
     * condition will be encapsulated in the returned response message and has to be
     * passed to the HTTP client as it is.
   
     * @param requestMsg The TCP (HTTP) stream request message.
     * @return The TCP (HTTP) stream response message, or <code>null</code> if a 404 should be sent to the client.
     */
    public StreamResponseMessage process(StreamRequestMessage requestMsg) {
		if (log.isDebugEnabled()) {
            log.debug("Processing stream request message: " + requestMsg);
		}

		try {
            // Try to get a protocol implementation that matches the request message
            syncProtocol = getProtocolFactory().createReceivingSync(requestMsg);
        } catch (ProtocolCreationException ex) {
			if (log.isWarnEnabled()) log.warn("Processing stream request failed - ", Exceptions.unwrap(ex));
            return new StreamResponseMessage(UpnpResponse.Status.NOT_IMPLEMENTED);
        }

        // Run it
		if (log.isDebugEnabled()) {
            log.debug("Running protocol for synchronous message processing: " + syncProtocol);
		}
		syncProtocol.run();

        // ... then grab the response
        StreamResponseMessage responseMsg = syncProtocol.getOutputMessage();

        if (responseMsg == null) {
            // That's ok, the caller is supposed to handle this properly (e.g. convert it to HTTP 404)
            log.trace("Protocol did not return any response message");
            return null;
        }
		if (log.isTraceEnabled()) {
			log.trace("Protocol returned response: " + responseMsg);
		}
		return responseMsg;
    }

    /**
     * Must be called by a subclass after the response has been successfully sent to the client.
     *
     * @param responseMessage The response message successfully sent to the client.
     */
    protected void responseSent(StreamResponseMessage responseMessage) {
        if (syncProtocol != null)
            syncProtocol.responseSent(responseMessage);
    }

    /**
     * Must be called by a subclass if the response was not delivered to the client.
     *
     * @param t The reason why the response wasn't delivered.
     */
    protected void responseException(Throwable t) {
        if (syncProtocol != null)
            syncProtocol.responseException(t);
    }

    @Override
    public String toString() {
        return "(" + getClass().getSimpleName() + ")";
    }
}
