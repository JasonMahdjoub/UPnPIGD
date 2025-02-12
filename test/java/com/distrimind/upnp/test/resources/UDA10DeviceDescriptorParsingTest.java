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

package com.distrimind.upnp.test.resources;

import com.distrimind.upnp.model.meta.LocalDevice;
import com.distrimind.upnp.binding.xml.DeviceDescriptorBinder;
import com.distrimind.upnp.binding.xml.UDA10DeviceDescriptorBinderImpl;
import com.distrimind.upnp.binding.xml.UDA10DeviceDescriptorBinderSAXImpl;
import com.distrimind.upnp.mock.MockUpnpService;
import com.distrimind.upnp.model.meta.RemoteDevice;
import com.distrimind.upnp.model.profile.RemoteClientInfo;
import com.distrimind.upnp.transport.impl.NetworkAddressFactoryImpl;
import com.distrimind.upnp.test.data.SampleData;
import com.distrimind.upnp.test.data.SampleDeviceRoot;
import com.distrimind.upnp.util.io.IO;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class UDA10DeviceDescriptorParsingTest {

    @Test
    public void readUDA10DescriptorDOM() throws Exception {

        DeviceDescriptorBinder binder = new UDA10DeviceDescriptorBinderImpl(new NetworkAddressFactoryImpl());

        RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
        device = binder.describe(device, IO.readLines(getClass().getResourceAsStream("/descriptors/device/uda10.xml")));

        SampleDeviceRoot.assertLocalResourcesMatch(
                new MockUpnpService().getConfiguration().getNamespace().getResources(device)
        );
        SampleDeviceRoot.assertMatch(device, SampleData.createRemoteDevice());

    }

    @Test
    public void readUDA10DescriptorSAX() throws Exception {

        DeviceDescriptorBinder binder = new UDA10DeviceDescriptorBinderSAXImpl(new NetworkAddressFactoryImpl());

        RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
        device = binder.describe(device, IO.readLines(getClass().getResourceAsStream("/descriptors/device/uda10.xml")));

        SampleDeviceRoot.assertLocalResourcesMatch(
                new MockUpnpService().getConfiguration().getNamespace().getResources(device)
        );
        SampleDeviceRoot.assertMatch(device, SampleData.createRemoteDevice());

    }

    @Test
    public void writeUDA10Descriptor() throws Exception {

        MockUpnpService upnpService = new MockUpnpService();
        DeviceDescriptorBinder binder = new UDA10DeviceDescriptorBinderImpl(new NetworkAddressFactoryImpl());
        
        RemoteDevice device = SampleData.createRemoteDevice();
        String descriptorXml = binder.generate(
                device,
                new RemoteClientInfo(),
                upnpService.getConfiguration().getNamespace()
        );

/*
        System.out.println("#######################################################################################");
        System.out.println(descriptorXml);
        System.out.println("#######################################################################################");
*/

        RemoteDevice hydratedDevice = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
        hydratedDevice = binder.describe(hydratedDevice, descriptorXml);

        SampleDeviceRoot.assertLocalResourcesMatch(
                upnpService.getConfiguration().getNamespace().getResources(hydratedDevice)

        );
        SampleDeviceRoot.assertMatch(hydratedDevice, device);

    }

    @Test
    public void writeUDA10DescriptorWithProvider() throws Exception {

        MockUpnpService upnpService = new MockUpnpService();
        DeviceDescriptorBinder binder = new UDA10DeviceDescriptorBinderImpl(new NetworkAddressFactoryImpl());

        LocalDevice<?> device = SampleData.createLocalDevice(true);
        String descriptorXml = binder.generate(
                device,
                new RemoteClientInfo(),
                upnpService.getConfiguration().getNamespace()
        );


        //System.out.println("#######################################################################################");
        //System.out.println(descriptorXml);
        //System.out.println("#######################################################################################");


        RemoteDevice hydratedDevice = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
        hydratedDevice = binder.describe(hydratedDevice, descriptorXml);

        SampleDeviceRoot.assertLocalResourcesMatch(
                upnpService.getConfiguration().getNamespace().getResources(hydratedDevice)

        );
        //SampleDeviceRoot.assertMatch(hydratedDevice, device, false);

    }

    @Test
    public void readUDA10DescriptorWithURLBase() throws Exception {
        MockUpnpService upnpService = new MockUpnpService();
        DeviceDescriptorBinder binder = upnpService.getConfiguration().getDeviceDescriptorBinderUDA10();

        RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
        device = binder.describe(
                device,
                IO.readLines(getClass().getResourceAsStream("/descriptors/device/uda10_withbase.xml"))
        );

        assertEquals(
                device.normalizeURI(device.getDetails().getManufacturerDetails().getManufacturerURI()).toString(),
                SampleData.getLocalBaseURL() + "mfc.html"
        );
        assertEquals(
                device.normalizeURI(device.getDetails().getModelDetails().getModelURI()).toString(),
                SampleData.getLocalBaseURL() + "someotherbase/MY-DEVICE-123/model.html"
        );
        assertEquals(
                device.normalizeURI(device.getDetails().getPresentationURI()).toString(),
                "http://www.4thline.org/some_ui"
        );

        assertEquals(
                device.normalizeURI(device.getIcons().get(0).getUri()).toString(),
                SampleData.getLocalBaseURL() + "someotherbase/MY-DEVICE-123/icon.png"
        );

        assertEquals(device.normalizeURI(
                device.getServices().get(0).getDescriptorURI()).toString(),
                     SampleData.getLocalBaseURL() + "someotherbase/MY-DEVICE-123/svc/upnp-org/MY-SERVICE-123/desc.xml"
        );
        assertEquals(
                device.normalizeURI(device.getServices().get(0).getControlURI()).toString(),
                SampleData.getLocalBaseURL() + "someotherbase/MY-DEVICE-123/svc/upnp-org/MY-SERVICE-123/control"
        );
        assertEquals(
                device.normalizeURI(device.getServices().get(0).getEventSubscriptionURI()).toString(),
                SampleData.getLocalBaseURL() + "someotherbase/MY-DEVICE-123/svc/upnp-org/MY-SERVICE-123/events"
        );

        assertTrue(device.isRoot());
    }

    @Test
    public void readUDA10DescriptorWithURLBase2() throws Exception {
        MockUpnpService upnpService = new MockUpnpService();
        DeviceDescriptorBinder binder = upnpService.getConfiguration().getDeviceDescriptorBinderUDA10();

        RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
        device = binder.describe(
                device,
                IO.readLines(getClass().getResourceAsStream("/descriptors/device/uda10_withbase2.xml"))
        );

        assertEquals(
                device.normalizeURI(device.getDetails().getManufacturerDetails().getManufacturerURI()).toString(),
                SampleData.getLocalBaseURL() + "mfc.html"
        );

        assertEquals(
                device.normalizeURI(device.getDetails().getModelDetails().getModelURI()).toString(),
                SampleData.getLocalBaseURL() + "model.html"
        );
        assertEquals(
                device.normalizeURI(device.getDetails().getPresentationURI()).toString(),
                "http://www.4thline.org/some_ui"
        );

        assertEquals(
                device.normalizeURI(device.getIcons().get(0).getUri()).toString(),
                SampleData.getLocalBaseURL() + "icon.png"
        );

        assertEquals(device.normalizeURI(
                device.getServices().get(0).getDescriptorURI()).toString(),
                     SampleData.getLocalBaseURL() + "svc.xml"
        );
        assertEquals(
                device.normalizeURI(device.getServices().get(0).getControlURI()).toString(),
                SampleData.getLocalBaseURL() + "control"
        );
        assertEquals(
                device.normalizeURI(device.getServices().get(0).getEventSubscriptionURI()).toString(),
                SampleData.getLocalBaseURL() + "events"
        );

        assertTrue(device.isRoot());
    }

    @Test
    public void readUDA10DescriptorWithEmptyURLBase() throws Exception {
        DeviceDescriptorBinder binder = new UDA10DeviceDescriptorBinderImpl(new NetworkAddressFactoryImpl());

        RemoteDevice device = new RemoteDevice(SampleData.createRemoteDeviceIdentity());
        device = binder.describe(device, IO.readLines(getClass().getResourceAsStream("/descriptors/device/uda10_emptybase.xml")));

        SampleDeviceRoot.assertLocalResourcesMatch(
                new MockUpnpService().getConfiguration().getNamespace().getResources(device)
        );
        SampleDeviceRoot.assertMatch(device, SampleData.createRemoteDevice());    }
}

