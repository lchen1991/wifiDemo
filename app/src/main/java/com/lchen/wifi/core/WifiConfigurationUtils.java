package com.lchen.wifi.core;

import android.net.wifi.WifiConfiguration;

import java.lang.reflect.Method;

/**
 * Created by chenlei on 2018/6/5.
 */

public class WifiConfigurationUtils {

    public static final int DISABLED_ASSOCIATION_REJECTION = 2;

    public static final int DISABLED_AUTHENTICATION_FAILURE = 3;

    public static final int DISABLED_DHCP_FAILURE = 4;

    public static final int DISABLED_DNS_FAILURE = 5;

    public static boolean hasNoInternetAccess(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        try {
            Method hasNoInternetAccess = config.getClass().getDeclaredMethod("hasNoInternetAccess");
            if (hasNoInternetAccess != null) {
                hasNoInternetAccess.setAccessible(true);
                return (boolean) hasNoInternetAccess.invoke(config);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static Object getNetworkSelectionStatus(WifiConfiguration config) {
        if (config == null) {
            return null;
        }
        try {
            Method networkSelectionStatus = config.getClass().getDeclaredMethod("getNetworkSelectionStatus");
            if (networkSelectionStatus != null) {
                networkSelectionStatus.setAccessible(true);
                return networkSelectionStatus.invoke(config);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isNetworkPermanentlyDisabled(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        try {
            Object o = getNetworkSelectionStatus(config);
            if (o != null) {
                Method isNetworkPermanentlyDisabled = o.getClass().getDeclaredMethod("isNetworkPermanentlyDisabled");
                if (isNetworkPermanentlyDisabled != null) {
                    isNetworkPermanentlyDisabled.setAccessible(true);
                    return (boolean) isNetworkPermanentlyDisabled.invoke(o);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isNetworkEnabled(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        try {
            Object o = getNetworkSelectionStatus(config);
            if (o != null) {
                Method isNetworkEnabled = o.getClass().getDeclaredMethod("isNetworkEnabled");
                if (isNetworkEnabled != null) {
                    isNetworkEnabled.setAccessible(true);
                    return (boolean) isNetworkEnabled.invoke(o);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int getNetworkSelectionDisableReason(WifiConfiguration config) {
        if (config == null) {
            return -1;
        }
        try {
            Object o = getNetworkSelectionStatus(config);
            if (o != null) {
                Method networkSelectionDisableReason = o.getClass().getDeclaredMethod("getNetworkSelectionDisableReason");
                if (networkSelectionDisableReason != null) {
                    networkSelectionDisableReason.setAccessible(true);
                    return (int) networkSelectionDisableReason.invoke(o);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }
}
