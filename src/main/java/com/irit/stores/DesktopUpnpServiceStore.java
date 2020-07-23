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

            Timer t = new Timer(3000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    upnpService.getControlPoint().search(3);
                    for(RemoteDevice remoteDevice : upnpService.getRegistry().getRemoteDevices()) {
                        try {
                            URLConnection connection = remoteDevice.getIdentity().getDescriptorURL().openConnection();
                            connection.getContent();
                        } catch (IOException e) {
                            upnpService.getRegistry().removeDevice(remoteDevice);
                        }
                    }
                }
            });
            t.start();
        }
        return upnpService;
    }

}
