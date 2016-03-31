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
- Honors the observable chain (it means you can go crazy chaining things). [RxOnActivityResult](https://github.com/VictorAlbertos/RxActivityResult) allows RxPaparazzo to transform every intent into a link of the wonderful global chaining process.


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



##Usage
Option
Size
TakePhoto



TODO: set name folder as name application

RxPaparazzo
     .delete(context)
     .input(path)
     .subscribe(success -> )
     
RxPaparazzo
     .takeFile(activity)
     .mimetype(String[])
     .usingSystem()
     .subscribe(path -> path)

TODO Tests: 
gallery, 
gallery + crop, 
gallery multiple images, 
gallery multiple images + crop, 

option -> check some aspect has been modified, 
sizes?
permissions?