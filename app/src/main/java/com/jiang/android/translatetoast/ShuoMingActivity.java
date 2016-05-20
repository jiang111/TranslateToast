package com.jiang.android.translatetoast;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ShuoMingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shuo_ming);
        final EditText name = (EditText) findViewById(R.id.sm_app);
        final EditText key = (EditText) findViewById(R.id.sm_key);
        TextView config = (TextView) findViewById(R.id.sm_config);
        config.setClickable(true);
        config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://fanyi.youdao.com/openapi?path=data-mode");
                Intent it = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(it);
            }
        });
        AppCompatButton commit = (AppCompatButton) findViewById(R.id.sm_commit);
        commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nRes = name.getText().toString();
                String keyRes = key.getText().toString();
                if (TextUtils.isEmpty(nRes) || TextUtils.isEmpty(keyRes)) {
                    showToast("数据不能为空");
                    return;
                }
                SharePrefUtil.saveString(ShuoMingActivity.this.getApplicationContext(), App.APP_NAME, nRes);
                SharePrefUtil.saveString(ShuoMingActivity.this.getApplicationContext(), App.KEY_NAME, keyRes);
                showToast("保存成功,请重启应用");
                ShuoMingActivity.this.finish();
            }
        });

        AppCompatButton clear = (AppCompatButton) findViewById(R.id.sm_clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharePrefUtil.saveString(ShuoMingActivity.this.getApplicationContext(), App.APP_NAME, App.keyfrom);
                SharePrefUtil.saveString(ShuoMingActivity.this.getApplicationContext(), App.KEY_NAME, App.API_KEY);
                showToast("清空配置成功,请重启应用");
                ShuoMingActivity.this.finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void showToast(String value) {

        Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
    }
}
