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

package com.distrimind.upnp.test.gena;

import com.distrimind.upnp.mock.MockUpnpService;
import com.distrimind.upnp.model.Namespace;
import com.distrimind.upnp.model.message.StreamRequestMessage;
import com.distrimind.upnp.model.message.StreamResponseMessage;
import com.distrimind.upnp.model.message.UpnpRequest;
import com.distrimind.upnp.model.message.UpnpResponse;
import com.distrimind.upnp.model.message.gena.OutgoingSubscribeResponseMessage;
import com.distrimind.upnp.model.message.header.CallbackHeader;
import com.distrimind.upnp.model.message.header.EventSequenceHeader;
import com.distrimind.upnp.model.message.header.NTEventHeader;
import com.distrimind.upnp.model.message.header.SubscriptionIdHeader;
import com.distrimind.upnp.model.message.header.TimeoutHeader;
import com.distrimind.upnp.model.message.header.UpnpHeader;
import com.distrimind.upnp.model.meta.LocalDevice;
import com.distrimind.upnp.model.meta.LocalService;
import com.distrimind.upnp.protocol.sync.ReceivingSubscribe;
import com.distrimind.upnp.protocol.sync.ReceivingUnsubscribe;
import com.distrimind.upnp.test.data.SampleData;
import com.distrimind.upnp.util.URIUtil;
import org.testng.annotations.Test;

import java.net.URL;
import java.util.List;

import static org.testng.Assert.assertEquals;


public class IncomingSubscriptionLifecycleTest {

    @Test
    public void subscriptionLifecycle() throws Exception {

        MockUpnpService upnpService = new MockUpnpService();

        // Register local device and its service
        LocalDevice<?> device = GenaSampleData.createTestDevice(GenaSampleData.LocalTestService.class);
        upnpService.getRegistry().addDevice(device);

        Namespace ns = upnpService.getConfiguration().getNamespace();

        LocalService<?> service = SampleData.getFirstService(device);
        URL callbackURL = URIUtil.createAbsoluteURL(
                SampleData.getLocalBaseURL(), ns.getEventCallbackPath(service)
        );


        StreamRequestMessage subscribeRequestMessage =
                new StreamRequestMessage(UpnpRequest.Method.SUBSCRIBE, ns.getEventSubscriptionPath(service));

        subscribeRequestMessage.getHeaders().add(
                UpnpHeader.Type.CALLBACK,
                new CallbackHeader(callbackURL)
        );
        subscribeRequestMessage.getHeaders().add(UpnpHeader.Type.NT, new NTEventHeader());

        ReceivingSubscribe subscribeProt = new ReceivingSubscribe(upnpService, subscribeRequestMessage);
        subscribeProt.run();
        OutgoingSubscribeResponseMessage subscribeResponseMessage = subscribeProt.getOutputMessage();

        assertEquals(subscribeResponseMessage.getOperation().getStatusCode(), UpnpResponse.Status.OK.getStatusCode());
        String subscriptionId = subscribeResponseMessage.getHeaders().getFirstHeader(UpnpHeader.Type.SID, SubscriptionIdHeader.class).getValue();
        assert subscriptionId.startsWith("uuid:");
        assertEquals(subscribeResponseMessage.getHeaders().getFirstHeader(UpnpHeader.Type.TIMEOUT, TimeoutHeader.class).getValue(), Integer.valueOf(1800));
        assertEquals(upnpService.getRegistry().getLocalSubscription(subscriptionId).getActualDurationSeconds(), 1800);

        // Now send the initial event
        subscribeProt.responseSent(subscribeResponseMessage);

        // And immediately "modify" the state of the service, this should result in "concurrent" event messages
        service.getManager().getPropertyChangeSupport().firePropertyChange("Status", false, true);

        StreamRequestMessage unsubscribeRequestMessage =
                new StreamRequestMessage(UpnpRequest.Method.UNSUBSCRIBE, ns.getEventSubscriptionPath(service));
        unsubscribeRequestMessage.getHeaders().add(UpnpHeader.Type.SID, new SubscriptionIdHeader(subscriptionId));

        ReceivingUnsubscribe unsubscribeProt = new ReceivingUnsubscribe(upnpService, unsubscribeRequestMessage);
        unsubscribeProt.run();
        StreamResponseMessage unsubscribeResponseMessage = unsubscribeProt.getOutputMessage();
        assertEquals(unsubscribeResponseMessage.getOperation().getStatusCode(), UpnpResponse.Status.OK.getStatusCode());
        assert(upnpService.getRegistry().getLocalSubscription(subscriptionId) == null);

        List<StreamRequestMessage> sentMessages = upnpService.getRouter().getSentStreamRequestMessages();
        assertEquals(sentMessages.size(), 2);
        assertEquals(
                (sentMessages.get(0).getOperation()).getMethod(),
                UpnpRequest.Method.NOTIFY
        );
        assertEquals(
                (sentMessages.get(1).getOperation()).getMethod(),
                UpnpRequest.Method.NOTIFY
        );
        assertEquals(
                sentMessages.get(0).getHeaders().getFirstHeader(UpnpHeader.Type.SID, SubscriptionIdHeader.class).getValue(),
                subscriptionId
        );
        assertEquals(
                sentMessages.get(1).getHeaders().getFirstHeader(UpnpHeader.Type.SID, SubscriptionIdHeader.class).getValue(),
                subscriptionId
        );
        assertEquals(
                (sentMessages.get(0).getOperation()).getURI().toString(),
                callbackURL.toString()
        );
        assertEquals(
                sentMessages.get(0).getHeaders().getFirstHeader(UpnpHeader.Type.SEQ, EventSequenceHeader.class).getValue().getValue(),
				Long.valueOf(0)
        );
        assertEquals(
                sentMessages.get(1).getHeaders().getFirstHeader(UpnpHeader.Type.SEQ, EventSequenceHeader.class).getValue().getValue(),
				Long.valueOf(1)
        );

    }

