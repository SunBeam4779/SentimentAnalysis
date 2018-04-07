package com.intsig.yann.analysis;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

/**
 * Created by wo on 2018/4/7.
 */

public class LoginOrRegisterActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String ACCOUNT_NAME = "ACCOUNT_NAME";
    private static final int REQUEST_PERMISSION = 102;
    private static final int REQUEST_CAMERA = 103;
    private static final int REQUEST_CROP = 104;

    private LinearLayout loginLinearLayout;
    private LinearLayout registerLinearLayout;
    private EditText loginEditText;
    private Button loginButton;
    private TextView registerTextView;
    private ImageView photoImageView;
    private ImageView smallImageView;
    private EditText registerEditText;
    private Button photoButton;
    private Button sureButton;
    private String accountName;
    private long accountId = -1;
    private File currentPhotoFile;
    private String myBigImage;
    private String mySmallImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initFromXml();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initFromXml() {
        loginLinearLayout = (LinearLayout) findViewById(R.id.login_LinearLayout);
        registerLinearLayout = (LinearLayout) findViewById(R.id.register_LinearLayout);
        loginEditText = (EditText) findViewById(R.id.login_EditText);
        loginButton = (Button) findViewById(R.id.login_Button);
        registerTextView = (TextView) findViewById(R.id.register_TextView);
        photoImageView = (ImageView) findViewById(R.id.photo_ImageView);
        registerEditText = (EditText) findViewById(R.id.register_EditText);
        photoButton = (Button) findViewById(R.id.photo_Button);
        sureButton = (Button) findViewById(R.id.sure_Button);
        smallImageView = (ImageView) findViewById(R.id.small_ImageView);
    }

    private long getAccountId(String name) {
        long accountId = -1;
        Cursor cursor = getContentResolver().query(Uri.parse(AccountData.CONTENT_URI_NAME + name),
                new String[] {AccountData._ID}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            accountId = cursor.getLong(0);
        }
        Util.safeCloseCursor(cursor);
        return accountId;
    }

    private void initView() {
        accountName = PreferenceManager.getDefaultSharedPreferences(this).getString(ACCOUNT_NAME, "");
        if (!TextUtils.isEmpty(accountName)) {
            accountId = getAccountId(accountName);
            if (accountId > 0) {
                AnalysisHolderActivity.startActivity(this,accountId);
                finish();
                return;
            }
        }
        if (accountId <= 0) {
            registerLinearLayout.setVisibility(View.VISIBLE);
            loginLinearLayout.setVisibility(View.GONE);
        }
        loginButton.setOnClickListener(this);
        registerTextView.setOnClickListener(this);
        photoButton.setOnClickListener(this);
        sureButton.setOnClickListener(this);
    }

    public static void startActivity(Activity activity) {
        Intent intent = new Intent(activity, LoginOrRegisterActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.login_Button) {
            accountId = getAccountId(accountName);
            if (accountId > 0) {
                AnalysisHolderActivity.startActivity(this,accountId);
                finish();
            } else {
                Toast.makeText(this, R.string.error_account, Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.register_TextView) {
            registerLinearLayout.setVisibility(View.VISIBLE);
            loginLinearLayout.setVisibility(View.GONE);
        } else if (id == R.id.photo_Button) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    PermissionChecker.checkSelfPermission(LoginOrRegisterActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.CAMERA}, REQUEST_PERMISSION);
            } else {
                takePhoto();
            }
        } else if (id == R.id.sure_Button) {
            accountName = registerEditText.getText().toString();
            if (TextUtils.isEmpty(accountName) || TextUtils.isEmpty(myBigImage) || TextUtils.isEmpty(mySmallImage)) {
                Toast.makeText(this, R.string.unComplete_info, Toast.LENGTH_LONG).show();
                return;
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(AccountData.SMALL_IMG, mySmallImage);
            contentValues.put(AccountData.BIG_IMG, myBigImage);
            contentValues.put(AccountData.CREATE_DATE, System.currentTimeMillis());
            contentValues.put(AccountData.ACCOUNT_NAME, accountName);
            Uri uri = getContentResolver().insert(AccountData.CONTENT_URI, contentValues);
            accountId = Long.valueOf(uri.getLastPathSegment());
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(ACCOUNT_NAME, accountName).commit();
            AnalysisHolderActivity.startActivity(this, accountId);
            finish();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                doCropPhoto(currentPhotoFile, REQUEST_CROP);
            } else if (requestCode == REQUEST_CROP) {
                String cropFilePath = null;
                if (new File(AnalysisHolderActivity.TempCropFile).exists()) {
                    cropFilePath = AnalysisHolderActivity.TempCropFile;
                } else {
                    if (data == null) {
                        return;
                    }
                    if (data.getData() != null) {
                        cropFilePath = Util.getPathFromUri(this, data.getData());
                    }
                }
                if (TextUtils.isEmpty(cropFilePath) || !new File(cropFilePath).exists()) {
                    return;
                }
                String time = Util.getDateAsName();
                File PHOTO_DIR = new File(Util.THUMB_IMG);
                PHOTO_DIR.mkdirs();
                mySmallImage = Util.THUMB_IMG + "/" + time + ".jpg";
                Util.copyFile(AnalysisHolderActivity.TempCropFile, mySmallImage);
                new File(cropFilePath).delete();
                photoImageView.setImageBitmap((Util.loadBitmap(myBigImage)));
                smallImageView.setImageBitmap((Util.loadBitmap(mySmallImage)));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION:
                if (grantResults.length > 0) {
                    for(int i = 0; i < permissions.length ; i++){
                        if(TextUtils.equals(permissions[i], Manifest.permission.CAMERA) &&
                                PermissionChecker.checkSelfPermission(this, permissions[i]) == PackageManager.PERMISSION_GRANTED){
                            takePhoto();
                            return;
                        } else if (TextUtils.equals(permissions[i], Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                                PermissionChecker.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, R.string.need_sdcard_permission, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }

    /**
     * Launches Camera to take a picture and store it in a file.
     */
    private void takePhoto() {
        try {
            File PHOTO_DIR = new File(Util.ORIGINAL_IMG);
            PHOTO_DIR.mkdirs();
            String time = Util.getDateAsName();
            myBigImage = Util.ORIGINAL_IMG + "/" + time + ".jpg";
            currentPhotoFile = new File(PHOTO_DIR, time + ".jpg");
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(currentPhotoFile));
            startActivityForResult(intent, REQUEST_CAMERA);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.photoPickerNotFoundText, Toast.LENGTH_LONG).show();
        }
    }

    private void doCropPhoto(File oriImg, int requestCode) {
        try {
            File cfile = new File(AnalysisHolderActivity.TempCropFile);
            if (cfile.exists()) {
                cfile.delete();
            }
            // Launch gallery to crop the photo
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(Uri.fromFile(oriImg), "image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB
                    || android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(AnalysisHolderActivity.TempCropFile)));
            }
            int outputX = 800;
            int outputY = 800;
            intent.putExtra("outputX", outputX);
            intent.putExtra("outputY", outputY);
            intent.putExtra("return-data", false);// 某些图片剪切出来的bitmap 会很大，导致 intent transaction
            // faield。
            startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            Toast.makeText(this, R.string.photoPickerNotFoundText, Toast.LENGTH_LONG).show();
        }
    }
}
