Reactive Android library to access camera and gallery using RxJava. 

#RxPaparazzo

What is a Paparazzo?

> A freelance photographer who aggressively pursues celebrities for the purpose of taking candid photographs.

This library does that. Mostly. Not really. But it was a funny name, thought. Was it?


##Features: 
- Runtime permissions. Not worries about the new fickle Android runtime permissions system. RxPaparazzo relies on [RxPermissions](https://github.com/tbruyelle/RxPermissions) to deal with that.  
- Crop images. RxPaparazzo depends on [UCrop](https://github.com/Yalantis/uCrop) to perform beautiful cuts to any face, body or place. 
- Take a photo using the built-in camera.
- Access to gallery. 
- Honors the observable chain (it means you can go crazy chaining operators). [RxOnActivityResult](https://github.com/VictorAlbertos/RxActivityResult) allows RxPaparazzo to transform every intent into an observable for a wonderful chaining process.


##Setup

Add the JitPack repository in your build.gradle (top level module):
```gradle
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```

And add next dependencies in the build.gradle of the module:
```gradle
dependencies {
    compile "com.github.RefineriaWeb:RxPaparazzo:0.1.0"
    compile "io.reactivex:rxjava:1.1.0"
}
```



## Usage
Because RxPaparazzo uses RxActivityResult to deal with intent calls, all its requirements and features are inherited too.

Before attempting to use RxPaparazzo, you need to call `RxActivityResult.register` in your Android `Application` class, supplying as parameter the current instance.
        
```java
public class SampleApp extends Application {

    @Override public void onCreate() {
        super.onCreate();
        RxActivityResult.register(this);
    }
}
```

Every feature RxPaparazzo exposes can be accessed from both, an `activity` or a `fragment` instance. 

The generic type of the observable returned by RxPaparazzo when subscribing to any of its features is always an instance of [Response]() class. 

This instance hols a reference to the current Activity/Fragment, accessible calling `targetUI()` method. Because the original one may be recreated (due to configuration changes or some other system events) it would be unsafe calling it. 

Instead, you must call any method/variable of your Activity/Fragment from this instance encapsulated in the Result object.

Also, this instance `response` holds a reference to the data as the appropriate response, as such as the result code of the specific operation.

### Calling built-in camera to take a photo.
```java
RxPaparazzo.takeImage(activityOrFragment)
        .usingCamera()
        .subscribe(response -> {
            if (response.resultCode() != RESULT_OK) {
                response.targetUI().showUserCanceled();
                return;
            }

            response.targetUI().loadImage(response.data());
        });
```

The instance response holds a reference to the path where the image was persisted. 


### Calling the gallery to retrieve an image.
```java
RxPaparazzo.takeImage(activityOrFragment)
        .usingGallery()
        .subscribe(response -> {
            if (response.resultCode() != RESULT_OK) {
                response.targetUI().showUserCanceled();
                return;
            }

            response.targetUI().loadImage(response.data());
        });
```

The instance response holds a reference to the path where the image was persisted. 

### Calling the gallery to retrieve multiple image (if the current level Android api is prior to 18, only one image will be retrieved). 
```java
RxPaparazzo.takeImages(activityOrFragment)
        .usingGallery()
        .subscribe(response -> {
            if (response.resultCode() != RESULT_OK) {
                response.targetUI().showUserCanceled();
                return;
            }

            if (response.data().size() == 1) response.targetUI().loadImage(response.data().get(0));
            else response.targetUI().loadImages(response.data());
        });
```

The instance response holds a reference to the paths where the images was persisted. 

### Size options for both camera and gallery features
After calling `RxPaparazzo.takeImage(activityOrFragment)`, you can set the size output for the image.

### Cropping options for both camera and gallery features


TODO: set name folder as name application

TODO Tests: 
gallery, 
gallery + crop, 
gallery multiple images, 
gallery multiple images + crop, 

option -> check some aspect has been modified, 
sizes?
permissions?