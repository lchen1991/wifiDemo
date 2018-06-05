package com.lchen.wifi.core;

import android.net.wifi.WifiInfo;

import java.lang.reflect.Method;

/**
 * Created by chenlei on 2018/6/5.
 */

public class WifiInfoUtils {

    public static boolean isEphemeral(WifiInfo info) {
        if (info == null) {
            return false;
        }
        try {
            Method ephemeral = info.getClass().getDeclaredMethod("isEphemeral");
            if (ephemeral != null) {
                ephemeral.setAccessible(true);
                return (boolean) ephemeral.invoke(info);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
