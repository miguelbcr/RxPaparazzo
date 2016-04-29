package com.refineriaweb.rx_paparazzo.sample.activities;

import com.refineriaweb.rx_paparazzo.library.entities.Size;

import java.util.List;

public interface Testable<T> {
    List<String> getFilePaths();
    Size getSize();
}
