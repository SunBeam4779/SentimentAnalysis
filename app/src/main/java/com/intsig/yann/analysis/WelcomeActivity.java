package com.intsig.yann.analysis;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener{
    //define two widgets
    private TextView newAccountTextView;
    private TextView oldAccountTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set the whole view
        setContentView(R.layout.activity_welcome);
        //modify the contents of the text view defined in the previous XML layout
        newAccountTextView = findViewById(R.id.new_account_TextView);
        oldAccountTextView = findViewById(R.id.old_account_TextView);
        //doing something after clicking the button
        newAccountTextView.setOnClickListener(this);
        oldAccountTextView.setOnClickListener(this);

    }

    public static void startActivity(Activity activity) {
        //jump to the activity
        Intent intent = new Intent(activity, WelcomeActivity.class);
        //start that activity
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
