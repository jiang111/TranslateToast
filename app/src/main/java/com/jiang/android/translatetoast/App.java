package com.jiang.android.translatetoast;

import android.app.Application;

import com.jiang.android.translatetoast.db.DbCore;

/**
 * Created by jiang on 5/20/16.
 */

public class App extends Application {

    public static final String API_KEY = "1264267832";

    public static final String keyfrom = "TranslateToastApp";

    @Override
    public void onCreate() {
        super.onCreate();
        DbCore.init(this);
    }
}
