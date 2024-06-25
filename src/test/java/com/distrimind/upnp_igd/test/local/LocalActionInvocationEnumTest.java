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

package com.distrimind.upnp_igd.test.local;

import com.distrimind.upnp_igd.binding.LocalServiceBinder;
import com.distrimind.upnp_igd.binding.annotations.AnnotationLocalServiceBinder;
import com.distrimind.upnp_igd.binding.annotations.UpnpAction;
import com.distrimind.upnp_igd.binding.annotations.UpnpInputArgument;
import com.distrimind.upnp_igd.binding.annotations.UpnpOutputArgument;
import com.distrimind.upnp_igd.binding.annotations.UpnpService;
import com.distrimind.upnp_igd.binding.annotations.UpnpServiceId;
import com.distrimind.upnp_igd.binding.annotations.UpnpServiceType;
import com.distrimind.upnp_igd.binding.annotations.UpnpStateVariable;
import com.distrimind.upnp_igd.model.action.ActionInvocation;
import com.distrimind.upnp_igd.model.meta.DeviceDetails;
import com.distrimind.upnp_igd.model.meta.LocalDevice;
import com.distrimind.upnp_igd.model.meta.LocalService;
import com.distrimind.upnp_igd.model.types.UDADeviceType;
import com.distrimind.upnp_igd.test.data.SampleData;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class LocalActionInvocationEnumTest {

    public <T> LocalDevice<T> createTestDevice(LocalService<T> service) throws Exception {
        return new LocalDevice<>(
                SampleData.createLocalDeviceIdentity(),
                new UDADeviceType("BinaryLight", 1),
                new DeviceDetails("Example Binary Light"),
                service
        );
    }

    @DataProvider(name = "devices")
    public Object[][] getDevices() throws Exception {
        LocalServiceBinder binder = new AnnotationLocalServiceBinder();
        return new LocalDevice[][]{
                {createTestDevice(SampleData.readService(binder, TestServiceOne.class))},
                {createTestDevice(SampleData.readService(binder,TestServiceTwo.class))},
                {createTestDevice(SampleData.readService(binder, TestServiceThree.class))},
        };
    }

    @Test(dataProvider = "devices")
    public void invokeActions(LocalDevice<?> device) throws Exception {

        LocalService<?> svc = SampleData.getFirstService(device);

        ActionInvocation<? extends LocalService<?>> checkTargetInvocation = new ActionInvocation<>(svc.getAction("GetTarget"));
        svc.getExecutor(checkTargetInvocation.getAction()).executeWithUntypedGeneric(checkTargetInvocation);
		assertNull(checkTargetInvocation.getFailure());
        assertEquals(checkTargetInvocation.getOutput().size(), 1);
        assertEquals(checkTargetInvocation.getOutput().iterator().next().toString(), "UNKNOWN");

        ActionInvocation<? extends LocalService<?>> setTargetInvocation = new ActionInvocation<>(svc.getAction("SetTarget"));
        setTargetInvocation.setInput("NewTargetValue", "ON");
        svc.getExecutor(setTargetInvocation.getAction()).executeWithUntypedGeneric(setTargetInvocation);
		assertNull(setTargetInvocation.getFailure());
        assertEquals(setTargetInvocation.getOutput().size(), 0);

        ActionInvocation<? extends LocalService<?>> getTargetInvocation = new ActionInvocation<>(svc.getAction("GetTarget"));
        svc.getExecutor(getTargetInvocation.getAction()).executeWithUntypedGeneric(getTargetInvocation);
		assertNull(getTargetInvocation.getFailure());
        assertEquals(getTargetInvocation.getOutput().size(), 1);
        assertEquals(getTargetInvocation.getOutput().iterator().next().toString(), "ON");

        ActionInvocation<? extends LocalService<?>> getStatusInvocation = new ActionInvocation<>(svc.getAction("GetStatus"));
        svc.getExecutor(getStatusInvocation.getAction()).executeWithUntypedGeneric(getStatusInvocation);
		assertNull(getStatusInvocation.getFailure());
        assertEquals(getStatusInvocation.getOutput().size(), 1);
        assertEquals(getStatusInvocation.getOutput().iterator().next().toString(), "1");

    }

    /* ####################################################################################################### */

    @UpnpService(
            serviceId = @UpnpServiceId("SwitchPower"),
            serviceType = @UpnpServiceType(value = "SwitchPower", version = 1)
    )
    public static class TestServiceOne {

        public enum Target {
            ON,
            OFF,
            UNKNOWN
        }

        @UpnpStateVariable(sendEvents = false)
        private Target target = Target.UNKNOWN;

        @UpnpStateVariable
        private boolean status = false;

        @UpnpAction
        public void setTarget(@UpnpInputArgument(name = "NewTargetValue") String newTargetValue) {
            target = Target.valueOf(newTargetValue);

            status = target == Target.ON;
        }

        @UpnpAction(out = @UpnpOutputArgument(name = "RetTargetValue"))
        public Target getTarget() {
            return target;
        }

        @UpnpAction(out = @UpnpOutputArgument(name = "ResultStatus"))
        public boolean getStatus() {
            return status;
        }
    }


    /* ####################################################################################################### */

    @UpnpService(
            serviceId = @UpnpServiceId("SwitchPower"),
            serviceType = @UpnpServiceType(value = "SwitchPower", version = 1)
    )
    public static class TestServiceTwo {

        public enum Target {
            ON,
            OFF,
            UNKNOWN
        }

        @UpnpStateVariable(sendEvents = false)
        private Target target = Target.UNKNOWN;

        @UpnpStateVariable
        private boolean status = false;

        @UpnpAction
        public void setTarget(@UpnpInputArgument(name = "NewTargetValue") String newTargetValue) {
            target = Target.valueOf(newTargetValue);

            status = target == Target.ON;
        }

        @UpnpAction(out = @UpnpOutputArgument(name = "RetTargetValue", stateVariable = "Target", getterName = "getRealTarget"))
        public void getTarget() {
        }

        public Target getRealTarget() {
            return target;
        }

        @UpnpAction(out = @UpnpOutputArgument(name = "ResultStatus"))
        public boolean getStatus() {
            return status;
        }
    }

    /* ####################################################################################################### */

    @UpnpService(
            serviceId = @UpnpServiceId("SwitchPower"),
            serviceType = @UpnpServiceType(value = "SwitchPower", version = 1)
    )
    public static class TestServiceThree {

        public enum Target {
            ON,
            OFF,
            UNKNOWN
        }

        public static class TargetHolder {
            private Target t;

            public TargetHolder(Target t) {
                this.t = t;
            }

            public Target getTarget() {
                return t;
            }
        }

        @UpnpStateVariable(sendEvents = false)
        private Target target = Target.UNKNOWN;

        @UpnpStateVariable
        private boolean status = false;

        @UpnpAction
        public void setTarget(@UpnpInputArgument(name = "NewTargetValue") String newTargetValue) {
            target = Target.valueOf(newTargetValue);

            status = target == Target.ON;
        }

        @UpnpAction(name = "GetTarget", out = @UpnpOutputArgument(name = "RetTargetValue", getterName = "getTarget"))
        public TargetHolder getTargetHolder() {
            return new TargetHolder(target);
        }

        @UpnpAction(out = @UpnpOutputArgument(name = "ResultStatus"))
        public boolean getStatus() {
            return status;
        }
    }

    /* ####################################################################################################### */

    @UpnpService(
            serviceId = @UpnpServiceId("SwitchPower"),
            serviceType = @UpnpServiceType(value = "SwitchPower", version = 1)
    )
    public static class TestServiceFour {

        public enum Target {
            ON,
            OFF,
            UNKNOWN
        }

        public static class TargetHolder {
            private Target t;

            public TargetHolder(Target t) {
                this.t = t;
            }

            public Target getT() {
                return t;
            }
        }

        @UpnpStateVariable(sendEvents = false)
        private Target target = Target.UNKNOWN;

        @UpnpStateVariable
        private boolean status = false;

        @UpnpAction
        public void setTarget(@UpnpInputArgument(name = "NewTargetValue") String newTargetValue) {
            target = Target.valueOf(newTargetValue);

            status = target == Target.ON;
        }

        @UpnpAction(name = "GetTarget", out = @UpnpOutputArgument(name = "RetTargetValue", stateVariable = "Target", getterName = "getT"))
        public TargetHolder getTargetHolder() {
            return new TargetHolder(target);
        }

        @UpnpAction(out = @UpnpOutputArgument(name = "ResultStatus"))
        public boolean getStatus() {
            return status;
        }
    }

}