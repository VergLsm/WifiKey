package verg.lib;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * App Infos
 * Created by verg on 16-5-10.
 */
public class AppInfos {

    public static PackageInfo getPackageInfo(Context context) {
        PackageManager manager;
        PackageInfo info = null;
        manager = context.getPackageManager();
        try {
            info = manager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info;
    }

    public static int getVersionCode(Context context) {
        PackageInfo packageInfo = getPackageInfo(context);
        return packageInfo.versionCode;
    }

}
