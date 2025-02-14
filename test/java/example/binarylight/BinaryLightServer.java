package example.binarylight;

import com.distrimind.flexilogxml.concurrent.ThreadType;
import com.distrimind.upnp.binding.LocalServiceBindingException;
import com.distrimind.upnp.binding.annotations.AnnotationLocalServiceBinder;
import com.distrimind.upnp.model.DefaultServiceManager;
import com.distrimind.upnp.model.ValidationException;
import com.distrimind.upnp.model.meta.*;
import com.distrimind.upnp.model.types.DeviceType;
import com.distrimind.upnp.model.types.UDADeviceType;
import com.distrimind.upnp.model.types.UDN;
import com.distrimind.upnp.UpnpService;
import com.distrimind.upnp.UpnpServiceImpl;

import java.io.IOException;

@SuppressWarnings("PMD")
public class BinaryLightServer implements Runnable {

    public static void main(String[] args) throws Exception {
        // Start a user thread that runs the UPnP stack
        Thread serverThread = ThreadType.VIRTUAL_THREAD_IF_AVAILABLE.startThread(new BinaryLightServer());
        serverThread.setDaemon(false);
        serverThread.start();
    }

    @Override
	public void run() {
        try {

            final UpnpService upnpService = new UpnpServiceImpl();

            Runtime.getRuntime().addShutdownHook(ThreadType.VIRTUAL_THREAD_IF_AVAILABLE.newThreadFactoryInstance().newThread(upnpService::shutdown));

            // Add the bound local device to the registry
            upnpService.getRegistry().addDevice(
                    createDevice()
            );

        } catch (Exception ex) {
            System.err.println("Exception occured: " + ex);
            ex.printStackTrace(System.err);
            System.exit(1);
        }
    }

    // DOC: CREATEDEVICE
    LocalDevice<SwitchPower> createDevice()
            throws ValidationException, LocalServiceBindingException, IOException {

        DeviceIdentity identity =
                new DeviceIdentity(
                        UDN.uniqueSystemIdentifier("Demo Binary Light")
                );

        DeviceType type =
                new UDADeviceType("BinaryLight", 1);

        DeviceDetails details =
                new DeviceDetails(
                        "Friendly Binary Light",
                        new ManufacturerDetails("ACME"),
                        new ModelDetails(
                                "BinLight2000",
                                "A demo light with on/off switch.",
                                "v1"
                        )
                );

        Icon icon =
                new Icon(
                        "image/png", 48, 48, 8,
                        "icon.png",
                        getClass()
                );

        LocalService<SwitchPower> switchPowerService =
                new AnnotationLocalServiceBinder().read(SwitchPower.class);

        switchPowerService.setManager(
                new DefaultServiceManager<>(switchPowerService, SwitchPower.class)
        );

        return new LocalDevice<>(identity, type, details, icon, switchPowerService);

        /* Several services can be bound to the same device:
        return new LocalDevice(
                identity, type, details, icon,
                new LocalService[] {switchPowerService, myOtherService}
        );
        */
        
    }
    // DOC: CREATEDEVICE
}
