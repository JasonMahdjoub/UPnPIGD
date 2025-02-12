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
import com.distrimind.upnp.model.meta.Service;
import com.distrimind.upnp.model.meta.StateVariable;
import com.distrimind.upnp.model.types.ServiceId;
import com.distrimind.upnp.model.types.ServiceType;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.Collection;

/**
 * @author Christian Bauer
 */
public abstract class SampleService {

    public abstract ServiceType getServiceType();
    public abstract ServiceId getServiceId();
    public abstract URI getDescriptorURI();
    public abstract URI getControlURI();
    public abstract URI getEventSubscriptionURI();
    public abstract Collection<Action<?>> getActions();
    public abstract Collection<StateVariable<?>> getStateVariables();

    public <S extends Service<?, ?, ?>> S newInstanceLocal(Constructor<S> ctor) {
        try {
            return ctor.newInstance(
                    getServiceType(), getServiceId(),
                    getActions(), getStateVariables()
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public <S extends Service<?, ?, ?>> S newInstanceRemote(Constructor<S> ctor) {
        try {
            return ctor.newInstance(
                    getServiceType(), getServiceId(),
                    getDescriptorURI(), getControlURI(), getEventSubscriptionURI(),
                    getActions(), getStateVariables()
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
