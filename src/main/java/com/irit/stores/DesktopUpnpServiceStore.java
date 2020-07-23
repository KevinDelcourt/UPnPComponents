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

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        try {
                            upnpService.getControlPoint().search(3);
                            for(RemoteDevice remoteDevice : upnpService.getRegistry().getRemoteDevices()) {
                                try {
                                    URLConnection connection = remoteDevice.getIdentity().getDescriptorURL().openConnection();
                                    connection.getContent();
                                } catch (IOException e) {
                                    upnpService.getRegistry().removeDevice(remoteDevice);
                                }
                            }

                            Thread.sleep(4000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
        return upnpService;
    }

}
