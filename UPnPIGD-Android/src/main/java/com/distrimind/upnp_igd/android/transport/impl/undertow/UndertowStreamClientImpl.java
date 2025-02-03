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

package com.distrimind.upnp_igd.android.transport.impl.undertow;

import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.upnp_igd.Log;
import com.distrimind.upnp_igd.model.Constants;
import com.distrimind.upnp_igd.model.message.*;
import com.distrimind.upnp_igd.model.message.header.UpnpHeader;
import com.distrimind.upnp_igd.transport.spi.AbstractStreamClient;
import com.distrimind.upnp_igd.transport.spi.InitializationException;
import io.undertow.client.*;
import io.undertow.util.*;
import org.xnio.*;
import org.xnio.channels.StreamSinkChannel;
import org.xnio.channels.StreamSourceChannel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

public class UndertowStreamClientImpl extends AbstractStreamClient<StreamClientConfigurationImpl, ClientExchange> {

	final private static DMLogger log = Log.getLogger(UndertowStreamClientImpl.class);
	public static final String COULD_NOT_CREATE_REQUEST = "Could not create request: ";

	final protected StreamClientConfigurationImpl configuration;
	final protected UndertowClient client;
	private final XnioWorker worker;

	public UndertowStreamClientImpl(StreamClientConfigurationImpl configuration) throws InitializationException {
		this.configuration = configuration;
		log.info("Starting Undertow HttpClient...");
		this.client = UndertowClient.getInstance();

		try {
			Xnio xnio = Xnio.getInstance("nio");
			worker = xnio.createWorker(OptionMap.builder()
					.set(Options.WORKER_IO_THREADS, 1)
					.set(Options.CONNECTION_HIGH_WATER, 1000000)
					.set(Options.CONNECTION_LOW_WATER, 1000000)
					.set(Options.WORKER_TASK_CORE_THREADS, 0)
					.set(Options.WORKER_TASK_MAX_THREADS, Runtime.getRuntime().availableProcessors())
					.set(Options.TCP_NODELAY, true)
					.set(Options.KEEP_ALIVE, true)
					.getMap());
		} catch (Exception ex) {
			log.error(ex);
			throw new InitializationException("Could not initialize Undertow client: " + ex, ex);
		}
	}

	@Override
	public StreamClientConfigurationImpl getConfiguration() {
		return configuration;
	}

