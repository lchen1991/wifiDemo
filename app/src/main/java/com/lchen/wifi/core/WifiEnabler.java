package com.lchen.wifi.core;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.widget.Switch;
import android.widget.Toast;

import com.lchen.wifi.widget.SwitchBar;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.lchen.wifi.widget.SwitchBar.*;

/**
 * Created by chenlei on 2018/6/2.
 */

public class WifiEnabler implements OnSwitchChangeListener {

    public static final int WIFI_AP_STATE_ENABLING = 12;

    public static final int WIFI_AP_STATE_ENABLED = 13;

    private Context mContext;
    private SwitchBar mSwitchBar;
    private boolean mListeningToOnSwitchChange = false;
    private AtomicBoolean mConnected = new AtomicBoolean(false);

    private final WifiManager mWifiManager;
    private boolean mStateMachineEvent;
    private final IntentFilter mIntentFilter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                handleWifiStateChanged(intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
            } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
                if (!mConnected.get()) {
                    handleStateChanged(WifiInfo.getDetailedStateOf((SupplicantState)
                            intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(
                        WifiManager.EXTRA_NETWORK_INFO);
                mConnected.set(info.isConnected());
                handleStateChanged(info.getDetailedState());
            }
        }
    };

    private static final String EVENT_DATA_IS_WIFI_ON = "is_wifi_on";
    private static final int EVENT_UPDATE_INDEX = 0;

    public WifiEnabler(Context context, SwitchBar switchBar) {
        mContext = context;
        mSwitchBar = switchBar;

        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        mIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        setupSwitchBar();
    }

    public void setupSwitchBar() {
        final int state = mWifiManager.getWifiState();
        handleWifiStateChanged(state);
        if (!mListeningToOnSwitchChange) {
            mSwitchBar.addOnSwitchChangeListener(this);
            mListeningToOnSwitchChange = true;
        }
        mSwitchBar.show();
    }

    public void teardownSwitchBar() {
        if (mListeningToOnSwitchChange) {
            mSwitchBar.removeOnSwitchChangeListener(this);
            mListeningToOnSwitchChange = false;
        }
        mSwitchBar.hide();
    }

    public void resume(Context context) {
        mContext = context;
        mContext.registerReceiver(mReceiver, mIntentFilter);
        if (!mListeningToOnSwitchChange) {
            mSwitchBar.addOnSwitchChangeListener(this);
            mListeningToOnSwitchChange = true;
        }
    }

    public void pause() {
        mContext.unregisterReceiver(mReceiver);
        if (mListeningToOnSwitchChange) {
            mSwitchBar.removeOnSwitchChangeListener(this);
            mListeningToOnSwitchChange = false;
        }
    }

    private void handleWifiStateChanged(int state) {

        switch (state) {
            case WifiManager.WIFI_STATE_ENABLING:
                mSwitchBar.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                setSwitchBarChecked(true);
                mSwitchBar.setEnabled(true);
                updateSearchIndex(true);
                break;
            case WifiManager.WIFI_STATE_DISABLING:
                mSwitchBar.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                setSwitchBarChecked(false);
                mSwitchBar.setEnabled(true);
                updateSearchIndex(false);
                break;
            default:
                setSwitchBarChecked(false);
                mSwitchBar.setEnabled(true);
                updateSearchIndex(false);
        }
    }

    private void updateSearchIndex(boolean isWiFiOn) {
        Message msg = new Message();
        msg.what = EVENT_UPDATE_INDEX;
        msg.getData().putBoolean(EVENT_DATA_IS_WIFI_ON, isWiFiOn);
    }

    private void setSwitchBarChecked(boolean checked) {
        mStateMachineEvent = true;
        mSwitchBar.setChecked(checked);
        mStateMachineEvent = false;
    }

    private void handleStateChanged(NetworkInfo.DetailedState state) {

    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        if (mStateMachineEvent) {
            return;
        }

        if (mayDisableTethering(isChecked)) {
            try {
                Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
                method.invoke(mWifiManager, null, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!mWifiManager.setWifiEnabled(isChecked)) {
            mSwitchBar.setEnabled(true);
            mSwitchBar.setChecked(mWifiManager.isWifiEnabled());
            Toast.makeText(mContext, "wifi enabled error", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean mayDisableTethering(boolean isChecked) {
        int wifiApState = WIFI_AP_STATE_ENABLED;
        Method method = null;
        try {
            method = mWifiManager.getClass().getMethod("getWifiApState");
            wifiApState = (int) method.invoke(mWifiManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isChecked && ((wifiApState == WIFI_AP_STATE_ENABLING) || (wifiApState == WIFI_AP_STATE_ENABLED));
    }
}
