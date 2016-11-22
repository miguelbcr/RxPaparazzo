package com.miguelbcr.ui.rx_paparazzo2.sample.activities;


import com.miguelbcr.ui.rx_paparazzo2.entities.size.Size;

import java.util.List;

public interface Testable<T> {
    List<String> getFilePaths();

    Size getSize();
}
