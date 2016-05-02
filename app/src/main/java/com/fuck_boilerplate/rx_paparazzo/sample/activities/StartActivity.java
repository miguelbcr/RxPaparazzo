package com.fuck_boilerplate.rx_paparazzo.sample.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.fuck_boilerplate.rx_paparazzo.sample.R;

public class StartActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);

        findViewById(R.id.bt_activity).setOnClickListener(view -> {
            startActivity(new Intent(StartActivity.this, SampleActivity.class));
        });

        findViewById(R.id.bt_fragment).setOnClickListener(view -> {
            startActivity(new Intent(StartActivity.this, HostActivitySampleFragment.class));
        });
    }
}
