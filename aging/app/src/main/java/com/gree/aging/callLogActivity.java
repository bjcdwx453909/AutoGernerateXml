package com.gree.aging;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.app.progresviews.ProgressWheel;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.FileWriter;
import java.util.Objects;
import java.util.Random;

public class callLogActivity extends AppCompatActivity implements View.OnClickListener {
    private static String TAG = "GREESTRESS";
    private boolean permissionFlag = true;
    private Button startFillBtn;
    private Button stopFillBtn;
    private ProgressWheel calllogfillprogressWheel;
    private EditText fillCountEt;
    private TextView fillCountTV;
    private int fillCount = 1000;
    private int fillCompleteCount;
    private int fillProgress;
    private boolean testFlag = false;
    private Handler handler;

    //中国移动
    public static final String[] CHINA_MOBILE = {
            "134", "135", "136", "137", "138", "139", "150", "151", "152", "157", "158", "159",
            "182", "183", "184", "187", "188", "178", "147", "172", "198"
    };
    //中国联通
    public static final String[] CHINA_UNICOM = {
            "130", "131", "132", "145", "155", "156", "166", "171", "175", "176", "185", "186", "166"
    };
    //中国电信
    public static final String[] CHINA_TELECOME = {
            "133", "149", "153", "173", "177", "180", "181", "189", "199"
    };

    @SuppressLint({"HandlerLeak", "CheckResult"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_log);
        IntentFilter filter=new IntentFilter();
        filter.addAction("CALLLOGFILL.TEST");
        callLogFillRevice callLogFillRevice = new callLogFillRevice();
        registerReceiver(callLogFillRevice,filter);
        fillCountEt = findViewById(R.id.calllogfillCountEt);
        fillCountTV = findViewById(R.id.calllogfillCountTV);
        startFillBtn = findViewById(R.id.calllogfillStartBtn);
        stopFillBtn = findViewById(R.id.calllogfillStopBtn);
        Button delBtn = findViewById(R.id.calllogDelBtn);
        calllogfillprogressWheel = findViewById(R.id.calllogfillprogressWheel);
        startFillBtn.setOnClickListener(this);
        stopFillBtn.setOnClickListener(this);
        delBtn.setOnClickListener(this);
        final RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .requestEach(Manifest.permission.WRITE_CALL_LOG, Manifest.permission.READ_CALL_LOG)
                .subscribe(permission -> {
                    if (!permission.granted || permission.shouldShowRequestPermissionRationale)
                        permissionFlag = false;
                });
        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case 0:
                        setTestPercentage();
                        break;
                    case 1:
                        fillCountTV.setText(String.valueOf(fillCompleteCount));
                        break;
                    case 99:
                        testFlag = false;
                        startFillBtn.setEnabled(true);
                        fillCountEt.setEnabled(true);
                        stopFillBtn.setEnabled(false);
                        setTestPercentage();
                        Toast.makeText(getApplicationContext(), "通话记录填充完成", Toast.LENGTH_SHORT).show();
                        break;

                }
                super.handleMessage(msg);
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.calllogfillStartBtn:
                if (!permissionFlag) {
                    Toast.makeText(this, "请先开启权限", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (getFillPecent()) {
                    startFillBtn.setEnabled(false);
                    fillCountEt.setEnabled(false);
                    stopFillBtn.setEnabled(true);
                    testFlag = true;
                    fillProgress = 0;
                    fillCompleteCount = 0;
                    fillCountTV.setText(String.valueOf(fillCompleteCount));
                    new threadTime().start();
                    new threadFillrom().start();

                }
                break;
            case R.id.calllogfillStopBtn:
                startFillBtn.setEnabled(true);
                fillCountEt.setEnabled(true);
                stopFillBtn.setEnabled(false);
                testFlag = false;
                setTestPercentage();
                Toast.makeText(this, "停止填充联系人", Toast.LENGTH_SHORT).show();
                break;
            case R.id.calllogDelBtn:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_CALL_BUTTON);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, String.valueOf(e), Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    /**
     * 通话记录填充数量
     */
    public boolean getFillPecent() {
        String fillPecentValue = fillCountEt.getText().toString();
        if (fillPecentValue.equals("")) {
            fillCount = 1000;
            Toast.makeText(this, "默认填充1000个通话记录", Toast.LENGTH_SHORT).show();
            return true;
        }
        try {
            fillCount = Integer.parseInt(fillPecentValue);
        } catch (Exception e) {
            Toast.makeText(this, "输入正确数字", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (fillCount < 0) {
            Toast.makeText(this, "输入正确数字", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;

    }


    /**
     * 填充进度
     */
    public void setTestPercentage() {
        calllogfillprogressWheel.setStepCountText(fillProgress + "%");
        calllogfillprogressWheel.setPercentage(fillProgress * 360 / 100);
        Log.w(TAG,String.format("填充通话记录:%s", fillProgress > 100 ? 100 : fillProgress));
//        writeProcess(String.format("填充通话记录:%s", fillProgress > 100 ? 100 : fillProgress));
    }
    private void writeProcess(String data){
        Log.w(TAG,data);
        File file =new File("sdcard/com.gree.aging/process.txt");
        try{
            if (!file.exists()) file.createNewFile();
            else {
                file.delete();
                file.createNewFile();
            }
            FileWriter fileWritter = new FileWriter(file,false);
            fileWritter.write("填充通话记录" +":"+data);
            fileWritter.close();
        }
        catch (Exception e){
            Log.e(TAG,e.toString());
        }
    }

    /**
     * 填充通话记录
     * @param number 电话号码
     * @param duration 通话时长（响铃时长）以秒为单位 1分30秒则输入90
     * @param type 通话类型  1呼入 2呼出 3未接
     * @param isNew 是否已查看    0已看1未看
     */
    private void insertCallLog(String number, String duration, String type, String isNew) {
        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.NUMBER, number);
        values.put(CallLog.Calls.DATE, System.currentTimeMillis());
        values.put(CallLog.Calls.DURATION, duration);
        values.put(CallLog.Calls.TYPE, type);
        values.put(CallLog.Calls.NEW, isNew);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_CALL_LOG}, 1000);
        }
        getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);

    }

