package com.jiang.android.translatetoast.clipboard;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jiang.android.translatetoast.App;
import com.jiang.android.translatetoast.TipViewController;
import com.jiang.android.translatetoast.Utils;
import com.jiang.android.translatetoast.db.model.Translate;
import com.jiang.android.translatetoast.http.RestAdapter;
import com.jiang.android.translatetoast.model.TranslateModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.jiang.android.translatetoast.db.DbUtil.getTranslateService;


public final class ListenClipboardService extends Service implements TipViewController.ViewDismissHandler {

    private static final String KEY_FOR_WEAK_LOCK = "weak-lock";
    private static final String KEY_FOR_CMD = "cmd";
    private static final String KEY_FOR_CONTENT = "content";
    private static final String CMD_TEST = "test";
    private static final String TAG = "ListenClipboardService";

    private Gson mGson = null;

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
        if (mGson == null) {
            mGson = new GsonBuilder().create();
        }
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

        getDataFromNetOrDB();

    }

    private void getDataFromNetOrDB() {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    List<Translate> list = getTranslateService().query("where search_name=?", sLastContent.toString());
                    String search_value;
                    TranslateModel translateModel = null;
                    if (list != null && list.size() > 0) {
                        search_value = list.get(0).getSearch_result();
                        translateModel = getGson().fromJson(search_value, TranslateModel.class);
                        String resultStr = getResultStrByModel(translateModel);
                        subscriber.onNext(resultStr);
                    } else {
                        subscriber.onNext(null);
                    }
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        if (TextUtils.isEmpty(s)) {
                            getDataFromNet();
                        } else {
                            if (mTipViewController != null) {
                                mTipViewController.updateContent(sLastContent, s);
                            } else {
                                mTipViewController = new TipViewController(getApplication(), sLastContent, s);
                                mTipViewController.setViewDismissHandler(ListenClipboardService.this);
                                mTipViewController.show();
                            }
                        }
                    }
                });
    }

    private void getDataFromNet() {
        Call<TranslateModel> call = RestAdapter.getApiService().translate(
                App.keyfrom,
                App.API_KEY,
                "data",
                "json",
                "show",
                "1.1",
                sLastContent.toString()
        );
        call.enqueue(new Callback<TranslateModel>() {
                         @Override
                         public void onResponse(Call<TranslateModel> call, Response<TranslateModel> response) {
                             Log.i(TAG, "onResponse: " + response.code());
                             if (response.code() == 200) {
                                 final TranslateModel translateModel = response.body();
                                 Observable.create(new Observable.OnSubscribe<String>() {
                                     @Override
                                     public void call(Subscriber<? super String> subscriber) {
                                         try {
                                             String dbStr = getGson().toJson(translateModel);
                                             getTranslateService().save(new Translate(getTranslateService().count() + 1, sLastContent.toString(), dbStr));
                                             subscriber.onNext("success");
                                             subscriber.onCompleted();
                                         } catch (Exception e) {
                                             subscriber.onError(e);
                                         }
                                     }
                                 }).subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
                                         .subscribe(new Action1<String>() {
                                             @Override
                                             public void call(String s) {
                                                 Log.i(TAG, "call: " + s);
                                             }
                                         });

                                 String resultStr = getResultStrByModel(translateModel);
                                 if (mTipViewController != null) {
                                     mTipViewController.updateContent(sLastContent, resultStr);
                                 } else {
                                     mTipViewController = new TipViewController(getApplication(), sLastContent, resultStr);
                                     mTipViewController.setViewDismissHandler(ListenClipboardService.this);
                                     mTipViewController.show();
                                 }
                             } else {
                                 showToast(response.message());
                             }
                         }

                         @Override
                         public void onFailure(Call<TranslateModel> call, Throwable t) {
                             showToast(t.getMessage());
                         }

                     }
        );
    }

    private void showToast(String message) {
        Toast.makeText(this.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private String getResultStrByModel(TranslateModel resultModel) {
        return Utils.getResultByModel(resultModel);
    }

    private Gson getGson() {
        if (mGson == null) {
            mGson = new GsonBuilder().create();
        }
        return mGson;
    }

    @Override
    public void onViewDismiss() {
        sLastContent = null;
        mTipViewController = null;
    }

    public static interface HttpCallBack {
        void call(TranslateModel model);
    }
}