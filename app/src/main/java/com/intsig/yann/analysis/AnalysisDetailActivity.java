package com.intsig.yann.analysis;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

public class AnalysisDetailActivity extends AppCompatActivity {

    private ImageView photoImageView;
    private TextView photoDataTextView;
    private TextView detailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis_detail);
    }
}
