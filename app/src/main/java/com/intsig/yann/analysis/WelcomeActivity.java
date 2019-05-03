package com.intsig.yann.analysis;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView newAccountTextView;
    private TextView oldAccountTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        newAccountTextView = (TextView) findViewById(R.id.new_account_TextView);
        oldAccountTextView = (TextView) findViewById(R.id.old_account_TextView);
        newAccountTextView.setOnClickListener(this);
        oldAccountTextView.setOnClickListener(this);

    }

    public static void startActivity(Activity activity) {
        Intent intent = new Intent(activity, WelcomeActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.new_account_TextView) {
            LoginOrRegisterActivity.startActivity(this, false);
            finish();
        } else if (id == R.id.old_account_TextView) {
            LoginOrRegisterActivity.startActivity(this, true);
            finish();
        }
    }
}
