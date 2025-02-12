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
import com.distrimind.upnp.model.message.*;
import com.distrimind.upnp.protocol.ProtocolFactory;
import com.distrimind.upnp.transport.spi.UpnpStream;
import com.distrimind.upnp.util.Exceptions;
import com.distrimind.upnp.util.io.IO;
import io.undertow.server.BlockingHttpExchange;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * @author Jason Mahdjoub
 * @since 1.2.0
 */
public abstract class UndertowHttpExchangeUpnpStream extends UpnpStream {

	final private static DMLogger log = Log.getLogger(UndertowHttpExchangeUpnpStream.class);

	private final HttpServerExchange httpExchange;

	public UndertowHttpExchangeUpnpStream(ProtocolFactory protocolFactory, HttpServerExchange httpExchange) {
		super(protocolFactory);
		this.httpExchange = httpExchange;
	}

	public HttpServerExchange getHttpExchange() {
		return httpExchange;
	}

	private UpnpRequest.Method getRequestMethod()
	{
		return UpnpRequest.Method.getByHttpName(getHttpExchange().getRequestMethod().toString());
	}
	private URI getRequestURI() throws URISyntaxException {
		return new URI(getHttpExchange().getRequestURI());
	}
	private String getProtocol()
	{
		return getHttpExchange().getProtocol().toString();
	}
	private Map<String, List<String>> getRequestHeaders()
	{
		return getRequestHeaders(getHttpExchange().getRequestHeaders());
	}
	@SuppressWarnings("PMD.LooseCoupling")
	static Map<String, List<String>> getRequestHeaders(HeaderMap headerMap)
	{
		Map<String, List<String>> m=new HashMap<>();
		for (HeaderValues hv : headerMap)
		{
			String k=hv.getHeaderName().toString();
			List<String> list=new ArrayList<>(hv.size());
			list.addAll(hv);
			m.put(k, list);
		}
		return m;
	}
	@SuppressWarnings("PMD.LooseCoupling")
	static void putAll(HeaderMap hm, Map<String, List<String>> m, DMLogger log)
	{
		for (Map.Entry<String, List<String>> e : m.entrySet())
		{
			HeaderValues hv=hm.get(e.getKey());
			hv.addAll(e.getValue());
			if (log!=null)
				log.debug("Setting header '" + e.getKey()+ "': " + e.getValue());
		}
	}

	@Override
	public void run() {
		try {
			final HttpServerExchange httpExchange=getHttpExchange();

			if (log.isDebugEnabled()) {
				log.debug("Processing HTTP request: " + httpExchange.getRequestMethod() + " " + httpExchange.getRequestURI());
			}

			// Status
			StreamRequestMessage requestMessage =
					new StreamRequestMessage(
							getRequestMethod(),
							getRequestURI()
					);

			if (requestMessage.getOperation().getMethod().equals(UpnpRequest.Method.UNKNOWN)) {
				if (log.isDebugEnabled()) {
					log.debug("Method not supported by UPnP stack: " + httpExchange.getRequestMethod());
				}
				throw new RuntimeException("Method not supported: " + httpExchange.getRequestMethod());
			}

			// Protocol
			requestMessage.getOperation().setHttpMinorVersion(
					"HTTP/1.1".equals(getProtocol().toUpperCase(Locale.ROOT)) ? 1 : 0
			);

			if (log.isDebugEnabled()) {
				log.debug("Created new request message: " + requestMessage);
			}

			// Connection wrapper
			requestMessage.setConnection(createConnection());

			// Headers
			requestMessage.setHeaders(new UpnpHeaders(getRequestHeaders()));

			// Body
			httpExchange.dispatch(() -> {
				byte[] bodyBytes;
				try {
					try (BlockingHttpExchange bhe = httpExchange.startBlocking(); InputStream is = bhe == null ? httpExchange.getInputStream() : bhe.getInputStream()) {
						bodyBytes = IO.readBytes(is);

					}
					if (log.isDebugEnabled()) {
						log.debug("Reading request body bytes: " + bodyBytes.length);
					}

					if (bodyBytes.length > 0 && requestMessage.isContentTypeMissingOrText()) {

						log.debug("Request contains textual entity body, converting then setting string on message");
						requestMessage.setBodyCharacters(bodyBytes);

					} else if (bodyBytes.length > 0) {

						log.debug("Request contains binary entity body, setting bytes on message");
						requestMessage.setBody(UpnpMessage.BodyType.BYTES, bodyBytes);

					} else {
						log.debug("Request did not contain entity body");
					}

					// Process it
					StreamResponseMessage responseMessage = process(requestMessage);

					// Return the response
					if (responseMessage != null) {
						if (log.isDebugEnabled()) {
							log.debug("Preparing HTTP response message: " + responseMessage);
						}

						// Headers
						putAll(httpExchange.getResponseHeaders(), responseMessage.getHeaders(), log.isDebugEnabled() ? log : null);

						// Body
						byte[] responseBodyBytes = responseMessage.hasBody() ? responseMessage.getBodyBytes() : null;
						int contentLength = responseBodyBytes != null ? responseBodyBytes.length : -1;

						if (log.isDebugEnabled()) {
							log.debug("Sending HTTP response message: " + responseMessage + " with content length: " + contentLength);
						}
						httpExchange.setStatusCode(responseMessage.getOperation().getStatusCode())
								.setResponseContentLength(contentLength);

						if (contentLength > 0) {
							log.debug("Response message has body, writing bytes to stream...");
							try (OutputStream os = getHttpExchange().getOutputStream()) {
								IO.writeBytes(os, responseBodyBytes);
								os.flush();
							}
						}

					} else {
						// If it's null, it's 404, everything else needs a proper httpResponse
						if (log.isDebugEnabled())
							log.debug("Sending HTTP response status: " + HttpURLConnection.HTTP_NOT_FOUND);

						httpExchange.setStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
								.setResponseContentLength(-1);
					}
					httpExchange.endExchange();

					responseSent(responseMessage);
				}
				catch (Throwable t) {

					throwDuringRun(t);
				}
			});



		} catch (Throwable t) {

			throwDuringRun(t);
		}
	}

	private void throwDuringRun(Throwable t) {
		// You definitely want to catch all Exceptions here, otherwise the server will
		// simply close the socket, and you get an "unexpected end of file" on the client.
		// The same is true if you just rethrow an IOException - it is a mystery why it
		// is declared then on the HttpHandler interface if it isn't handled in any
		// way... so we always do error handling here.

		// TODO: We should only send an error if the problem was on our side
		// You don't have to catch Throwable unless, like we do here in unit tests,
		// you might run into Errors as well (assertions).
		if (log.isDebugEnabled()) {
			log.debug("Exception occured during UPnP stream processing: " + t);
		}
		if (log.isDebugEnabled()) {
			log.debug("Cause: ", Exceptions.unwrap(t));
		}
		getHttpExchange().setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);

		responseException(t);
	}

	abstract protected Connection createConnection();


}
