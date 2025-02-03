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

import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import java.io.IOException;

public class Worker {
	public static XnioWorker createDefaultWorker() throws IOException {
		Xnio xnio = Xnio.getInstance("nio");
		return xnio.createWorker(OptionMap.builder()
				.set(Options.WORKER_IO_THREADS, 1)
				.set(Options.CONNECTION_HIGH_WATER, 1000000)
				.set(Options.CONNECTION_LOW_WATER, 1000000)
				.set(Options.WORKER_TASK_CORE_THREADS, 0)
				.set(Options.WORKER_TASK_MAX_THREADS, Runtime.getRuntime().availableProcessors())
				.set(Options.TCP_NODELAY, true)
				.set(Options.KEEP_ALIVE, true)
				.getMap());
	}
}
