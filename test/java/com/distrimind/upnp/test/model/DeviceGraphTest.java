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

import com.distrimind.upnp.model.meta.LocalDevice;
import com.distrimind.upnp.model.meta.RemoteDevice;
import com.distrimind.upnp.model.meta.Service;
import com.distrimind.upnp.model.types.ServiceType;
import com.distrimind.upnp.test.data.SampleData;
import com.distrimind.upnp.test.data.SampleDeviceEmbeddedOne;
import com.distrimind.upnp.test.data.SampleDeviceEmbeddedTwo;
import com.distrimind.upnp.test.data.SampleDeviceRootLocal;
import org.testng.annotations.Test;

import java.util.Collection;

import static org.testng.Assert.assertEquals;

/**
 * @author Christian Bauer
 */
public class DeviceGraphTest {

    @Test
    public void findRoot() throws Exception {
        LocalDevice<?> ld = SampleData.createLocalDevice();

        LocalDevice<?> root = ld.getEmbeddedDevices().iterator().next().getRoot();
        assertEquals(root.getIdentity().getUdn(), SampleDeviceRootLocal.getRootUDN());

        root = ld.getEmbeddedDevices().iterator().next().getEmbeddedDevices().iterator().next().getRoot();
        assertEquals(root.getIdentity().getUdn(), SampleDeviceRootLocal.getRootUDN());

    }

    @Test
    public void findEmbeddedDevices() throws Exception {
        LocalDevice<?> ld = SampleData.createLocalDevice();

        Collection<? extends LocalDevice<?>> embedded = ld.findEmbeddedDevices();
        assertEquals(embedded.size(), 2);

        boolean haveOne = false;
        boolean haveTwo = false;

        for (LocalDevice<?> em : embedded) {
            if (em.getIdentity().getUdn().equals(ld.getEmbeddedDevices().iterator().next().getIdentity().getUdn())) haveOne = true;
            if (em.getIdentity().getUdn().equals(ld.getEmbeddedDevices().iterator().next().getEmbeddedDevices().iterator().next().getIdentity().getUdn()))
                haveTwo = true;
        }

        assert haveOne;
        assert haveTwo;
    }

    @Test
    public void findDevicesWithUDN() throws Exception {
        LocalDevice<?> ld = SampleData.createLocalDevice();

        LocalDevice<?> ldOne = ld.findDevice(SampleDeviceRootLocal.getRootUDN());
        assertEquals(ldOne.getIdentity().getUdn(), SampleDeviceRootLocal.getRootUDN());

        LocalDevice<?> ldTwo = ld.findDevice(SampleDeviceEmbeddedOne.getEmbeddedOneUDN());
        assertEquals(ldTwo.getIdentity().getUdn(), SampleDeviceEmbeddedOne.getEmbeddedOneUDN());

        LocalDevice<?> ldThree = ld.findDevice(SampleDeviceEmbeddedTwo.getEmbeddedTwoUDN());
        assertEquals(ldThree.getIdentity().getUdn(), SampleDeviceEmbeddedTwo.getEmbeddedTwoUDN());

        RemoteDevice rd = SampleData.createRemoteDevice();

        RemoteDevice rdOne = rd.findDevice(SampleDeviceRootLocal.getRootUDN());
        assertEquals(rdOne.getIdentity().getUdn(), SampleDeviceRootLocal.getRootUDN());

        RemoteDevice rdTwo = rd.findDevice(SampleDeviceEmbeddedOne.getEmbeddedOneUDN());
        assertEquals(rdTwo.getIdentity().getUdn(), SampleDeviceEmbeddedOne.getEmbeddedOneUDN());

        RemoteDevice rdThree = rd.findDevice(SampleDeviceEmbeddedTwo.getEmbeddedTwoUDN());
        assertEquals(rdThree.getIdentity().getUdn(), SampleDeviceEmbeddedTwo.getEmbeddedTwoUDN());

    }

    @Test
    public void findDevicesWithDeviceType() throws Exception {
        LocalDevice<?> ld = SampleData.createLocalDevice();

        Collection<? extends LocalDevice<?>> ldOne = ld.findDevices(ld.getType());
        assertEquals(ldOne.size(), 1);
        assertEquals(ldOne.iterator().next().getIdentity().getUdn(), SampleDeviceRootLocal.getRootUDN());

        Collection<? extends LocalDevice<?>> ldTwo = ld.findDevices(ld.getEmbeddedDevices().iterator().next().getType());
        assertEquals(ldTwo.size(), 1);
        assertEquals(ldTwo.iterator().next().getIdentity().getUdn(), SampleDeviceEmbeddedOne.getEmbeddedOneUDN());

        Collection<? extends LocalDevice<?>> ldThree = ld.findDevices(ld.getEmbeddedDevices().iterator().next().getEmbeddedDevices().iterator().next().getType());
        assertEquals(ldThree.size(), 1);
        assertEquals(ldThree.iterator().next().getIdentity().getUdn(), SampleDeviceEmbeddedTwo.getEmbeddedTwoUDN());

        RemoteDevice rd = SampleData.createRemoteDevice();

        Collection<RemoteDevice> rdOne = rd.findDevices(rd.getType());
        assertEquals(rdOne.size(), 1);
        assertEquals(rdOne.iterator().next().getIdentity().getUdn(), SampleDeviceRootLocal.getRootUDN());

        Collection<RemoteDevice> rdTwo = rd.findDevices(rd.getEmbeddedDevices().iterator().next().getType());
        assertEquals(rdTwo.size(), 1);
        assertEquals(rdTwo.iterator().next().getIdentity().getUdn(), SampleDeviceEmbeddedOne.getEmbeddedOneUDN());

        Collection<RemoteDevice> rdThree = rd.findDevices(rd.getEmbeddedDevices().iterator().next().getEmbeddedDevices().iterator().next().getType());
        assertEquals(rdThree.size(), 1);
        assertEquals(rdThree.iterator().next().getIdentity().getUdn(), SampleDeviceEmbeddedTwo.getEmbeddedTwoUDN());

    }

