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

package com.distrimind.upnp_igd.transport.impl.jetty;

import com.distrimind.upnp_igd.model.message.*;
import com.distrimind.upnp_igd.model.message.header.UpnpHeader;
import com.distrimind.upnp_igd.transport.spi.AbstractStreamClient;
import com.distrimind.upnp_igd.transport.spi.InitializationException;
import com.distrimind.upnp_igd.transport.spi.StreamClient;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpContentResponse;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesRequestContent;
import org.eclipse.jetty.client.util.StringRequestContent;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation based on Jetty 8 client API.
 * <p>
 * This implementation works on Android, dependencies are the <code>jetty-client</code>
 * Maven module.
 * </p>
 *
 * @author Christian Bauer
 */
public class StreamClientImpl extends AbstractStreamClient<StreamClientConfigurationImpl, HttpRequest> {

    final private static Logger log = Logger.getLogger(StreamClient.class.getName());

    final protected StreamClientConfigurationImpl configuration;
    final protected HttpClient client;

    public StreamClientImpl(StreamClientConfigurationImpl configuration) throws InitializationException {
        this.configuration = configuration;

        log.info("Starting Jetty HttpClient...");
        client = new HttpClient();

        // Jetty client needs threads for its internal expiration routines, which we don't need but
        // can't disable, so let's abuse the request executor service for this
        /*client.setThreadPool(
            new ExecutorThreadPool(getConfiguration().getRequestExecutorService()) {
                @Override
                protected void doStop() throws Exception {
                    // Do nothing, don't shut down the Cling ExecutorService when Jetty stops!
                }
            }
        );*/

        // These are some safety settings, we should never run into these timeouts as we
        // do our own expiration checking
        //client.setIdleTimeout((configuration.getTimeoutSeconds()+5) * 1000L);
        client.setConnectTimeout((configuration.getTimeoutSeconds()+5) * 1000L);

        //client.setMaxRetries(configuration.getRequestRetryCount());

        try {
            client.start();
        } catch (Exception ex) {
            throw new InitializationException(
                "Could not start Jetty HTTP client: " + ex, ex
            );
        }
    }

    @Override
    public StreamClientConfigurationImpl getConfiguration() {
        return configuration;
    }

    @Override
    protected HttpRequest createRequest(StreamRequestMessage requestMessage) {
        UpnpRequest requestOperation=requestMessage.getOperation();

        if (log.isLoggable(Level.FINE))
            log.fine(
                    "Preparing HTTP request message with method '"
                            + requestOperation.getHttpMethodName()
                            + "': " + requestMessage
            );
        HttpRequest request=(HttpRequest)client.newRequest(requestOperation.getURI());
        request.method(requestOperation.getHttpMethodName());

        //set headers
        UpnpHeaders headers = requestMessage.getHeaders();
        if (log.isLoggable(Level.FINE))
            log.fine("Writing headers on HttpContentExchange: " + headers.size());
        // TODO Always add the Host header
        // TODO: ? setRequestHeader(UpnpHeader.Type.HOST.getHttpName(), );
        // Add the default user agent if not already set on the message
        if (!headers.containsKey(UpnpHeader.Type.USER_AGENT)) {
            request.addHeader(new HttpField(UpnpHeader.Type.USER_AGENT.getHttpName(), getConfiguration().getUserAgentValue(
                    requestMessage.getUdaMajorVersion(),
                    requestMessage.getUdaMinorVersion())));
        }
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            for (String v : entry.getValue()) {
                String headerName = entry.getKey();
                if (log.isLoggable(Level.FINE))
                    log.fine("Setting header '" + headerName + "': " + v);
                request.addHeader(new HttpField(headerName, v));
            }
        }

        //set body
        if (requestMessage.hasBody()) {
            if (requestMessage.getBodyType() == UpnpMessage.BodyType.STRING) {
                if (log.isLoggable(Level.FINE))
                    log.fine("Writing textual request body: " + requestMessage);


                String charset =
                        requestMessage.getContentTypeCharset() != null
                                ? requestMessage.getContentTypeCharset()
                                : "UTF-8";

                Request.Content body=new StringRequestContent(requestMessage.getBodyString(), charset);
                request.body(body);

            } else {
                if (log.isLoggable(Level.FINE))
                    log.fine("Writing binary request body: " + requestMessage);

                if (requestMessage.getContentTypeHeader() == null)
                    throw new RuntimeException(
                            "Missing content type header in request message: " + requestMessage
                    );
                Request.Content body=new BytesRequestContent(requestMessage.getBodyBytes());
                request.body(body);
            }
        }

