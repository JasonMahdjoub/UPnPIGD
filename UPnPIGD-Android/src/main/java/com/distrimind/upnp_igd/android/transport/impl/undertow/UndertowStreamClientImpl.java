/*
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.distrimind.upnp.android.transport.impl.undertow;

import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.upnp.Log;
import com.distrimind.upnp.http.IHeaders;
import com.distrimind.upnp.model.message.*;
import com.distrimind.upnp.model.message.header.UpnpHeader;
import com.distrimind.upnp.transport.spi.InitializationException;
import com.distrimind.upnp.transport.spi.StreamClient;
import com.distrimind.upnp.util.Exceptions;
import com.distrimind.upnp.util.io.IO;
import io.undertow.client.*;
import io.undertow.connector.ByteBufferPool;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import org.xnio.*;
import org.xnio.channels.StreamSinkChannel;
import org.xnio.channels.StreamSourceChannel;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


/**
 * @author Jason Mahdjoub
 * @since 1.2.0
 */
public class UndertowStreamClientImpl implements StreamClient<UndertowStreamClientConfigurationImpl> {

	final private static DMLogger log = Log.getLogger(UndertowStreamClientImpl.class);
	public static final String COULD_NOT_CREATE_REQUEST = "Could not create request: ";

	final protected UndertowStreamClientConfigurationImpl configuration;
	private final UndertowClient client;
	private final OptionMap options;
	public UndertowStreamClientImpl(UndertowStreamClientConfigurationImpl configuration) throws InitializationException {
		this.configuration = configuration;

		if (log.isDebugEnabled()) {
			log.debug("Using persistent HTTP stream client connections: " + configuration.isUsePersistentConnections());
		}

		log.info("Starting Undertow HttpClient...");
		this.client = UndertowClient.getInstance();
		int timeout=configuration.getTimeoutSeconds();

		options = OptionMap.builder()
				.set(Options.READ_TIMEOUT, timeout*1000)
				.set(Options.WRITE_TIMEOUT, timeout*1000)
				.set(Options.SSL_CLIENT_SESSION_TIMEOUT, timeout)
				.set(Options.SSL_SERVER_SESSION_TIMEOUT, timeout)
				.getMap();
	}

	@Override
	public UndertowStreamClientConfigurationImpl getConfiguration() {
		return configuration;
	}

	@Override
	public StreamResponseMessage sendRequest(StreamRequestMessage requestMessage) throws InterruptedException {

		final UpnpRequest requestOperation = requestMessage.getOperation();
		if (log.isDebugEnabled()) {
			log.debug("Preparing HTTP request message with method '" + requestOperation.getHttpMethodName() + "': " + requestMessage);
		}


		IoFuture<ClientConnection> connectionFuture=null;
		CompletableFuture<StreamResponseMessage> responseFuture=null;
		try (ByteBufferPool bufferPool = new DefaultByteBufferPool(true, 4096)){

			int timeout=configuration.getTimeoutSeconds();

			connectionFuture= client.connect(requestOperation.getURI(), configuration.getRequestExecutorService(), bufferPool, options);

			if (connectionFuture.await(timeout, TimeUnit.SECONDS)==IoFuture.Status.DONE) {
				try(ClientConnection connection=connectionFuture.get()) {
					connectionFuture = null;

					ClientRequest request = new ClientRequest()
							.setPath(requestOperation.getURI().getPath())
							.setMethod(Objects.requireNonNull(Methods.fromString(requestOperation.getHttpMethodName())));


					applyRequestProperties(request, requestMessage, requestOperation);
					responseFuture = new CompletableFuture<>();
					applyRequestBodyAndGetResponse(connection, request, requestMessage, responseFuture);
					StreamResponseMessage r = responseFuture.get();
					responseFuture = null;
					return r;
				}
			}
			else {
				if (log.isWarnEnabled())
					log.warn("HTTP request failed: " + requestMessage);
				return null;
			}

		}
		catch (InterruptedException ex) {
			if (log.isDebugEnabled())
				log.debug("Interruption, aborting request: " + requestMessage);

			throw new InterruptedException("HTTP request interrupted and aborted");

		}
		catch (ProtocolException ex) {
			if (log.isWarnEnabled()) log.warn("HTTP request failed: " + requestMessage, Exceptions.unwrap(ex));
			return null;
		}
		catch (IOException ex) {

			if (log.isDebugEnabled())
				log.debug("Exception occurred, trying to read the error stream: ", Exceptions.unwrap(ex));
			return null;
		}
		catch (Exception ex) {
			if (log.isWarnEnabled()) log.warn("HTTP request failed: " + requestMessage, Exceptions.unwrap(ex));
			return null;

		} finally {

			if (connectionFuture != null) {
				connectionFuture.cancel();
			}
			if (responseFuture!=null) {

				responseFuture.cancel(true);

			}
		}
	}

	@Override
	public void stop() {
		// NOOP
	}

