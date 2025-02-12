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

package com.distrimind.upnp;

import com.distrimind.upnp.model.message.header.STAllHeader;
import com.distrimind.upnp.model.meta.LocalDevice;
import com.distrimind.upnp.model.meta.RemoteDevice;
import com.distrimind.upnp.registry.Registry;
import com.distrimind.upnp.registry.RegistryListener;

/**
 * Runs a simple UPnP discovery procedure.
 */
public class Main {

    @SuppressWarnings("PMD.SystemPrintln")
    public static void main(String[] args) throws Exception {

        // UPnP discovery is asynchronous, we need a callback
        RegistryListener listener = new RegistryListener() {

            @Override
            public void remoteDeviceDiscoveryStarted(Registry registry,
                                                     RemoteDevice device) {
                System.out.println(
                        "Discovery started: " + device.getDisplayString()
                );
            }

            @Override
            public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
                System.out.println(
                        "Discovery failed: " + device.getDisplayString() + " => " + ex
                );
            }

            @Override
            public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
                System.out.println(
                        "Remote device available: " + device.getDisplayString()
                );
            }

            @Override
            public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
                System.out.println(
                        "Remote device updated: " + device.getDisplayString()
                );
            }

            @Override
            public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
                System.out.println(
                        "Remote device removed: " + device.getDisplayString()
                );
            }

            @Override
            public void localDeviceAdded(Registry registry, LocalDevice<?> device) {
                System.out.println(
                        "Local device added: " + device.getDisplayString()
                );
            }

            @Override
            public void localDeviceRemoved(Registry registry, LocalDevice<?> device) {
                System.out.println(
                        "Local device removed: " + device.getDisplayString()
                );
            }

            @Override
            public void beforeShutdown(Registry registry) {
                System.out.println(
                        "Before shutdown, the registry has devices: " + registry.getDevices().size()
                );
            }

            @Override
            public void afterShutdown() {
                System.out.println("Shutdown of registry complete!");

            }
        };

        // This will create necessary network resources for UPnP right away
        System.out.println("Starting UPnPIGD...");
        UpnpService upnpService = new UpnpServiceImpl(listener);

        // Send a search message to all devices and services, they should respond soon
        System.out.println("Sending SEARCH message to all devices...");
        upnpService.getControlPoint().search(new STAllHeader());

        // Let's wait 10 seconds for them to respond
        System.out.println("Waiting 10 seconds before shutting down...");
        Thread.sleep(10000);

        // Release all resources and advertise BYEBYE to other UPnP devices
        System.out.println("Stopping UPnPIGD...");
        upnpService.shutdown();
    }
}

