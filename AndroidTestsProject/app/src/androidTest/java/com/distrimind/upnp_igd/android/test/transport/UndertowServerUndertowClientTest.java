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

package com.distrimind.upnp_igd.android.test.transport;

import com.distrimind.upnp_igd.UpnpServiceConfiguration;
import com.distrimind.upnp_igd.android.transport.impl.AsyncServletStreamServerConfigurationImpl;
import com.distrimind.upnp_igd.android.transport.impl.AsyncServletStreamServerImpl;
import com.distrimind.upnp_igd.android.transport.impl.undertow.StreamClientConfigurationImpl;
import com.distrimind.upnp_igd.android.transport.impl.undertow.UndertowServletContainer;
import com.distrimind.upnp_igd.android.transport.impl.undertow.UndertowStreamClientImpl;
import com.distrimind.upnp_igd.transport.spi.StreamClient;
import com.distrimind.upnp_igd.transport.spi.StreamServer;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;

public class UndertowServerUndertowClientTest extends StreamServerClientTest {
    protected UndertowServerUndertowClientTest() throws IOException {
    }

    @Override
    public StreamServer<?> createStreamServer(int port) {
        AsyncServletStreamServerConfigurationImpl configuration =
                new AsyncServletStreamServerConfigurationImpl(
                        UndertowServletContainer.INSTANCE,
                        port
                );

        return new AsyncServletStreamServerImpl(
                configuration
        ) {
            @Override
            protected boolean isConnectionOpen(HttpServletRequest request) {
                return UndertowServletContainer.isConnectionOpen(request);
            }
        };
    }

    @Override
    public StreamClient<?> createStreamClient(UpnpServiceConfiguration configuration) {
        return new UndertowStreamClientImpl(
                new StreamClientConfigurationImpl(
                        configuration.getSyncProtocolExecutorService(),
                        3  // Timeout in seconds
                )
        );
    }

}
