/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.distrimind.upnp_igd.test.model;

import com.distrimind.upnp_igd.binding.LocalServiceBinder;
import com.distrimind.upnp_igd.binding.annotations.AnnotationLocalServiceBinder;
import com.distrimind.upnp_igd.binding.annotations.UpnpAction;
import com.distrimind.upnp_igd.binding.annotations.UpnpOutputArgument;
import com.distrimind.upnp_igd.binding.annotations.UpnpService;
import com.distrimind.upnp_igd.binding.annotations.UpnpServiceId;
import com.distrimind.upnp_igd.binding.annotations.UpnpServiceType;
import com.distrimind.upnp_igd.binding.annotations.UpnpStateVariable;
import com.distrimind.upnp_igd.model.meta.ActionArgument;
import com.distrimind.upnp_igd.model.meta.DeviceDetails;
import com.distrimind.upnp_igd.model.meta.LocalDevice;
import com.distrimind.upnp_igd.model.meta.LocalService;
import com.distrimind.upnp_igd.model.types.Datatype;
import com.distrimind.upnp_igd.model.types.UDADeviceType;
import com.distrimind.upnp_igd.test.data.SampleData;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Random;

import static org.testng.Assert.assertEquals;

/**
 * @author Christian Bauer
 */
public class LocalServiceBindingDatatypesTest {

    public LocalDevice createTestDevice(LocalService service) throws Exception {
        return new LocalDevice(
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
    public void validateBinding(LocalDevice device) {

        LocalService svc = SampleData.getFirstService(device);

        //System.out.println("############################################################################");
        //ServiceDescriptorBinder binder = new DefaultRouterConfiguration().getServiceDescriptorBinderUDA10();
        //System.out.println(binder.generate(svc));
        //System.out.println("############################################################################");

        assertEquals(svc.getStateVariables().length, 1);
        assertEquals(svc.getStateVariable("Data").getTypeDetails().getDatatype().getBuiltin(), Datatype.Builtin.BIN_BASE64);
        assertEquals(svc.getStateVariable("Data").getEventDetails().isSendEvents(), false);

        assertEquals(svc.getActions().length, 1);

        assertEquals(svc.getAction("GetData").getName(), "GetData");
        assertEquals(svc.getAction("GetData").getArguments().length, 1);
        assertEquals(svc.getAction("GetData").getArguments()[0].getName(), "RandomData");
        assertEquals(svc.getAction("GetData").getArguments()[0].getDirection(), ActionArgument.Direction.OUT);
        assertEquals(svc.getAction("GetData").getArguments()[0].getRelatedStateVariableName(), "Data");
        assertEquals(svc.getAction("GetData").getArguments()[0].isReturnValue(), true);

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
        private byte[] data;

        @UpnpAction(out = @UpnpOutputArgument(name = "RandomData"))
        public byte[] getData() {
            return data;
        }
    }


}
