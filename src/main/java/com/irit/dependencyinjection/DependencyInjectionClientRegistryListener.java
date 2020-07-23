package com.irit.dependencyinjection;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import java.io.IOException;

import static com.irit.dependencyinjection.DependencyInjectionClient.bindIfDeviceRequired;

public class DependencyInjectionClientRegistryListener extends DefaultRegistryListener {

    private DependencyInjectionService dependencyInjectionService;

    public DependencyInjectionClientRegistryListener(DependencyInjectionService dependencyInjectionService){
        this.dependencyInjectionService = dependencyInjectionService;
    }

    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        System.out.println("Device added : " + device.getIdentity().toString());

        for(RequiredBinding requiredBinding : dependencyInjectionService.getRequired().values()) {
            try {
                bindIfDeviceRequired(device, requiredBinding);
            } catch (IOException ignored) { }
        }
    }


    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        System.out.println("Device removed : " + device.getIdentity().toString());

    }
}