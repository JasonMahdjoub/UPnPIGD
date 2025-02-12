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

import com.distrimind.upnp.transport.spi.AbstractStreamClientConfiguration;
import org.xnio.XnioWorker;

import java.util.concurrent.ExecutorService;

/**
 * @author Jason Mahdjoub
 * @since 1.2.0
 */
public class UndertowStreamClientConfigurationImpl extends AbstractStreamClientConfiguration {
	private boolean usePersistentConnections = false;
	public UndertowStreamClientConfigurationImpl(XnioWorker timeoutExecutorService) {
		super(timeoutExecutorService);
	}

	public UndertowStreamClientConfigurationImpl(XnioWorker timeoutExecutorService, int timeoutSeconds) {
		super(timeoutExecutorService, timeoutSeconds);
	}
	public UndertowStreamClientConfigurationImpl(ExecutorService timeoutExecutorService, int timeoutSeconds) {
		super(timeoutExecutorService, timeoutSeconds);
	}

	/**
	 * Defaults to <code>false</code>, avoiding obscure bugs in the JDK.
	 */
	public boolean isUsePersistentConnections() {
		return usePersistentConnections;
	}

	public void setUsePersistentConnections(boolean usePersistentConnections) {
		this.usePersistentConnections = usePersistentConnections;
	}

	@Override
	public XnioWorker getRequestExecutorService() {
		return (XnioWorker)super.getRequestExecutorService();
	}
	@Override
	public void setRequestExecutorService(ExecutorService requestExecutorService) {
		if (!(requestExecutorService instanceof XnioWorker))
			throw new IllegalArgumentException();
		this.requestExecutorService = requestExecutorService;
	}
	public void setRequestExecutorService(XnioWorker requestExecutorService) {
		this.requestExecutorService = requestExecutorService;
	}
}
