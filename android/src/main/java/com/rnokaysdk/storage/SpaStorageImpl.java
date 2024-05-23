package com.rnokaysdk.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.itransition.protectoria.psa_multitenant.data.SpaStorage;

public class SpaStorageImpl implements SpaStorage {

    private static String PREFERENCE_KEY = "firebase_instance_id";
    private static String APP_PNS = "app_pns";
    private static String EXTERNAL_ID = "external_id";
    private static String PUB_PSS_B64 = "pub_pss_b64";
    private static String ENROLLMENT_ID = "enrollment_id";
    private static String INSTALLATION_ID = "installation_id";
    private SharedPreferences sharedPreferences;

    public SpaStorageImpl(Context context) {
       sharedPreferences = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
    }

    @Override
    public void putAppPNS(String s) {
        sharedPreferences.edit().putString(APP_PNS, s).commit();
    }

    @Override
    public String getAppPNS() {
        return sharedPreferences.getString(APP_PNS, "");
    }

    @Override
    public void putExternalId(String s) {
        sharedPreferences.edit().putString(EXTERNAL_ID, s).commit();
    }

    @Override
    public String getExternalId() {
        return sharedPreferences.getString(EXTERNAL_ID, "");
    }

    @Override
    public void putEnrollmentId(String s) {
        sharedPreferences.edit().putString(ENROLLMENT_ID, s).commit();
    }

    @Override
    public String getEnrollmentId() {
        return sharedPreferences.getString(ENROLLMENT_ID, "");
    }

    @Override
    public void putInstallationId(String s) {
        sharedPreferences.edit().putString(INSTALLATION_ID, s).commit();
    }

    @Override
    public String getInstallationId() {
        return sharedPreferences.getString(INSTALLATION_ID, "");
    }

    @Override
    public void putPubPssBase64(String s) {
        sharedPreferences.edit().putString(PUB_PSS_B64, s).commit();
    }

    @Override
    public String getPubPssBase64() {
        return sharedPreferences.getString(PUB_PSS_B64, "");
    }
}