    @Test
    public void subscriptionLifecycleFailedResponse() throws Exception {

        MockUpnpService upnpService = new MockUpnpService();

        // Register local device and its service
        LocalDevice<?> device = GenaSampleData.createTestDevice(GenaSampleData.LocalTestService.class);
        upnpService.getRegistry().addDevice(device);

        Namespace ns = upnpService.getConfiguration().getNamespace();

        LocalService<?> service = SampleData.getFirstService(device);
        URL callbackURL = URIUtil.createAbsoluteURL(
                SampleData.getLocalBaseURL(), ns.getEventCallbackPath(service)
        );

        StreamRequestMessage subscribeRequestMessage =
                new StreamRequestMessage(UpnpRequest.Method.SUBSCRIBE, ns.getEventSubscriptionPath(service));

        subscribeRequestMessage.getHeaders().add(
                UpnpHeader.Type.CALLBACK,
                new CallbackHeader(callbackURL)
        );
        subscribeRequestMessage.getHeaders().add(UpnpHeader.Type.NT, new NTEventHeader());

        ReceivingSubscribe subscribeProt = new ReceivingSubscribe(upnpService, subscribeRequestMessage);
        subscribeProt.run();

        // From the response the subsciber _should_ receive, keep the identifier for later
        OutgoingSubscribeResponseMessage subscribeResponseMessage = subscribeProt.getOutputMessage();
        String subscriptionId = subscribeResponseMessage.getHeaders().getFirstHeader(UpnpHeader.Type.SID, SubscriptionIdHeader.class).getValue();

        // Now, instead of passing the successful response to the protocol, we make it think something went wrong
        subscribeProt.responseSent(null);

        // The subscription should be removed from the registry!
        assert upnpService.getRegistry().getLocalSubscription(subscriptionId) == null;

    }
}