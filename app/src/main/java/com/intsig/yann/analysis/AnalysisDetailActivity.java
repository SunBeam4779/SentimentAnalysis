package com.intsig.yann.analysis;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class AnalysisDetailActivity extends AppCompatActivity {

    public static final String DATA_ID = "DATA_ID";
    public static final String IMAGE_NAME = "IMAGE_NAME";

    private long dataId;
    private String imageName;
    private long detailDate;
    private String detailResult;
    private boolean isAnalysis;
    private ImageView photoImageView;
    private TextView photoDateTextView;
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
                String bigImage = cursor.getString(cursor.getColumnIndex(AnalysisData.SMALL_IMG));
                long date = cursor.getLong(cursor.getColumnIndex(AnalysisData.CREATE_DATE));
                String status = cursor.getString(cursor.getColumnIndex(AnalysisData.FATIGUE));
                photoImageView.setImageBitmap(Util.loadBitmap(bigImage));
                detailTextView.setText(status);
                photoDateTextView.setText(getString(R.string.photo_data, Util.parseDateString(date)));
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
        getContentResolver().insert(AnalysisData.CONTENT_URI, contentValues);
    }

    private void initFromXml() {
        photoImageView = (ImageView) findViewById(R.id.photo_ImageView);
        photoDateTextView = (TextView) findViewById(R.id.photo_date_TextView);
        detailTextView = (TextView) findViewById(R.id.detail_TextView);
        saveButton = (Button)findViewById(R.id.save_Button);
    }

    private void initData() {
        if (getIntent() == null) {
            finish();
        }
        dataId = getIntent().getLongExtra(DATA_ID, 0L);
        imageName = getIntent().getStringExtra(IMAGE_NAME);
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
            showProgress();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismissProgress();
                    photoImageView.setImageBitmap(Util.loadBitmap(Util.ORIGINAL_IMG + "/"+ imageName));
                    detailResult = "疲劳度为67，非常疲劳，建议应该注重休息，平常多喝水，调整睡眠，日常多进行运动，这样有助于改善身体机能";
                    detailTextView.setText(detailResult);
                    detailDate = System.currentTimeMillis();
                    photoDateTextView.setText(Util.parseDateString(detailDate));

                }
            }, 3000);
        } else {
            saveButton.setVisibility(View.GONE);
            isAnalysis = false;
        }
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

    public static void startActivity(Activity activity, long dataId) {
        Intent intent = new Intent(activity, AnalysisDetailActivity.class);
        intent.putExtra(DATA_ID, dataId);
        activity.startActivity(intent);
    }

    public static void startActivity(Activity activity, String imageName) {
        Intent intent = new Intent(activity, AnalysisDetailActivity.class);
        intent.putExtra(IMAGE_NAME, imageName);
        activity.startActivity(intent);
    }
}
