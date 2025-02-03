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

import com.distrimind.upnp_igd.transport.spi.AbstractStreamClientConfiguration;
import org.xnio.XnioWorker;

import java.util.concurrent.ExecutorService;

public class StreamClientConfigurationImpl  extends AbstractStreamClientConfiguration {
	public StreamClientConfigurationImpl(XnioWorker timeoutExecutorService) {
		super(timeoutExecutorService);
	}

	public StreamClientConfigurationImpl(XnioWorker timeoutExecutorService, int timeoutSeconds) {
		super(timeoutExecutorService, timeoutSeconds);
	}
	public StreamClientConfigurationImpl(ExecutorService timeoutExecutorService, int timeoutSeconds) {
		super(timeoutExecutorService, timeoutSeconds);
	}

	/**
	 * @return By default <code>0</code>.
	 */
	public int getRequestRetryCount() {
		return 0;
	}
}