        return request;
        //return new HttpContentExchange(getConfiguration(), client, requestMessage);
    }

    @Override
    protected Callable<StreamResponseMessage> createCallable(final StreamRequestMessage requestMessage,
                                                             final HttpRequest exchange) {
        return new Callable<>() {
			public StreamResponseMessage call() throws Exception {

				if (log.isLoggable(Level.FINE))
					log.fine("Sending HTTP request: " + requestMessage);
				final Callable<StreamResponseMessage> callable = this;
				final AtomicReference<StreamResponseMessage> result = new AtomicReference<>(null);
				final AtomicBoolean responseOK = new AtomicBoolean(false);
				exchange.onResponseSuccess((response) -> {
					// Status
					UpnpResponse.Status s = UpnpResponse.Status.getByStatusCode(response.getStatus());
					UpnpResponse responseOperation =
							new UpnpResponse(
									response.getStatus(),
									s == null ? null : s.getStatusMsg()
							);

					if (log.isLoggable(Level.FINE))
						log.fine("Received response: " + responseOperation);

					StreamResponseMessage responseMessage = new StreamResponseMessage(responseOperation);

					// Headers
					UpnpHeaders headers = new UpnpHeaders();
					HttpFields responseFields = response.getHeaders();
					for (String name : responseFields.getFieldNamesCollection()) {
						for (Enumeration<String> e = responseFields.getValues(name); e.hasMoreElements(); ) {
							headers.add(name, e.nextElement());
						}
					}
					responseMessage.setHeaders(headers);

					// Body
					byte[] bytes = ((HttpContentResponse) response).getContent();
					if (bytes != null && bytes.length > 0 && responseMessage.isContentTypeMissingOrText()) {

						if (log.isLoggable(Level.FINE))
							log.fine("Response contains textual entity body, converting then setting string on message");
						try {
							responseMessage.setBodyCharacters(bytes);
						} catch (UnsupportedEncodingException ex) {
							throw new RuntimeException("Unsupported character encoding: " + ex, ex);
						}

					} else if (bytes != null && bytes.length > 0) {

						if (log.isLoggable(Level.FINE))
							log.fine("Response contains binary entity body, setting bytes on message");
						responseMessage.setBody(UpnpMessage.BodyType.BYTES, bytes);

					} else {
						if (log.isLoggable(Level.FINE))
							log.fine("Response did not contain entity body");
					}

					if (log.isLoggable(Level.FINE))
						log.fine("Response message complete: " + responseMessage);
					result.set(responseMessage);
					synchronized (callable) {
						responseOK.set(true);
						callable.notifyAll();
					}
				});
				exchange.onResponseFailure(((response, failure) -> {
					synchronized (callable) {
						responseOK.set(true);
						callable.notifyAll();
					}
				}));
				synchronized (callable) {
					while (!responseOK.get()) {
						callable.wait();
					}
				}
				return result.get();

			}
		};
    }

    @Override
    protected void abort(HttpRequest exchange) {
        exchange.abort(new UnknownError());
    }

    @Override
    protected boolean logExecutionException(Throwable t) {
        return false;
    }

    @Override
    public void stop() {
        try {
            client.stop();
        } catch (Exception ex) {
            log.info("Error stopping HTTP client: " + ex);
        }
    }

    /*static public class HttpContentExchange extends ContentExchange {

        final protected StreamClientConfigurationImpl configuration;
        final protected HttpClient client;
        final protected StreamRequestMessage requestMessage;

        protected Throwable exception;

        public HttpContentExchange(StreamClientConfigurationImpl configuration,
                                   HttpClient client,
                                   StreamRequestMessage requestMessage) {
            super(true);
            this.configuration = configuration;
            this.client = client;
            this.requestMessage = requestMessage;
            applyRequestURLMethod();
            applyRequestHeaders();
            applyRequestBody();
        }

        @Override
        protected void onConnectionFailed(Throwable t) {
            log.log(Level.WARNING, "HTTP connection failed: " + requestMessage, Exceptions.unwrap(t));
        }

        @Override
        protected void onException(Throwable t) {
            log.log(Level.WARNING, "HTTP request failed: " + requestMessage, Exceptions.unwrap(t));
        }

        public StreamClientConfigurationImpl getConfiguration() {
            return configuration;
        }

        public StreamRequestMessage getRequestMessage() {
            return requestMessage;
        }

        protected void applyRequestURLMethod() {
            final UpnpRequest requestOperation = getRequestMessage().getOperation();
            if (log.isLoggable(Level.FINE))
                log.fine(
                    "Preparing HTTP request message with method '"
                        + requestOperation.getHttpMethodName()
                        + "': " + getRequestMessage()
                );

            setURL(requestOperation.getURI().toString());
            setMethod(requestOperation.getHttpMethodName());
        }

        protected void applyRequestHeaders() {
            // Headers
            UpnpHeaders headers = getRequestMessage().getHeaders();
            if (log.isLoggable(Level.FINE))
                log.fine("Writing headers on HttpContentExchange: " + headers.size());
            // TODO Always add the Host header
            // TODO: ? setRequestHeader(UpnpHeader.Type.HOST.getHttpName(), );
            // Add the default user agent if not already set on the message
            if (!headers.containsKey(UpnpHeader.Type.USER_AGENT)) {
                setRequestHeader(
                    UpnpHeader.Type.USER_AGENT.getHttpName(),
                    getConfiguration().getUserAgentValue(
                        getRequestMessage().getUdaMajorVersion(),
                        getRequestMessage().getUdaMinorVersion())
                );
            }
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                for (String v : entry.getValue()) {
                    String headerName = entry.getKey();
                    if (log.isLoggable(Level.FINE))
                        log.fine("Setting header '" + headerName + "': " + v);
                    addRequestHeader(headerName, v);
                }
            }
        }

        protected void applyRequestBody() {
            // Body
            if (getRequestMessage().hasBody()) {
                if (getRequestMessage().getBodyType() == UpnpMessage.BodyType.STRING) {
                    if (log.isLoggable(Level.FINE))
                        log.fine("Writing textual request body: " + getRequestMessage());

                    MimeType contentType =
                        getRequestMessage().getContentTypeHeader() != null
                            ? getRequestMessage().getContentTypeHeader().getValue()
                            : ContentTypeHeader.DEFAULT_CONTENT_TYPE_UTF8;

                    String charset =
                        getRequestMessage().getContentTypeCharset() != null
                            ? getRequestMessage().getContentTypeCharset()
                            : "UTF-8";

                    setRequestContentType(contentType.toString());
                    ByteArrayBuffer buffer;
                    try {
                        buffer = new ByteArrayBuffer(getRequestMessage().getBodyString(), charset);
                    } catch (UnsupportedEncodingException ex) {
                        throw new RuntimeException("Unsupported character encoding: " + charset, ex);
                    }
                    setRequestHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(buffer.length()));
                    setRequestContent(buffer);

                } else {
                    if (log.isLoggable(Level.FINE))
                        log.fine("Writing binary request body: " + getRequestMessage());

                    if (getRequestMessage().getContentTypeHeader() == null)
                        throw new RuntimeException(
                            "Missing content type header in request message: " + requestMessage
                        );
                    MimeType contentType = getRequestMessage().getContentTypeHeader().getValue();

                    setRequestContentType(contentType.toString());
                    ByteArrayBuffer buffer;
                    buffer = new ByteArrayBuffer(getRequestMessage().getBodyBytes());
                    setRequestHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(buffer.length()));
                    setRequestContent(buffer);
                }
            }
        }

        protected StreamResponseMessage createResponse() {
            // Status
            UpnpResponse responseOperation =
                new UpnpResponse(
                    getResponseStatus(),
                    UpnpResponse.Status.getByStatusCode(getResponseStatus()).getStatusMsg()
                );

            if (log.isLoggable(Level.FINE))
                log.fine("Received response: " + responseOperation);

            StreamResponseMessage responseMessage = new StreamResponseMessage(responseOperation);

            // Headers
            UpnpHeaders headers = new UpnpHeaders();
            HttpFields responseFields = getResponseFields();
            for (String name : responseFields.getFieldNamesCollection()) {
                for (String value : responseFields.getValuesCollection(name)) {
                    headers.add(name, value);
                }
            }
            responseMessage.setHeaders(headers);

            // Body
            byte[] bytes = getResponseContentBytes();
            if (bytes != null && bytes.length > 0 && responseMessage.isContentTypeMissingOrText()) {

                if (log.isLoggable(Level.FINE))
                    log.fine("Response contains textual entity body, converting then setting string on message");
                try {
                    responseMessage.setBodyCharacters(bytes);
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException("Unsupported character encoding: " + ex, ex);
                }

            } else if (bytes != null && bytes.length > 0) {

                if (log.isLoggable(Level.FINE))
                    log.fine("Response contains binary entity body, setting bytes on message");
                responseMessage.setBody(UpnpMessage.BodyType.BYTES, bytes);

            } else {
                if (log.isLoggable(Level.FINE))
                    log.fine("Response did not contain entity body");
            }

            if (log.isLoggable(Level.FINE))
                log.fine("Response message complete: " + responseMessage);
            return responseMessage;
        }
    }*/
}


