package com.refineriaweb.rxpaparazzo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import rx_paparazzo.RxPaparazzo;
import rx_paparazzo.entities.Folder;
import rx_paparazzo.entities.Size;
import rx_paparazzo.entities.Style;

public class MainActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RxPaparazzo.takeImage(this)
                .crop(Style.Square)
                .output(Folder.Private)
                .size(Size.Small)
                .usingGallery()//usingCamera()
                .subscribe(path -> {

                });

        RxPaparazzo.takeImages(this)
                .crop(Style.Square)
                .output(Folder.Private)
                .size(Size.Small)
                .usingGallery()
                .subscribe(paths -> {

                });
    }
}
