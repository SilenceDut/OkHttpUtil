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
    public enum PostThread {
        UI_THREAD,WORK_THREAD
    }

    private OkHttpClient mOkHttpClient;
    private Gson mGson = new Gson();
    private Handler mMainHandler;
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


    public <T> void getAsync(final String url,PostThread postThread,final ResponseCallBack<T> responseCallBack) {
        final Request request = new Request.Builder()
                .url(url)
                .build();
        boolean runOnUIThread = PostThread.UI_THREAD.equals(postThread);
        executeRequest(request,runOnUIThread,responseCallBack);
    }


    /**
     *
     * @param request a http request
     * @param responseCallBack a callback where you can handle the result
     * @param <T> class type which you want convert to
     * @param postToUIThread if execute the responseCallBack on UI Thread
     */
    private <T> void executeRequest(Request request,final boolean postToUIThread,final ResponseCallBack<T> responseCallBack) {

        Call newCall = mOkHttpClient.newCall(request);


        newCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postFailInformation(call,e,postToUIThread,responseCallBack);

            }

            @Override
            public void onResponse( Call call, Response response) throws IOException {
                T result ;

                try {
                    if(String.class.equals(responseCallBack.tClass)) {
                        result = (T) response.body().string();
                    }else {
                        result = mGson.fromJson(response.body().string(), responseCallBack.tClass);
                    }
                    postSuccessResult(call,result,postToUIThread,responseCallBack);
                }catch (Exception e) {
                    postFailInformation(call,e,postToUIThread,responseCallBack);
                }
            }
        });


    }

    private <T >void postSuccessResult(final Call call,final T result,boolean postToUIThread,final ResponseCallBack<T> responseCallBack) {
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

    private  void postFailInformation(final Call call,final Exception e,boolean postToUIThread,final ResponseCallBack responseCallBack) {
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

    public void  cancelRequest(@Nullable String url) {
        for (Call call :mOkHttpClient.dispatcher().queuedCalls()) {
            if(call!=null&&call.request().tag().toString().equals(url)) {
                call.cancel();
            }
        }
    }


    public void cancelAllRequests (){
        if(mMainHandler==null) {
            return;
        }
        mMainHandler.removeCallbacksAndMessages(null);

        mOkHttpClient.dispatcher().cancelAll();
    }

}
