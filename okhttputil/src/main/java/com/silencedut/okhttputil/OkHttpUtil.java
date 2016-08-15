package com.silencedut.okhttputil;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by SilenceDut on 16/8/15.
 */

public class OkHttpUtil {
    private OkHttpClient mOkHttpClient;
    private Gson mGson = new Gson();
    private Handler mMainHandler;
    private Map<String,Call> mCallMap = new ConcurrentHashMap<>();
    private OkHttpUtil() {
        mOkHttpClient = new OkHttpClient();
        mMainHandler = new Handler(Looper.getMainLooper());

    }

    private static class LazyHolder {
        private static OkHttpUtil INSTANCE = new OkHttpUtil();
    }

    public static OkHttpUtil getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void setOkHttpClient(@Nullable OkHttpClient okHttpClient) {
        if(okHttpClient==null) {
            return;
        }
        this.mOkHttpClient = okHttpClient;
    }

    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    public <T> void getAsync(final String url,final ResponseCallBack<T> responseCallBack) {

        get(url, responseCallBack, true);
    }


    public <T> void getOnWorkThread(final String url,final ResponseCallBack<T> responseCallBack) {

        get(url, responseCallBack, false);
    }

    /**
     *
     * @param url a http request url
     * @param responseCallBack a callback where you can handle the result
     * @param <T> class type which you want convert to
     * @param postToUIThread if execute the responseCallBack on  UI Thread
     */
    private <T> void get(String url,  final ResponseCallBack<T> responseCallBack,final boolean postToUIThread) {

        final Request request = new Request.Builder()
                .url(url)
                .build();
        Call newCall = mOkHttpClient.newCall(request);
        mCallMap.put(url,newCall);

            newCall.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    postFailInformation(responseCallBack,call,e,postToUIThread);
                    mCallMap.remove(call.request().url());
                }

                @Override
                public void onResponse( Call call, Response response) throws IOException {
                    mCallMap.remove(call.request().url());
                    try {
                        final T result = mGson.fromJson(response.body().string(),responseCallBack.tClass);
                        postSuccessResult(responseCallBack,call,result,postToUIThread);
                    }catch (Exception e) {
                        postFailInformation(responseCallBack,call,e,postToUIThread);
                    }
                }
            });


    }

    private <T >void postSuccessResult(final ResponseCallBack<T> responseCallBack,final Call call,final T result,boolean postToUIThread) {
        if(!postToUIThread) {
            responseCallBack.onSucceeded(call,result);
            return;
        }
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                responseCallBack.onSucceeded(call,result);
            }
        });
    }

    private  void postFailInformation(final ResponseCallBack responseCallBack,final Call call,final Exception e,boolean postToUIThread) {
        if(!postToUIThread) {
            responseCallBack.onFailure(call,e);
            return;
        }
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                responseCallBack.onFailure(call,e);
            }
        });
    }

    public void  cancelRequest(String url) {
        Call call = mCallMap.get(url);
        if(call!=null) {
            call.cancel();
        }
        mCallMap.remove(url);
    }

    public void cancelAllRequests (){
        if(mMainHandler==null) {
            return;
        }
        mMainHandler.removeCallbacksAndMessages(null);

        for(Call call:mCallMap.values()) {
            if(call!=null) {
                call.cancel();
            }
        }
        mCallMap.clear();
    }


}
