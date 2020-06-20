package com.irit;

import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;

import java.util.UUID;

public class DeviceFactory {

    public static LocalDevice makeLocalDevice(
            String deviceName,
            String description,
            int versionNumber,
            String manufacturer,
            LocalService[] services
    ) throws ValidationException {
        UDN udn;
        try{
            udn = UDN.uniqueSystemIdentifier(deviceName);
        }catch (RuntimeException e){
            udn = new UDN(UUID.randomUUID());
        }

        return new LocalDevice(
                new DeviceIdentity(
                        udn
                ),
                new UDADeviceType(deviceName,versionNumber),
                new DeviceDetails(
                        deviceName,
                        new ManufacturerDetails(manufacturer),
                        new ModelDetails(
                                deviceName,
                                description,
                                "v" + versionNumber
                        )
                ),
                services
        );
    }
}

