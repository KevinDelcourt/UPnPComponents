# UPnP Components Library

The goal of this library is to provide a lightweight API to build UPnP components that can be used by [OCE](https://github.com/SylvieTrouilhet/OCE). This library is an extension of the [Cling library](http://4thline.org/projects/cling/core/manual/cling-core-manual.xhtml) and is designed to be used on Android as well as on desktop OS.

This library requires the use of [JAVA 8](https://www.java.com/en/download/faq/java8.xml).

The following projects currently use this library :
- https://github.com/KevinDelcourt/DesktopUpnpComponents
- https://github.com/KevinDelcourt/UPnPAndroidComponents
- https://github.com/KevinDelcourt/UPnPAndroidMap
- https://github.com/KevinDelcourt/UPnPAndroidGPS

This file describes how to use the library to create a component on desktop using Maven or on Android using Gradle.

## Desktop - Maven

### 1. Project set up

In your `pom.xml` file, first add the necessary repositories :

```xml
<repositories>
    <!-- ... stuff ... -->
    <repository>
          <id>4thline-repo</id>
          <url>http://4thline.org/m2</url>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
    </repository>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
    <!-- ... stuff ... -->
</repositories>
```

Then add the dependency :
```xml
<dependencies>
    <!-- ... stuff ... -->
    <dependency>
        <groupId>com.github.KevinDelcourt</groupId>
        <artifactId>UPnPComponents</artifactId>
        <version>1.0</version>
    </dependency>
    <!-- ... stuff ... -->   
</dependencies>
```

For this example, we will create a component that provides and requires a service that consists in a setter and a getter.

### 2. Providing a service

The java interface of our service is the following :
```java
public interface MyService {

    String getVar();
    void setVar(String var);

}
```

First, we create the class describing the provided service implementation. Implementing a service relies on the annotations added by the Cling library, please refer to their [user manual](http://4thline.org/projects/cling/core/manual/cling-core-manual.xhtml) for further information on those.

```java
@UpnpService(
        serviceId = @UpnpServiceId("MyService"),
        serviceType = @UpnpServiceType(value = "MyService", version = 1)
)
public class MyServiceImpl implements MyService {
    
    @UpnpStateVariable
    private String var;
    
    @UpnpAction(name = "GetVar", out = @UpnpOutputArgument(name = "Var"))
    public String getVar() {
        System.out.println("Returning var : " + var);
        return var;
    }
    
    @UpnpAction(name = "SetVar")
    public void setVar(@UpnpInputArgument(name = "Var") String var) {
        this.var = var;
        System.out.println("New var : " + var);
    }
}
```

Then, we can create the component that provides this service.

```java
public class MyComponent implements Runnable {

    private static final int VERSION = 1;
    
    public static void main(String[] args) {
        Thread serverThread = new Thread(new MyComponent());
        serverThread.setDaemon(false);
        serverThread.start();
    }

    @Override
    public void run() {
        //This will create the service instance
        LocalService<MyServiceImpl> myServiceLocalService = ServiceFactory.makeLocalServiceFrom(MyServiceImpl.class);

        //If you need to access the actual MyServiceImpl object (for whatever purpose), use the following :
        MyServiceImpl realImpl = myServiceLocalService.getManager().getImplementation();
        realImpl.setVar("Some value");

        //This will create the component (=device) instance
        LocalDevice device = DeviceFactory.makeLocalDevice(
                 "MyComponent",
                 "Do amazing stuff",
                 VERSION,
                 "My organisation",
                 new LocalService[]{
                         myServiceLocalService
                 }
         );
        
        //this will make the component available on the network
        UpnpServiceStore.addLocalDevice(device);
    }    

}
```

At this point, if you run the application, the component will be made available to the network to be used by other components or a composition engine.

### 3. Requiring a service

Now we want that when `setVar` or `getVar` is called, our component calls the `setVar` or `getVar` of a bound component providing the `MyService` interface.

To do that, we need to declare and provide a `DependencyInjectionService`, a special service made to be used by a composition engine, that allows our component to declare and execute actions on required services.

First, we need to edit `MyComponent` :

```java
public class MyComponent implements Runnable {

    public static final String MY_SERVICE = "MyService";

    private static final int VERSION = 1;
    
    public static void main(String[] args) {
        Thread serverThread = new Thread(new MyComponent());
        serverThread.setDaemon(false);
        serverThread.start();
    }

    @Override
    public void run() {
        //This map is used to declare the required services our component need.
        Map<String, ServiceId> requiredServices = new HashMap<>();
        requiredServices.put(MY_SERVICE, new UDAServiceId(MY_SERVICE));

        //This will create the dependency injection service
        LocalService<DependencyInjectionService> dependencyInjectionLocalService = ServiceFactory.makeDependencyInjectionService(requiredServices);
        
        LocalService<MyServiceImpl> myServiceLocalService = ServiceFactory.makeLocalServiceFrom(MyServiceImpl.class);

        //Now we need to pass down the instance of the dependency injection service if we want to use it later
        MyServiceImpl realImpl = myServiceLocalService.getManager().getImplementation();
        realImpl.setDependencyInjectionService(dependencyInjectionLocalService.getManager().getImplementation());

        LocalDevice device = DeviceFactory.makeLocalDevice(
                 "MyComponent",
                 "Do amazing stuff",
                 VERSION,
                 "My organisation",
                 new LocalService[]{
                        myServiceLocalService,
                        dependencyInjectionLocalService
                 }
         );
        
        UpnpServiceStore.addLocalDevice(device);
    }    
}
```

We've modified the `MyServiceImpl` class, here it is :
```java
@UpnpService(
        serviceId = @UpnpServiceId("MyService"),
        serviceType = @UpnpServiceType(value = "MyService", version = 1)
)
public class MyServiceImpl implements MyService {
    
    @UpnpStateVariable
    private String var;
    
    private DependencyInjectionService dependencyInjectionService;

    @UpnpAction(name = "GetVar", out = @UpnpOutputArgument(name = "Var"))
    public String getVar() {
        System.out.println("My var is : " + var);
        return var;

        //Here we call getVar on our required service.
        //The first parameter is the name of the action we call
        //The second parameter is a map containing the arguments for that action (here, none)
        //The third parameter (optional) is the callback called when a response is received from the required component.
        dependencyInjectionService.getRequired().get(MY_SERVICE).execute(
            "GetVar",
            new HashMap<>(),
            new Consumer<ActionInvocation>() {
                @Override
                public void accept(ActionInvocation actionInvocation) {
                    System.out.println("The var of my required is : " + actionInvocation.getOutput("Var").toString());
                }
            }
        );
    }
    
    @UpnpAction(name = "SetVar")
    public void setVar(@UpnpInputArgument(name = "Var") String var) {
        this.var = var;
        System.out.println("My new var is : " + var);

        //Here we call setVar on our required service
        //The first parameter is the name of the action we call
        //The second parameter is a map containing the arguments for that action (here, the "Var" argument takes the value of the var variable)
        dependencyInjectionService.getRequired().get(MY_SERVICE).execute(
            "SetVar",
            map.of("Var", var)
        );
    }

    public void setDependencyInjectionService(DependencyInjectionService dependencyInjectionService) {
        this.dependencyInjectionService = dependencyInjectionService;
    }
}
```

Now if we call those actions on our component, it will try to call them on its required component too. Note that nothing happens if there is no component bound to provide our component's required service.

## Android - Gradle

We will now follow the same example on a AndroidStudio java project while focusing on the differences with the above example.

### 1. Project set up

First, in your application's `build.gradle` file, add the necessary repositories and declare the dependency :

```
repositories {
    maven {
        url "http://4thline.org/m2"
    }
    maven {
        url 'https://jitpack.io'
    }
}

dependencies {
    //...All your other dependencies...
    
    implementation 'com.github.KevinDelcourt:UPnPComponents:1.0'
}
```

Then, in your `AndroidManifest.xml` file, add the following permissions, those are necessary because UPnP components need to be available on the local network :

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
<uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.WAKE_LOCK"/>
```

In the same file, in your application details, add the following :

```xml
<application
    android:usesCleartextTraffic="true"
    >

    <!--
        ...
         Stuff
        ...
    -->

    <service android:name="org.fourthline.cling.android.AndroidUpnpServiceImpl"/>
</application>
```

The `usesCleartextTraffic="true"` is necessary only if your component have a required service whereas the service declaration is mandatory as the UPnP service needs to be declared.

### 2. Providing a service

Before any service or component instantiation, we need to bind the UPnP service to our app, so in your main activity, add the following :

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    //Other creation code
    AndroidUpnpServiceStore.bindAndroidUpnpService(
        this,
        new Consumer<UpnpService>() {
            @Override
            public void accept(UpnpService upnpService) {
                //You can create and use the upnpService after this method is executed
                Toast.makeText(getActivity(), "Service bound", Toast.LENGTH_SHORT).show();
            }
        }
    );
}
```

After this, the component and service declaration part is the same as for desktop.

### 3. Requiring a service

This part is the same as for desktop.
