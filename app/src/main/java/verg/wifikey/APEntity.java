package verg.wifikey;

import android.net.wifi.ScanResult;
import android.support.annotation.NonNull;

import verg.lib.WifiUtils;

/**
 * APEntity
 * Created by verg on 16-5-17.
 */
public class APEntity implements Comparable<APEntity> {
    private boolean isOpen;
    private ScanResult scanResult;
    private boolean isOnline;
    private String mPWD;

    public APEntity(ScanResult scanResult) {
        this.scanResult = scanResult;
        isOpen = WifiUtils.isOpen(scanResult);
        isOnline = true;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public boolean isWPS() {
        return WifiUtils.isWPS(scanResult);
    }

    public boolean isHasKey() {
        return mPWD != null;
    }

    public void setScanResult(ScanResult scanResult) {
        this.scanResult = scanResult;
    }

    public void setOnLine(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public String getBSSID() {
        return scanResult.BSSID;
    }

    public String getSSID() {
        return scanResult.SSID;
    }

    public int getSignalLevel() {
        return scanResult.level;
    }

    public String getPWD() {
        return mPWD;
    }

    public void setPWD(String pwd) {
        this.mPWD = pwd;
    }

    String getCapabilities(){
        return scanResult.capabilities;
    }

    @Override
    public int compareTo(@NonNull APEntity another) {
        return another.getSignalLevel() - getSignalLevel();
    }

    @Override
    public boolean equals(Object o) {
        try {
            APEntity apEntity = (APEntity) o;
            return getBSSID().equals(apEntity.getBSSID());
        } catch (ClassCastException e) {
            return false;
        }
    }
}
