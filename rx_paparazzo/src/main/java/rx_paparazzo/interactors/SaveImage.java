package rx_paparazzo.interactors;

import android.content.Context;
import android.net.Uri;

import rx.Observable;

public class SaveImage extends UseCase<String> {
    private Uri uri;
    private Context context;

    public SaveImage with(Context context, Uri uri) {
        this.context = context;
        this.uri = uri;
        return this;
    }

    @Override public Observable<String> react() {

        return Observable.just(null);
    }
}
