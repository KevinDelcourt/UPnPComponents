package com.irit.dependencyinjection;

import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

public class DependencyInjectionClientRegistryListener extends DefaultRegistryListener {

    private DependencyInjectionService dependencyInjectionService;

    public DependencyInjectionClientRegistryListener(DependencyInjectionService dependencyInjectionService){
        this.dependencyInjectionService = dependencyInjectionService;
    }

    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        System.out.println("hey " + device.getIdentity().toString());

        for(RequiredBinding requiredBinding : dependencyInjectionService.getRequired().values()) {
            if(device.getIdentity().getUdn().toString().equals(requiredBinding.getDesiredUDN()) &&
                    device.findService(requiredBinding.getServiceId()) != null) {
                System.out.println("ok " + device);
                requiredBinding.setDevice(device);
            }
        }
    }


    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        System.out.println("bye " + device.getIdentity().toString());

    }
}