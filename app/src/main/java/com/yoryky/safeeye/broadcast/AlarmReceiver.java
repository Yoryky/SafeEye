package com.yoryky.safeeye.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.yoryky.safeeye.activity.MainActivity;

public class AlarmReceiver extends BroadcastReceiver {
    private MainActivity mActivity;
    public AlarmReceiver(MainActivity activity){
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if("com.yoryky.safeeye.alarm.receiver".equals(intent.getAction())){
            int overtime = intent.getIntExtra("overtime",0);
            Log.d("yjing","overtime =  " + overtime);
            mActivity.updateOvertime(overtime);
        }
    }
}
