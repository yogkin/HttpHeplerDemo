package com.czm.httpheplerdemo;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;

import com.czm.httphelper.api.Api;
import com.czm.httphelper.bean.LzyResponse;
import com.czm.httphelper.callback.JsonCallback;
import com.czm.httpheplerdemo.bean.LoginBean;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;
import com.orhanobut.logger.Logger;


public class MainActivity extends AppCompatActivity {

    private AppCompatButton mBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtn = findViewById(R.id.mbtn);


        AlertDialog dialog= new AlertDialog.Builder(this)
                .setTitle("提示")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create();



        mBtn.setOnClickListener(v -> OkGo.<LzyResponse<LoginBean>>post(Api.API_LOGIN)
//                action=user_login&UserCode=d11285555&Password=123456
                .params("user_login","user_login")
                .params("UserCode","d11285555")
                .params("Password","123456")
                .execute(new JsonCallback<LzyResponse<LoginBean>>(MainActivity.this,dialog) {
                    @Override
                    public void onSuccess(Response<LzyResponse<LoginBean>> response) {
                        Logger.d(response);
                    }
                }));
    }
}
