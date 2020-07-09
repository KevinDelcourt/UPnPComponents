package com.irit.dependencyinjection;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.Device;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class DependencyInjectionClient implements Runnable {

    private UpnpService upnpService;

    private DependencyInjectionService dependencyInjectionService;

    public DependencyInjectionClient(final UpnpService upnpService, final DependencyInjectionService dependencyInjectionService){
        this.upnpService = upnpService;
        this.dependencyInjectionService = dependencyInjectionService;

        for(RequiredBinding requiredBinding : dependencyInjectionService.getRequired().values()){
            requiredBinding.setUpnpService(upnpService);
            requiredBinding.addDesiredUDNChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    boolean bound = false;
                    for(Device device : upnpService.getRegistry().getDevices()){
                        bindIfDeviceRequired(device, requiredBinding);
                        bound = true;
                    }

                    if(!bound) {
                        requiredBinding.setDevice(null);
                    }
                }
            });
        }
    }

    private void bindIfDeviceRequired(Device device, RequiredBinding requiredBinding){
        if(device.getIdentity().getUdn().toString().equals(requiredBinding.getDesiredUDN()) &&
                device.findService(requiredBinding.getServiceId()) != null) {
            System.out.println("ok " + device);
            requiredBinding.setDevice(device);
        }else{
            for( Device embedded : device.getEmbeddedDevices()){
                bindIfDeviceRequired(embedded, requiredBinding);
            }
        }
    }

    @Override
    public void run() {
        try {
            // Add a listener for device registration events
            upnpService.getRegistry().addListener(
                    new DependencyInjectionClientRegistryListener(dependencyInjectionService)
            );

            upnpService.getControlPoint().search(
                    new STAllHeader()
            );

        } catch (Exception ex) {
            System.err.println("Exception occured: " + ex);
            System.exit(1);
        }
    }

}

