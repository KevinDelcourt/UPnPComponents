package com.irit;

import com.irit.dependencyinjection.DependencyInjectionService;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.types.ServiceId;

import java.util.Map;

public class ServiceFactory {

    private static final AnnotationLocalServiceBinder BINDER = new AnnotationLocalServiceBinder();

    public static <T> LocalService<T> makeLocalServiceFrom(Class<T> tClass){
        LocalService<T> localService = BINDER.read(tClass);

        localService.setManager(
                new DefaultServiceManager(localService, tClass)
        );

        return localService;
    }

    public static LocalService<DependencyInjectionService> makeDependencyInjectionService(
            Map<String, ServiceId> requiredServicesMap,
            UpnpService upnpService
    ){
        LocalService<DependencyInjectionService> localService = makeLocalServiceFrom(DependencyInjectionService.class);
        localService.getManager().getImplementation().init(
                requiredServicesMap,
                upnpService
        );

        return localService;
    }
}
