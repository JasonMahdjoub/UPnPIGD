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

package com.distrimind.upnp.test.data;

import com.distrimind.upnp.model.meta.LocalDevice;
import com.distrimind.upnp.model.message.OutgoingDatagramMessage;
import com.distrimind.upnp.model.message.UpnpMessage;
import com.distrimind.upnp.model.message.header.DeviceTypeHeader;
import com.distrimind.upnp.model.message.header.DeviceUSNHeader;
import com.distrimind.upnp.model.message.header.RootDeviceHeader;
import com.distrimind.upnp.model.message.header.ServiceTypeHeader;
import com.distrimind.upnp.model.message.header.ServiceUSNHeader;
import com.distrimind.upnp.model.message.header.UDNHeader;
import com.distrimind.upnp.model.message.header.USNRootDeviceHeader;
import com.distrimind.upnp.model.message.header.UpnpHeader;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Christian Bauer
 */
public class SampleUSNHeaders {

    public static void assertUSNHeaders(List<OutgoingDatagramMessage<?>> msgs, LocalDevice<?> rootDevice, LocalDevice<?> embeddedDevice, UpnpHeader.Type ntstHeaderType) {

        // See the tables in UDA 1.0 section 1.1.2

        boolean gotRootDeviceFirstMsg = false;
        boolean gotRootDeviceSecondMsg = false;
        boolean gotRootDeviceThirdMsg = false;

        boolean gotEmbeddedDeviceFirstMsg = false;
        boolean gotEmbeddedDeviceSecondMsg = false;

        boolean gotFirstServiceMsg = false;
        boolean gotSecondServiceMsg = false;

        for (UpnpMessage<?> msg : msgs) {

            if (msg.getHeaders().getFirstHeader(ntstHeaderType, RootDeviceHeader.class) != null) {
                assertEquals(
                        msg.getHeaders().getFirstHeader(UpnpHeader.Type.USN, USNRootDeviceHeader.class).getString(),
                        new USNRootDeviceHeader(rootDevice.getIdentity().getUdn()).getString()
                );
                gotRootDeviceFirstMsg = true;
            }

            UDNHeader foundUDN = msg.getHeaders().getFirstHeader(ntstHeaderType, UDNHeader.class);
            if (foundUDN != null && foundUDN.getString().equals(new UDNHeader(rootDevice.getIdentity().getUdn()).getString())) {
                assertEquals(
                        msg.getHeaders().getFirstHeader(ntstHeaderType).getString(),
                        msg.getHeaders().getFirstHeader(UpnpHeader.Type.USN).getString()
                );
                gotRootDeviceSecondMsg = true;
            }

            if (foundUDN != null && foundUDN.getString().equals(new UDNHeader(embeddedDevice.getIdentity().getUdn()).getString())) {
                assertEquals(
                        msg.getHeaders().getFirstHeader(ntstHeaderType).getString(),
                        msg.getHeaders().getFirstHeader(UpnpHeader.Type.USN).getString()
                );
                gotEmbeddedDeviceFirstMsg = true;

            }

            DeviceTypeHeader foundDeviceNTST = msg.getHeaders().getFirstHeader(ntstHeaderType, DeviceTypeHeader.class);
            if (foundDeviceNTST != null && foundDeviceNTST.getString().equals(new DeviceTypeHeader(rootDevice.getType()).getString())) {
                assertEquals(
                        msg.getHeaders().getFirstHeader(UpnpHeader.Type.USN, DeviceUSNHeader.class).getString(),
                        new DeviceUSNHeader(rootDevice.getIdentity().getUdn(), rootDevice.getType()).getString()
                );
                gotRootDeviceThirdMsg = true;
            }

            if (foundDeviceNTST != null && foundDeviceNTST.getString().equals(new DeviceTypeHeader(embeddedDevice.getType()).getString())) {
                assertEquals(
                        msg.getHeaders().getFirstHeader(UpnpHeader.Type.USN, DeviceUSNHeader.class).getString(),
                        new DeviceUSNHeader(embeddedDevice.getIdentity().getUdn(), embeddedDevice.getType()).getString()
                );
                gotEmbeddedDeviceSecondMsg = true;
            }

            ServiceTypeHeader foundServiceNTST = msg.getHeaders().getFirstHeader(ntstHeaderType, ServiceTypeHeader.class);
            if (foundServiceNTST != null && foundServiceNTST.getString().equals(new ServiceTypeHeader(SampleServiceOne.getThisServiceType()).getString())) {
                assertEquals(
                        msg.getHeaders().getFirstHeader(UpnpHeader.Type.USN, ServiceUSNHeader.class).getString(),
                        new ServiceUSNHeader(rootDevice.getIdentity().getUdn(), SampleServiceOne.getThisServiceType()).getString()
                );
                gotFirstServiceMsg = true;
            }

            if (foundServiceNTST != null && foundServiceNTST.getString().equals(new ServiceTypeHeader(SampleServiceTwo.getThisServiceType()).getString())) {
                assertEquals(
                        msg.getHeaders().getFirstHeader(UpnpHeader.Type.USN, ServiceUSNHeader.class).getString(),
                        new ServiceUSNHeader(rootDevice.getIdentity().getUdn(), SampleServiceTwo.getThisServiceType()).getString()
                );
                gotSecondServiceMsg = true;
            }
        }

        assertTrue(gotRootDeviceFirstMsg);
        assertTrue(gotRootDeviceSecondMsg);
        assertTrue(gotRootDeviceThirdMsg);

        assertTrue(gotEmbeddedDeviceFirstMsg);
        assertTrue(gotEmbeddedDeviceSecondMsg);

        assertTrue(gotFirstServiceMsg);
        assertTrue(gotSecondServiceMsg);

    }
}
