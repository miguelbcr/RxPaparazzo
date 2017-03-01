package com.miguelbcr.ui.rx_paparazzo2.sample.activities;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo;
import com.miguelbcr.ui.rx_paparazzo2.sample.R;

public class PickerUtil {

    public static boolean checkResultCode(Context context, int code) {
        if (code == RxPaparazzo.RESULT_DENIED_PERMISSION) {
            showUserDidNotGrantPermissions(context);
        } else if (code == RxPaparazzo.RESULT_DENIED_PERMISSION_NEVER_ASK) {
            showUserDidNotGrantPermissionsNeverAsk(context);
        } else if (code != Activity.RESULT_OK) {
            showUserCanceled(context);
        }

        return code == Activity.RESULT_OK;
    }

    private static void showUserCanceled(Context context) {
        Toast.makeText(context, context.getString(R.string.user_canceled), Toast.LENGTH_SHORT).show();
    }

    private static void showUserDidNotGrantPermissions(Context context) {
        Toast.makeText(context, context.getString(R.string.user_did_not_grant_permissions), Toast.LENGTH_SHORT).show();
    }

    private static void showUserDidNotGrantPermissionsNeverAsk(Context context) {
        Toast.makeText(context, context.getString(R.string.user_did_not_grant_permissions_never_ask), Toast.LENGTH_SHORT).show();
    }

}
