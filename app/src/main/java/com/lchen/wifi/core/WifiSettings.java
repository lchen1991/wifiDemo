package com.lchen.wifi.core;

import android.net.wifi.WifiManager;

/**
 * Created by chenlei on 2018/6/3.
 */

public class WifiSettings implements WifiTracker.WifiListener {


    protected WifiManager mWifiManager;

    @Override
    public void onWifiStateChanged(int state) {
        switch (state) {
            case WifiManager.WIFI_STATE_ENABLING:
                // wifi_starting (Turning wifi on)
                break;

            case WifiManager.WIFI_STATE_DISABLED:
//                setOffMessage();
//                setProgressBarVisible(false);
                break;
        }
    }

    @Override
    public void onConnectedChanged() {

    }

    @Override
    public void onAccessPointsChanged() {

    }
}
