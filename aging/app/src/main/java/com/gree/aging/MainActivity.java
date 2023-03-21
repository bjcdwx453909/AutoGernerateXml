package com.gree.aging;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.tbruyelle.rxpermissions2.RxPermissions;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button agingBtn = findViewById(R.id.aging_chip);
        Button aging_content_fillBtn = findViewById(R.id.aging_content_fill);
        Button aging_calllog_fillBtn = findViewById(R.id.aging_calllog_fill);
        Button aging_static_fillBtn = findViewById(R.id.aging_static_fill);
        agingBtn.setOnClickListener(this);
        aging_content_fillBtn.setOnClickListener(this);
        aging_calllog_fillBtn.setOnClickListener(this);
        aging_static_fillBtn.setOnClickListener(this);
        final RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .requestEach(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_CALL_LOG, Manifest.permission.READ_CALL_LOG)
                .subscribe(permission -> {
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.aging_chip:
                startActivity(new Intent(this, AgingActivity.class));
                break;
            case R.id.aging_content_fill:
                startActivity(new Intent(this, contactFillActivity.class));
                break;
            case R.id.aging_calllog_fill:
                startActivity(new Intent(this, callLogActivity.class));
                break;
            case R.id.aging_static_fill:
                startActivity(new Intent(this, StaticActivity.class));
                break;

        }

    }


}
