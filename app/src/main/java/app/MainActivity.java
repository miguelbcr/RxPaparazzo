package app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.refineriaweb.app.R;
import com.squareup.picasso.Picasso;

import java.io.File;

import rx_paparazzo.RxPaparazzo;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private View fabCamera;
    private ImageView imageView;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configureToolbar();
        initViews();
    }

    private void configureToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
    }

    private void initViews() {
        fabCamera = findViewById(R.id.fab_camera);
        imageView = (ImageView) findViewById(R.id.iv_image);

        fabCamera.setOnClickListener(v -> {
            RxPaparazzo.takeImage(MainActivity.this)
//                    .crop(Style.Square)
//                    .output(Folder.Private)
//                    .size(Size.Small)
                    .usingCamera()
                    .subscribe(filePath -> {
                        File imageFile = new File(filePath);
                        Picasso.with(getApplicationContext()).invalidate(imageFile);
                        Picasso.with(getApplicationContext()).load(imageFile).into(imageView);
                    });
        });
    }
}
