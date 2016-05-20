package com.jiang.android.translatetoast;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jiang.android.recyclerview.BaseAdapter;
import com.jiang.android.recyclerview.holder.BaseViewHolder;
import com.jiang.android.translatetoast.clipboard.ListenClipboardService;
import com.jiang.android.translatetoast.db.DbUtil;
import com.jiang.android.translatetoast.db.model.Translate;
import com.jiang.android.translatetoast.model.TranslateModel;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {


    private RecyclerView mRecyclerView;
    private List<TranslateModel> mLists = new ArrayList<>();
    private Subscription subscription;
    private Gson mGson;

    public static void startForContent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.main_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(new BaseAdapter() {
            @Override
            protected void onBindView(BaseViewHolder holder, int position) {
                TextView content = holder.getView(R.id.main_view_text);
                TextView result = holder.getView(R.id.main_view_text_value);
                content.setText(mLists.get(position).getQuery());
                result.setText(Utils.getResultByModel(mLists.get(position)));
            }

            @Override
            protected int getLayoutID(int position) {
                return R.layout.main_item;
            }

            @Override
            public int getItemCount() {
                return mLists.size();
            }
        });
        ListenClipboardService.start(this);
        getDateFromDb();
    }

    private void getDateFromDb() {
        subscription = Observable.create(new Observable.OnSubscribe<TranslateModel>() {
            @Override
            public void call(Subscriber<? super TranslateModel> subscriber) {

                try {
                    List<Translate> list = DbUtil.getTranslateService().queryAll();
                    if (list != null && list.size() > 0) {
                        for (int i = 0; i < list.size(); i++) {
                            TranslateModel model = getGson().fromJson(list.get(i).getSearch_result(), TranslateModel.class);
                            subscriber.onNext(model);
                        }
                    }
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }


            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<TranslateModel>() {
                    @Override
                    public void onCompleted() {
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        showToast(e.toString());
                    }

                    @Override
                    public void onNext(TranslateModel translateModels) {
                        mLists.add(translateModels);

                    }
                });

    }

    private Gson getGson() {
        if (mGson == null) {
            mGson = new GsonBuilder().create();
        }
        return mGson;
    }

    private void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }


    public static interface CallBack {
        void onCompleted();
    }

}
