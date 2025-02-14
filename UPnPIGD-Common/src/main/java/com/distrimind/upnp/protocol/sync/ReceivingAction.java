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

import com.distrimind.flexilogxml.FlexiLogXML;
import com.distrimind.upnp.model.action.ActionExecutor;
import com.distrimind.upnp.model.meta.Action;
import com.distrimind.upnp.model.meta.LocalService;
import com.distrimind.upnp.protocol.ReceivingSync;
import com.distrimind.upnp.transport.RouterException;
import com.distrimind.upnp.UpnpService;
import com.distrimind.upnp.model.action.ActionCancelledException;
import com.distrimind.upnp.model.action.ActionException;
import com.distrimind.upnp.model.action.RemoteActionInvocation;
import com.distrimind.upnp.model.message.StreamRequestMessage;
import com.distrimind.upnp.model.message.StreamResponseMessage;
import com.distrimind.upnp.model.message.UpnpResponse;
import com.distrimind.upnp.model.message.control.IncomingActionRequestMessage;
import com.distrimind.upnp.model.message.control.OutgoingActionResponseMessage;
import com.distrimind.upnp.model.message.header.ContentTypeHeader;
import com.distrimind.upnp.model.message.header.UpnpHeader;
import com.distrimind.upnp.model.resource.ServiceControlResource;
import com.distrimind.upnp.model.types.ErrorCode;
import com.distrimind.upnp.model.UnsupportedDataException;
import com.distrimind.upnp.util.Exceptions;

import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.upnp.Log;

import com.distrimind.flexilogxml.log.Level;

/**
 * Handles reception of control messages, invoking actions on local services.
 * <p>
 * Actions are invoked through the {@link ActionExecutor} returned
 * by the registered {@link LocalService#getExecutor(Action)}
 * method.
 * </p>
 *
 * @author Christian Bauer
 */
public class ReceivingAction extends ReceivingSync<StreamRequestMessage, StreamResponseMessage> {

    final private static DMLogger log = Log.getLogger(ReceivingAction.class);

    public ReceivingAction(UpnpService upnpService, StreamRequestMessage inputMessage) {
        super(upnpService, inputMessage);
    }

    @Override
	protected StreamResponseMessage executeSync() throws RouterException {

        ContentTypeHeader contentTypeHeader =
                getInputMessage().getHeaders().getFirstHeader(UpnpHeader.Type.CONTENT_TYPE, ContentTypeHeader.class);

        // Special rules for action messages! UDA 1.0 says:
        // 'If the CONTENT-TYPE header specifies an unsupported value (other than "text/xml") the
        // device must return an HTTP status code "415 Unsupported Media Type".'
        if (contentTypeHeader != null && !contentTypeHeader.isUDACompliantXML()) {
			if (log.isWarnEnabled())
                log.warn("Received invalid Content-Type '" + contentTypeHeader + "': " + getInputMessage());
            return new StreamResponseMessage(new UpnpResponse(UpnpResponse.Status.UNSUPPORTED_MEDIA_TYPE));
        }

        if (contentTypeHeader == null) {
			if (log.isWarnEnabled()) log.warn("Received without Content-Type: " + getInputMessage());
        }

        ServiceControlResource<?> resource =
                getUpnpService().getRegistry().getResource(
                        ServiceControlResource.class,
                        getInputMessage().getUri()
                );

        if (resource == null) {
			if (log.isDebugEnabled()) {
				log.debug("No local resource found: " + getInputMessage());
			}
			return null;
        }

		if (log.isDebugEnabled()) {
            log.debug("Found local action resource matching relative request URI: " + getInputMessage().getUri());
		}

		RemoteActionInvocation<? extends LocalService<?>> invocation;
        OutgoingActionResponseMessage responseMessage;

        try {

            // Throws ActionException if the action can't be found
            IncomingActionRequestMessage requestMessage =
                    new IncomingActionRequestMessage(getInputMessage(), resource.getModel());

			if (log.isTraceEnabled()) {
				log.trace("Created incoming action request message: " + requestMessage);
			}
			invocation = new RemoteActionInvocation<>(requestMessage.getAction(), getRemoteClientInfo());

            // Throws UnsupportedDataException if the body can't be read
            log.debug("Reading body of request message");
            getUpnpService().getConfiguration().getSoapActionProcessor().readBody(requestMessage, invocation);

			if (log.isDebugEnabled()) {
				log.debug("Executing on local service: " + invocation);
			}
			resource.getModel().getExecutor(invocation.getAction()).executeWithUntypedGeneric(invocation);

            if (invocation.getFailure() == null) {
                responseMessage =
                        new OutgoingActionResponseMessage(invocation.getAction());
            } else {

                if (invocation.getFailure() instanceof ActionCancelledException) {
                    log.debug("Action execution was cancelled, returning 404 to client");
                    // A 404 status is appropriate for this situation: The resource is gone/not available, and it's
                    // a temporary condition. Most likely the cancellation happened because the client connection
                    // has been dropped, so it doesn't really matter what we return here anyway.
                    return null;
                } else {
                    responseMessage =
                            new OutgoingActionResponseMessage(
                                UpnpResponse.Status.INTERNAL_SERVER_ERROR,
                                invocation.getAction()
                            );
                }
            }

        } catch (ActionException ex) {
			if (log.isTraceEnabled()) {
				log.trace("Error executing local action: ", ex);
			}

			invocation = new RemoteActionInvocation<>(ex, getRemoteClientInfo());
            responseMessage = new OutgoingActionResponseMessage(UpnpResponse.Status.INTERNAL_SERVER_ERROR);

        } catch (UnsupportedDataException ex) {
			if (log.isWarnEnabled()) log.warn("Error reading action request XML body: ", ex, Exceptions.unwrap(ex));

            invocation =
                    new RemoteActionInvocation<>(
                        Exceptions.unwrap(ex) instanceof ActionException
                                ? (ActionException)Exceptions.unwrap(ex)
                                : new ActionException(ErrorCode.ACTION_FAILED, ex.getMessage()),
                        getRemoteClientInfo()
                    );
            responseMessage = new OutgoingActionResponseMessage(UpnpResponse.Status.INTERNAL_SERVER_ERROR);

        }

        try {

            log.debug("Writing body of response message");
            getUpnpService().getConfiguration().getSoapActionProcessor().writeBody(responseMessage, invocation);

			if (log.isDebugEnabled()) {
				log.debug("Returning finished response message: " + responseMessage);
			}
			return responseMessage;

        } catch (UnsupportedDataException ex) {
            FlexiLogXML.log(Level.WARN, ex);
			if (log.isWarnEnabled()) {
				log.warn("Failure writing body of response message, sending '500 Internal Server Error' without body");
				log.warn("Exception root cause: ", Exceptions.unwrap(ex));
			}
            return new StreamResponseMessage(UpnpResponse.Status.INTERNAL_SERVER_ERROR);
        }
    }

}
