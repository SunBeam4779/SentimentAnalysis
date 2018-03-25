package com.intsig.yann.analysis;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

public class AnalysisDetailActivity extends AppCompatActivity {

    public static final String DATA_ID = "DATA_ID";

    private long dataId;
    private ImageView photoImageView;
    private TextView photoDataTextView;
    private TextView detailTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis_detail);
    }

    public static void startActivity(Activity activity, long dataId) {
        Intent intent = new Intent(activity, AnalysisDetailActivity.class);
        intent.putExtra(DATA_ID, dataId);
        activity.startActivity(intent);
    }
}
