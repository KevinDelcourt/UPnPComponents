package com.irit.stores;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.meta.RemoteDevice;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URLConnection;
import java.util.function.Consumer;

public class AndroidUpnpServiceStore {

    private static AndroidUpnpService upnpService;

    private static ServiceConnection serviceConnection;

    public static void bindAndroidUpnpService(Context context, final Consumer<UpnpService> callback) {

        serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                upnpService = (AndroidUpnpService) service;

                Timer t = new Timer(4000, new ActionListener() {
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

                callback.accept(upnpService.get());
            }
            public void onServiceDisconnected(ComponentName className) {
                upnpService.get().shutdown();
                upnpService = null;
            }
        };

        context.bindService(
                new Intent(context, AndroidUpnpServiceImpl.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );
    }

    public static void unbindAndroidUpnpService(Activity activity){
        activity.getApplicationContext().unbindService(serviceConnection);
    }

    public static UpnpService getUpnpService(){
        if(upnpService == null){
            return null;
        }
        return upnpService.get();
    }

}