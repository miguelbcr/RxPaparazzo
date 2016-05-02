RxJava extension for Android to access camera and gallery to take images. 

# RxPaparazzo

What is a Paparazzo?

> A freelance photographer who aggressively pursues celebrities for the purpose of taking candid photographs.

This library does that. Not really. But it was a funny name, thought. Was it?


## Features:
 
- Runtime permissions. Not worries about the tricky Android runtime permissions system. RxPaparazzo relies on [RxPermissions](https://github.com/tbruyelle/RxPermissions) to deal with that.  
- Take a photo using the built-in camera.
- Access to gallery. 
- Crop images. RxPaparazzo relies on [UCrop](https://github.com/Yalantis/uCrop) to perform beautiful cuts to any face, body or place. 
- Honors the observable chain (it means you can go crazy chaining operators). [RxOnActivityResult](https://github.com/VictorAlbertos/RxActivityResult) allows RxPaparazzo to transform every intent into an observable for a wonderful chaining process.


## Setup

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
    compile "com.github.FuckBoilerplate:RxPaparazzo:0.0.1"
    compile "io.reactivex:rxjava:1.1.3"
}
```


## Usage
Because RxPaparazzo uses RxActivityResult to deal with intent calls, all its requirements and features are inherited too.

Before attempting to use RxPaparazzo, you need to call `RxPaparazzo.register` in your Android `Application` class, supplying as parameter the current instance.
        
```java
public class SampleApp extends Application {

    @Override public void onCreate() {
        super.onCreate();
        RxPaparazzo.register(this);
    }
}
```

Every feature RxPaparazzo exposes can be accessed from both, an `activity` or a `fragment` instance. 

The generic type of the `observable` returned by RxPaparazzo when subscribing to any of its features is always an instance of [Response](https://github.com/FuckBoilerplate/RxPaparazzo/blob/master/rx_paparazzo/src/main/java/com/fuck_boilerplate/rx_paparazzo/entities/Response.java) class. 

This instance hols a reference to the current Activity/Fragment, accessible calling `targetUI()` method. Because the original one may be recreated it would be unsafe calling it. Instead, you must call any method/variable of your Activity/Fragment from this instance encapsulated in the `response` instance.

Also, this instance holds a reference to the data as the appropriate response, as such as the result code of the specific operation.

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

The `response` instance holds a reference to the path where the image was persisted. 


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

The `response` instance holds a reference to the path where the image was persisted. Same as the previous example. 

### Calling the gallery to retrieve multiple image 
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

The `response` instance holds a reference to the paths where the images was persisted. 
**Note**: if the level Android api device is minor than 18, only one image will be retrieved. 

## Customizations
When asking RxPaparazzo for an image -whether it was retrieved using the built-in camera or via gallery, it's possible to apply some configurations to the action. 

### Size options
`Size` values `enum` can be used to set the size of the image to retrieve. There are 3 options:

* Small: 1/8 aprox. of the screen resolution
* Screen: The size image matches aprox. the screen resolution.
* Original: The original size of the image.

`Screen` value will be set as default.

```java
RxPaparazzo.takeImages(activityOrFragment)
                .size(Size.Small)
                .usingGallery()
```                 
                
### Cropping support for image.
This feature is available thanks to the amazing library [uCrop](https://github.com/Yalantis/uCrop) authored by [Yalantis](https://github.com/Yalantis) group. 

```java
RxPaparazzo.takeImages(activityOrFragment)
                .crop()
``` 

By calling `crop()` method when building the observable instance, all they images retrieved will be able to be cropped, regardless if the images were retrieved using the built-in camera or gallery, even if multiple images were requested in a single call using `takeImages()` approach.
Because uCrop Yalantis library exposes some configuration in order to customize the crop screen, RxPaparazzo exposes an overloaded method of `crop(UCrop.Options)` which allow to pass an instance of [UCrop.Options](https://github.com/Yalantis/uCrop/blob/master/ucrop/src/main/java/com/yalantis/ucrop/UCrop.java#L211).

```java
UCrop.Options options = new UCrop.Options();
options.setToolbarColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
 
RxPaparazzo.takeImage(activityOrFragment)
         .crop(options)
```


## Credits
* Runtime permissions: [RxPermissions](https://github.com/tbruyelle/RxPermissions)
* Crop: [uCrop](https://github.com/Yalantis/uCrop)

## Authors

**Víctor Albertos**

* <https://twitter.com/_victorAlbertos>
* <https://www.linkedin.com/in/victoralbertos>
* <https://github.com/VictorAlbertos>

**Miguel García**

* <https://es.linkedin.com/in/miguelbcr>
* <https://github.com/miguelbcr>


## Another author's libraries using RxJava:
* [RxCache](https://github.com/VictorAlbertos/RxCache): Reactive caching library for Android and Java. 
* [RxGcm](https://github.com/VictorAlbertos/RxGcm): RxJava extension for Gcm which acts as an architectural approach to easily satisfy the requirements of an android app when dealing with push notifications.
* [RxActivityResult](https://github.com/VictorAlbertos/RxActivityResult): A reactive-tiny-badass-vindictive library to break with the OnActivityResult implementation as it breaks the observables chain. 
