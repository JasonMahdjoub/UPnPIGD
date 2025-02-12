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

package com.distrimind.upnp.test.ssdp;

import com.distrimind.upnp.UpnpService;
import com.distrimind.upnp.mock.MockUpnpService;
import com.distrimind.upnp.model.Constants;
import com.distrimind.upnp.model.message.IncomingDatagramMessage;
import com.distrimind.upnp.model.message.UpnpResponse;
import com.distrimind.upnp.model.message.discovery.IncomingSearchResponse;
import com.distrimind.upnp.model.message.header.EXTHeader;
import com.distrimind.upnp.model.message.header.HostHeader;
import com.distrimind.upnp.model.message.header.LocationHeader;
import com.distrimind.upnp.model.message.header.MaxAgeHeader;
import com.distrimind.upnp.model.message.header.STAllHeader;
import com.distrimind.upnp.model.message.header.UDNHeader;
import com.distrimind.upnp.model.message.header.USNRootDeviceHeader;
import com.distrimind.upnp.model.message.header.UpnpHeader;
import com.distrimind.upnp.model.meta.LocalDevice;
import com.distrimind.upnp.model.meta.RemoteDevice;
import com.distrimind.upnp.test.data.SampleData;
import com.distrimind.upnp.test.data.SampleDeviceRoot;
import org.testng.annotations.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.testng.Assert.assertEquals;

/**
 * @author Christian Bauer
 */
public class SearchResponseTest {

    @Test
    public void receivedValidResponse() throws Exception {

        MockUpnpService upnpService = new MockUpnpService();

        RemoteDevice rd = SampleData.createRemoteDevice();

        IncomingSearchResponse msg = createResponseMessage(new STAllHeader());
        msg.getHeaders().add(UpnpHeader.Type.USN, new USNRootDeviceHeader(rd.getIdentity().getUdn()));
        msg.getHeaders().add(UpnpHeader.Type.LOCATION, new LocationHeader(SampleDeviceRoot.getDeviceDescriptorURL()));
        msg.getHeaders().add(UpnpHeader.Type.MAX_AGE, new MaxAgeHeader(rd.getIdentity().getMaxAgeSeconds()));

        upnpService.getProtocolFactory().createReceivingAsync(msg).run();
        Thread.sleep(100);
        assertEquals(upnpService.getRouter().getSentStreamRequestMessages().size(), 1);
    }

    @Test
    public void receivedInvalidSearchResponses() throws Exception {

        MockUpnpService upnpService = new MockUpnpService();

        RemoteDevice rd = SampleData.createRemoteDevice();

        // Missing USN header
        IncomingSearchResponse msg = createResponseMessage(new STAllHeader());
        upnpService.getProtocolFactory().createReceivingAsync(msg).run();
        Thread.sleep(100);
        assertEquals(upnpService.getRouter().getSentStreamRequestMessages().size(), 0);

        // Missing location header
        msg = createResponseMessage(new STAllHeader());
        msg.getHeaders().add(UpnpHeader.Type.USN, new USNRootDeviceHeader(rd.getIdentity().getUdn()));
        upnpService.getProtocolFactory().createReceivingAsync(msg).run();
        Thread.sleep(100);
        assertEquals(upnpService.getRouter().getSentStreamRequestMessages().size(), 0);

        // Missing max age header
        msg = createResponseMessage(new STAllHeader());
        msg.getHeaders().add(UpnpHeader.Type.USN, new USNRootDeviceHeader(rd.getIdentity().getUdn()));
        msg.getHeaders().add(UpnpHeader.Type.LOCATION, new LocationHeader(SampleDeviceRoot.getDeviceDescriptorURL()));
        upnpService.getProtocolFactory().createReceivingAsync(msg).run();
        Thread.sleep(100);
        assertEquals(upnpService.getRouter().getSentStreamRequestMessages().size(), 0);

    }

    @Test
    public void receivedAlreadyKnownLocalUDN() throws Exception {

        MockUpnpService upnpService = new MockUpnpService();

        LocalDevice<?> localDevice = SampleData.createLocalDevice();
        upnpService.getRegistry().addDevice(localDevice);

        RemoteDevice rd = SampleData.createRemoteDevice();

        IncomingSearchResponse msg = createResponseMessage(new STAllHeader());
        msg.getHeaders().add(UpnpHeader.Type.USN, new USNRootDeviceHeader(rd.getIdentity().getUdn()));
        msg.getHeaders().add(UpnpHeader.Type.LOCATION, new LocationHeader(SampleDeviceRoot.getDeviceDescriptorURL()));
        msg.getHeaders().add(UpnpHeader.Type.MAX_AGE, new MaxAgeHeader(rd.getIdentity().getMaxAgeSeconds()));

        upnpService.getProtocolFactory().createReceivingAsync(msg).run();
        Thread.sleep(100);
        assertEquals(upnpService.getRouter().getSentStreamRequestMessages().size(), 0);
    }

    @Test
    public void receiveEmbeddedTriggersUpdate() throws Exception {

        UpnpService upnpService = new MockUpnpService(false, true);

        RemoteDevice rd = SampleData.createRemoteDevice();
        RemoteDevice embedded = rd.getEmbeddedDevices().iterator().next();

        upnpService.getRegistry().addDevice(rd);

        assertEquals(upnpService.getRegistry().getRemoteDevices().size(), 1);

        IncomingSearchResponse msg = createResponseMessage(new STAllHeader());
        msg.getHeaders().add(UpnpHeader.Type.USN, new UDNHeader(embedded.getIdentity().getUdn()));
        msg.getHeaders().add(UpnpHeader.Type.LOCATION, new LocationHeader(SampleDeviceRoot.getDeviceDescriptorURL()));
        msg.getHeaders().add(UpnpHeader.Type.MAX_AGE, new MaxAgeHeader(rd.getIdentity().getMaxAgeSeconds()));

        Thread.sleep(1000);
        upnpService.getProtocolFactory().createReceivingAsync(msg).run();

        Thread.sleep(1000);
        upnpService.getProtocolFactory().createReceivingAsync(msg).run();

        Thread.sleep(1000);
        assertEquals(upnpService.getRegistry().getRemoteDevices().size(), 1);

        upnpService.shutdown();
    }

    protected IncomingSearchResponse createResponseMessage(UpnpHeader<?> stHeader) throws UnknownHostException {
        IncomingSearchResponse msg = new IncomingSearchResponse(
                new IncomingDatagramMessage<>(
                        new UpnpResponse(UpnpResponse.Status.OK),
                        InetAddress.getByName("127.0.0.1"),
                        Constants.UPNP_MULTICAST_PORT,
                        InetAddress.getByName("127.0.0.1")
                )
        );

        msg.getHeaders().add(UpnpHeader.Type.ST, stHeader);
        msg.getHeaders().add(UpnpHeader.Type.EXT, new EXTHeader());
        msg.getHeaders().add(UpnpHeader.Type.HOST, new HostHeader());
        return msg;

    }
    
}
