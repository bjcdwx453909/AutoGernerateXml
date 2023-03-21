package com.gree.aging;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import com.app.progresviews.ProgressWheel;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AgingActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    private String TAG = "GREESTRESS";
    private ProgressWheel laggingromWheel;
    private ProgressWheel laggingprogressWheel;
    private Button laggingStartBtn;
    private Button laggingStopBtn;
    private Button laggingFreeBtn;
    private TextView lagging_log_view;
    private StatFs dataFs;
    private File rootPath;
    private boolean testFlag;
    private boolean fillFlag;
    private boolean interruptFlag;
    private Handler handler;
    private int laggingMode = 0;
    private boolean fillMode = false;
    private int file_type_a = 0;
    private int file_type_b = 0;
    private int file_type_c = 0;
    private String fillpath = "sdcard/com.gree.aging/filldata/";
    private int pathIndex = 1;
    private int index = 0;
    private boolean permissionFlag = true;
    private Queue<Map<String, String>> queue = new ConcurrentLinkedQueue<>();

    @SuppressLint({"CheckResult", "HandlerLeak"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aging);
        IntentFilter filter = new IntentFilter();
        filter.addAction("LAGGING.TEST");
        LaggingRevice laggingRevice = new LaggingRevice();
        registerReceiver(laggingRevice, filter);
        RadioGroup lagging_type_rg_type = findViewById(R.id.lagging_type_rg_type);
        RadioGroup lagging_path_rg_type = findViewById(R.id.lagging_path_rg_type);
        laggingStartBtn = findViewById(R.id.laggingStartBtn);
        laggingStopBtn = findViewById(R.id.laggingStopBtn);
        laggingFreeBtn = findViewById(R.id.laggingFreeBtn);
        laggingromWheel = findViewById(R.id.laggingromWheel);
        laggingprogressWheel = findViewById(R.id.laggingprogressWheel);
        lagging_log_view = findViewById(R.id.lagging_log_view);
        laggingStartBtn.setOnClickListener(this);
        laggingStopBtn.setOnClickListener(this);
        laggingFreeBtn.setOnClickListener(this);
        lagging_type_rg_type.setOnCheckedChangeListener(this);
        lagging_path_rg_type.setOnCheckedChangeListener(this);
        rootPath = this.getDataDir();
        dataFs = new StatFs(rootPath.getPath());
        final RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(permission -> {
                    if (!permission.granted || permission.shouldShowRequestPermissionRationale)
                        permissionFlag = false;
                });
        int progressValue = getMemoryUserSize();
        setLaggingPercentage(progressValue);
        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case 0:
                        int value = msg.arg1;
                        setLaggingPercentage(value);
                        if (fillFlag) {
                            setLaggingProgreess(value);
                        }
                        break;
                    case 2:
                        String view = (String) msg.obj;
                        lagging_log_view.setText(view);
                        break;
                    case 99:
                        testFlag = false;
                        laggingStartBtn.setEnabled(true);
                        laggingStopBtn.setEnabled(false);
                        laggingFreeBtn.setEnabled(true);
                        int value99 = msg.arg1;
                        int value100 = msg.arg2;
                        setLaggingPercentage(value99);
                        setLaggingProgreess(value100);
                        lagging_log_view.setText("");
                        break;


                }
                super.handleMessage(msg);
            }
        };

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        testFlag = false;
    }

    private void writeProcess(String data) {
        File file = new File("sdcard/com.gree.aging/process.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWritter = new FileWriter(file, false);
            fileWritter.write(data);
            fileWritter.close();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private int getMemoryUserSize() {
        long userSize = 0;
        try {
            dataFs = new StatFs(rootPath.getPath());
            long blockSize = dataFs.getBlockSizeLong();
            long totalSize = dataFs.getBlockCountLong() * blockSize;
            long freeSize = dataFs.getFreeBlocksLong() * blockSize;
            userSize = totalSize - freeSize;
            return (int) (userSize * 100 / totalSize);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return 0;
        }
    }

    public void setLaggingPercentage(int progressValue) {
        laggingromWheel.setStepCountText(progressValue + "%");
        laggingromWheel.setPercentage(progressValue * 360 / 100);
    }

    public void setLaggingProgreess(int value) {
        laggingprogressWheel.setStepCountText(value + "%");
        laggingprogressWheel.setPercentage(value * 360 / 100);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.laggingStartBtn:
                if (!permissionFlag) {
                    Toast.makeText(this, "请先开启权限", Toast.LENGTH_SHORT).show();
                    return;
                }
                startLagging();
                break;
            case R.id.laggingStopBtn:
                stopLagging();
                break;
            case R.id.laggingFreeBtn:
                laggingStartBtn.setEnabled(false);
                laggingStopBtn.setEnabled(false);
                laggingFreeBtn.setEnabled(false);
                new threadFree().start();
                break;
        }

    }

    private void stopLagging() {
        interruptFlag = true;
        lagging_log_view.setText("");
        testFlag = false;
        laggingStartBtn.setEnabled(true);
        laggingStopBtn.setEnabled(false);
        laggingFreeBtn.setEnabled(true);
    }

    private void startLagging() {
        File file = new File(fillpath);
        if (!file.exists()) {
            boolean wasSuccessful = file.mkdirs();
            Log.i(TAG, "wasSuccessful:" + wasSuccessful);
        }
        lagging_log_view.setText("");
        testFlag = true;
        fillFlag = true;
        interruptFlag = false;
        laggingStartBtn.setEnabled(false);
        laggingStopBtn.setEnabled(true);
        laggingFreeBtn.setEnabled(false);
        new threadTime().start();
        new laggingThread().start();
        new fullFileThread().start();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.lagging_type_10:
                laggingMode = 0;
                break;
            case R.id.lagging_type_18:
                laggingMode = 1;
                break;
            case R.id.lagging_type_24:
                laggingMode = 2;
                break;
            case R.id.lagging_type_false:
                fillMode = false;
                break;
            case R.id.lagging_type_true:
                fillMode = true;
                break;
        }

    }


    class LaggingRevice extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //停止老化
            String cmd = intent.getStringExtra("cmd");
            if (cmd != null && cmd.equals("stop")) {
                stopLagging();
                return;
            }
            //开始老化
            if (!permissionFlag) {
                Toast.makeText(context.getApplicationContext(), "请先开启权限", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                RadioButton lagging_type_10 = findViewById(R.id.lagging_type_10);
                RadioButton lagging_type_18 = findViewById(R.id.lagging_type_18);
                RadioButton lagging_type_24 = findViewById(R.id.lagging_type_24);
                RadioButton lagging_type_true = findViewById(R.id.lagging_type_true);
                RadioButton lagging_type_false = findViewById(R.id.lagging_type_false);
                int LgMode = Integer.parseInt(Objects.requireNonNull(intent.getStringExtra("lgMode")));
                String fMode = intent.getStringExtra("fMode");
                assert fMode != null;
                laggingMode = LgMode;
                fillMode = fMode.equals("true");
                switch (laggingMode) {
                    case 0:
                        lagging_type_10.setChecked(true);
                        lagging_type_18.setChecked(false);
                        lagging_type_24.setChecked(false);
                        break;
                    case 1:
                        lagging_type_10.setChecked(false);
                        lagging_type_18.setChecked(true);
                        lagging_type_24.setChecked(false);
                        break;
                    case 2:
                        lagging_type_10.setChecked(false);
                        lagging_type_18.setChecked(false);
                        lagging_type_24.setChecked(true);
                        break;
                }
                if (fillMode) {
                    lagging_type_true.setChecked(true);
                    lagging_type_false.setChecked(false);
                } else {
                    lagging_type_true.setChecked(false);
                    lagging_type_false.setChecked(true);
                }
                startLagging();

            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }


        }
    }

    class threadTime extends Thread {

        //时间线程，更新测试时长
        @Override
        public void run() {
            super.run();
            while (testFlag) {
                Message message = new Message();
                message.what = 0;
                int memoryUseag = getMemoryUserSize();
                message.arg1 = memoryUseag;
//                writeProcess(String.format("系统老化:%s", memoryUseag > 100 ? 100 : memoryUseag));
                Log.w(TAG,String.format("系统老化:%s", memoryUseag > 100 ? 100 : memoryUseag));
                if (memoryUseag >= 99) {
                    fillFlag = false;
                    testFlag = false;
                    queue.clear();
                    break;
                }
                handler.sendMessage(message);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, e.toString());
                }
            }
        }
    }

    class laggingThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (testFlag && fillFlag) {
                Map<String, String> data = new HashMap<>();
                //防止队列过程内存泄漏
                while (queue.size() > 100) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {

                        e.printStackTrace();
                    }
                }
                if (laggingMode == 2) {
                    //24个月碎片化模型全部用4k小文件
                    file_type_c++;
                    data.put("input", "4k.txt");
                    data.put("output", "4k" + file_type_c + ".txt");
                } else {
                    //10个月及18个月碎片化模型为4k80%,8k15%,128k5%
                    int result = new Random().nextInt(100);
                    if (result >= 95) {
                        file_type_a++;
                        data.put("input", "128k.txt");
                        data.put("output", "128k" + file_type_a + ".txt");
                    } else if (result >= 80) {
                        file_type_b++;
                        data.put("input", "8k.txt");
                        data.put("output", "8k" + file_type_b + ".txt");
                    } else {
                        file_type_c++;
                        data.put("input", "4k.txt");
                        data.put("output", "4k" + file_type_c + ".txt");

                    }
                }
                queue.offer(data);


            }
        }


    }

    class threadFree extends Thread {
        @Override
        public void run() {
            super.run();
            delFile(fillpath);
            Message message = new Message();
            message.what = 99;
            message.arg1 = getMemoryUserSize();
            message.arg2 = 0;
            handler.sendMessage(message);
        }

        private void delFile(String path) {
            File file = new File(path);
            File[] files = file.listFiles();
            assert files != null;
            for (File value : files) {
                if (value.isDirectory()) {
                    delFile(value.getPath());
                } else {
                    boolean result = value.delete();
                }
            }

        }
    }

    class fullFileThread extends Thread {
        @Override
        public void run() {
            super.run();
            long startTime = System.currentTimeMillis();
            while (testFlag) {
                Message message = new Message();
                message.what = 2;
                Map<String, String> data = queue.poll();
                if (data != null) {
                    message.obj = "正在填充" + data.get("output");
                    handler.sendMessage(message);
                    try {
                        if (index == 10000) {
                            index = 0;
                            pathIndex++;
                        }
                        writeFile(data.get("input"), data.get("output"));
                        if (Objects.equals(data.get("input"), "4k.txt")) index++;
//                        Log.i(TAG, "创建"+data.get("output")+"文件耗时:"+c+"毫秒;填充总耗时:" + (b - a) + "毫秒");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
            long stopTime = System.currentTimeMillis();
            long dur = stopTime - startTime;
            Log.i(TAG, "填充耗时:" + dur / 1000 + "秒");
            long startTime1 = System.currentTimeMillis();

            deleteFile();//间隔删除文件，使文件碎片化
            long stopTime1 = System.currentTimeMillis();
            long dur2 = stopTime1 - startTime1;
            Log.i(TAG, "释放耗时:" + dur2 / 1000 + "秒");
            Message message = new Message();
            message.what = 99;
            message.arg2 = interruptFlag ? 0 : 100;//如果手动停止中断，进度为0，否则正在结束，进度为100
            message.arg1 = getMemoryUserSize();
            Log.w(TAG, "系统老化:100");
//            Log.i(TAG, "碎片化完成");
//            writeProcess("系统老化:100");
            if (!interruptFlag) queue.clear();
            handler.sendMessage(message);
        }

    }

    private void writeFile(String inputFile, String outPutFileName) {
//        Log.i(TAG, "正在填充" + outPutFileName);
        try {
            File outfile;
            if (!fillMode) {
                File filePath = new File(fillpath + pathIndex + "/");
                if (!filePath.exists()) {
                    boolean wasSuccessful = filePath.mkdirs();
                }

                outfile = new File(fillpath + pathIndex + "/" + outPutFileName);
            } else {
                outfile = new File(fillpath + outPutFileName);
            }

            boolean result = outfile.createNewFile();
            InputStream inputStream = getAssets().open(inputFile);
            OutputStream outputStream = new FileOutputStream(outfile, false);
            int len = -1;
            byte[] buffer = new byte[1024];
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

    }

    private void deleteFile() {
        int i = file_type_c;
        while (i > 0) {
            File file;
            if (fillMode) {
                file = new File(fillpath + "4k" + i + ".txt");
            } else {
                int index;
                if (i % 10000 == 0) {
                    index = i / 10000;
                } else {
                    index = i / 10000 + 1;
                }


                file = new File(fillpath + index + "/4k" + i + ".txt");
            }
            Message msg = new Message();
            if (laggingMode == 0) {
                //删除1,2,3,5,6,7,9

                if (i % 8 != 0) {
                    if (file.exists()) {
                        msg.what = 2;
                        msg.obj = "正在删除" + file.getName();
                        handler.sendMessage(msg);
                        file.delete();
                    }
                }

            } else {
                //删除1,3,5,7,9
                Log.i(TAG, "删除" + file.getPath() + file.getName());
                if (i % 2 != 0) {
                    if (file.exists()) {
                        msg.what = 2;
                        msg.obj = "正在删除" + file.getName();
                        handler.sendMessage(msg);
                        file.delete();
                    }
                }
            }
            i--;
        }

    }
//    class threadFillrom extends Thread{
//        private void writeFile(String inputFilePath, String outputFilePath) {
//            try {
//                File outfile = new File(outputFilePath);
//                if (!outfile.exists()) {
//                    try {
//                        boolean result=outfile.createNewFile();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                InputStream inputStream = getAssets().open(inputFilePath);
//                FileOutputStream outputStream = new FileOutputStream(outfile, true);
//                int len = -1;
//                byte[] buffer = new byte[1024];
//                while ((len = inputStream.read(buffer)) != -1) {
//                    outputStream.write(buffer, 0, len);
//                    outputStream.flush();
//                }
//                inputStream.close();
//                outputStream.close();
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        @Override
//        public void run() {
//            super.run();
//            long a=System.currentTimeMillis();
//            while (true){
//                String output="sdcard/test.txt";
//                File file=new File(output);
//                Log.i(TAG,"size:"+file.length());
//                if(file.length()>1000*102400) break;
//                writeFile("4k.txt",output);
//            }
//            long b=System.currentTimeMillis();
//            long d=(b-a)/1000;
//            long c=100000/d;
//            Log.i(TAG,"d:"+d);
//            Log.i(TAG,"c:"+c/1024);
//
//        }
//    }
}
