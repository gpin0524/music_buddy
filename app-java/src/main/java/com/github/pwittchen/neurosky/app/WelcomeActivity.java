package com.github.pwittchen.neurosky.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);



        new Handler().postDelayed(new Runnable()




                                  {
                                      @Override
                                      public void run() {

                                          Boolean isFirst = SharePreUtil.getBoolean(getApplicationContext(), ConstantValue.ISFIRST, true);
                                          if(isFirst){
                                              //进入包含了pager那个导航界面
                                              Intent intent = new Intent(getApplicationContext(), FirstTimeIntroActivity.class);
                                              startActivity(intent);
                                              //将isFirst改为false,并且在本地持久化
                                              SharePreUtil.saveBoolean(getApplicationContext(), ConstantValue.ISFIRST, false);
                                          }else{
                                              //进入应用程序主界面
                                              Intent intent = new Intent(getApplicationContext(), SituationSelect.class);
                                              startActivity(intent);
                                          }
                                          finish();
                                      }
                                  }




                , 2000);
    }
}
