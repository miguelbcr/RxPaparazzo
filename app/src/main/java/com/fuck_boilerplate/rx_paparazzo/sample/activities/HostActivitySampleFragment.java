package com.fuck_boilerplate.rx_paparazzo.sample.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.fuck_boilerplate.rx_paparazzo.entities.size.Size;
import com.fuck_boilerplate.rx_paparazzo.sample.R;
import com.fuck_boilerplate.rx_paparazzo.sample.fragments.SampleFragment;

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
}
