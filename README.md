[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-RxPaparazzo-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/3523)

RxJava extension for Android to take photos using the camera, select files or photos from the device and optionally crop or rotate any selected images.

# RxPaparazzo

What is RX?

> Reactive Extensions for the JVM – a library for composing asynchronous and event-based programs using observable sequences for the Java VM.

What is a Paparazzo?

> A freelance photographer who aggressively pursues celebrities for the purpose of taking candid photographs.

This library does that (well not really). But it is a cool name.


## Features:
 
- Runtime permissions. Not worries about the tricky Android runtime permissions system. RxPaparazzo relies on [RxPermissions](https://github.com/tbruyelle/RxPermissions) to deal with that.  
- Takes a photo using the built-in camera.
- Access to gallery and other sources of photos.
- Access to files and documents stored locally and on the cloud.
- Crop and rotate images. RxPaparazzo relies on [UCrop](https://github.com/Yalantis/uCrop) to perform beautiful cuts to any face, body or place.
- Honors the observable chain (it means you can go crazy chaining operators). [RxOnActivityResult](https://github.com/VictorAlbertos/RxActivityResult) allows RxPaparazzo to transform every intent into an observable for a wonderful chaining process.


## Setup RxJava1 [DEPRECATED]
Add the JitPack repository in your build.gradle (top level module):
```gradle
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```

Add dependencies in the build.gradle of the module:
```gradle
dependencies {
    compile "com.github.miguelbcr:RxPaparazzo:0.4.7"
    compile 'io.reactivex:rxandroid:1.2.1'
}
```

## Setup RxJava2
Add the JitPack repository in your build.gradle (top level module):
```gradle
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
}
```

Add dependencies in the build.gradle of the module:
```gradle
dependencies {
    compile "com.github.miguelbcr:RxPaparazzo:0.5.4-2.x"
    compile 'io.reactivex.rxjava2:rxandroid:2.0.1'
}
```


## Usage
Because RxPaparazzo uses [RxActivityResult](https://github.com/VictorAlbertos/RxActivityResult) to deal with intent calls, all its requirements and features are inherited too.

Before attempting to use RxPaparazzo, you need to call `RxPaparazzo.register` in your Android Application's `onCreate` supplying the current Application instance.
        
```java
public class SampleApp extends Application {

    @Override public void onCreate() {
        super.onCreate();
        RxPaparazzo.register(this);
    }
}
```

You will need to also add a FileProvider named `android.support.v4.content.FileProvider` to your `AndroidManifest.xml` and create a paths xml file in your `src/main/res/xml directory.

```xml
 <provider
    android:name="android.support.v4.content.FileProvider"
    android:authorities="${applicationId}.file_provider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_provider_paths"/>
</provider>
```

If you set the provider `android:authorities` attribute to a value other than `${applicationId}.file_provider` name you must set the configuration it using `RxPaparazzo.Builder.setFileProviderAuthority(String authority)`

Example: `file_provider_paths.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
  <files-path name="RxPaparazzoImages" path="RxPaparazzo/"/>
</paths>
```

The `file_provider_paths.xml` is where files are exposed in the FileProvider.
If you set the files-path `path` attribute to a value other than `RxPaparazzo/` you must set the configuration using `RxPaparazzo.Builder.setFileProviderPath(String path)`

All features RxPaparazzo exposes can be accessed from both, an `activity` or a `fragment` instance.

**Limitation:**: Your fragments need to extend from `android.support.v4.app.Fragment` instead of `android.app.Fragment`, otherwise they won't be notified.

The generic type of the `observable` returned by RxPaparazzo when subscribing to any of its features is always an instance of [Response](https://github.com/miguelbcr/RxPaparazzo/blob/2.x/rx_paparazzo/src/main/java/com/miguelbcr/ui/rx_paparazzo2/entities/Response.java) class.

This instance holds a reference to the current Activity/Fragment, accessible calling `targetUI()` method. Because the original one may be recreated it would be unsafe calling it. Instead, you must call any method/variable of your Activity/Fragment from this instance encapsulated in the `response` instance.

Also, this instance holds a reference to the data as the appropriate response, as such as the result code of the specific operation.


### Saving files

By default, the image / file is saved in a directory the same as the app name on the root of the external storage. You can choose to save the images in internal storage by using `.useInternalStorage()`

The `response` in the callback function supplied to the `subscribe()` method holds a reference to the path where the image was persisted.

### Calling built-in camera to take a photo.
```java
RxPaparazzo.single(activityOrFragment)
        .usingCamera()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(response -> {
            // See response.resultCode() doc
            if (response.resultCode() != RESULT_OK) {
                response.targetUI().showUserCanceled();
                return;
            }

            response.targetUI().loadImage(response.data());
        });
```

### Calling the file picker to retrieve a file.
```java
RxPaparazzo.single(activityOrFragment)
        .usingFile()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(response -> {        
            // See response.resultCode() doc
            if (response.resultCode() != RESULT_OK) {
                response.targetUI().showUserCanceled();
                return;
            }

            response.targetUI().loadImage(response.data());
        });
```

### Calling the file picker to retrieve multiple files
```java
RxPaparazzo.multiple(activityOrFragment)
        .usingFiles()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(response -> {
            // See response.resultCode() doc
            if (response.resultCode() != RESULT_OK) {
                response.targetUI().showUserCanceled();
                return;
            }

            if (response.data().size() == 1) response.targetUI().loadImage(response.data().get(0));
            else response.targetUI().loadImages(response.data());
        });
```

### Calling the gallery to retrieve an image.
```java
RxPaparazzo.single(activityOrFragment)
        .usingGallery()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(response -> {
            // See response.resultCode() doc
            if (response.resultCode() != RESULT_OK) {
                response.targetUI().showUserCanceled();
                return;
            }

            response.targetUI().loadImage(response.data());
        });
```

### Calling the gallery to retrieve multiple image
```java
RxPaparazzo.multiple(activityOrFragment)
        .usingGallery()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(response -> {
            // See response.resultCode() doc
            if (response.resultCode() != RESULT_OK) {
                response.targetUI().showUserCanceled();
                return;
            }

            if (response.data().size() == 1) response.targetUI().loadImage(response.data().get(0));
            else response.targetUI().loadImages(response.data());
        });
```

**Note**: if the level Android api device is minor than 18, only one image will be retrieved. 

## Customizations
When asking RxPaparazzo for an photo / image / file it's possible to apply some configurations to the action.

### Size options
[Size](https://github.com/miguelbcr/RxPaparazzo/blob/2.x/rx_paparazzo/src/main/java/com/miguelbcr/ui/rx_paparazzo2/entities/size/Size.java) values can be used to set the size of the image to retrieve. There are 4 options:

* [SmallSize](https://github.com/miguelbcr/RxPaparazzo/blob/2.x/rx_paparazzo/src/main/java/com/miguelbcr/ui/rx_paparazzo2/entities/size/SmallSize.java): 1/8 aprox. of the screen resolution
* [ScreenSize](https://github.com/miguelbcr/RxPaparazzo/blob/2.x/rx_paparazzo/src/main/java/com/miguelbcr/ui/rx_paparazzo2/entities/size/ScreenSize.java): The size image matches aprox. the screen resolution.
* [OriginalSize](https://github.com/miguelbcr/RxPaparazzo/blob/2.x/rx_paparazzo/src/main/java/com/miguelbcr/ui/rx_paparazzo2/entities/size/OriginalSize.java): The original size of the image.
* [CustomMaxSize](https://github.com/miguelbcr/RxPaparazzo/blob/2.x/rx_paparazzo/src/main/java/com/miguelbcr/ui/rx_paparazzo2/entities/size/CustomMaxSize.java): Yot can specify max size you want and image will be scaled proportionally.

[ScreenSize](https://github.com/miguelbcr/RxPaparazzo/blob/2.x/rx_paparazzo/src/main/java/com/miguelbcr/ui/rx_paparazzo2/entities/size/ScreenSize.java) value will be set as default.

```java
RxPaparazzo.multiple(activityOrFragment)
                .size(new ScreenSize())
                .usingGallery()
```                 
                
### Cropping support for image.
This feature is available thanks to the amazing library [uCrop](https://github.com/Yalantis/uCrop) authored by [Yalantis](https://github.com/Yalantis) group. 

```java
RxPaparazzo.multiple(activityOrFragment)
                .crop()
``` 

By calling `crop()` method when building the observable instance, all they images retrieved will be able to be cropped, regardless if the images were retrieved using the built-in camera or gallery, even if multiple images were requested in a single call using `single()` approach.
Because uCrop Yalantis library exposes some configuration in order to customize the crop screen, RxPaparazzo exposes an overloaded method of `crop(UCrop.Options)` which allow to pass an instance of [UCrop.Options](https://github.com/Yalantis/uCrop/blob/master/ucrop/src/main/java/com/yalantis/ucrop/UCrop.java#L236).
If you need to configure the aspect ratio, the max result size or using the source image aspect ratio, you must pass an instance of [Options](https://github.com/miguelbcr/RxPaparazzo/blob/2.x/rx_paparazzo/src/main/java/com/miguelbcr/ui/rx_paparazzo2/entities/Options.java) class, which extends from `UCrop.Options` and adds the three missing properties.  

```java
UCrop.Options options = new UCrop.Options();
options.setToolbarColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));

RxPaparazzo.single(activityOrFragment).crop(options)
```

```java
Options options = new Options();
options.setToolbarColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
options.setAspectRatio(25, 50); 

RxPaparazzo.single(activityOrFragment)
         .crop(options)
```

### Media scanning

To send files to the media scanner so that they can be indexed and available in applications such as the Gallery use `sendToMediaScanner()`. If you are using `useInternalStorage()` then the media scanner will not be able to access the file.

### Picking files

If you wish to limit the type of images or files then use `setMimeType(String mimeType)` to specify a specific mime type for the Intent.
By default `Intent.ACTION_GET_CONTENT` is used to request images and files. If you wish to edit the original file call `useDocumentPicker()`, this will allow greater, possiblty persistent access to the source file.

## Proguard

```
# Rxjava rules
-dontwarn rx.internal.util.**

-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    long producerNode;
    long consumerNode;
}
```
## Testing

Testing has been done using the following Genymotion devices:

* Genymotion - Google Nexus 5 5.0.0 API 21 1080x1920 480dpi
* Genymotion - Google Nexus 7 5.1.0 API 22 800x1280 213dpi


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

**James McIntosh**

* <https://www.linkedin.com/in/james-mcintosh>
* <https://github.com/jamesmcintosh>


## Another author's libraries using RxJava:
* [RxCache](https://github.com/VictorAlbertos/RxCache): Reactive caching library for Android and Java. 
* [RxGcm](https://github.com/VictorAlbertos/RxGcm): RxJava extension for Gcm which acts as an architectural approach to easily satisfy the requirements of an android app when dealing with push notifications.
* [RxActivityResult](https://github.com/VictorAlbertos/RxActivityResult): A tiny reactive library to break with the OnActivityResult implementation as it breaks the observables chain.
