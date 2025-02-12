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

import com.distrimind.upnp.model.meta.Action;
import com.distrimind.upnp.model.meta.StateVariable;
import com.distrimind.upnp.model.types.ServiceId;
import com.distrimind.upnp.model.types.ServiceType;
import com.distrimind.upnp.model.types.UDAServiceId;
import com.distrimind.upnp.model.types.UDAServiceType;
import com.distrimind.upnp.util.URIUtil;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Christian Bauer
 */
public class SampleServiceThree extends SampleService {

    public static URI getThisDescriptorURI() {
        return URI.create("service/upnp-org/MY-SERVICE-789/desc");
    }

    public static URL getDescriptorURL() {
        return URIUtil.createAbsoluteURL(SampleDeviceRoot.getDeviceDescriptorURL(), getThisDescriptorURI());
    }

    public static ServiceId getThisServiceId() {
        return new UDAServiceId("MY-SERVICE-789");
    }

    public static ServiceType getThisServiceType() {
        return new UDAServiceType("MY-SERVICE-TYPE-THREE", 3);
    }

    @Override
    public ServiceType getServiceType() {
        return getThisServiceType();
    }

    @Override
    public ServiceId getServiceId() {
        return getThisServiceId();
    }

    @Override
    public URI getDescriptorURI() {
        return getThisDescriptorURI();
    }

    @Override
    public URI getControlURI() {
        return URI.create("service/upnp-org/MY-SERVICE-789/control");
    }

    @Override
    public URI getEventSubscriptionURI() {
        return URI.create("service/upnp-org/MY-SERVICE-789/events");
    }

    @Override
    public Collection<Action<?>> getActions() {
        return Collections.emptyList();
    }

    @Override
    public Collection<StateVariable<?>> getStateVariables() {
        return Collections.emptyList();
    }

}
