package com.irit.dependencyinjection;

import org.fourthline.cling.binding.annotations.*;
import org.fourthline.cling.model.types.ServiceId;

import java.util.HashMap;
import java.util.Map;

@UpnpService(
        serviceId = @UpnpServiceId("DependencyInjectionService"),
        serviceType = @UpnpServiceType(value = "DependencyInjectionService", version = 1)
)
public class DependencyInjectionService {

    private Map<String, RequiredBinding> required = new HashMap<>();

    @UpnpStateVariable(name = "Required")
    private String Required;

    public Map<String, RequiredBinding> getRequired(){
        return required;
    }

    public void init(Map<String, ServiceId> requiredServicesNamesAndServicesId, org.fourthline.cling.UpnpService upnpService){
        for(String name : requiredServicesNamesAndServicesId.keySet()){
            required.put(
                    name,
                    new RequiredBinding(
                            name,
                            requiredServicesNamesAndServicesId.get(name)
                    )
            );
        }

        Thread clientThread = new Thread(new DependencyInjectionClient(upnpService,this));
        clientThread.setDaemon(false);
        clientThread.start();
    }

    @UpnpAction(
            out = @UpnpOutputArgument(
                    name = "RequiredServiceDescription",
                    stateVariable = "Required"
            )
    )
    public String getRequiredServicesDescription(){
        StringBuilder result = new StringBuilder();

        for(RequiredBinding requiredBinding : required.values()){
            result.append(requiredBinding.getName())
                    .append(" ")
                    .append(requiredBinding.getServiceId().toString())
                    .append(",");
        }

        result.deleteCharAt(result.length()-1);

        return result.toString();
    }


    @UpnpAction
    public void bindRequiredService(
            @UpnpInputArgument(name = "ServiceName", stateVariable = "Required") String name,
            @UpnpInputArgument(name = "DeviceNumber", stateVariable = "Required") String udn
    ) {

        required.get(name).setDesiredUDN(udn);
    }
}

