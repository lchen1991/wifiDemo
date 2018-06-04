package com.lchen.wifi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.HandlerThread;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.lchen.wifi.core.AccessPoint;
import com.lchen.wifi.core.WifiEnabler;
import com.lchen.wifi.core.WifiTracker;
import com.lchen.wifi.widget.SwitchBar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity implements WifiTracker.WifiListener, AccessPoint.AccessPointListener {

    private static final String TAG = "MainActivity";

    private boolean mHasPermission;

    //权限请求码
    private static final int PERMISSION_REQUEST_CODE = 0;
    //两个危险权限需要动态申请
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private WifiEnabler mWifiEnabler = null;

    private WifiManager mWifiManager;

    private ProgressBar mProgressHeader;

    private WifiTracker mWifiTracker;

    private HandlerThread mBgThread;

    private RecyclerView mWifiList;

    private WifiAdapter mWifiAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBgThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        mBgThread.start();
        mWifiEnabler = new WifiEnabler(this, (SwitchBar) findViewById(R.id.switch_bar));
        mWifiTracker = new WifiTracker(this, this, mBgThread.getLooper(), true, true, false);
        mWifiManager = mWifiTracker.getManager();

        initView();

        mHasPermission = checkPermission();
        if (!mHasPermission) {
            requestPermission();
        }
    }

    private void initView() {
        mProgressHeader = findViewById(R.id.wifi_progress_header);
        mWifiList = findViewById(R.id.rv_wifi_list);

        mWifiAdapter = new WifiAdapter(null);
        mWifiList.setAdapter(mWifiAdapter);
        mWifiList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWifiEnabler != null) {
            mWifiEnabler.resume(this);
        }

        if (mHasPermission) {
            mWifiTracker.startTracking();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWifiEnabler != null) {
            mWifiEnabler.pause();
        }
        mWifiTracker.stopTracking();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWifiEnabler != null) {
            mWifiEnabler.teardownSwitchBar();
        }

        if (mBgThread != null) {
            mBgThread.quit();
        }
    }

    @Override
    public void onWifiStateChanged(int state) {
        switch (state) {
            case WifiManager.WIFI_STATE_ENABLING:
                // wifi_starting (Turning wifi on)
                setProgressBarVisible(true);
                Log.e(TAG, "Turning wifi on...");
                break;

            case WifiManager.WIFI_STATE_DISABLED:
//                setOffMessage();
                setProgressBarVisible(false);
                Log.e(TAG, "Turning wifi off...");
                break;
        }
    }

    @Override
    public void onConnectedChanged() {
        Log.e(TAG, " onConnectedChanged ");
    }

    private List<AccessPoint> aps = new ArrayList<>();

    @Override
    public void onAccessPointsChanged() {
        final int wifiState = mWifiManager.getWifiState();

        switch (wifiState) {
            case WifiManager.WIFI_STATE_ENABLED:

                final Collection<AccessPoint> accessPoints = mWifiTracker.getAccessPoints();

                Log.e(TAG, " onAccessPointsChanged -  WIFI_STATE_ENABLED : p_size:" + accessPoints.size());

                boolean hasAvailableAccessPoints = false;

                aps.clear();
                for (AccessPoint accessPoint : accessPoints) {

                    if (accessPoint.getLevel() != -1) {
                        hasAvailableAccessPoints = true;
                        accessPoint.setListener(this);

                        aps.add(accessPoint);
                        Log.e(TAG, " onAccessPointsChanged -  WIFI_STATE_ENABLED ::" + accessPoint.getConfigName() + "===" + accessPoint.getLevel());
                    }
                }

                if (!hasAvailableAccessPoints) {
                    setProgressBarVisible(true);
                } else {
                    setProgressBarVisible(false);
                    if (mWifiAdapter != null) {
                        mWifiAdapter.setData(aps);
                    }
                }
                break;

            case WifiManager.WIFI_STATE_ENABLING:
//                getPreferenceScreen().removeAll();
                Log.e(TAG, " onAccessPointsChanged -  WIFI_STATE_ENABLING ");
                setProgressBarVisible(true);
                break;

            case WifiManager.WIFI_STATE_DISABLING:
                Log.e(TAG, " onAccessPointsChanged -  WIFI_STATE_DISABLING ");
//                addMessagePreference(R.string.wifi_stopping);
                setProgressBarVisible(true);
                break;

            case WifiManager.WIFI_STATE_DISABLED:
                Log.e(TAG, " onAccessPointsChanged -  WIFI_STATE_DISABLED ");
//                setOffMessage();
                setProgressBarVisible(false);
//                if (mScanMenuItem != null) {
//                    mScanMenuItem.setEnabled(false);
//                }
                break;
        }
    }

    @Override
    public void onAccessPointChanged(AccessPoint accessPoint) {
        Log.e(TAG, " onAccessPointChanged :" + accessPoint.toString());
    }

    @Override
    public void onLevelChanged(AccessPoint accessPoint) {
        Log.e(TAG, " onLevelChanged " + accessPoint.toString());
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        Log.e(TAG, " onPointerCaptureChanged " + hasCapture);
    }


    protected void setProgressBarVisible(boolean visible) {
        if (mProgressHeader != null) {
            mProgressHeader.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * 检查是否已经授予权限
     *
     * @return
     */
    private boolean checkPermission() {
        for (String permission : NEEDED_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 申请权限
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                NEEDED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasAllPermission = true;
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i : grantResults) {
                if (i != PackageManager.PERMISSION_GRANTED) {
                    hasAllPermission = false;   //判断用户是否同意获取权限
                    break;
                }
            }

            //如果同意权限
            if (hasAllPermission) {
                mHasPermission = true;
                if (mHasPermission) {  //如果wifi开关是开 并且 已经获取权限
                    mWifiTracker.startTracking();
                } else {
                    Toast.makeText(MainActivity.this, "WIFI处于关闭状态或权限获取失败", Toast.LENGTH_SHORT).show();
                }

            } else {  //用户不同意权限
                mHasPermission = false;
                Toast.makeText(MainActivity.this, "获取权限失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
