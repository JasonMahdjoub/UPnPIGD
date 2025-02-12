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

package com.distrimind.upnp;

import com.distrimind.upnp.controlpoint.ControlPoint;
import com.distrimind.upnp.controlpoint.ControlPointImpl;
import com.distrimind.upnp.protocol.ProtocolFactory;
import com.distrimind.upnp.protocol.ProtocolFactoryImpl;
import com.distrimind.upnp.registry.Registry;
import com.distrimind.upnp.registry.RegistryImpl;
import com.distrimind.upnp.registry.RegistryListener;
import com.distrimind.upnp.transport.Router;
import com.distrimind.upnp.transport.RouterException;
import com.distrimind.upnp.transport.RouterImpl;
import com.distrimind.upnp.util.Exceptions;

import jakarta.enterprise.inject.Alternative;
import com.distrimind.flexilogxml.log.DMLogger;

import java.io.IOException;

/**
 * Default implementation of {@link UpnpService}, starts immediately on construction.
 * <p>
 * If no {@link UpnpServiceConfiguration} is provided it will automatically
 * instantiate {@link DefaultUpnpServiceConfiguration}. This configuration <strong>does not
 * work</strong> on Android! Use the com.distrimind.upnp.androidAndroidUpnpService interface
 * application component instead.
 * </p>
 * <p>
 * Override the various <code>create...()</code> methods to customize instantiation of protocol factory,
 * router, etc.
 * </p>
 *
 * @author Christian Bauer
 */
@Alternative
public class UpnpServiceImpl implements UpnpService {

    final private static DMLogger log = Log.getLogger(UpnpServiceImpl.class);

    protected final UpnpServiceConfiguration configuration;
    protected final ControlPoint controlPoint;
    protected final ProtocolFactory protocolFactory;
    protected final Registry registry;
    protected final Router router;

    public UpnpServiceImpl() throws IOException {
        this(new DefaultUpnpServiceConfiguration());
    }

    public UpnpServiceImpl(RegistryListener... registryListeners) throws IOException {
        this(new DefaultUpnpServiceConfiguration(), registryListeners);
    }

    public UpnpServiceImpl(UpnpServiceConfiguration configuration, RegistryListener... registryListeners) {
        this.configuration = configuration;
        if (log.isInfoEnabled()) {
            log.info(">>> Starting UPnP service...");

            log.info("Using configuration: " + getConfiguration().getClass().getName());
        }

        // Instantiation order is important: Router needs to start its network services after registry is ready

        this.protocolFactory = createProtocolFactory();

        this.registry = createRegistry(protocolFactory);
        for (RegistryListener registryListener : registryListeners) {
            this.registry.addListener(registryListener);
        }

        this.router = createRouter(protocolFactory, registry);

        try {
            this.router.enable();
        } catch (RouterException ex) {
            throw new RuntimeException("Enabling network router failed: " + ex, ex);
        }

        this.controlPoint = createControlPoint(protocolFactory, registry);

        log.info("<<< UPnP service started successfully");
    }

    protected ProtocolFactory createProtocolFactory() {
        return new ProtocolFactoryImpl(this);
    }

    protected Registry createRegistry(ProtocolFactory protocolFactory) {
        return new RegistryImpl(this);
    }

    protected Router createRouter(ProtocolFactory protocolFactory, Registry registry) {
        return new RouterImpl(getConfiguration(), protocolFactory);
    }

    protected ControlPoint createControlPoint(ProtocolFactory protocolFactory, Registry registry) {
        return new ControlPointImpl(getConfiguration(), protocolFactory, registry);
    }

    @Override
    public UpnpServiceConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public ControlPoint getControlPoint() {
        return controlPoint;
    }

    @Override
    public ProtocolFactory getProtocolFactory() {
        return protocolFactory;
    }

    @Override
    public Registry getRegistry() {
        return registry;
    }

    @Override
    public Router getRouter() {
        return router;
    }

    @Override
    synchronized public void shutdown() {
        shutdown(false);
    }

    protected void shutdown(boolean separateThread) {
        Runnable shutdown = () -> {
			log.info(">>> Shutting down UPnP service...");
			shutdownRegistry();
			shutdownRouter();
			shutdownConfiguration();
			log.info("<<< UPnP service shutdown completed");
		};
        if (separateThread) {
            // This is not a daemon thread, it has to complete!
            configuration.startThread(shutdown);
        } else {
            shutdown.run();
        }
    }

    protected void shutdownRegistry() {
        getRegistry().shutdown();
    }

    protected void shutdownRouter() {
        try {
            getRouter().shutdown();
        } catch (RouterException ex) {
            Throwable cause = Exceptions.unwrap(ex);
            if (cause instanceof InterruptedException) {
                if (log.isInfoEnabled())
                    log.info("Router shutdown was interrupted: " + ex, cause);
            } else {
                if (log.isErrorEnabled())
                    log.error("Router error on shutdown: " + ex, cause);
            }
        }
    }

    protected void shutdownConfiguration() {
        getConfiguration().shutdown();
    }

}
