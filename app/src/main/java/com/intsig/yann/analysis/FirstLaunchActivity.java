package com.intsig.yann.analysis;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * Created by yann_qiu on 2018/4/7.
 */

public class FirstLaunchActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 102;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_launch);
        getSdcardPermission();
    }

    private void getSdcardPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                PermissionChecker.checkSelfPermission(FirstLaunchActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    WelcomeActivity.startActivity(FirstLaunchActivity.this);
                    finish();
                }
            }, 2000);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION:
                if (grantResults.length > 0) {
                    for(int i = 0; i < permissions.length ; i++){
                         if (TextUtils.equals(permissions[i], Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                                PermissionChecker.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, R.string.need_sdcard_permission, Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                             WelcomeActivity.startActivity(FirstLaunchActivity.this);
                             finish();
                         }
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }
}
