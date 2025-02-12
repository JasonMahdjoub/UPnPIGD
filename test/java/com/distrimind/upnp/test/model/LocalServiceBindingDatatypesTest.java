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

package com.distrimind.upnp.test.model;

import com.distrimind.upnp.binding.LocalServiceBinder;
import com.distrimind.upnp.binding.annotations.AnnotationLocalServiceBinder;
import com.distrimind.upnp.binding.annotations.UpnpAction;
import com.distrimind.upnp.binding.annotations.UpnpOutputArgument;
import com.distrimind.upnp.binding.annotations.UpnpService;
import com.distrimind.upnp.binding.annotations.UpnpServiceId;
import com.distrimind.upnp.binding.annotations.UpnpServiceType;
import com.distrimind.upnp.binding.annotations.UpnpStateVariable;
import com.distrimind.upnp.model.meta.ActionArgument;
import com.distrimind.upnp.model.meta.DeviceDetails;
import com.distrimind.upnp.model.meta.LocalDevice;
import com.distrimind.upnp.model.meta.LocalService;
import com.distrimind.upnp.model.types.Datatype;
import com.distrimind.upnp.model.types.UDADeviceType;
import com.distrimind.upnp.test.data.SampleData;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Random;

import static org.testng.Assert.*;

/**
 * @author Christian Bauer
 */
public class LocalServiceBindingDatatypesTest {

    public static final String GET_DATA = "GetData";

    public <T> LocalDevice<T> createTestDevice(LocalService<T> service) throws Exception {
        return new LocalDevice<>(
                SampleData.createLocalDeviceIdentity(),
                new UDADeviceType("TestDevice", 1),
                new DeviceDetails("Test Device"),
                service
        );
    }

    @DataProvider(name = "devices")
    public Object[][] getDevices() throws Exception {

        // This is what we are actually testing
        LocalServiceBinder binder = new AnnotationLocalServiceBinder();

        return new LocalDevice[][]{
                {createTestDevice(binder.read(TestServiceOne.class))},
        };
    }

    @Test(dataProvider = "devices")
    public void validateBinding(LocalDevice<?> device) {

        LocalService<?> svc = SampleData.getFirstService(device);

        //System.out.println("############################################################################");
        //ServiceDescriptorBinder binder = new DefaultRouterConfiguration().getServiceDescriptorBinderUDA10();
        //System.out.println(binder.generate(svc));
        //System.out.println("############################################################################");

        assertEquals(svc.getStateVariables().size(), 1);
        assertEquals(svc.getStateVariable("Data").getTypeDetails().getDatatype().getBuiltin(), Datatype.Builtin.BIN_BASE64);
		assertFalse(svc.getStateVariable("Data").getEventDetails().isSendEvents());

        assertEquals(svc.getActions().size(), 1);

        assertEquals(svc.getAction(GET_DATA).getName(), GET_DATA);
        assertEquals(svc.getAction(GET_DATA).getArguments().size(), 1);
        assertEquals(svc.getAction(GET_DATA).getArguments().get(0).getName(), "RandomData");
        assertEquals(svc.getAction(GET_DATA).getArguments().get(0).getDirection(), ActionArgument.Direction.OUT);
        assertEquals(svc.getAction(GET_DATA).getArguments().get(0).getRelatedStateVariableName(), "Data");
		assertTrue(svc.getAction(GET_DATA).getArguments().get(0).isReturnValue());

    }

    /* ####################################################################################################### */

    @UpnpService(
            serviceId = @UpnpServiceId("SomeService"),
            serviceType = @UpnpServiceType(value = "SomeService", version = 1),
            supportsQueryStateVariables = false
    )
    public static class TestServiceOne {

        public TestServiceOne() {
            data = new byte[8];
            new Random().nextBytes(data);
        }

        @UpnpStateVariable(sendEvents = false)
        private final byte[] data;

        @UpnpAction(out = @UpnpOutputArgument(name = "RandomData"))
        public byte[] getData() {
            return data.clone();
        }
    }


}
