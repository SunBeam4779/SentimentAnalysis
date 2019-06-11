package com.intsig.yann.analysis;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.PermissionChecker;

import java.io.File;

public class PhotoBaseActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String ACCOUNT_NAME = "ACCOUNT_NAME";
    private static final int REQUEST_PERMISSION = 102;
    private static final int REQUEST_CAMERA = 103;
    private static final int REQUEST_CROP = 104;
    private static final int REQUEST_ALBUM = 105;
    private static final int REQUEST_PERMISSION2 = 106;
    private static final int REQUEST_TAKE_PICTURE = 107;

    private ImageView photoImageView;
    private ImageView smallImageView;
    private Button photoButton;
    private Button sureButton;
    private String accountName;
    private File currentPhotoFile;
    private String myBigImage;
    private String mySmallImage;
    private Button selectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_base);
        initFromXml();
        initView();
    }

    private void initFromXml() {
        photoImageView = findViewById(R.id.photo_ImageView);
        photoButton = findViewById(R.id.photo_Button);
        sureButton = findViewById(R.id.sure_Button);
        smallImageView = findViewById(R.id.small_ImageView);
        selectButton = findViewById(R.id.select_Button);
    }

    private void initView() {
        if (getIntent() != null) {
            accountName = getIntent().getStringExtra(ACCOUNT_NAME);
        }
        photoButton.setOnClickListener(this);
        sureButton.setOnClickListener(this);
        selectButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();
        if (id == R.id.photo_Button) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    PermissionChecker.checkSelfPermission(PhotoBaseActivity.this, Manifest.permission.CAMERA) != PermissionChecker.PERMISSION_GRANTED) {//
                requestPermissions(new String[] {Manifest.permission.CAMERA}, REQUEST_PERMISSION);
            } else {
                //startActivity(intent);
                takePicture();


            }
        } else if (id == R.id.select_Button) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    ContextCompat.checkSelfPermission(PhotoBaseActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(PhotoBaseActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION2);
            } else {
                openAlbum();
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
                doCropPhoto(currentPhotoFile);
            } else if (requestCode == REQUEST_TAKE_PICTURE) {
                doCropPicture();
            } else if (requestCode == REQUEST_ALBUM) {
                if (Build.VERSION.SDK_INT >= 19) {
                    //4.4及以上系统使用这个方法处理图片
                    handleImageOnKitKat(data);
                } else {
                    //4.4以下系统使用这个方法处理图片
                    handleImageBeforeKitKat(data);
                }
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
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0) {
                for (String permission : permissions) {
                    if (TextUtils.equals(permission, Manifest.permission.CAMERA) &&
                            PermissionChecker.checkSelfPermission(this, permission) == PermissionChecker.PERMISSION_GRANTED) {//
                        takePicture();//takePhoto();
                        return;
                    } else if (TextUtils.equals(permission, Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                            PermissionChecker.checkSelfPermission(this, permission) != PermissionChecker.PERMISSION_GRANTED) {//
                        Toast.makeText(this, R.string.need_sdcard_permission, Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Launches Camera to take a picture and store it in a file.
     */
    /*private void takePhoto() {

        //startActivityForResult(intent, REQUEST_CAMERA);
        try {
            File PHOTO_DIR = new File(Util.ORIGINAL_IMG);
            PHOTO_DIR.mkdirs();
            String time = Util.getDateAsName();
            myBigImage = Util.ORIGINAL_IMG + "/" + time + ".jpg";
            currentPhotoFile = new File(PHOTO_DIR, time + ".jpg");
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
            //Intent intent1 = new Intent(this, Camera_test.class);
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
    }*/

    private void takePicture() {
        Intent intent = new Intent(this, Camera_test.class);
        String time = Util.getDateAsName();
        myBigImage = Util.ORIGINAL_IMG + "/" + time + ".jpg";
        intent.putExtra("uri", myBigImage);
        File PHOTO_DIR = new File(Util.ORIGINAL_IMG);
        if (PHOTO_DIR.exists()) {
            currentPhotoFile = new File(PHOTO_DIR, time + ".jpg");
        }
        startActivityForResult(intent, REQUEST_TAKE_PICTURE);
    }

    private void openAlbum() {
        File PHOTO_DIR = new File(Util.ORIGINAL_IMG);
        PHOTO_DIR.mkdirs();
        String time = Util.getDateAsName();
        myBigImage = Util.ORIGINAL_IMG + "/" + time + ".jpg";
        currentPhotoFile = new File(PHOTO_DIR, time + ".jpg");


        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_ALBUM);
    }

    private void doCropPhoto(File oriImg) {
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
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(AnalysisHolderActivity.TempCropFile)));
            int outputX = 1840;//返回数据的时候的 X 像素大小。
            int outputY = 3264;//返回的时候 Y 的像素大小。
            intent.putExtra("outputX", outputX);
            intent.putExtra("outputY", outputY);
            intent.putExtra("return-data", false);// 某些图片剪切出来的bitmap 会很大，导致 intent transaction
            // faield。
            intent.putExtra("noFaceDetection", true);
            startActivityForResult(intent, PhotoBaseActivity.REQUEST_CROP);
        } catch (Exception e) {
            Toast.makeText(this, R.string.photoPickerNotFoundText, Toast.LENGTH_LONG).show();
        }
    }

    private void doCropPicture() {
        Intent intent = new Intent(this, Crop.class);
        intent.putExtra("uri", currentPhotoFile.toString());

        startActivityForResult(intent, PhotoBaseActivity.REQUEST_CROP);
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(".")[1];//解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        File currentPhotoFile = new File(imagePath);
        Util.copyFile(imagePath, myBigImage);
        doCropPhoto(currentPhotoFile);
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        File currentPhotoFile = new File(imagePath);
        Util.copyFile(imagePath, myBigImage);
        doCropPhoto(currentPhotoFile);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    static void startActivity(Activity activity, String accountName) {
        Intent intent = new Intent(activity, PhotoBaseActivity.class);
        intent.putExtra(ACCOUNT_NAME, accountName);
        activity.startActivity(intent);
    }
}
