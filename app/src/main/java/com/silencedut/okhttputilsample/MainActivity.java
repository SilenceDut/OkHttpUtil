package com.silencedut.okhttputilsample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.silencedut.okhttputil.OkHttpUtil;
import com.silencedut.okhttputil.ResponseCallBack;


import okhttp3.Call;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();
    private TextView mResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mResult = (TextView)findViewById(R.id.resultTv) ;
        for(int i=0;i<20;i++) {
            final int finalI = i;
            OkHttpUtil.getInstance().getAsync("http://nbaplus.sinaapp.com/api/v1.0/news/update", new ResponseCallBack<News>() {
                @Override
                public void onFailure(Call request, Exception e) {
                    mResult.setText(e.getMessage());
                }

                @Override
                public void onSucceeded(Call call, News news) {

                    mResult.setText(mResult.getText()+"\n"+"i:"+ finalI+news.toString());
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkHttpUtil.getInstance().cancelAllRequests();
    }
}
