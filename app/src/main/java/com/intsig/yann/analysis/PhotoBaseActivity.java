package com.intsig.yann.analysis;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class PhotoBaseActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String ACCOUNT_NAME = "ACCOUNT_NAME";
    private static final int REQUEST_PERMISSION = 102;
    private static final int REQUEST_CAMERA = 103;
    private static final int REQUEST_CROP = 104;

    private ImageView photoImageView;
    private ImageView smallImageView;
    private Button photoButton;
    private Button sureButton;
    private String accountName;
    private File currentPhotoFile;
    private String myBigImage;
    private String mySmallImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_base);
        initFromXml();
        initView();
    }

    private void initFromXml() {
        photoImageView = (ImageView) findViewById(R.id.photo_ImageView);
        photoButton = (Button) findViewById(R.id.photo_Button);
        sureButton = (Button) findViewById(R.id.sure_Button);
        smallImageView = (ImageView) findViewById(R.id.small_ImageView);
    }

    private void initView() {
        if (getIntent() != null) {
            accountName = getIntent().getStringExtra(ACCOUNT_NAME);
        }
        photoButton.setOnClickListener(this);
        sureButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.photo_Button) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    PermissionChecker.checkSelfPermission(PhotoBaseActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.CAMERA}, REQUEST_PERMISSION);
            } else {
                takePhoto();
            }
        } else if (id == R.id.sure_Button) {
            if (TextUtils.isEmpty(accountName) || TextUtils.isEmpty(myBigImage) || TextUtils.isEmpty(mySmallImage)) {
                Toast.makeText(this, R.string.unComplete_info, Toast.LENGTH_LONG).show();
                return;
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(AccountData.SMALL_IMG, mySmallImage);
            contentValues.put(AccountData.BIG_IMG, myBigImage);
            contentValues.put(AccountData.CREATE_DATE, System.currentTimeMillis());
            contentValues.put(AccountData.ACCOUNT_NAME, accountName);
            getContentResolver().insert(AccountData.CONTENT_URI, contentValues);
            Toast.makeText(this, R.string.base_photo_save_tips, Toast.LENGTH_LONG).show();
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
            Uri uri = null;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + Util.FILE_PROVIDER_AUTHORITIES, currentPhotoFile);
            } else {
                uri = Uri.fromFile(currentPhotoFile);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(intent, REQUEST_CAMERA);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.photoPickerNotFoundText, Toast.LENGTH_LONG).show();
        }
    }

    private void doCropPhoto(File oriImg, int requestCode) {
        Uri photoURI = null;
        try {
            photoURI = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + Util.FILE_PROVIDER_AUTHORITIES, oriImg);
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            File cfile = new File(AnalysisHolderActivity.TempCropFile);
            if (cfile.exists()) {
                cfile.delete();
            }
            // Launch gallery to crop the photo
            Intent intent = new Intent("com.android.camera.action.CROP");
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.M) {//7.0以上
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(photoURI, "image/*");
            } else {
                intent.setDataAndType(Uri.fromFile(oriImg), "image/*");
            }
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

    public static void startActivity(Activity activity, String accountName) {
        Intent intent = new Intent(activity, PhotoBaseActivity.class);
        intent.putExtra(ACCOUNT_NAME, accountName);
        activity.startActivity(intent);
    }
}
