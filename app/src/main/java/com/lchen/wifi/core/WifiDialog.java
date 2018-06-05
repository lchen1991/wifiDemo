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

import com.lchen.wifi.R;

@RequiresApi(api = Build.VERSION_CODES.M)
class WifiDialog extends AlertDialog implements DialogInterface.OnClickListener {

    /* These values come from "wifi_peap_phase2_entries" resource array */
    public static final int WIFI_PEAP_PHASE2_NONE       = 0;
    public static final int WIFI_PEAP_PHASE2_MSCHAPV2   = 1;
    public static final int WIFI_PEAP_PHASE2_GTC        = 2;

    public interface WifiDialogListener {
        void onForget(WifiDialog dialog);
        void onSubmit(WifiDialog dialog);
    }

    private static final int BUTTON_SUBMIT = DialogInterface.BUTTON_POSITIVE;
    private static final int BUTTON_FORGET = DialogInterface.BUTTON_NEUTRAL;

    private final int mMode;
    private final WifiDialogListener mListener;
    private final AccessPoint mAccessPoint;

    private View mView;
//    private WifiConfigController mController;
    private boolean mHideSubmitButton;

    public WifiDialog(Context context, WifiDialogListener listener, AccessPoint accessPoint,
            int mode, boolean hideSubmitButton) {
        this(context, listener, accessPoint, mode);
        mHideSubmitButton = hideSubmitButton;
    }

    /*@Override
    public WifiConfigController getController() {
        return mController;
    }*/

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
        setView(mView);
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
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
            super.onRestoreInstanceState(savedInstanceState);
//            mController.updatePassword();
    }

    public void dispatchSubmit() {
        if (mListener != null) {
            mListener.onSubmit(this);
        }
        dismiss();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int id) {
        if (mListener != null) {
            switch (id) {
                case BUTTON_SUBMIT:
                    mListener.onSubmit(this);
                    break;
                case BUTTON_FORGET:
                   /* if (WifiSettings.isEditabilityLockedDown(
                            getContext(), mAccessPoint.getConfig())) {
                        RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getContext(),
                                RestrictedLockUtils.getDeviceOwner(getContext()));
                        return;
                    }*/
                    mListener.onForget(this);
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
                if (mPasswordView.length() != 0) {
                    int length = mPasswordView.length();
                    String password = mPasswordView.getText().toString();
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
                if (mPasswordView.length() != 0) {
                    String password = mPasswordView.getText().toString();
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
                        switch(phase2Method) {
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

                String caCert = (String) mEapCaCertSpinner.getSelectedItem();
                config.enterpriseConfig.setCaCertificateAliases(null);
                config.enterpriseConfig.setCaPath(null);
                config.enterpriseConfig.setDomainSuffixMatch(mEapDomainView.getText().toString());
                if (caCert.equals(mUnspecifiedCertString)
                        || caCert.equals(mDoNotValidateEapServerString)) {
                    // ca_cert already set to null, so do nothing.
                } else if (caCert.equals(mUseSystemCertsString)) {
                    config.enterpriseConfig.setCaPath(SYSTEM_CA_STORE_PATH);
                } else if (caCert.equals(mMultipleCertSetString)) {
                    if (mAccessPoint != null) {
                        if (!mAccessPoint.isSaved()) {
                            Log.e("info", "Multiple certs can only be set "
                                    + "when editing saved network");
                        }
                        config.enterpriseConfig.setCaCertificateAliases(
                                mAccessPoint
                                        .getConfig()
                                        .enterpriseConfig
                                        .getCaCertificateAliases());
                    }
                } else {
                    config.enterpriseConfig.setCaCertificateAliases(new String[] {caCert});
                }

                // ca_cert or ca_path should not both be non-null, since we only intend to let
                // the use either their own certificate, or the system certificates, not both.
                // The variable that is not used must explicitly be set to null, so that a
                // previously-set value on a saved configuration will be erased on an update.
                if (config.enterpriseConfig.getCaCertificateAliases() != null
                        && config.enterpriseConfig.getCaPath() != null) {
                    Log.e(TAG, "ca_cert ("
                            + config.enterpriseConfig.getCaCertificateAliases()
                            + ") and ca_path ("
                            + config.enterpriseConfig.getCaPath()
                            + ") should not both be non-null");
                }

                String clientCert = (String) mEapUserCertSpinner.getSelectedItem();
                if (clientCert.equals(mUnspecifiedCertString)
                        || clientCert.equals(mDoNotProvideEapUserCertString)) {
                    // Note: |clientCert| should not be able to take the value |unspecifiedCert|,
                    // since we prevent such configurations from being saved.
                    clientCert = "";
                }
                config.enterpriseConfig.setClientCertificateAlias(clientCert);
                if (eapMethod == Eap.SIM || eapMethod == Eap.AKA || eapMethod == Eap.AKA_PRIME) {
                    config.enterpriseConfig.setIdentity("");
                    config.enterpriseConfig.setAnonymousIdentity("");
                } else if (eapMethod == Eap.PWD) {
                    config.enterpriseConfig.setIdentity(mEapIdentityView.getText().toString());
                    config.enterpriseConfig.setAnonymousIdentity("");
                } else {
                    config.enterpriseConfig.setIdentity(mEapIdentityView.getText().toString());
                    config.enterpriseConfig.setAnonymousIdentity(
                            mEapAnonymousView.getText().toString());
                }

                if (mPasswordView.isShown()) {
                    // For security reasons, a previous password is not displayed to user.
                    // Update only if it has been changed.
                    if (mPasswordView.length() > 0) {
                        config.enterpriseConfig.setPassword(mPasswordView.getText().toString());
                    }
                } else {
                    // clear password
                    config.enterpriseConfig.setPassword(mPasswordView.getText().toString());
                }
                break;
            default:
                return null;
        }

        config.setIpConfiguration(
                new IpConfiguration(mIpAssignment, mProxySettings,
                        mStaticIpConfiguration, mHttpProxy));

        return config;
    }
}