	@Override
	protected ClientExchange createRequest(StreamRequestMessage requestMessage) {
		try {
			URI uri = requestMessage.getOperation().getURI();

			final ClientConnection connection = client.connect(uri, worker, null, OptionMap.EMPTY).get();

			ClientRequest request = new ClientRequest()
					.setPath(uri.getPath())
					.setMethod(Objects.requireNonNull(Methods.fromString(requestMessage.getOperation().getHttpMethodName())));

			// Set headers
			IUpnpHeaders headers = requestMessage.getHeaders();
			if (!headers.containsKey(UpnpHeader.Type.USER_AGENT)) {
				request.getRequestHeaders().put(Headers.USER_AGENT, getConfiguration().getUserAgentValue(
						requestMessage.getUdaMajorVersion(),
						requestMessage.getUdaMinorVersion()));
			}

			for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
				for (String v : entry.getValue()) {
					request.getRequestHeaders().add(new HttpString(entry.getKey()), v);
				}
			}

			// Set body
			if (requestMessage.hasBody()) {
				if (requestMessage.getBodyType() == UpnpMessage.BodyType.STRING) {
					String charset = requestMessage.getContentTypeCharset() != null
							? requestMessage.getContentTypeCharset()
							: "UTF-8";
					byte[] bytes = requestMessage.getBodyString().getBytes(charset);
					request.getRequestHeaders().put(Headers.CONTENT_LENGTH, String.valueOf(bytes.length));
					connection.sendRequest(request, new ClientCallback<>() {
						@Override
						public void completed(ClientExchange result) {
							//result.setRequestContentLength(bytes.length);
							try {
								result.getRequestChannel().write(ByteBuffer.wrap(bytes));
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}

						@Override
						public void failed(IOException e) {
							log.error("Failed to send request", e);
						}
					});
				} else {
					byte[] bytes = requestMessage.getBodyBytes();
					request.getRequestHeaders().put(Headers.CONTENT_LENGTH, String.valueOf(bytes.length));
					connection.sendRequest(request, new ClientCallback<ClientExchange>() {
						@Override
						public void completed(ClientExchange result) {
							//result.setRequestContentLength(bytes.length);
							try {
								result.getRequestChannel().write(ByteBuffer.wrap(bytes));
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}

						@Override
						public void failed(IOException e) {
							log.error("Failed to send request", e);
						}
					});
				}
			}

			final ClientExchangeWaiter res=new ClientExchangeWaiter();
			connection.sendRequest(request, new ClientCallback<>() {
				@Override
				public void completed(ClientExchange clientExchange) {
					synchronized (res) {
						res.clientExchange = clientExchange;
						res.notify();
					}
				}

				@Override
				public void failed(IOException e) {
					log.error(() -> COULD_NOT_CREATE_REQUEST + e.getMessage(), e);
					throw new RuntimeException(COULD_NOT_CREATE_REQUEST + e.getMessage(), e);
				}
			});
			return res;

		} catch (Exception ex) {
			log.error(() -> COULD_NOT_CREATE_REQUEST + ex.getMessage(), ex);
			throw new RuntimeException(COULD_NOT_CREATE_REQUEST + ex.getMessage(), ex);
		}
	}

	static class ClientExchangeWaiter implements ClientExchange
	{
		private ClientExchange clientExchange=null;
		void waitForResponse() throws InterruptedException {
			synchronized (this)
			{
				while (clientExchange==null)
				{
					wait();
				}
			}
		}

		@Override
		public void setResponseListener(ClientCallback<ClientExchange> clientCallback) {
			clientExchange.setResponseListener(clientCallback);
		}

		@Override
		public void setContinueHandler(ContinueNotification continueNotification) {
			clientExchange.setContinueHandler(continueNotification);
		}

		@Override
		public void setPushHandler(PushCallback pushCallback) {
			clientExchange.setPushHandler(pushCallback);
		}

		@Override
		public StreamSinkChannel getRequestChannel() {
			return clientExchange.getRequestChannel();
		}

		@Override
		public StreamSourceChannel getResponseChannel() {
			return clientExchange.getResponseChannel();
		}

		@Override
		public ClientRequest getRequest() {
			return clientExchange.getRequest();
		}

		@Override
		public ClientResponse getResponse() {
			return clientExchange.getResponse();
		}

		@Override
		public ClientResponse getContinueResponse() {
			return clientExchange.getContinueResponse();
		}

		@Override
		public ClientConnection getConnection() {
			return clientExchange.getConnection();
		}

		@Override
		public <T> T getAttachment(AttachmentKey<T> attachmentKey) {
			return clientExchange.getAttachment(attachmentKey);
		}

		@Override
		public <T> List<T> getAttachmentList(AttachmentKey<? extends List<T>> attachmentKey) {
			return clientExchange.getAttachmentList(attachmentKey);
		}

		@Override
		public <T> T putAttachment(AttachmentKey<T> attachmentKey, T t) {
			return clientExchange.putAttachment(attachmentKey, t);
		}

		@Override
		public <T> T removeAttachment(AttachmentKey<T> attachmentKey) {
			return clientExchange.removeAttachment(attachmentKey);
		}

		@Override
		public <T> void addToAttachmentList(AttachmentKey<AttachmentList<T>> attachmentKey, T t) {
			clientExchange.addToAttachmentList(attachmentKey, t);
		}
	}
	private static byte[] channelToBytes(StreamSourceChannel channel) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ByteBuffer buffer = ByteBuffer.allocate(8192); // 8KB buffer

		try (channel) {
			while (true) {
				buffer.clear();
				int read = channel.read(buffer);

				if (read == -1) { // End of stream
					break;
				}

				if (read > 0) {
					buffer.flip();
					output.write(buffer.array(), 0, buffer.remaining());
					if (output.size() > Constants.MAX_INPUT_STREAM_SIZE_IN_BYTES)
						throw new IOException("The stream send more than " + Constants.MAX_INPUT_STREAM_SIZE_IN_BYTES + " bytes");
				}
			}

			return output.toByteArray();

		}
	}

	@Override
	protected Callable<StreamResponseMessage> createCallable(
			final StreamRequestMessage requestMessage,
			final ClientExchange exchange) {

		return () -> {
			try {
				((ClientExchangeWaiter) exchange).waitForResponse();

				ClientResponse response = exchange.getResponse();
				HeaderMap responseHeaders = response.getResponseHeaders();

				// Create response message
				UpnpResponse responseOperation = new UpnpResponse(
						response.getResponseCode(),
						response.getStatus()
				);

				StreamResponseMessage responseMessage = new StreamResponseMessage(responseOperation);

				// Set headers
				IUpnpHeaders headers = new UpnpHeaders();
				for (HeaderValues header : responseHeaders) {
					headers.add(header.getHeaderName().toString(), header.getFirst());
				}
				responseMessage.setHeaders(headers);

				// Set body
				if (responseMessage.isContentTypeMissingOrText()) {
					responseMessage.setBodyCharacters(channelToBytes(exchange.getResponseChannel()));
				} else {
					responseMessage.setBody(UpnpMessage.BodyType.BYTES, channelToBytes(exchange.getResponseChannel()));
				}

				return responseMessage;

			} catch (Exception ex) {
				throw new RuntimeException("Error processing response: " + ex.getMessage(), ex);
			}
		};
	}

	@Override
	protected void abort(ClientExchange exchange) {
		try {
			exchange.getConnection().close();
		} catch (IOException ignored) {
		}
	}

	@Override
	protected boolean logExecutionException(Throwable t) {
		return false;
	}

	@Override
	public void stop() {

		if (worker != null) {
			worker.shutdown();
		}
	}
}