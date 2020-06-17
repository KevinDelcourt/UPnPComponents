package com.irit;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;

import java.util.function.Consumer;

public class AndroidUpnpServiceStore {

    public static void bindAndroidUpnpService(Activity activity, Consumer<AndroidUpnpService> connectedCallback, Runnable disconnectedCallback) {
        activity.getApplicationContext().bindService(
                new Intent(activity, AndroidUpnpServiceImpl.class),
                new ServiceConnection() {

                    public void onServiceConnected(ComponentName className, IBinder service) {
                        connectedCallback.accept((AndroidUpnpService) service);
                    }
                    public void onServiceDisconnected(ComponentName className) {
                        disconnectedCallback.run();
                    }
                },
                Context.BIND_AUTO_CREATE
        );
    }
}
