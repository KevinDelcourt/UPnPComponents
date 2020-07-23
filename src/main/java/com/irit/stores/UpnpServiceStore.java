package com.irit.stores;

import com.irit.stores.AndroidUpnpServiceStore;
import com.irit.stores.DesktopUpnpServiceStore;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.meta.LocalDevice;

public class UpnpServiceStore {

    public static UpnpService getUpnpService() {
        try {
            Class.forName( "android.content.ServiceConnection" );
            return AndroidUpnpServiceStore.getUpnpService();
        } catch (Exception e) {
            return DesktopUpnpServiceStore.getUpnpService();
        }
    }

    public static void addLocalDevice(LocalDevice localDevice) {
        getUpnpService().getRegistry().addDevice(localDevice);
    }
}
