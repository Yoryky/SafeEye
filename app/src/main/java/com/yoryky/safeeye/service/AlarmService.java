package com.yoryky.safeeye.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.yoryky.safeeye.R;

import java.io.IOException;


public class AlarmService extends Service {
    private static final String TAG = "AlarmService";
    private static final String CHANNEL_ID = "com.appname.notification.channel";
    private static final int NOTIFICATION_ID = 10090;
    private MediaPlayer mediaRestPlayer;
    private MediaPlayer mediaWorkPlayer;
    private Vibrator vibrator;
    private int workTime;
    private int restTime;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        this.initMediaPlayer();
        this.initVibrator();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            String channelName = getString(R.string.channel);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            //在创建的通知渠道上发送通知
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
            builder.setSmallIcon(R.mipmap.ic_launcher) //设置通知图标
                    .setContentTitle("护眼闹钟")//设置通知标题
                    .setContentText("闹钟将会提示护眼")//设置通知内容
                    .setAutoCancel(true) //用户触摸时，自动关闭
                    .setOngoing(true);//设置处于运行状态
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            startForeground(NOTIFICATION_ID, builder.build());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        String timeStr = intent.getExtras().get("time").toString();
        workTime = Integer.parseInt(timeStr);
        startCountWorkTime();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startCountWorkTime() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int totalTime = workTime;
                    while (totalTime > 0) {
                        Thread.sleep(1000);
                        totalTime--;
                        sendBroadCast(totalTime);
                        remindRest(totalTime);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startCountRestTime() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int totalTime = restTime;
                    while (totalTime > 0) {
                        Thread.sleep(1000);
                        totalTime--;
                        if (totalTime == 0) {
                            remindWork(totalTime);
                            startCountWorkTime();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void remindWork(int time){
        if(time != 0)return;
        remindWorkVibrate();
    }

    private void remindRest(int time){
        if(time != 0)return;
        remindRestVibrate();
        Message message = Message.obtain();
        message.what = 1;
        handler.sendMessage(message);
    }

    private void initVibrator(){
        vibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
    }

    private void initMediaPlayer() {
        try {
            AssetManager assetManager = this.getAssets();
            AssetFileDescriptor afd = assetManager.openFd("wow.wav");
            AssetFileDescriptor afdWork = assetManager.openFd("biu.wav");
            mediaRestPlayer = new MediaPlayer();
            mediaWorkPlayer = new MediaPlayer();
            mediaRestPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaWorkPlayer.setDataSource(afdWork.getFileDescriptor(), afdWork.getStartOffset(), afdWork.getLength());
            mediaRestPlayer.prepare();
            mediaWorkPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void remindRestVibrate(){
        vibrator.vibrate(new long[]{500,500,500,500,500,500},-1);
    }

    private void remindRestVoice() {
        if (!mediaRestPlayer.isPlaying()) {
            mediaRestPlayer.start();
        }
    }

    private void remindWorkVibrate(){
        vibrator.vibrate(new long[]{500,500,500,500},-1);
    }


    private void remindWorkVoice() {
        if (!mediaWorkPlayer.isPlaying()) {
            mediaWorkPlayer.start();
        }
    }

    private void showRemindRestDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setTitle("护眼提示");
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage("又到了保护眼睛的时间啦,请选择休息时间！");
        View view = View.inflate(this,R.layout.dialog_rest,null);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        TextView tvTime1 = (TextView) view.findViewById(R.id.tv_rest_time1);
        TextView tvTime2 = (TextView)view.findViewById(R.id.tv_rest_time2);
        TextView tvCancel = (TextView)view.findViewById(R.id.tv_cancel);
        tvTime1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restTime = 6;
                stopRemindRestVoice();
                startCountRestTime();
                alertDialog.dismiss();
            }
        });

        tvTime2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restTime = 6 * 5;
                stopRemindRestVoice();
                startCountRestTime();
                alertDialog.dismiss();
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRemindRestVoice();
                alertDialog.dismiss();
            }
        });

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        alertDialog.getWindow().setType(params.type);
        alertDialog.show();
    }

    private void stopRemindRestVoice() {
        if (mediaRestPlayer.isPlaying()) {
            mediaRestPlayer.reset();
            try {
                AssetManager assetManager = this.getAssets();
                AssetFileDescriptor afd = assetManager.openFd("wow.wav");
                mediaRestPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                mediaRestPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendBroadCast(int overtime) {
        Intent intent = new Intent("com.yoryky.safeeye.alarm.receiver");
        intent.putExtra("overtime", overtime);
        sendBroadcast(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                showRemindRestDialog();
            }
        }
    };
}
