package com.intsig.yann.analysis;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class AnalysisDetailActivity extends AppCompatActivity {

    public static final String DATA_ID = "DATA_ID";
    public static final String IMAGE_NAME = "IMAGE_NAME";
    public static final String ACCOUNT_ID = "ACCOUNT_ID";

    private long dataId;
    private long accountId;
    private String imageName;
    private long detailDate;
    private String detailResult;
    private boolean isAnalysis;
    private ImageView photoImageView;
    private ImageView myPhotoImageView;
    private TextView detailTextView;
    private Button saveButton;
    private ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis_detail);
        initFromXml();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isAnalysis) {
            Cursor cursor = getContentResolver().query(ContentUris.withAppendedId(AnalysisData.CONTENT_URI_ID, dataId), null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String bigImage = cursor.getString(cursor.getColumnIndex(AnalysisData.BIG_IMG));
                String status = cursor.getString(cursor.getColumnIndex(AnalysisData.FATIGUE));
                photoImageView.setImageBitmap(Util.loadBitmap(bigImage));
                detailTextView.setText(status);
                Util.safeCloseCursor(cursor);
            } else {
                finish();
            }
        } else {
            saveButton.setVisibility(View.VISIBLE);
        }
    }

    private void saveResult() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(AnalysisData.CREATE_DATE, detailDate);
        contentValues.put(AnalysisData.FATIGUE, detailResult);
        contentValues.put(AnalysisData.BIG_IMG, Util.ORIGINAL_IMG + "/" + imageName);
        contentValues.put(AnalysisData.SMALL_IMG, Util.THUMB_IMG + "/" +  imageName);
        contentValues.put(AnalysisData.ACCOUNT_ID, accountId);
        getContentResolver().insert(AnalysisData.CONTENT_URI, contentValues);
    }

    private void initFromXml() {
        photoImageView = (ImageView) findViewById(R.id.photo_ImageView);
        myPhotoImageView = (ImageView) findViewById(R.id.my_photo_ImageView);
        detailTextView = (TextView) findViewById(R.id.detail_TextView);
        saveButton = (Button)findViewById(R.id.save_Button);
    }

    private void initData() {
        if (getIntent() == null) {
            finish();
        }
        dataId = getIntent().getLongExtra(DATA_ID, 0L);
        imageName = getIntent().getStringExtra(IMAGE_NAME);
        accountId = getIntent().getLongExtra(ACCOUNT_ID, 0L);
        if (!TextUtils.isEmpty(imageName)) {
            saveButton.setVisibility(View.VISIBLE);
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveResult();
                    finish();
                }
            });
            isAnalysis = true;
            photoImageView.setImageBitmap(Util.loadBitmap(Util.ORIGINAL_IMG + "/"+ imageName));
            detailDate = System.currentTimeMillis();
        } else {
            saveButton.setVisibility(View.GONE);
            isAnalysis = false;
        }
        refreshMyPhoto();
    }

    private void refreshMyPhoto() {
        Cursor cursor = getContentResolver().query(AccountData.CONTENT_URI, null, AccountData._ID + "=?",
                new String[]{accountId + ""}, null);
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            myPhotoImageView.setImageBitmap(Util.loadBitmap(cursor.getString(cursor.getColumnIndex(AccountData.BIG_IMG))));
            if (isAnalysis) {
                final Bitmap origin = Util.loadBitmap(cursor.getString(cursor.getColumnIndex(AccountData.SMALL_IMG)));
                final Bitmap test = Util.loadBitmap(Util.THUMB_IMG + "/"+ imageName);
                showProgress();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        double[] originResult = getFeatureCal(origin);
                        double[] testResult = getFeatureCal(test);
                        final double result = FeatureNdkManager.featuresResult(originResult, testResult);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissProgress();
                                detailResult = getString(R.string.feature_result, result + "");
                                detailTextView.setText(detailResult);
                            }
                        });
                    }
                }).start();
            }
        }
        Util.safeCloseCursor(cursor);
    }

    private double[] getFeatureCal(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int[] pixels = new int[w*h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        double[] resultDouble = FeatureNdkManager.featuresCal(pixels, w, h);
        return resultDouble;
    }

    private void showProgress() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.analysis));
        }
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private void dismissProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public static void startActivity(Activity activity, long dataId, long accountId) {
        Intent intent = new Intent(activity, AnalysisDetailActivity.class);
        intent.putExtra(DATA_ID, dataId);
        intent.putExtra(ACCOUNT_ID, accountId);
        activity.startActivity(intent);
    }

    public static void startActivity(Activity activity, String imageName, long accountId) {
        Intent intent = new Intent(activity, AnalysisDetailActivity.class);
        intent.putExtra(IMAGE_NAME, imageName);
        intent.putExtra(ACCOUNT_ID, accountId);
        activity.startActivity(intent);
    }
}
