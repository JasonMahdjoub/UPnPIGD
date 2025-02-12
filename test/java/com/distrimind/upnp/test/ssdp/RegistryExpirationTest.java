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

import com.distrimind.upnp.mock.MockUpnpService;
import com.distrimind.upnp.mock.MockUpnpServiceConfiguration;
import com.distrimind.upnp.model.ExpirationDetails;
import com.distrimind.upnp.model.meta.RemoteDevice;
import com.distrimind.upnp.model.resource.Resource;
import com.distrimind.upnp.test.data.SampleData;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class RegistryExpirationTest {

    @Test
    public void addAndExpire() throws Exception {

        MockUpnpService upnpService = new MockUpnpService(false, new MockUpnpServiceConfiguration(true) {

            @Override
            public int getRegistryMaintenanceIntervalMillis() {
                return getDesktopPlatformUpnpServiceConfiguration().getRegistryMaintenanceIntervalMillis();
            }
        }
        );

        RemoteDevice rd = SampleData.createRemoteDevice(
                SampleData.createRemoteDeviceIdentity(1)
        );
        upnpService.getRegistry().addDevice(rd);
        
        assertEquals(upnpService.getRegistry().getRemoteDevices().size(), 1);

        Thread.sleep(3000);

        assertEquals(upnpService.getRegistry().getRemoteDevices().size(), 0);

        upnpService.shutdown();
    }

    @Test
    public void overrideAgeThenAddAndExpire() throws Exception {

        MockUpnpService upnpService = new MockUpnpService(
            new MockUpnpServiceConfiguration(true) {

                @Override
                public Integer getRemoteDeviceMaxAgeSeconds() {
                    return 0;
                }
            }
        );

        RemoteDevice rd = SampleData.createRemoteDevice(
                SampleData.createRemoteDeviceIdentity(1)
        );
        upnpService.getRegistry().addDevice(rd);

        assertEquals(upnpService.getRegistry().getRemoteDevices().size(), 1);

        Thread.sleep(3000);

        // Still registered!
        assertEquals(upnpService.getRegistry().getRemoteDevices().size(), 1);

        // Update should not change the expiration time
        upnpService.getRegistry().update(rd.getIdentity());

        Thread.sleep(3000);

        // Still registered!
        assertEquals(upnpService.getRegistry().getRemoteDevices().size(), 1);

        upnpService.shutdown();
    }

    @Test
    public void addAndUpdateAndExpire() throws Exception {

        MockUpnpService upnpService = new MockUpnpService(false, true);

        RemoteDevice rd = SampleData.createRemoteDevice(
                SampleData.createRemoteDeviceIdentity(2)
        );

        // Add it to registry
        upnpService.getRegistry().addDevice(rd);
        Thread.sleep(1000);
        assertEquals(upnpService.getRegistry().getRemoteDevices().size(), 1);

        // Update it in registry
        upnpService.getRegistry().addDevice(rd);
        Thread.sleep(1000);
        assertEquals(upnpService.getRegistry().getRemoteDevices().size(), 1);

        // Update again
        upnpService.getRegistry().update(rd.getIdentity());
        Thread.sleep(1000);
        assertEquals(upnpService.getRegistry().getRemoteDevices().size(), 1);

        // Wait for expiration
        Thread.sleep(3000);
        assertEquals(upnpService.getRegistry().getRemoteDevices().size(), 0);


        upnpService.shutdown();
    }

    @Test
    public void addResourceAndExpire() throws Exception {

        MockUpnpService upnpService = new MockUpnpService(false, true);

        Resource<String> resource = new Resource<>(URI.create("/this/is/a/test"), "foo");
        upnpService.getRegistry().addResource(resource, 2);

        assertEquals(upnpService.getRegistry().getResources().size(), 1);

        Thread.sleep(4000);

        assertEquals(upnpService.getRegistry().getResources().size(), 0);

        upnpService.shutdown();
    }

    @Test
    public void addResourceAndMaintain() throws Exception {

        MockUpnpService upnpService = new MockUpnpService(false, new MockUpnpServiceConfiguration(true, false){
            @Override
            public int getRegistryMaintenanceIntervalMillis() {
                return getDesktopPlatformUpnpServiceConfiguration().getRegistryMaintenanceIntervalMillis();
            }
        });

        final TestRunnable testRunnable = new TestRunnable();

        Resource<String> resource = new Resource<>(URI.create("/this/is/a/test"), "foo") {
            @Override
            public void maintain(List<Runnable> pendingExecutions, ExpirationDetails expirationDetails) {
                if (expirationDetails.getSecondsUntilExpiration() == 1) {
                    pendingExecutions.add(testRunnable);
                }
            }
        };
        upnpService.getRegistry().addResource(resource, 2);

        assertEquals(upnpService.getRegistry().getResources().size(), 1);

        Thread.sleep(2000);

		assertTrue(testRunnable.wasExecuted);

        upnpService.shutdown();
    }

    protected static class TestRunnable implements Runnable {
        boolean wasExecuted = false;

        @Override
		public void run() {
            wasExecuted = true;
        }
    }

}
