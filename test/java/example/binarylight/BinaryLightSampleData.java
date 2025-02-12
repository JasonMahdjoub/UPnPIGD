package example.binarylight;

import com.distrimind.upnp.binding.annotations.AnnotationLocalServiceBinder;
import com.distrimind.upnp.model.meta.DeviceDetails;
import com.distrimind.upnp.model.meta.LocalDevice;
import com.distrimind.upnp.model.meta.LocalService;
import com.distrimind.upnp.model.types.UDADeviceType;
import com.distrimind.upnp.test.data.SampleData;

/**
 * @author Christian Bauer
 */
public class BinaryLightSampleData {

    public static <T> LocalDevice<T> createDevice(Class<T> serviceClass) throws Exception {
        return createDevice(
                SampleData.readService(
                        new AnnotationLocalServiceBinder(),
                        serviceClass
                )
        );
    }

    public static <T> LocalDevice<T> createDevice(LocalService<T> service) throws Exception {
        return new LocalDevice<>(
                SampleData.createLocalDeviceIdentity(),
                new UDADeviceType("BinaryLight", 1),
                new DeviceDetails("Example Binary Light"),
                service
        );
    }

}
