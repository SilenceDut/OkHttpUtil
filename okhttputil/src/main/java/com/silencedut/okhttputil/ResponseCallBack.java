package com.silencedut.okhttputil;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

import okhttp3.Call;

/**
 * Created by SilenceDut on 16/8/15.
 */

public abstract class  ResponseCallBack<T> {
     Class <T>  tClass = (Class <T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[ 0 ];
     public abstract void onFailure(Call request, Exception e);
     public abstract void onSucceeded(Call call,T result);

}
