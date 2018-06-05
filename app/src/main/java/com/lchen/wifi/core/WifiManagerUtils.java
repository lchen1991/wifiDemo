package com.lchen.wifi.core;

import android.net.Network;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import java.lang.reflect.Method;

/**
 * Created by chenlei on 2018/6/5.
 */

public class WifiManagerUtils {

    public static void connectByConfig(WifiManager manager, WifiConfiguration config) {
        if (manager == null) {
            return;
        }
        try {
            Method connect = manager.getClass().getDeclaredMethod("connect", WifiConfiguration.class, Class.forName("android.net.wifi.WifiManager$ActionListener"));
            if (connect != null) {
                connect.setAccessible(true);
                connect.invoke(manager, config, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void connectByNetworkId(WifiManager manager, int networkId) {
        if (manager == null) {
            return;
        }
        try {
            Method connect = manager.getClass().getDeclaredMethod("connect", int.class, Class.forName("android.net.wifi.WifiManager$ActionListener"));
            if (connect != null) {
                connect.setAccessible(true);
                connect.invoke(manager, networkId, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveNetworkByConfig(WifiManager manager, WifiConfiguration config) {
        if (manager == null) {
            return;
        }
        try {
            Method save = manager.getClass().getDeclaredMethod("save", WifiConfiguration.class, Class.forName("android.net.wifi.WifiManager$ActionListener"));
            if (save != null) {
                save.setAccessible(true);
                save.invoke(manager, config, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int addNetwork(WifiManager manager, WifiConfiguration config) {
        if (manager != null) {
            return manager.addNetwork(config);
        }
        return -1;
    }

    public static void forgetNetwork(WifiManager manager, int networkId) {
        if (manager == null) {
            return;
        }
        try {
            Method forget = manager.getClass().getDeclaredMethod("forget", int.class, Class.forName("android.net.wifi.WifiManager$ActionListener"));
            if (forget != null) {
                forget.setAccessible(true);
                forget.invoke(manager, networkId, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void disableNetwork(WifiManager manager, int networkId) {
        if (manager == null) {
            return;
        }
        try {
            Method disable = manager.getClass().getDeclaredMethod("disable", int.class, Class.forName("android.net.wifi.WifiManager$ActionListener"));
            if (disable != null) {
                disable.setAccessible(true);
                disable.invoke(manager, networkId, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean disconnectNetwork(WifiManager manager) {
        return manager != null && manager.disconnect();
    }


    public static void disableEphemeralNetwork(WifiManager manager, String SSID) {
        if (manager == null || TextUtils.isEmpty(SSID))
            return;
        try {
            Method disableEphemeralNetwork = manager.getClass().getDeclaredMethod("disableEphemeralNetwork", String.class);
            if (disableEphemeralNetwork != null) {
                disableEphemeralNetwork.setAccessible(true);
                disableEphemeralNetwork.invoke(manager, SSID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Network getCurrentNetwork(WifiManager manager) {
        if (manager == null) {
            return null;
        }
        try {
            Method currentNetwork = manager.getClass().getDeclaredMethod("getCurrentNetwork");
            if (currentNetwork != null) {
                currentNetwork.setAccessible(true);
               return (Network) currentNetwork.invoke(manager);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
