package com.jiang.android.translatetoast;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.TextUtils;

import com.jiang.android.translatetoast.clipboard.ClipboardManagerCompat;
import com.jiang.android.translatetoast.model.TranslateModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public final class ListenClipboardService extends Service implements TipViewController.ViewDismissHandler {

    private static final String KEY_FOR_WEAK_LOCK = "weak-lock";
    private static final String KEY_FOR_CMD = "cmd";
    private static final String KEY_FOR_CONTENT = "content";
    private static final String CMD_TEST = "test";

    private static CharSequence sLastContent = null;
    private ClipboardManagerCompat mClipboardWatcher;
    private TipViewController mTipViewController;
    private ClipboardManagerCompat.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener = new ClipboardManagerCompat.OnPrimaryClipChangedListener() {
        public void onPrimaryClipChanged() {
            performClipboardCheck();
        }
    };

    public static void start(Context context) {
        Intent serviceIntent = new Intent(context, ListenClipboardService.class);
        context.startService(serviceIntent);
    }

    /**
     * for dev
     */
    public static void startForTest(Context context, String content) {

        Intent serviceIntent = new Intent(context, ListenClipboardService.class);
        serviceIntent.putExtra(KEY_FOR_CMD, CMD_TEST);
        serviceIntent.putExtra(KEY_FOR_CONTENT, content);
        context.startService(serviceIntent);
    }

    public static void startForWeakLock(Context context, Intent intent) {

        Intent serviceIntent = new Intent(context, ListenClipboardService.class);
        context.startService(serviceIntent);

        intent.putExtra(ListenClipboardService.KEY_FOR_WEAK_LOCK, true);
        Intent myIntent = new Intent(context, ListenClipboardService.class);

        // using wake lock to start service
        WakefulBroadcastReceiver.startWakefulService(context, myIntent);
    }

    @Override
    public void onCreate() {
        mClipboardWatcher = ClipboardManagerCompat.create(this);
        mClipboardWatcher.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mClipboardWatcher.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);

        sLastContent = null;
        if (mTipViewController != null) {
            mTipViewController.setViewDismissHandler(null);
            mTipViewController = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Utils.printIntent("onStartCommand", intent);

        if (intent != null) {
            // remove wake lock
            if (intent.getBooleanExtra(KEY_FOR_WEAK_LOCK, false)) {
                BootCompletedReceiver.completeWakefulIntent(intent);
            }
            String cmd = intent.getStringExtra(KEY_FOR_CMD);
            if (!TextUtils.isEmpty(cmd)) {
                if (cmd.equals(CMD_TEST)) {
                    String content = intent.getStringExtra(KEY_FOR_CONTENT);
                    showContent(content);
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void performClipboardCheck() {
        CharSequence content = mClipboardWatcher.getText();
        if (TextUtils.isEmpty(content)) {
            return;
        }
        showContent(content);
    }

    private void showContent(CharSequence content) {
        if (sLastContent != null && sLastContent.equals(content) || content == null) {
            return;
        }
        sLastContent = content;

        Call<TranslateModel> call = RestAdapter.getApiService().translate(
                "TranslateToastApp",
                "1264267832",
                "data",
                "json",
                "show",
                "1.1",
                sLastContent.toString()
        );
        call.enqueue(new Callback<TranslateModel>() {
            @Override
            public void onResponse(Call<TranslateModel> call, Response<TranslateModel> response) {
                if (response.code() == 200) {
                    StringBuilder result = new StringBuilder();
                    TranslateModel resultModel = response.body();
                    int size = resultModel.getBasic().getExplains().size();
                    if (size == 0) {
                        result.append("暂无结果");
                    } else {
                        for (int i = 0; i < size; i++) {
                            result.append(resultModel.getBasic().getExplains().get(i)).append(" \n");
                        }
                    }
                    String resultStr = result.substring(0, result.length() - 2);
                    if (mTipViewController != null) {
                        mTipViewController.updateContent(sLastContent, resultStr);
                    } else {
                        mTipViewController = new TipViewController(getApplication(), sLastContent, resultStr);
                        mTipViewController.setViewDismissHandler(ListenClipboardService.this);
                        mTipViewController.show();
                    }
                }
            }

            @Override
            public void onFailure(Call<TranslateModel> call, Throwable t) {

            }
        });


    }

    @Override
    public void onViewDismiss() {
        sLastContent = null;
        mTipViewController = null;
    }
}