    /**
     * 生成手机号
     *

     */
    public static String createMobile() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        String mobile01;//手机号前三位
        int temp;
        int op=random.nextInt(3);
//        param op 0 移动 1 联通 2 电信
        switch (op) {
            case 0:
                mobile01 = CHINA_MOBILE[random.nextInt(CHINA_MOBILE.length)];
                break;
            case 1:
                mobile01 = CHINA_UNICOM[random.nextInt(CHINA_UNICOM.length)];
                break;
            case 2:
                mobile01 = CHINA_TELECOME[random.nextInt(CHINA_TELECOME.length)];
                break;
            default:
                mobile01 = CHINA_MOBILE[random.nextInt(CHINA_MOBILE.length)];
                break;
        }
        if (mobile01.length() > 3) {
            return mobile01;
        }
        sb.append(mobile01);
        //生成手机号后8位
        for (int i = 0; i < 8; i++) {
            temp = random.nextInt(10);
            sb.append(temp);
        }
        return sb.toString();
    }



    class threadTime extends Thread {

        //时间线程，更新测试进度，没秒更新一次进度

        @Override
        public void run() {
            super.run();
            while (testFlag) {
                Message message = new Message();
                message.what = 0;
                handler.sendMessage(message);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }
    class threadFillrom extends Thread {
        //填充线程

        @Override
        public void run() {
            super.run();
            fillCompleteCount=0;
            Random random = new Random();
            while(testFlag){
                Message message = new Message();
                if(fillCompleteCount>=fillCount){
                    Log.i(TAG, "填充完成");
//                    writeProcess("100");
                    Log.w(TAG,"填充通话记录:100");
                    fillProgress = 100;
                    message.what = 99;
                    handler.sendMessage(message);
                    return;
                }
                insertCallLog(createMobile(),String.valueOf(random.nextInt(90)),String.valueOf(random.nextInt(3)+1),String.valueOf(random.nextInt(1)));
                fillCompleteCount++;
                message.what = 1;
                handler.sendMessage(message);
                fillProgress = (fillCompleteCount * 100 / fillCount);

            }
        }
    }
    class callLogFillRevice extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!permissionFlag) {
                Toast.makeText(context.getApplicationContext(), "请先开启权限", Toast.LENGTH_SHORT).show();
                return;
            }
            try{
                fillCount = Integer.valueOf(Objects.requireNonNull(intent.getStringExtra("number")));
                startFillBtn.setEnabled(false);
                fillCountEt.setEnabled(false);
                stopFillBtn.setEnabled(true);
                testFlag = true;
                fillProgress = 0;
                fillCompleteCount = 0;
                fillCountTV.setText(String.valueOf(fillCompleteCount));
                new threadTime().start();
                new threadFillrom().start();

            }catch (Exception ignored){

            }
        }
    }


}

