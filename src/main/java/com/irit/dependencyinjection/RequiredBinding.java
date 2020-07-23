package com.irit.dependencyinjection;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.model.types.ServiceId;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.function.Consumer;

public class RequiredBinding {

    private final PropertyChangeSupport propertyChangeSupport;

    private String name;

    private ServiceId serviceId;

    private String desiredUDN;

    private Device device;

    private UpnpService upnpService;

    public RequiredBinding(String name, ServiceId serviceId) {
        propertyChangeSupport = new PropertyChangeSupport(this);
        this.name = name;
        this.serviceId = serviceId;
    }

    public void execute(final String actionName, Map<String,Object> arguments){
        execute(
                actionName,
                arguments,
                new Consumer<ActionInvocation>() {
                    @Override
                    public void accept(ActionInvocation actionInvocation) {
                        System.out.println("Called " + actionName + " successfully.");
                    }
                }
        );
    }

    public void execute(String actionName, Map<String,Object> arguments, final Consumer<ActionInvocation> onSuccess){
        if(device == null){
            System.out.println("Can't execute '" + actionName + "' because there is no device bound to " + name);
            return;
        }

        ActionInvocation actionInvocation = new ActionInvocation(device.findService(serviceId).getAction(actionName));
        try {
            for(String argName : arguments.keySet()){
                actionInvocation.setInput(argName, arguments.get(argName));
            }

            upnpService.getControlPoint().execute(new ActionCallback(actionInvocation) {

                @Override
                public void success(ActionInvocation actionInvocation) {
                    onSuccess.accept(actionInvocation);
                }

                @Override
                public void failure(ActionInvocation invocation,
                                    UpnpResponse operation,
                                    String defaultMsg) {
                    System.err.println(defaultMsg);
                }
            });
        } catch (InvalidValueException ex) {
            System.err.println(ex.getMessage());
        }
    }

    public void addDesiredUDNChangeListener(PropertyChangeListener propertyChangeListener){
        propertyChangeSupport.addPropertyChangeListener("desiredUDN", propertyChangeListener);
    }

    public String getDesiredUDN() {
        return desiredUDN;
    }

    public void setDesiredUDN(String desiredUDN) {
        String oldValue = this.desiredUDN;
        this.desiredUDN = desiredUDN;

        propertyChangeSupport.firePropertyChange("desiredUDN", oldValue, desiredUDN);
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public String getName() {
        return name;
    }

    public ServiceId getServiceId() {
        return serviceId;
    }

    public UpnpService getUpnpService() {
        return upnpService;
    }

    public void setUpnpService(UpnpService upnpService) {
        this.upnpService = upnpService;
    }
}
