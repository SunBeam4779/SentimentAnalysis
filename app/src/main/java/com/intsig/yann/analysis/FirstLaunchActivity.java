package com.intsig.yann.analysis;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.PermissionChecker;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && // SDK version is the same as or higher than the running SDK
                PermissionChecker.checkSelfPermission(FirstLaunchActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);//request permission
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
                                 PermissionChecker.checkSelfPermission(this, permissions[i]) != PermissionChecker.PERMISSION_GRANTED) {
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
