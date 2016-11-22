package com.miguelbcr.ui.rx_paparazzo.sample.activities;


import com.miguelbcr.ui.rx_paparazzo.entities.size.Size;

import java.util.List;

public interface Testable<T> {
    List<String> getFilePaths();

    Size getSize();
}
