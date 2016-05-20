package com.jiang.android.translatetoast;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import com.jiang.android.translatetoast.model.TranslateModel;

public final class Utils {

    private final static String LOG_TAG = "uc-toast";

    public static String bundleToString(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        String string = "Bundle{";
        for (String key : bundle.keySet()) {
            string += " " + key + " => " + bundle.get(key) + ";";
        }
        string += " }Bundle";
        return string;
    }

    public static void printIntent(String tag, Intent intent) {
        if (intent == null || intent.getExtras() == null) {
            Log.d(LOG_TAG, String.format("%s, intent: %s", tag, intent));
            return;
        }

        Bundle bundle = intent.getExtras();
        Log.d(LOG_TAG, String.format("%s, intent: %s, %s", tag, intent, bundleToString(bundle)));
    }

    public static String getResultByModel(TranslateModel resultModel) {

        StringBuilder result = new StringBuilder();
        if (resultModel.getBasic() == null || resultModel.getBasic().getExplains() == null) {
            result.append("暂无结果");
            return result.toString();
        }
        int size = resultModel.getBasic().getExplains().size();
        if (size == 0) {
            result.append("暂无结果");
        } else {
            for (int i = 0; i < size; i++) {
                result.append(resultModel.getBasic().getExplains().get(i)).append(" \n");
            }
        }
        return result.substring(0, result.length() - 2);

    }

    public static boolean isNetworkConnected(Context ct) {
        ConnectivityManager cm = (ConnectivityManager) ct.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }
}
