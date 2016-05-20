package com.jiang.android.translatetoast;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {


    private static final String SPLIT = "----";
    private RecyclerView mRecyclerView;
    private List<TranslateModel> mLists = new ArrayList<>();
    private Subscription subscription;
    private Gson mGson;
    private AlertDialog mDialog;
    private Subscription subscriptionSave;
    private BaseAdapter mAdapter;
    private AlertDialog mDeleteDialog;
    private AlertDialog mClearDialog;
    private String path;
    private AlertDialog mImportDialog;

    public static void startForContent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.main_recyclerview);
        ListenClipboardService.start(this);
        getDateFromDb();
        initData();

    }

    private void initData() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new BaseAdapter() {
            @Override
            protected void onBindView(BaseViewHolder holder, final int position) {
                TextView content = holder.getView(R.id.main_view_text);
                TextView result = holder.getView(R.id.main_view_text_value);
                content.setText(mLists.get(position).getQuery());
                result.setText(Utils.getResultByModel(mLists.get(position)));
                holder.getmConvertView().setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (mDeleteDialog == null) {
                            mDeleteDialog = new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("确认删除?")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            DbUtil.getTranslateService().deleteByKey(mLists.get(position).getId());
                                            mLists.remove(position);
                                            notifyDataSetChanged();
                                        }
                                    }).setNegativeButton("取消", null)
                                    .create();
                        }
                        mDeleteDialog.show();
                        return true;
                    }
                });
            }

            @Override
            protected int getLayoutID(int position) {
                return R.layout.main_item;
            }

            @Override
            public int getItemCount() {
                return mLists.size();
            }
        };
        mRecyclerView.setAdapter(mAdapter);

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
                            model.setId(list.get(i).getId());
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
            mGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        }
        return mGson;
    }

    private void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            String url = "https://github.com/jiang111/TranslateToast";
            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(myIntent);
            return true;
        }
        if (id == R.id.action_export) {
            if (mDialog == null) {
                mDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("确认导出?")
                        .setMessage("将把数据库中的数据保存成TXT文本,并拷贝到SD卡")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                saveData();
                            }
                        }).setNegativeButton("取消", null)
                        .create();
            }
            mDialog.show();
            return true;
        }

        if (id == R.id.action_import) {
            if (mImportDialog == null) {
                final EditText editText = new EditText(this);
                editText.setText(Environment.getExternalStorageDirectory() + "/");
                mImportDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("输入路径")
                        .setView(editText)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String path = editText.getText().toString();
                                getDataFromPath(path);

                            }
                        }).setNegativeButton("取消", null)
                        .create();
            }
            mImportDialog.show();
            return true;
        }

        if (id == R.id.action_config) {
            startActivity(new Intent(this, ShuoMingActivity.class));
            return true;
        }

        if (id == R.id.action_clear) {
            if (mClearDialog == null) {
                mClearDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("确认清空数据?")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                clearAllData();
                            }
                        }).setNegativeButton("取消", null)
                        .create();
            }
            mClearDialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getDataFromPath(final String path) {
        Observable.create(new Observable.OnSubscribe<TranslateModel>() {
            @Override
            public void call(Subscriber<? super TranslateModel> subscriber) {
                try {
                    String result = Utils.ReadTxtFile(MainActivity.this, path);
                    String[] data = result.split(SPLIT);
                    if (data == null || data.length == 0) {
                        subscriber.onError(new NullPointerException("没有数据"));
                    }
                    for (int i = 0; i < data.length; i++) {
                        String itemData = data[i];
                        TranslateModel model = mGson.fromJson(itemData, TranslateModel.class);
                        DbUtil.getTranslateService().save(new Translate(System.currentTimeMillis(), model.getQuery(), itemData));
                        subscriber.onNext(model);
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
                        mAdapter.notifyDataSetChanged();

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

    private void clearAllData() {
        Single.create(new Single.OnSubscribe<String>() {
            @Override
            public void call(SingleSubscriber<? super String> singleSubscriber) {
                try {
                    DbUtil.getTranslateService().deleteAll();
                    singleSubscriber.onSuccess("success");
                } catch (Exception e) {
                    singleSubscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        mLists.clear();
                        showToast("已全部清空");
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        showToast(e.toString());

                    }

                    @Override
                    public void onNext(String s) {

                    }
                });
    }

    private void saveData() {
        subscriptionSave = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    boolean isSuccess = saveData2Local();
                    if (isSuccess) {
                        subscriber.onNext("保存成功:" + path);
                    } else {
                        subscriber.onNext("保存失败");
                    }
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {

                    private ProgressDialog progressDialog;

                    @Override
                    public void onStart() {
                        super.onStart();
                        progressDialog = new ProgressDialog(MainActivity.this);
                        progressDialog.setTitle("处理中...");
                        progressDialog.show();

                    }

                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        progressDialog.dismiss();
                        showToast(e.toString());

                    }

                    @Override
                    public void onNext(String s) {
                        showToast(s);

                    }
                });

    }

    private boolean saveData2Local() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < mLists.size(); i++) {
            builder.append(getGson().toJson(mLists.get(i))).append(SPLIT);
        }
        path = Environment.getExternalStorageDirectory() + "/translatehelper/" + System.
                currentTimeMillis() + ".txt";
        boolean isSuccess = Utils.writeTxtToFile(this, builder.toString(), Environment.getExternalStorageDirectory() + "/translatehelper/", System.currentTimeMillis() + ".txt");
        return isSuccess;
    }


    @Override
    protected void onDestroy() {
        if (subscriptionSave != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        if (subscriptionSave != null && !subscriptionSave.isUnsubscribed()) {
            subscriptionSave.unsubscribe();
        }
        super.onDestroy();
    }

    public static interface CallBack {
        void onCompleted();
    }

}
