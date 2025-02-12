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

package com.distrimind.upnp.test.resources;

import com.distrimind.upnp.binding.xml.ServiceDescriptorBinder;
import com.distrimind.upnp.mock.MockUpnpService;
import com.distrimind.upnp.model.meta.LocalDevice;
import com.distrimind.upnp.model.meta.LocalService;
import com.distrimind.upnp.model.meta.RemoteService;
import com.distrimind.upnp.model.meta.Service;
import com.distrimind.upnp.model.message.StreamRequestMessage;
import com.distrimind.upnp.model.message.StreamResponseMessage;
import com.distrimind.upnp.model.message.UpnpRequest;
import com.distrimind.upnp.model.message.header.ContentTypeHeader;
import com.distrimind.upnp.model.message.header.HostHeader;
import com.distrimind.upnp.model.message.header.UpnpHeader;
import com.distrimind.upnp.protocol.sync.ReceivingRetrieval;
import com.distrimind.upnp.test.data.SampleData;
import com.distrimind.upnp.test.data.SampleServiceOne;
import org.testng.annotations.Test;

import java.net.URI;

import static org.testng.Assert.*;


public class ServiceDescriptorRetrievalTest {

    @Test
    public void registerAndRetrieveDescriptor() throws Exception {

        MockUpnpService upnpService = new MockUpnpService();

        // Register a device
        LocalDevice<?> localDevice = SampleData.createLocalDevice();
        LocalService<?> service = SampleData.getFirstService(localDevice);
        upnpService.getRegistry().addDevice(localDevice);

        // Retrieve the descriptor
        URI descriptorURI = upnpService.getConfiguration().getNamespace().getDescriptorPath(service);
        StreamRequestMessage descRetrievalMessage = new StreamRequestMessage(UpnpRequest.Method.GET, descriptorURI);
        descRetrievalMessage.getHeaders().add(UpnpHeader.Type.HOST, new HostHeader("localhost", 1234));
        ReceivingRetrieval prot = new ReceivingRetrieval(upnpService, descRetrievalMessage);
        prot.run();
        StreamResponseMessage descriptorMessage = prot.getOutputMessage();

        // UDA 1.0 spec days this musst be 'text/xml'
        assertEquals(
                descriptorMessage.getHeaders().getFirstHeader(UpnpHeader.Type.CONTENT_TYPE).getValue(),
                ContentTypeHeader.DEFAULT_CONTENT_TYPE
        );

        // Read the response and compare the returned device descriptor
        ServiceDescriptorBinder binder = upnpService.getConfiguration().getServiceDescriptorBinderUDA10();

        RemoteService remoteService = SampleData.createUndescribedRemoteService();

        remoteService = binder.describe(remoteService, descriptorMessage.getBodyString());
        SampleServiceOne.assertMatch(remoteService, service);
    }

    @Test
    public void retrieveNonExistentDescriptor() throws Exception {

        MockUpnpService upnpService = new MockUpnpService();

        // Retrieve the descriptor
        LocalDevice<?> localDevice = SampleData.createLocalDevice();
        Service<?, ?, ?> service = SampleData.getFirstService(localDevice);

        URI descriptorURI = upnpService.getConfiguration().getNamespace().getDescriptorPath(service);
        StreamRequestMessage descRetrievalMessage =
                new StreamRequestMessage(UpnpRequest.Method.GET, descriptorURI);
        descRetrievalMessage.getHeaders().add(UpnpHeader.Type.HOST, new HostHeader("localhost", 1234));
        ReceivingRetrieval prot = new ReceivingRetrieval(upnpService, descRetrievalMessage);
        prot.run();
        StreamResponseMessage descriptorMessage = prot.getOutputMessage();

        // Should be null because it can't be found
        assertNull(descriptorMessage);

    }

}