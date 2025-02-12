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

import com.distrimind.upnp.model.meta.DeviceDetails;
import com.distrimind.upnp.model.meta.Icon;
import com.distrimind.upnp.model.meta.RemoteDevice;
import com.distrimind.upnp.model.types.UDADeviceType;
import com.distrimind.upnp.test.data.SampleData;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.*;

/**
 * @author Christian Bauer
 */
public class IconTest {

    public static final String FOO = "Foo";

    @Test
    public void validIcons() throws Exception {
        RemoteDevice rd = new RemoteDevice(
            SampleData.createRemoteDeviceIdentity(),
            new UDADeviceType(FOO, 1),
            new DeviceDetails(FOO),
            List.of(
                new Icon(null, 0, 0, 0, URI.create("foo")),
                new Icon("foo/bar", 0, 0, 0, URI.create("foo")),
                new Icon("foo/bar", 123, 456, 0, URI.create("foo"))
            ),
                Collections.emptyList()
        );
        assertEquals(rd.findIcons().size(), 3);
    }

    @Test
    public void invalidIcons() throws Exception {
        RemoteDevice rd = new RemoteDevice(
            SampleData.createRemoteDeviceIdentity(),
            new UDADeviceType(FOO, 1),
            new DeviceDetails(FOO),
            List.of(
                new Icon("image/png", 123, 123, 8, URI.create("urn:not_a_URL"))
                    ),
            Collections.emptyList()
        );
        assertEquals(rd.findIcons().size(), 0);
    }
}
