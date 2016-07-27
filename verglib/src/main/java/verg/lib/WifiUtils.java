package verg.lib;

import android.net.wifi.ScanResult;

/**
 * WifiUtils
 * Created by verg on 16-5-15.
 */
public class WifiUtils {
    public static boolean isOpen(ScanResult scanResult) {
        String[] tag = {"WPA", "WEP"};
        for (String str : tag) {
            if (scanResult.capabilities.contains(str))
                return false;
        }
        return true;
    }

}
