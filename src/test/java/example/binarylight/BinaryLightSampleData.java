package example.binarylight;

import com.distrimind.upnp_igd.binding.annotations.AnnotationLocalServiceBinder;
import com.distrimind.upnp_igd.model.meta.DeviceDetails;
import com.distrimind.upnp_igd.model.meta.LocalDevice;
import com.distrimind.upnp_igd.model.meta.LocalService;
import com.distrimind.upnp_igd.model.types.UDADeviceType;
import org.fourthline.cling.test.data.SampleData;

/**
 * @author Christian Bauer
 */
public class BinaryLightSampleData {

    public static LocalDevice createDevice(Class<?> serviceClass) throws Exception {
        return createDevice(
                SampleData.readService(
                        new AnnotationLocalServiceBinder(),
                        serviceClass
                )
        );
    }

    public static LocalDevice createDevice(LocalService service) throws Exception {
        return new LocalDevice(
                SampleData.createLocalDeviceIdentity(),
                new UDADeviceType("BinaryLight", 1),
                new DeviceDetails("Example Binary Light"),
                service
        );
    }

}
