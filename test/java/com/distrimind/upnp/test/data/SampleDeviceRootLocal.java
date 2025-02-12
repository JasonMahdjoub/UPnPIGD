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

import com.distrimind.upnp.model.meta.Service;
import com.distrimind.upnp.model.resource.DeviceDescriptorResource;
import com.distrimind.upnp.model.resource.IconResource;
import com.distrimind.upnp.model.resource.ServiceControlResource;
import com.distrimind.upnp.model.resource.ServiceDescriptorResource;
import com.distrimind.upnp.model.resource.ServiceEventSubscriptionResource;
import com.distrimind.upnp.model.resource.Resource;
import com.distrimind.upnp.model.meta.Device;
import com.distrimind.upnp.model.meta.DeviceIdentity;

import java.net.URI;
import java.util.Collection;
import java.util.Objects;

import static org.testng.Assert.assertEquals;

/**
 * @author Christian Bauer
 */
public class SampleDeviceRootLocal<D extends Device<?, D, S>, S extends Service<?, D, S>> extends SampleDeviceRoot<D,S> {

    public SampleDeviceRootLocal(DeviceIdentity identity, S service, D embeddedDevice) {
        super(identity, service, embeddedDevice);
    }

    public static void assertLocalResourcesMatch(Collection<Resource<?>> resources){
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-123/desc"))).getClass(),
                DeviceDescriptorResource.class
        );
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-123/icon.png"))).getClass(),
                IconResource.class
        );
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-123/icon2.png"))).getClass(),
                IconResource.class
        );
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-123/svc/upnp-org/MY-SERVICE-123/desc"))).getClass(),
                ServiceDescriptorResource.class
        );
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-123/svc/upnp-org/MY-SERVICE-123/action"))).getClass(),
                ServiceControlResource.class
        );
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-123/svc/upnp-org/MY-SERVICE-123/event"))).getClass(),
                ServiceEventSubscriptionResource.class
        );
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-456/icon3.png"))).getClass(),
                IconResource.class
        );
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-456/svc/upnp-org/MY-SERVICE-456/desc"))).getClass(),
                ServiceDescriptorResource.class
        );
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-456/svc/upnp-org/MY-SERVICE-456/action"))).getClass(),
                ServiceControlResource.class
        );
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-456/svc/upnp-org/MY-SERVICE-456/event"))).getClass(),
                ServiceEventSubscriptionResource.class
        );
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-789/svc/upnp-org/MY-SERVICE-789/desc"))).getClass(),
                ServiceDescriptorResource.class
        );
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-789/svc/upnp-org/MY-SERVICE-789/action"))).getClass(),
                ServiceControlResource.class
        );
        assertEquals(
                Objects.requireNonNull(getLocalResource(resources, URI.create("/dev/MY-DEVICE-789/svc/upnp-org/MY-SERVICE-789/event"))).getClass(),
                ServiceEventSubscriptionResource.class
        );

    }

}
