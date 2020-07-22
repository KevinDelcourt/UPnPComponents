package com.irit.dependencyinjection;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.RemoteDevice;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URLConnection;

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
                        bound = bound || bindIfDeviceRequired(device, requiredBinding);
                    }

                    if(!bound) {
                        requiredBinding.setDevice(null);
                    }
                }
            });
        }
    }

    private boolean bindIfDeviceRequired(Device device, RequiredBinding requiredBinding){
        if(device.getIdentity().getUdn().toString().equals(requiredBinding.getDesiredUDN()) &&
                device.findService(requiredBinding.getServiceId()) != null) {
            System.out.println("ok " + device);
            requiredBinding.setDevice(device);
            return true;
        }

        boolean bound = false;
        for( Device embedded : device.getEmbeddedDevices()){
            bound = bound || bindIfDeviceRequired(embedded, requiredBinding);
        }

        return bound;
    }

    @Override
    public void run() {
        try {
            // Add a listener for device registration events
            upnpService.getRegistry().addListener(
                    new DependencyInjectionClientRegistryListener(dependencyInjectionService)
            );

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

        } catch (Exception ex) {
            System.err.println("Exception occured: " + ex);
            System.exit(1);
        }
    }

}

