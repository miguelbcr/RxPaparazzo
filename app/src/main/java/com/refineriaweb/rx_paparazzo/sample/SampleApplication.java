package com.refineriaweb.rx_paparazzo.sample;

import android.app.Application;

import com.refineriaweb.rx_paparazzo.library.RxPaparazzo;

/**
 * Created by miguel on 16/03/2016.
 */
public class SampleApplication extends Application {

    @Override public void onCreate() {
        super.onCreate();
        RxPaparazzo.register(this);
    }

}
