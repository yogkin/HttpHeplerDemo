/*
 * Copyright 2016 jeasonlzy(廖子尧)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.czm.httphelper.callback;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.Window;

import com.czm.httphelper.bean.LzyResponse;
import com.czm.httphelper.bean.SimpleResponse;
import com.czm.httphelper.utils.Convert;
import com.google.gson.stream.JsonReader;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.convert.Converter;
import com.lzy.okgo.request.base.Request;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧）Github地址：https://github.com/jeasonlzy
 * 版    本：1.0
 * 创建日期：2016/1/14
 * 描    述：默认将返回的数据解析成需要的Bean,可以是 BaseBean，String，List，Map
 * 修订历史：
 * ================================================
 */
public abstract class JsonCallback<T> extends AbsCallback<T> {

    private Type type;
    private Class<T> clazz;

    private ProgressDialog dialog;
    private AlertDialog mAlertDailog;

    private void initDialog(Activity activity, AlertDialog alertDialog) {
        dialog = new ProgressDialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("请求网络中...");

        if (alertDialog == null) {
            mAlertDailog = new AlertDialog.Builder(activity)
                    .setTitle("提示")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
        } else {
            mAlertDailog = alertDialog;
        }

    }

    @Override
    public void onFinish() {
        //网络请求结束后关闭对话框
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public JsonCallback(Activity activity) {
        initDialog(activity, null);
    }

    public JsonCallback(Activity activity, AlertDialog dialog) {
        initDialog(activity, dialog);
    }

    public JsonCallback() {
    }

    public JsonCallback(Type type) {
        this.type = type;
    }

    public JsonCallback(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void onStart(Request<T, ? extends Request> request) {
        super.onStart(request);
        // 主要用于在所有请求之前添加公共的请求头或请求参数
        // 例如登录授权的 token
        // 使用的设备信息
        // 可以随意添加,也可以什么都不传
        // 还可以在这里对所有的参数进行加密，均在这里实现

        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }

        request.headers("header1", "HeaderValue1")//
                .params("params1", "ParamsValue1")//
                .params("token", "3215sdf13ad1f65asd4f3ads1f");
    }

    /**
     * 该方法是子线程处理，不能做ui相关的工作
     * 主要作用是解析网络返回的 response 对象,生产onSuccess回调中需要的数据对象
     * 这里的解析工作不同的业务逻辑基本都不一样,所以需要自己实现,以下给出的时模板代码,实际使用根据需要修改
     */
    @Override
    public T convertResponse(Response response) throws Throwable {

        // 重要的事情说三遍，不同的业务，这里的代码逻辑都不一样，如果你不修改，那么基本不可用
        // 重要的事情说三遍，不同的业务，这里的代码逻辑都不一样，如果你不修改，那么基本不可用
        // 重要的事情说三遍，不同的业务，这里的代码逻辑都不一样，如果你不修改，那么基本不可用

        //详细自定义的原理和文档，看这里： https://github.com/jeasonlzy/okhttp-OkGo/wiki/JsonCallback

        if (type == null) {
            if (clazz == null) {
                Type genType = getClass().getGenericSuperclass();
                type = ((ParameterizedType) genType).getActualTypeArguments()[0];
            } else {
                JsonConvert<T> convert = new JsonConvert<>(clazz);
                return convert.convertResponse(response);
            }
        }


        JsonConvert<T> convert = new JsonConvert<>(type);
        return convert.convertResponse(response);
    }

    @Override
    public void onError(com.lzy.okgo.model.Response<T> response) {
        super.onError(response);
        if (mAlertDailog != null && !mAlertDailog.isShowing()) {
            mAlertDailog.setMessage(response.getException().getMessage());
            mAlertDailog.show();
        }
    }

    /**
     * ================================================
     * 作    者：jeasonlzy（廖子尧）Github地址：https://github.com/jeasonlzy
     * 版    本：1.0
     * 创建日期：16/9/11
     * 描    述：
     * 修订历史：
     * ================================================
     */
    public static class JsonConvert<T> implements Converter<T> {

        private Type type;
        private Class<T> clazz;

        public JsonConvert() {
        }

        public JsonConvert(Type type) {
            this.type = type;
        }

        public JsonConvert(Class<T> clazz) {
            this.clazz = clazz;
        }

        /**
         * 该方法是子线程处理，不能做ui相关的工作
         * 主要作用是解析网络返回的 response 对象，生成onSuccess回调中需要的数据对象
         * 这里的解析工作不同的业务逻辑基本都不一样,所以需要自己实现,以下给出的时模板代码,实际使用根据需要修改
         */
        @Override
        public T convertResponse(Response response) throws Throwable {

            // 重要的事情说三遍，不同的业务，这里的代码逻辑都不一样，如果你不修改，那么基本不可用
            // 重要的事情说三遍，不同的业务，这里的代码逻辑都不一样，如果你不修改，那么基本不可用
            // 重要的事情说三遍，不同的业务，这里的代码逻辑都不一样，如果你不修改，那么基本不可用

            // 如果你对这里的代码原理不清楚，可以看这里的详细原理说明: https://github.com/jeasonlzy/okhttp-OkGo/wiki/JsonCallback
            // 如果你对这里的代码原理不清楚，可以看这里的详细原理说明: https://github.com/jeasonlzy/okhttp-OkGo/wiki/JsonCallback
            // 如果你对这里的代码原理不清楚，可以看这里的详细原理说明: https://github.com/jeasonlzy/okhttp-OkGo/wiki/JsonCallback

            if (type == null) {
                if (clazz == null) {
                    // 如果没有通过构造函数传进来，就自动解析父类泛型的真实类型（有局限性，继承后就无法解析到）
                    Type genType = getClass().getGenericSuperclass();
                    type = ((ParameterizedType) genType).getActualTypeArguments()[0];
                } else {
                    return parseClass(response, clazz);
                }
            }

            if (type instanceof ParameterizedType) {
                return parseParameterizedType(response, (ParameterizedType) type);
            } else if (type instanceof Class) {
                return parseClass(response, (Class<?>) type);
            } else {
                return parseType(response, type);
            }
        }

        private T parseClass(Response response, Class<?> rawType) throws Exception {
            if (rawType == null) return null;
            ResponseBody body = response.body();
            if (body == null) return null;
            JsonReader jsonReader = new JsonReader(body.charStream());

            if (rawType == String.class) {
                //noinspection unchecked
                return (T) body.string();
            } else if (rawType == JSONObject.class) {
                //noinspection unchecked
                return (T) new JSONObject(body.string());
            } else if (rawType == JSONArray.class) {
                //noinspection unchecked
                return (T) new JSONArray(body.string());
            } else {
                T t = Convert.fromJson(jsonReader, rawType);
                response.close();
                return t;
            }
        }

        private T parseType(Response response, Type type) throws Exception {
            if (type == null) return null;
            ResponseBody body = response.body();
            if (body == null) return null;
            JsonReader jsonReader = new JsonReader(body.charStream());

            // 泛型格式如下： new JsonCallback<任意JavaBean>(this)
            T t = Convert.fromJson(jsonReader, type);
            response.close();
            return t;
        }

        private T parseParameterizedType(Response response, ParameterizedType type) throws Exception {
            if (type == null) return null;
            ResponseBody body = response.body();
            if (body == null) return null;
            JsonReader jsonReader = new JsonReader(body.charStream());

            Type rawType = type.getRawType();                     // 泛型的实际类型
            Type typeArgument = type.getActualTypeArguments()[0]; // 泛型的参数
            if (rawType != LzyResponse.class) {
                // 泛型格式如下： new JsonCallback<外层BaseBean<内层JavaBean>>(this)
                T t = Convert.fromJson(jsonReader, type);
                response.close();
                return t;
            } else {
                if (typeArgument == Void.class) {
                    // 泛型格式如下： new JsonCallback<LzyResponse<Void>>(this)
                    SimpleResponse simpleResponse = Convert.fromJson(jsonReader, SimpleResponse.class);
                    response.close();
                    //noinspection unchecked
                    return (T) simpleResponse.toLzyResponse();
                } else {
                    // 泛型格式如下： new JsonCallback<LzyResponse<内层JavaBean>>(this)
                    LzyResponse lzyResponse = Convert.fromJson(jsonReader, type);
                    response.close();
                    int code = lzyResponse.code;
                    //这里的0是以下意思
                    //一般来说服务器会和客户端约定一个数表示成功，其余的表示失败，这里根据实际情况修改
                    if (code == 0) {
                        //noinspection unchecked
                        return (T) lzyResponse;
                    } else if (code == 104) {
                        throw new IllegalStateException("用户授权信息无效");
                    } else if (code == 105) {
                        throw new IllegalStateException("用户收取信息已过期");
                    } else {
                        //直接将服务端的错误信息抛出，onError中可以获取
                        throw new IllegalStateException("错误代码：" + code + "，错误信息：" + lzyResponse.msg);
                    }
                }
            }
        }
    }
}
