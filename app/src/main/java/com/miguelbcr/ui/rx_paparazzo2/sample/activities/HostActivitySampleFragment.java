package com.miguelbcr.ui.rx_paparazzo2.sample.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.miguelbcr.ui.rx_paparazzo2.entities.FileData;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.Size;
import com.miguelbcr.ui.rx_paparazzo2.sample.R;
import com.miguelbcr.ui.rx_paparazzo2.sample.fragments.SampleFragment;

import java.util.List;

public class HostActivitySampleFragment extends AppCompatActivity implements Testable {
    private SampleFragment fragment;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host_activity_sample_fragment);
        fragment = (SampleFragment) getSupportFragmentManager().findFragmentById(R.id.sample_fragment);
    }

    @Override public List<String> getFilePaths() {
        return fragment.getFilePaths();
    }

    @Override public Size getSize() {
        return fragment.getSize();
    }

    @Override
    public List<FileData> getFileDatas() {
        return fragment.getFileDatas();
    }
}
