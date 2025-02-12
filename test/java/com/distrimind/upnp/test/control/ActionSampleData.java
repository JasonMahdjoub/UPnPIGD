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

package com.distrimind.upnp.test.control;

import com.distrimind.upnp.binding.annotations.*;
import com.distrimind.upnp.model.meta.DeviceDetails;
import com.distrimind.upnp.model.meta.LocalDevice;
import com.distrimind.upnp.model.meta.LocalService;
import com.distrimind.upnp.model.profile.RemoteClientInfo;
import com.distrimind.upnp.model.types.UDADeviceType;
import com.distrimind.upnp.test.data.SampleData;

import static org.testng.Assert.*;

/**
 * @author Christian Bauer
 */
public class ActionSampleData {

    public static final String SWITCH_POWER = "SwitchPower";

    public static LocalDevice<LocalTestService> createTestDevice() throws Exception {
        return createTestDevice(LocalTestService.class);
    }

    public static <T> LocalDevice<T> createTestDevice(Class<T> clazz) throws Exception {
        return createTestDevice(
                SampleData.readService(
                        new AnnotationLocalServiceBinder(),
                        clazz
                )
        );
    }

    public static <T> LocalDevice<T> createTestDevice(LocalService<T> service) throws Exception {
        return new LocalDevice<>(
                SampleData.createLocalDeviceIdentity(),
                new UDADeviceType("BinaryLight", 1),
                new DeviceDetails("Example Binary Light"),
                service
        );
    }

    @UpnpService(
            serviceId = @UpnpServiceId(SWITCH_POWER),
            serviceType = @UpnpServiceType(value = SWITCH_POWER, version = 1)
    )
    public static class LocalTestService {

        @UpnpStateVariable(sendEvents = false)
        private boolean target = false;

        @UpnpStateVariable
        private boolean status = false;

        @UpnpAction
        public void setTarget(@UpnpInputArgument(name = "NewTargetValue") boolean newTargetValue) {
            target = newTargetValue;
            status = newTargetValue;
        }

        @UpnpAction(out = @UpnpOutputArgument(name = "RetTargetValue"))
        public boolean getTarget() {
            return target;
        }

        @UpnpAction(name = "GetStatus", out = @UpnpOutputArgument(name = "ResultStatus", getterName = "getStatus"))
        public void dummyStatus() {
            // NOOP
        }

        public boolean getStatus() {
            return status;
        }
    }

    public static class LocalTestServiceThrowsException extends LocalTestService {
        @Override
        public void setTarget(@UpnpInputArgument(name = "NewTargetValue") boolean newTargetValue) {
            throw new RuntimeException("Something is wrong");
        }
    }

    public static class LocalTestServiceDelays extends LocalTestService {
        @Override
        public boolean getTarget() {
            try {
                Thread.sleep(50); // A small delay so they are really concurrent
            } catch (InterruptedException ignored) {}
            return super.getTarget();
        }
    }

    public static class LocalTestServiceExtended extends LocalTestService {

        @UpnpStateVariable
        String someValue;

        @UpnpAction
        public void setSomeValue(@UpnpInputArgument(name = "SomeValue", aliases ={"SomeValue1"}) String someValue) {
            this.someValue = someValue;
        }

        @UpnpAction(out = @UpnpOutputArgument(name = "SomeValue"))
        public String getSomeValue() {
            return someValue;
        }

    }
    
    @UpnpService(
            serviceId = @UpnpServiceId(SWITCH_POWER),
            serviceType = @UpnpServiceType(value = SWITCH_POWER, version = 1)
    )
    public static class LocalTestServiceWithClientInfo {

        @UpnpStateVariable(sendEvents = false)
        private boolean target = false;

        @UpnpStateVariable
        private boolean status = false;

        @UpnpAction
        public void setTarget(@UpnpInputArgument(name = "NewTargetValue") boolean newTargetValue) {
            target = newTargetValue;
            status = newTargetValue;
        }

        @UpnpAction(out = @UpnpOutputArgument(name = "RetTargetValue"))
        public boolean getTarget(RemoteClientInfo clientInfo) {
            assertNotNull(clientInfo);
            assertEquals(clientInfo.getRemoteAddress().getHostAddress(), "10.0.0.1");
            assertEquals(clientInfo.getLocalAddress().getHostAddress(), "10.0.0.2");
            assertEquals(clientInfo.getRequestUserAgent(), "foo/bar");
            clientInfo.getExtraResponseHeaders().add("X-MY-HEADER", "foobar");
            return target;
        }

        @UpnpAction(name = "GetStatus", out = @UpnpOutputArgument(name = "ResultStatus", getterName = "getStatus"))
        public void dummyStatus() {
            // NOOP
        }

        public boolean getStatus() {
            return status;
        }

    }

}
