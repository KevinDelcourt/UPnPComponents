package com.irit.stores;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;


public class DesktopUpnpServiceStore {

    private static UpnpService upnpService = null;

    public static UpnpService getUpnpService() {
        if(upnpService == null) {
            upnpService = new UpnpServiceImpl();
            upnpService.getControlPoint().search();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    upnpService.shutdown();
                }
            });
        }
        return upnpService;
    }

}
