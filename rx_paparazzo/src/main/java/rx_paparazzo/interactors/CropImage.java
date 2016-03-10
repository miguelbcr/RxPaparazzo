package rx_paparazzo.interactors;

import android.app.Activity;
import android.net.Uri;

import rx.Observable;
import rx_paparazzo.entities.Config;

/**
 * Created by victor on 10/03/16.
 */
public class CropImage extends UseCase<Uri> {
    private Uri uri;
    private Config config;
    private Activity activity;

    public CropImage with(Activity activity, Config config, Uri uri) {
        this.activity = activity;
        this.config = config;
        this.uri = uri;
        return this;
    }

    @Override public Observable<Uri> react() {
        //if back return userBack()
        if (config.doCrop()) return Observable.just(null);
        else return Observable.just(uri);
    }
}