    @Test
    public void findServicesAll() throws Exception {
        LocalDevice<?> ld = SampleData.createLocalDevice();

        Service<?, ?, ?> one = ld.getServices().iterator().next();
        Service<?, ?, ?> two = ld.getEmbeddedDevices().iterator().next().getServices().iterator().next();
        Service<?, ?, ?> three = ld.getEmbeddedDevices().iterator().next().getEmbeddedDevices().get(0).getServices().get(0);

        Collection<? extends Service<?, ?, ?>> services = ld.findServices();

        boolean haveOne = false;
        boolean haveTwo = false;
        boolean haveThree = false;
        for (Service<?, ?, ?> service : services) {
            if (service.getServiceId().equals(one.getServiceId())) haveOne = true;
            if (service.getServiceId().equals(two.getServiceId())) haveTwo = true;
            if (service.getServiceId().equals(three.getServiceId())) haveThree = true;
        }
        assert haveOne;
        assert haveTwo;
        assert haveThree;
    }

    @Test
    public void findServicesType() throws Exception {
        LocalDevice<?> ld = SampleData.createLocalDevice();

        Service<?, ?, ?> one = ld.getServices().get(0);
        Service<?, ?, ?> two = ld.getEmbeddedDevices().get(0).getServices().get(0);
        Service<?, ?, ?> three = ld.getEmbeddedDevices().get(0).getEmbeddedDevices().get(0).getServices().get(0);

        Collection<? extends Service<?, ?, ?>> services = ld.findServices(one.getServiceType());
        assertEquals(services.size(), 1);
        assertEquals(services.iterator().next().getServiceId(), one.getServiceId());

        services = ld.findServices(two.getServiceType());
        assertEquals(services.size(), 1);
        assertEquals(services.iterator().next().getServiceId(), two.getServiceId());

        services = ld.findServices(three.getServiceType());
        assertEquals(services.size(), 1);
        assertEquals(services.iterator().next().getServiceId(), three.getServiceId());
    }

    @Test
    public void findServicesId() throws Exception {
        LocalDevice<?> ld = SampleData.createLocalDevice();

        Service<?, ?, ?> one = ld.getServices().iterator().next();
        Service<?, ?, ?> two = ld.getEmbeddedDevices().get(0).getServices().get(0);
        Service<?, ?, ?> three = ld.getEmbeddedDevices().get(0).getEmbeddedDevices().get(0).getServices().get(0);

        Service<?, ?, ?> service = ld.findService(one.getServiceId());
        assertEquals(service.getServiceId(), one.getServiceId());

        service = ld.findService(two.getServiceId());
        assertEquals(service.getServiceId(), two.getServiceId());

        service = ld.findService(three.getServiceId());
        assertEquals(service.getServiceId(), three.getServiceId());
    }

    @Test
    public void findServicesFirst() throws Exception {
        LocalDevice<?> ld = SampleData.createLocalDevice();

        Service<?, ?, ?> one = ld.getServices().get(0);
        Service<?, ?, ?> two = ld.getEmbeddedDevices().get(0).getServices().get(0);
        Service<?, ?, ?> three = ld.getEmbeddedDevices().get(0).getEmbeddedDevices().get(0).getServices().get(0);

        Service<?, ?, ?> service = ld.findService(one.getServiceType());
        assertEquals(service.getServiceId(), one.getServiceId());

        service = ld.findService(two.getServiceType());
        assertEquals(service.getServiceId(), two.getServiceId());

        service = ld.findService(three.getServiceType());
        assertEquals(service.getServiceId(), three.getServiceId());
    }

    @Test
    public void findServiceTypes() throws Exception {
        LocalDevice<?> ld = SampleData.createLocalDevice();

        Collection<ServiceType> svcTypes = ld.findServiceTypes();
        assertEquals(svcTypes.size(), 3);

        boolean haveOne = false;
        boolean haveTwo = false;
        boolean haveThree = false;

        for (ServiceType svcType : svcTypes) {
            if (svcType.equals(ld.getServices().get(0).getServiceType())) haveOne = true;
            if (svcType.equals(ld.getEmbeddedDevices().get(0).getServices().get(0).getServiceType())) haveTwo = true;
            if (svcType.equals(ld.getEmbeddedDevices().get(0).getEmbeddedDevices().get(0).getServices().get(0).getServiceType()))
                haveThree = true;
        }

        assert haveOne;
        assert haveTwo;
        assert haveThree;

    }
}
