package com.gree.aging;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.app.progresviews.ProgressWheel;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class StaticActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "GREESTRESS";
    private TextView staticLogView;
    private int deviceMode = 1;
    private String fillpath = "sdcard/com.gree.aging/static/";
    private int laggingMode = 0;
    private boolean permissionFlag = true;
    private boolean fillFlag = false;
    private Handler handler;
    private RadioButton static_type_10Btn;
    private RadioButton static_type_18Btn;
    private RadioButton static_type_24Btn;
    private RadioButton static_32Btn;
    private RadioButton static_64Btn;
    private Button staticStartBtn;
    private Button staticStopBtn;
    private Button staticFreeBtn;
    private StatFs dataFs;
    private File rootPath;
    private ProgressWheel romfillromWheelromWheel;


    @SuppressLint({"CheckResult", "HandlerLeak"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_static);
        IntentFilter filter = new IntentFilter();
        filter.addAction("STATIC.TEST");
        StaticRevice staticRevice =new StaticRevice();
        registerReceiver(staticRevice,filter);
        RadioGroup static_device_type = findViewById(R.id.static_device_type);
        RadioGroup static_rg_type = findViewById(R.id.static_type_rg_type);
        romfillromWheelromWheel = findViewById(R.id.romfillprogressWheel);
        static_type_10Btn = findViewById(R.id.static_type_10);
        static_type_18Btn = findViewById(R.id.static_type_18);
        static_type_24Btn = findViewById(R.id.static_type_24);
        static_32Btn = findViewById(R.id.static_32);
        static_64Btn = findViewById(R.id.static_64);
        staticStartBtn = findViewById(R.id.staticStartBtn);
        staticStopBtn = findViewById(R.id.staticStopBtn);
        staticFreeBtn = findViewById(R.id.staticFreeBtn);
        staticLogView = findViewById(R.id.static_log_view);
        staticStartBtn.setOnClickListener(this);
        staticStopBtn.setOnClickListener(this);
        staticFreeBtn.setOnClickListener(this);
        rootPath = this.getDataDir();
        dataFs = new StatFs(rootPath.getPath());
        static_device_type.setOnCheckedChangeListener(this);
        static_rg_type.setOnCheckedChangeListener(this);
        setromPercentage();
        final RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(permission -> {
                    if (!permission.granted || permission.shouldShowRequestPermissionRationale)
                        permissionFlag = false;
                });
        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        String textView = String.valueOf(msg.obj);
                        staticLogView.setText(textView);
                        break;
                    case 1:
                        setromPercentage();
                        break;
                    case 100:
                        fillFlag = false;
                        static_type_10Btn.setEnabled(true);
                        static_type_18Btn.setEnabled(true);
                        static_type_24Btn.setEnabled(true);
                        static_32Btn.setEnabled(true);
                        static_64Btn.setEnabled(true);
                        staticStartBtn.setEnabled(true);
                        staticStopBtn.setEnabled(false);
                        staticFreeBtn.setEnabled(true);
                        setromPercentage();
                        break;

                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fillFlag=false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.staticStartBtn:
                if (fillFlag) {
                    Toast.makeText(this, "请稍后...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!permissionFlag) {
                    Toast.makeText(this, "请先开启权限", Toast.LENGTH_SHORT).show();
                    return;
                }
                startStaticFull();
                break;
            case R.id.staticStopBtn:
                stopStaticFull();
                break;
            case R.id.staticFreeBtn:
                fillFlag = true;
                staticStartBtn.setEnabled(false);
                staticStopBtn.setEnabled(false);
                staticFreeBtn.setEnabled(false);
                new threadFree().start();
                new threadTime().start();
                break;
        }

    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.static_32:
                deviceMode = 0;
                break;
            case R.id.static_64:
                deviceMode = 1;
                break;
            case R.id.static_type_10:
                laggingMode = 0;
                break;
            case R.id.static_type_18:
                laggingMode = 1;
                break;
            case R.id.static_type_24:
                laggingMode = 2;
                break;
        }

    }

    public void setromPercentage() {
        dataFs = new StatFs(rootPath.getPath());
        long blockSize = dataFs.getBlockSizeLong();
        long totalSize = dataFs.getBlockCountLong() * blockSize;
        long freeSize = dataFs.getAvailableBlocksLong() * blockSize;
        long userSize = totalSize - freeSize;
        int progressValue = (int) (userSize * 100 / totalSize);
        romfillromWheelromWheel.setStepCountText(progressValue + "%");
        romfillromWheelromWheel.setPercentage(progressValue * 360 / 100);

    }

    @SuppressLint("DefaultLocale")
    private void fileFull(double size) {
        while (true) {
            dataFs = new StatFs(rootPath.getPath());
            long blockSize = dataFs.getBlockSizeLong();
            long freeSize = dataFs.getFreeBlocksLong() * blockSize;
            int percent = (int) (size * 1000 * 1000 * 1000 * 100 / freeSize);
            Message message = new Message();
            message.what = 0;
            message.obj = "正在填充剩余存储,填充进度:" + (percent > 100 ? 100 : percent) + "%";
            Log.w(TAG,String.format("填充剩余存储:%s", percent > 100 ? 100 : percent));
//            writeProcess(String.format("填充剩余存储:%s", percent > 100 ? 100 : percent));
            handler.sendMessage(message);
            if (freeSize <= size * 1000 * 1000 * 1000) break;
            writeFile("50M.mp4", "data", "data", true);
        }

    }


    private void writeFile(String inputFile, String outPutFileName, String type, boolean append) {
        File outfile;
        try {
            File filePath = new File(fillpath + type);
            if (!filePath.exists()) {
                filePath.mkdirs();
            }
            outfile = new File(filePath + "/" + outPutFileName);
            outfile.createNewFile();
            InputStream inputStream = getAssets().open(inputFile);
            OutputStream outputStream = new FileOutputStream(outfile, append);
            int len;
            byte[] buffer = new byte[1024];
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            inputStream.close();
            outputStream.close();
        } catch (
                Exception e) {
            Log.e(TAG, e.toString());
        }

    }
    private void startStaticFull(){
        static_type_10Btn.setEnabled(false);
        static_type_18Btn.setEnabled(false);
        static_type_24Btn.setEnabled(false);
        static_32Btn.setEnabled(false);
        static_64Btn.setEnabled(false);
        staticStartBtn.setEnabled(false);
        staticStopBtn.setEnabled(true);
        staticFreeBtn.setEnabled(false);
        fillFlag = true;
        new fullStaticThread().start();
        new threadTime().start();

    }
    private void stopStaticFull(){
        fillFlag = false;
        staticLogView.setText("");
        static_type_10Btn.setEnabled(true);
        static_type_18Btn.setEnabled(true);
        static_type_24Btn.setEnabled(true);
        static_32Btn.setEnabled(true);
        static_64Btn.setEnabled(true);
        staticStartBtn.setEnabled(true);
        staticStopBtn.setEnabled(false);
        staticFreeBtn.setEnabled(true);
    }
    private void writeProcess(){
        File file =new File("sdcard/com.gree.aging/process.txt");
        try{
            if (!file.exists()) file.createNewFile();
            else {
                file.delete();
                file.createNewFile();
            }
            FileWriter fileWritter = new FileWriter(file,false);
            fileWritter.write("静态资源:100");
            fileWritter.close();
        }
        catch (Exception e){
            Log.e(TAG,e.toString());
        }
    }

    class fullStaticThread extends Thread {
        @Override
        public void run() {
            super.run();
            int picCount = 3000;
            int musicCount = 300;
            int vedioCount = 30;
            double size = 5;
            if (deviceMode == 1) {
                switch (laggingMode) {
                    case 0:
                        picCount = 3000;
                        musicCount = 300;
                        vedioCount = 30;
                        size = 5;
                        break;
                    case 1:
                        picCount = 4500;
                        musicCount = 450;
                        vedioCount = 45;
                        size = 3;
                        break;
                    case 2:
                        picCount = 5400;
                        musicCount = 540;
                        vedioCount = 54;
                        size = 2;
                        break;
                }

            } else {
                switch (laggingMode) {
                    case 0:
                        picCount = 1000;
                        musicCount = 200;
                        vedioCount = 20;
                        size = 2;
                        break;
                    case 1:
                        picCount = 1500;
                        musicCount = 250;
                        vedioCount = 25;
                        size = 1.5;
                        break;
                    case 2:
                        picCount = 2000;
                        musicCount = 300;
                        vedioCount = 30;
                        size = 1.5;
                        break;
                }

            }

            //填充音乐资源

            for (int i = 0; i < picCount; i++) {
                if (!fillFlag) break;
                Message message = new Message();
                message.what = 0;
                message.obj = String.format("正在填充图片,填充进度:%s", i * 100 / picCount) + "%";
                handler.sendMessage(message);
                Log.w(TAG,String.format("填充图片:%s",i * 100 / picCount));
//                writeProcess(String.format("填充图片:%s", i * 100 / picCount));

                writeFile("3M.jpg", String.format("3M%s.jpg", i + 1), "picture", false);
            }

            //填充音乐资源
            for (int i = 0; i < musicCount; i++) {
                if (!fillFlag) break;
                Message message = new Message();
                message.what = 0;
                message.obj = String.format("正在填充音乐,填充进度:%s", i * 100 / musicCount) + "%";
                handler.sendMessage(message);
                Log.w(TAG,String.format("填充音乐:%s",i * 100 / musicCount));
//                writeProcess(String.format("填充音乐:%s", i * 100 / musicCount));
                writeFile("爱的纪念6M.mp3", String.format("爱的纪念6M%s.mp3", i + 1), "music", false);
            }
            //填充视频资源
            for (int i = 0; i < vedioCount; i++) {
                if (!fillFlag) break;
                Message message = new Message();
                message.what = 0;
                message.obj = String.format("正在填充视频,填充进度:%s", i * 100 / vedioCount) + "%";
                handler.sendMessage(message);
                Log.w(TAG,String.format("填充视频:%s",i * 100 / vedioCount));
//                writeProcess(String.format("填充视频:%s", i * 100 / vedioCount));
                writeFile("50M.mp4", String.format("50M%s.mp4", i + 1), "vedio", false);
            }
            if (fillFlag) fileFull(size);
            Message message = new Message();
            message.what = 100;
            message.obj = "填充完成";
//            writeProcess();
            Log.w(TAG,"静态资源:100");
            handler.sendMessage(message);
        }
    }

    class threadFree extends Thread {
        @Override
        public void run() {
            super.run();
            delFile(fillpath);
            Message message = new Message();
            message.what = 100;
            handler.sendMessage(message);
        }

        private void delFile(String path) {
            File file = new File(path);
            if (!file.exists()) return;
            File[] files = file.listFiles();
            assert files != null;
            for (File value : files) {
                if (value.isDirectory()) {
                    delFile(value.getPath());
                } else {
                    value.delete();
                }
            }
            file.delete();

        }
    }
    class threadTime extends Thread {

        //时间线程，更新测试时长

        @Override
        public void run() {
            super.run();
            while (fillFlag) {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }
    class StaticRevice extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String cmd = intent.getStringExtra("cmd");
            if (cmd != null && cmd.equals("stop")) {
                stopStaticFull();
                return;
            }
            if (!permissionFlag) {
                Toast.makeText(context.getApplicationContext(), "请先开启权限", Toast.LENGTH_SHORT).show();
                return;
            }
            laggingMode = Integer.parseInt(Objects.requireNonNull(intent.getStringExtra("lgMode")));
            String dMode = intent.getStringExtra("dMode");
            assert dMode != null;
            switch (dMode){
                case "32":
                    deviceMode=0;
                    static_32Btn.setChecked(true);
                    static_64Btn.setChecked(false);
                    break;
                case "64":
                    static_32Btn.setChecked(false);
                    static_64Btn.setChecked(true);
                    deviceMode=1;
            }
            switch (laggingMode) {
                case 0:
                    static_type_10Btn.setChecked(true);
                    static_type_18Btn.setChecked(false);
                    static_type_24Btn.setChecked(false);
                    break;
                case 1:
                    static_type_10Btn.setChecked(false);
                    static_type_18Btn.setChecked(true);
                    static_type_24Btn.setChecked(false);
                    break;
                case 2:
                    static_type_10Btn.setChecked(false);
                    static_type_18Btn.setChecked(false);
                    static_type_24Btn.setChecked(true);
                    break;
            }
            startStaticFull();

        }
    }

}
