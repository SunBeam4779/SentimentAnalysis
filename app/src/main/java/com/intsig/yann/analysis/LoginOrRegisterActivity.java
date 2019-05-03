package com.intsig.yann.analysis;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by wo on 2018/4/7.
 */

public class LoginOrRegisterActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String EXTRA_IS_LOGIN = "EXTRA_IS_LOGIN";

    private Button loginButton;
    private EditText loginEditText;
    private String accountName;
    private long accountId = -1;
    private boolean isLogin = false;

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

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.login_Button) {
            accountName = loginEditText.getText().toString();
            if (TextUtils.isEmpty(accountName)) {
                Toast.makeText(this, R.string.no_name, Toast.LENGTH_LONG).show();
                return;
            }
            accountId = getAccountId(accountName);
            if (isLogin) {
                if (accountId > 0) {
                    AnalysisHolderActivity.startActivity(this,accountId);
                    finish();
                } else {
                    Toast.makeText(this, R.string.error_account, Toast.LENGTH_LONG).show();
                }
            } else {
                if (accountId > 0) {
                    Toast.makeText(this, R.string.has_account, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    PhotoBaseActivity.startActivity(LoginOrRegisterActivity.this, accountName);
                    finish();
                }
            }
        }
    }

    private void initFromXml() {
        loginButton = (Button) findViewById(R.id.login_Button);
        loginEditText = (EditText) findViewById(R.id.login_EditText);
    }

    private long getAccountId(String name) {
        long accountId = -1;
        Cursor cursor = getContentResolver().query(Uri.withAppendedPath(AccountData.CONTENT_URI, name),
                new String[] {AccountData._ID}, null, null, null);
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            accountId = cursor.getLong(0);
        }
        Util.safeCloseCursor(cursor);
        return accountId;
    }

    private void initView() {
        if (getIntent() != null) {
            isLogin = getIntent().getBooleanExtra(EXTRA_IS_LOGIN, false);
        }
        loginButton.setOnClickListener(this);
    }

    public static void startActivity(Activity activity, boolean isLogin) {
        Intent intent = new Intent(activity, LoginOrRegisterActivity.class);
        intent.putExtra(EXTRA_IS_LOGIN, isLogin);
        activity.startActivity(intent);
    }


}
