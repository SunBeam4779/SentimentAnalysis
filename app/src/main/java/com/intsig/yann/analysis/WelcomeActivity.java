package com.intsig.yann.analysis;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener{
    //define two widgets
    private TextView newAccountTextView;
    private TextView oldAccountTextView;

    //the time point of the first back-button pressing
    private long lastBackTime = 0;
    //the time point of the current back-button pressing
    private long currentBackTime = 0;

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
            //LoginOrRegisterActivity.startActivity(this, false);
            //finish(); // delete this "finish()", then you can return to the welcome activity from the login activity.
            Intent intent1 = new Intent(this, LoginOrRegisterActivity.class);
            intent1.putExtra(LoginOrRegisterActivity.EXTRA_IS_LOGIN, false);
            startActivity(intent1);
        } else if (id == R.id.old_account_TextView) {
            //LoginOrRegisterActivity.startActivity(this, true);
            //finish(); // the same as the upper.
            Intent intent2 = new Intent(this, LoginOrRegisterActivity.class);
            intent2.putExtra(LoginOrRegisterActivity.EXTRA_IS_LOGIN, true);
            startActivity(intent2);
        }
    }
    //apply the function of pressing back-button to exit the app.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            currentBackTime = System.currentTimeMillis();
            if(currentBackTime - lastBackTime > 2*1000){
                Toast.makeText(this,"再次点击以退出", Toast.LENGTH_SHORT).show();
                lastBackTime = currentBackTime;
            }else{
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
