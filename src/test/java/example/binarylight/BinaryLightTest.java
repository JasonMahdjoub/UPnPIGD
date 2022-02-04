package example.binarylight;

import com.distrimind.upnp_igd.mock.MockUpnpService;
import com.distrimind.upnp_igd.model.meta.LocalDevice;
import com.distrimind.upnp_igd.model.meta.LocalService;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Christian Bauer
 */
public class BinaryLightTest {

    @Test
    public void testServer() throws Exception {
        LocalDevice binaryLight = new BinaryLightServer().createDevice();
        assertEquals(binaryLight.getServices()[0].getAction("SetTarget").getName(), "SetTarget");
    }

    @Test
    public void testClient() throws Exception {
        // Well we can't really test the listener easily, but the action invocation should work on a local device

        MockUpnpService upnpService = new MockUpnpService();

        BinaryLightClient client = new BinaryLightClient();
        LocalDevice binaryLight = new BinaryLightServer().createDevice();

        LocalService<SwitchPower> service = binaryLight.getServices()[0];
        client.executeAction(upnpService, binaryLight.getServices()[0]);
        Thread.sleep(100);
        assertEquals(service.getManager().getImplementation().getStatus(), true);
    }

}
