package com.lchen.wifi.core;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.lchen.wifi.R;

@RequiresApi(api = Build.VERSION_CODES.M)
public class WifiDialog extends AlertDialog implements DialogInterface.OnClickListener {

    /* These values come from "wifi_peap_phase2_entries" resource array */
    public static final int WIFI_PEAP_PHASE2_NONE = 0;
    public static final int WIFI_PEAP_PHASE2_MSCHAPV2 = 1;
    public static final int WIFI_PEAP_PHASE2_GTC = 2;

    public interface WifiDialogListener {
        void onForget(WifiDialog dialog, AccessPoint mAccessPoint);

        void onSubmit(WifiDialog dialog, AccessPoint mAccessPoint);
    }

    private static final int BUTTON_SUBMIT = DialogInterface.BUTTON_POSITIVE;
    private static final int BUTTON_FORGET = DialogInterface.BUTTON_NEUTRAL;

    private final int mMode;
    private final WifiDialogListener mListener;
    private final AccessPoint mAccessPoint;

    private View mView;
    //    private WifiConfigController mController;
    private boolean mHideSubmitButton;

    private EditText mPwdEdt;
    private Button mBtnSub;
    private Button mBtnFGT;

    public WifiDialog(Context context, WifiDialogListener listener, AccessPoint accessPoint,
                      int mode, boolean hideSubmitButton) {
        this(context, listener, accessPoint, mode);
        mHideSubmitButton = hideSubmitButton;
    }

    public WifiDialog(Context context, WifiDialogListener listener, AccessPoint accessPoint,
                      int mode) {
        super(context);
        mMode = mode;
        mListener = listener;
        mAccessPoint = accessPoint;
        mHideSubmitButton = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mView = getLayoutInflater().inflate(R.layout.wifi_dialog, null);
        setContentView(mView);
        mPwdEdt = mView.findViewById(R.id.wifi_pwd);
        mBtnSub = mView.findViewById(R.id.wifi_sub);
        mBtnFGT = mView.findViewById(R.id.wifi_fgt);
        setInverseBackgroundForced(true);
      /*  mController = new WifiConfigController(this, mView, mAccessPoint, mMode);
        super.onCreate(savedInstanceState);

        if (mHideSubmitButton) {
            mController.hideSubmitButton();
        } else {
            *//* During creation, the submit button can be unavailable to determine
             * visibility. Right after creation, update button visibility *//*
            mController.enableSubmitIfAppropriate();
        }

        if (mAccessPoint == null) {
            mController.hideForgetButton();
        }*/

        mBtnSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onSubmit(WifiDialog.this, mAccessPoint);
                dismiss();
            }
        });
        mBtnFGT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onForget(WifiDialog.this, mAccessPoint);
                dismiss();
            }
        });
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
//            mController.updatePassword();
    }

    public void dispatchSubmit() {
        if (mListener != null) {
            mListener.onSubmit(this, mAccessPoint);
        }
        dismiss();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int id) {
        if (mListener != null) {
            switch (id) {
                case BUTTON_SUBMIT:
                    mListener.onSubmit(this, mAccessPoint);
                    break;
                case BUTTON_FORGET:
                   /* if (WifiSettings.isEditabilityLockedDown(
                            getContext(), mAccessPoint.getConfig())) {
                        RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getContext(),
                                RestrictedLockUtils.getDeviceOwner(getContext()));
                        return;
                    }*/
                    mListener.onForget(this, mAccessPoint);
                    break;
            }
        }
    }

    public int getMode() {
        return mMode;
    }

    public Button getSubmitButton() {
        return getButton(BUTTON_SUBMIT);
    }

    public Button getForgetButton() {
        return getButton(BUTTON_FORGET);
    }

    public Button getCancelButton() {
        return getButton(BUTTON_NEGATIVE);
    }

    public void setSubmitButton(CharSequence text) {
        setButton(BUTTON_SUBMIT, text, this);
    }

    public void setForgetButton(CharSequence text) {
        setButton(BUTTON_FORGET, text, this);
    }

    public void setCancelButton(CharSequence text) {
        setButton(BUTTON_NEGATIVE, text, this);
    }

    public WifiConfiguration getConfig() {
        WifiConfiguration config = new WifiConfiguration();
        if (mAccessPoint == null) {
            config.SSID = AccessPoint.convertToQuotedString(mAccessPoint.getSsidStr());
            // If the user adds a network manually, assume that it is hidden.
            config.hiddenSSID = true;
        } else if (!mAccessPoint.isSaved()) {
            config.SSID = AccessPoint.convertToQuotedString(mAccessPoint.getSsidStr());
        } else {
            config.networkId = mAccessPoint.getConfig().networkId;
        }

        switch (mAccessPoint.getSecurity()) {
            case AccessPoint.SECURITY_NONE:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                break;

            case AccessPoint.SECURITY_WEP:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                if (mPwdEdt.length() != 0) {
                    int length = mPwdEdt.length();
                    String password = mPwdEdt.getText().toString();
                    // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                    if ((length == 10 || length == 26 || length == 58)
                            && password.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = password;
                    } else {
                        config.wepKeys[0] = '"' + password + '"';
                    }
                }
                break;

            case AccessPoint.SECURITY_PSK:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                if (mPwdEdt.length() != 0) {
                    String password = mPwdEdt.getText().toString();
                    if (password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = password;
                    } else {
                        config.preSharedKey = '"' + password + '"';
                    }
                }
                break;

            case AccessPoint.SECURITY_EAP:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
                config.enterpriseConfig = new WifiEnterpriseConfig();
                int eapMethod = config.enterpriseConfig.getEapMethod();
                int phase2Method = config.enterpriseConfig.getPhase2Method();
                config.enterpriseConfig.setEapMethod(eapMethod);
                switch (eapMethod) {
                    case WifiEnterpriseConfig.Eap.PEAP:
                        // PEAP supports limited phase2 values
                        // Map the index from the mPhase2PeapAdapter to the one used
                        // by the API which has the full list of PEAP methods.
                        switch (phase2Method) {
                            case WIFI_PEAP_PHASE2_NONE:
                                config.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.NONE);
                                break;
                            case WIFI_PEAP_PHASE2_MSCHAPV2:
                                config.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.MSCHAPV2);
                                break;
                            case WIFI_PEAP_PHASE2_GTC:
                                config.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.GTC);
                                break;
                            default:
                                Log.e("info", "Unknown phase2 method" + phase2Method);
                                break;
                        }
                        break;
                    default:
                        // The default index from mPhase2FullAdapter maps to the API
                        config.enterpriseConfig.setPhase2Method(phase2Method);
                        break;
                }

                // clear password
                config.enterpriseConfig.setPassword(mPwdEdt.getText().toString());
                break;
            default:
                return null;
        }
        return config;
    }
}
