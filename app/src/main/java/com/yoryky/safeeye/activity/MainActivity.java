package com.yoryky.safeeye.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.yoryky.safeeye.R;
import com.yoryky.safeeye.broadcast.AlarmReceiver;
import com.yoryky.safeeye.service.AlarmService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private NumberPicker numberPicker;
    private Button btnStart;
    AlarmReceiver alarmReceiver;
    IntentFilter intentFilter;
    AlertDialog alertDialog;
    private TextView tvOverTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.initViews();
        this.initEvents();
        alarmReceiver = new AlarmReceiver(this);
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.yoryky.safeeye.alarm.receiver");
        registerReceiver(alarmReceiver, intentFilter);
        checkPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Settings.canDrawOverlays(this)) {
            // SYSTEM_ALERT_WINDOW permission not granted...
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("悬浮窗设置提醒");
            builder.setMessage("请将悬浮穿设置为允许");
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setPositiveButton("立即设置", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    checkPermission();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alertDialog = builder.create();
            alertDialog.setCancelable(false);
            alertDialog.show();
        } else if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_setting, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting:
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initViews() {
        numberPicker = (NumberPicker) findViewById(R.id.np_picker);
        btnStart = (Button) findViewById(R.id.btn_start);
        tvOverTime = (TextView) findViewById(R.id.tv_overtime);
        numberPicker.setMaxValue(60);
        numberPicker.setMinValue(20);
        numberPicker.setValue(40);
    }

    private void initEvents() {
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AlarmService.class);
                intent.putExtra("time", numberPicker.getValue());
                startService(intent);
            }
        });
    }


    public void updateOvertime(int overtime) {
        Log.d(TAG, "updateOvertime = " + overtime);
        StringBuilder sb = new StringBuilder(String.valueOf(overtime));
        sb.append(" 分钟后将提示休息!");
        tvOverTime.setText(sb.toString());
    }


    private void checkPermission() {
        if (!Settings.canDrawOverlays(MainActivity.this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 10);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(alarmReceiver);
        Log.d(TAG, "onDestroy");
    }
}
