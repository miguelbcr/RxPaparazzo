package com.fuck_boilerplate.rx_paparazzo.sample.activities;

import com.fuck_boilerplate.rx_paparazzo.entities.Size;

import java.util.List;

public interface Testable<T> {
    List<String> getFilePaths();
    Size getSize();
}
