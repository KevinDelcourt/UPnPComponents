package com.irit.stores;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.meta.RemoteDevice;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URLConnection;

public class DesktopUpnpServiceStore {

    private static UpnpService upnpService = null;

    public static UpnpService getUpnpService() {
        if(upnpService == null) {
            upnpService = new UpnpServiceImpl();

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    upnpService.shutdown();
                }
            });

            upnpService.getControlPoint().search(3);

        }
        return upnpService;
    }

}
