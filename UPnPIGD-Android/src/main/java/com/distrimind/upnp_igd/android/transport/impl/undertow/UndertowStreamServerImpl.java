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
import com.distrimind.upnp.model.message.Connection;
import com.distrimind.upnp.model.types.HostPort;
import com.distrimind.upnp.transport.Router;
import com.distrimind.upnp.transport.impl.StreamServerConfigurationImpl;
import com.distrimind.upnp.transport.spi.InitializationException;
import com.distrimind.upnp.transport.spi.NetworkAddressFactory;
import com.distrimind.upnp.transport.spi.StreamServer;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jason Mahdjoub
 * @since 1.2.0
 */
public class UndertowStreamServerImpl implements StreamServer<StreamServerConfigurationImpl> {
	final private static DMLogger log = Log.getLogger(UndertowStreamServerImpl.class);

	final protected StreamServerConfigurationImpl configuration;
	protected Undertow server=null;
	private boolean started=false;
	private final List<HostPort> hostPorts=new ArrayList<>();

	public UndertowStreamServerImpl(StreamServerConfigurationImpl configuration) {
		this.configuration = configuration;
	}

	@Override
	synchronized public void init(InetAddress bindAddress, Router router, NetworkAddressFactory networkAddressFactory) throws InitializationException {
		try {
			hostPorts.add(new HostPort(bindAddress.getHostAddress(), configuration.getListenPort()));
			if (started) {
				stop();
			}
			Undertow.Builder b=Undertow.builder();
			for(HostPort hp : hostPorts)
				b.addHttpListener(hp.getPort(), hp.getHost());
			server = b.setHandler(new RequestHttpHandler(router, networkAddressFactory))
					.build();

		} catch (Exception ex) {
			throw new InitializationException("Could not initialize " + getClass().getSimpleName() + ": " + ex, ex);
		}
	}

	@Override
	synchronized public int getPort() {
		if (configuration.getListenPort()==0)
			return ((InetSocketAddress)server.getListenerInfo().get(0).getAddress()).getPort();
		return configuration.getListenPort();
	}

	@Override
	public StreamServerConfigurationImpl getConfiguration() {
		return configuration;
	}

	@Override
	synchronized public void run() {
		log.debug("Starting StreamServer...");
		// Starts a new thread but inherits the properties of the calling thread
		server.start();
		started=true;
		if (log.isInfoEnabled()) log.info("Created server (for receiving TCP streams) on: " + server.getListenerInfo());
	}

	@Override
	synchronized public void stop() {
		if (server != null)
		{
			try {
				log.debug("Stopping StreamServer...");
				server.stop();
			}
			finally {
				server=null;
				started=false;
			}


		}
	}

	protected class RequestHttpHandler implements HttpHandler {

		private final Router router;
		private final NetworkAddressFactory networkAddressFactory;

		public RequestHttpHandler(Router router, NetworkAddressFactory networkAddressFactory) {
			this.router = router;
			this.networkAddressFactory=networkAddressFactory;
		}



		// This is executed in the request receiving thread!
		@Override
		public void handleRequest(HttpServerExchange exchange) throws Exception {
			InetSocketAddress isa=exchange.getSourceAddress();
			if (isa==null)
				return;
			InetAddress receivedOnLocalAddress =
					networkAddressFactory.getLocalAddress(
							null,
							isa.getAddress() instanceof Inet6Address,
							isa.getAddress()
					);
			if (receivedOnLocalAddress==null)
				return;
			// And we pass control to the service, which will (hopefully) start a new thread immediately, so we can
			// continue the receiving thread ASAP
			if (log.isDebugEnabled()) {
				log.debug("Received HTTP exchange: " + exchange.getRequestMethod() + " " + exchange.getRequestURI());
			}
			router.received(
					new UndertowHttpExchangeUpnpStream(router.getProtocolFactory(), exchange) {
						@Override
						protected Connection createConnection() {
							return new HttpServerConnection(exchange);
						}
					}
			);
		}
	}

	/**
	 * Logs a warning and returns <code>true</code>, we can't access the socket using the awful JDK webserver API.
	 * <p>
	 * Override this method if you know how to do it.

	 */
	protected boolean isConnectionOpen(HttpServerExchange exchange) {
		return exchange.getConnection().isOpen();
	}

	protected class HttpServerConnection implements Connection {

		protected HttpServerExchange exchange;

		public HttpServerConnection(HttpServerExchange exchange) {
			this.exchange = exchange;
		}

		@Override
		public boolean isOpen() {
			return isConnectionOpen(exchange);
		}

		@Override
		public InetAddress getRemoteAddress() {
			return exchange.getSourceAddress() != null
					? exchange.getSourceAddress().getAddress()
					: null;
		}

		@Override
		public InetAddress getLocalAddress() {
			return exchange.getDestinationAddress() != null
					? exchange.getDestinationAddress().getAddress()
					: null;
		}
	}

}
