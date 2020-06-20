package com.irit;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;

import java.util.function.Consumer;

public class AndroidUpnpServiceStore {

    private static AndroidUpnpService upnpService;

    private static ServiceConnection serviceConnection;

    public static void bindAndroidUpnpService(Activity activity, final Consumer<UpnpService> callback) {

        serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                upnpService = (AndroidUpnpService) service;
                callback.accept(upnpService.get());
            }
            public void onServiceDisconnected(ComponentName className) {
                upnpService.get().shutdown();
                upnpService = null;
            }
        };

        activity.getApplicationContext().bindService(
                new Intent(activity, AndroidUpnpServiceImpl.class),
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