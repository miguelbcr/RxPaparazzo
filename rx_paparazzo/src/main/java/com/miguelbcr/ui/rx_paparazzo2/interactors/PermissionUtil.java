package com.miguelbcr.ui.rx_paparazzo2.interactors;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;

import com.miguelbcr.ui.rx_paparazzo2.entities.TargetUi;

import java.util.List;

import io.reactivex.functions.Function;

public class PermissionUtil {

    public static final int READ_WRITE_PERMISSIONS = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

    public static Intent requestReadWritePermission(TargetUi targetUi, Intent intent, Uri uri) {
        addReadWritePermission(intent);

        grantFileReadWritePermissions(targetUi, intent, uri);

        return intent;
    }

    public static void addReadWritePermission(Intent intent) {
        intent.addFlags(READ_WRITE_PERMISSIONS);
    }

    /**
     * Workaround for Android bug.<br/>
     * See https://code.google.com/p/android/issues/detail?id=76683 <br/>
     * See http://stackoverflow.com/questions/18249007/how-to-use-support-fileprovider-for-sharing-content-to-other-apps
     */
    public static void grantFileReadWritePermissions(TargetUi targetUi, Intent intent, Uri uri) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            List<ResolveInfo> resInfoList = targetUi.getContext()
                    .getPackageManager()
                    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                targetUi.getContext().grantUriPermission(packageName, uri, READ_WRITE_PERMISSIONS);
            }
        }
    }

    public static void revokeFileReadWritePermissions(TargetUi targetUi, Uri uri) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            targetUi.getContext().revokeUriPermission(uri, READ_WRITE_PERMISSIONS);
        }
    }

    public static Function<Intent, Uri> createRevokeFileReadWritePermissionsFunction(final TargetUi targetUi, final Uri uri) {
        return new Function<Intent, Uri>() {
            @Override public Uri apply(Intent data) throws Exception {
                PermissionUtil.revokeFileReadWritePermissions(targetUi, uri);

                return uri;
            }
        };
    }
}