	protected void applyRequestProperties(ClientRequest request, StreamRequestMessage requestMessage, UpnpRequest requestOperation) {

		// HttpURLConnection always adds a "Host" header

		// HttpURLConnection always adds an "Accept" header (not needed but shouldn't hurt)

		// Add the default user agent if not already set on the message
		IUpnpHeaders headers = requestMessage.getHeaders();
		if (!headers.containsKey(UpnpHeader.Type.USER_AGENT)) {
			request.getRequestHeaders().put(Headers.USER_AGENT, getConfiguration().getUserAgentValue(
					requestMessage.getUdaMajorVersion(),
					requestMessage.getUdaMinorVersion()));
		}




		// Other headers
		applyHeaders(request, requestMessage.getHeaders(), requestOperation);
	}

	protected void applyHeaders(ClientRequest request, IHeaders headers, UpnpRequest requestOperation) {
		if (log.isDebugEnabled()) {
			log.debug("Writing headers on HttpURLConnection: " + headers.size());
		}
		UndertowHttpExchangeUpnpStream.putAll(request.getRequestHeaders(), headers, log.isDebugEnabled()?log:null);
		request.getRequestHeaders().put(Headers.HOST, requestOperation.getURI().getHost());
	}

	protected void applyRequestBodyAndGetResponse(ClientConnection connection, ClientRequest request, StreamRequestMessage requestMessage, CompletableFuture<StreamResponseMessage> responseFuture) throws IOException {
		if (log.isDebugEnabled()) {
			log.debug("Sending HTTP request: " + requestMessage);
		}
		byte[] bytes;
		String contentLength;
		if (requestMessage.hasBody()) {
			if (requestMessage.getBodyType() == UpnpMessage.BodyType.STRING) {
				String charset = requestMessage.getContentTypeCharset() != null
						? requestMessage.getContentTypeCharset()
						: StandardCharsets.UTF_8.toString();
				bytes = requestMessage.getBodyString().getBytes(charset);

			} else {
				bytes = requestMessage.getBodyBytes();
			}
			contentLength=String.valueOf(bytes.length);

		}
		else {
			bytes=null;
			contentLength="0";
		}

		request.getRequestHeaders().put(Headers.CONTENT_LENGTH, contentLength);

		connection.sendRequest(request, new ClientCallback<>() {
			@Override
			public void completed(ClientExchange exchange) {
				try (StreamSinkChannel requestChannel=exchange.getRequestChannel()){

					if (bytes!=null)
						requestChannel.write(ByteBuffer.wrap(bytes));
					requestChannel.shutdownWrites();
					if (!requestChannel.flush()) {
						requestChannel.getWriteSetter().set(ChannelListeners.flushingChannelListener(null, null));
						requestChannel.resumeWrites();
					}

					exchange.setResponseListener(new ClientCallback<>() {
						@Override
						public void completed(ClientExchange result) {
							try {

								responseFuture.complete(createResponse(result, request));
							} catch (Exception e) {
								log.error("Failed to read response", e);
								responseFuture.complete(null);
							}
						}

						@Override
						public void failed(IOException e) {
							log.error("Failed to get response", e);
							responseFuture.complete(null);
						}
					});
				} catch (IOException e) {
					log.error("Failed to write body", e);
					responseFuture.complete(null);
				}
			}

			@Override
			public void failed(IOException e) {
				log.error("Failed to send request", e);
				responseFuture.complete(null);
			}
		});


	}

	protected StreamResponseMessage createResponse(ClientExchange result, ClientRequest request) throws Exception {

		final ClientResponse response=result.getResponse();
		final int responseCode=response.getResponseCode();

		if (responseCode == -1) {
			if (log.isWarnEnabled()) {
				log.warn("Received an invalid HTTP response: " + request.getPath());
				log.warn("Is your UPnPIGD-based server sending connection heartbeats with " +
						"RemoteClientInfo#isRequestCancelled? This client can't handle " +
						"heartbeats, read the manual.");
			}
			return null;
		}

		// Status
		UpnpResponse responseOperation = new UpnpResponse(responseCode, response.getStatus());

		if (log.isDebugEnabled()) {
			log.debug("Received response: " + responseOperation);
		}

		// Message
		StreamResponseMessage responseMessage = new StreamResponseMessage(responseOperation);

		// Headers
		responseMessage.setHeaders(new UpnpHeaders(UndertowHttpExchangeUpnpStream.getRequestHeaders(response.getResponseHeaders())));

		// Body
		byte[] bodyBytes = null;
		StreamSourceChannel responseChannel = result.getResponseChannel();
		if (responseChannel!=null) {
			try (InputStream is = Channels.newInputStream(responseChannel)) {
				bodyBytes = IO.readBytes(is);
			}
		}

		if (bodyBytes != null && bodyBytes.length > 0 && responseMessage.isContentTypeMissingOrText()) {

			log.debug("Response contains textual entity body, converting then setting string on message");
			responseMessage.setBodyCharacters(bodyBytes);

		} else if (bodyBytes != null && bodyBytes.length > 0) {

			log.debug("Response contains binary entity body, setting bytes on message");
			responseMessage.setBody(UpnpMessage.BodyType.BYTES, bodyBytes);

		} else {
			log.debug("Response did not contain entity body");
		}

		if (log.isDebugEnabled()) {
			log.debug("Response message complete: " + responseMessage);
		}
		return responseMessage;
	}

}