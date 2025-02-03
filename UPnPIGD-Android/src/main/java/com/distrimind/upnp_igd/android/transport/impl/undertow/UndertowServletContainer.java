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

import com.distrimind.flexilogxml.FlexiLogXML;
import com.distrimind.flexilogxml.UtilClassLoader;
import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.upnp_igd.Log;
import com.distrimind.upnp_igd.android.transport.spi.ServletContainerAdapter;
import io.undertow.Undertow;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.InstanceHandle;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.spec.HttpServletResponseImpl;
import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServletRequest;
import org.xnio.XnioWorker;

public class UndertowServletContainer implements ServletContainerAdapter {

	final private static DMLogger log = Log.getLogger(UndertowServletContainer.class);

	// Singleton
	public static final UndertowServletContainer INSTANCE = new UndertowServletContainer();
	public static final String WITH_PORT = " with port ";

	private Undertow server;
	private ServletContainer container;
	private DeploymentManager manager;
	private final List<ConnectorInfo> listeners;
	private XnioWorker worker;
	boolean indent=false;

	private UndertowServletContainer() {
		server=null;
		container=null;
		manager=null;
		listeners=new ArrayList<>();
		worker=null;
	}

	@Override
	public void setExecutorService(ExecutorService executorService) {
		try {
			stopIfRunning();
			if (executorService instanceof XnioWorker) {
				this.worker = (XnioWorker) executorService;
			}
			else {
				throw new IllegalArgumentException();
			}
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("Failed to initialize XNIO worker: ", e);
			}
			throw new RuntimeException("Failed to initialize XNIO worker", e);
		}
	}

	@Override
	public int addConnector(String host, int port) {
		log.debug(() -> "Add connector at "+host+ WITH_PORT +port+"...");
		indent=true;
		ConnectorInfo ci=new ConnectorInfo(host, port);
		try
		{
			listeners.add(ci);
			startOrRestartServer();

		} catch (Exception e) {
			listeners.remove(ci);
			throw e;
		}
		finally {
			indent=false;
		}
		log.debug(() -> "Connector at "+host+ WITH_PORT +port+" created");

		return port;
	}


	@Override
	public void removeConnector(String host, int port) {
		log.debug("Remove connector "+host+ WITH_PORT +port+"...");
		indent=true;
		try {
			if (listeners.removeIf(listener -> listener.isConcernedBy(host, port))) {
				if (server != null)
					startOrRestartServer();
			}
		}
		finally {
			indent=false;
		}
		log.debug("Connector " + host + WITH_PORT + port + " removed");

	}

	@Override
	public void registerServlet(String contextPath, Servlet servlet) {
		log.debug("Register servlet "+contextPath+"...");
		indent=true;
		if (container == null) {
			container = ServletContainer.Factory.newInstance();
		}

		try {
			DeploymentInfo servletBuilder = Servlets.deployment()
					.setClassLoader(UtilClassLoader.getLoader())
					.setContextPath(contextPath)
					.setDeploymentName("upnp-deployment")
					.addServlet(Servlets.servlet("UPnPServlet", servlet.getClass(), () -> new InstanceHandle<>() {
                        @Override
                        public Servlet getInstance() {
                            return servlet;
                        }

                        @Override
                        public void release() {
                            servlet.destroy();
                        }
                    })
							.addMapping("/*"));

			manager = container.addDeployment(servletBuilder);
			startOrRestartServer();


		} catch (Exception e) {
			log.error("Failed to initialize servlet", e);
			throw new RuntimeException("Failed to initialize servlet", e);
		}
		finally {
			indent=false;
		}
		log.debug("Servlet "+contextPath+" registered");
	}

	@Override
	public void startIfNotRunning() {
		if (server == null) {
			startOrRestartServer();
		}
	}

	@Override
	public void stopIfRunning() {
		if (server != null) {
			log.debug("Stopping Undertow server and worker...");
			try {
				if (manager!=null && manager.getDeployment()!=null) {
					manager.stop();
					manager.undeploy();
				}
			}
			catch (Exception e) {
				log.error("Failed to stop servlet", e);
				throw new RuntimeException("Failed to stop servlet", e);
			}
			if (server!=null)
				server.stop();
			/*Undertow.Builder builder = Undertow.builder()
					.setWorker(worker)
					.setIoThreads(worker.getIoThreadCount());
			server = builder.build();*/
			server=null;
			container = null;
			manager = null;
			listeners.clear();
			log.debug("Undertow server and worker stopped");

		}
	}

	protected void startOrRestartServer()
	{
		boolean initiallyStarted;
		if (server==null) {
			initiallyStarted=false;
			log.info((indent?"\t":"")+"Starting undertow server");
		}
		else {
			initiallyStarted=true;
			log.info((indent?"\t":"")+"Restarting undertow server");
		}
		Undertow.Builder builder = Undertow.builder()
				.setWorker(worker)
				.setIoThreads(worker.getIoThreadCount());
		for (ConnectorInfo ci : listeners)
			builder.addHttpListener(ci.getPort(), ci.getHost());
		if (manager!=null) {
			try {
				if (manager.getDeployment()!=null) {
					manager.stop();
					manager.undeploy();
				}
				manager.deploy();
				builder.setHandler(manager.start());
			}
			catch (Exception e) {
				log.error((indent?"\t":"")+"Failed to reinitialize servlet", e);
				throw new RuntimeException("Failed to reinitialize servlet", e);
			}
		}
		if (server!=null)
			server.stop();
		server = builder.build();
		if (initiallyStarted)
			log.info((indent?"\t":"")+"Undertow server restarted");
		else
			log.info((indent?"\t":"")+"Undertow server started");
	}


	public static boolean isConnectionOpen(HttpServletRequest request) {
		return isConnectionOpen(request, " ".getBytes());
	}

	public static boolean isConnectionOpen(HttpServletRequest request, byte[] heartbeat) {
		boolean res;
		final String remoteAddress = request.getRemoteAddr();
		try {
			if (log.isDebugEnabled()) {
				log.debug("Checking if client connection is still open: " + remoteAddress);
			}
			if (!request.isAsyncSupported() || !request.isAsyncStarted()) {
				res = false;
			} else {
				jakarta.servlet.AsyncContext asyncContext = request.getAsyncContext();
				if (asyncContext == null) {
					res = false;
				} else {
					HttpServletResponseImpl response = (HttpServletResponseImpl) asyncContext.getResponse();
					response.getOutputStream().write(heartbeat);
					response.getOutputStream().flush();
					res = true;
				}
			}
		} catch (Exception ignored) {
			res = false;
		}

		if (!res && log.isDebugEnabled()) {
			log.debug("Client connection has been closed: " + remoteAddress);
		}
		return res;
	}
	private static class ConnectorInfo
	{
		private final String host;
		private final int port;

		public ConnectorInfo(String host, int port) {
			this.host = host;
			this.port = port;
		}

		public String getHost() {
			return host;
		}

		public int getPort() {
			return port;
		}

		boolean isConcernedBy(String host, int port)
		{
			return this.host.contains(host) && port==this.port;
		}
	}
}