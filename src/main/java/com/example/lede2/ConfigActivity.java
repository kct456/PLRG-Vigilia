package com.example.lede2;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.*;

public class ConfigActivity extends AppCompatActivity implements View.OnClickListener,View.OnFocusChangeListener {

    EditText ssid;
    EditText pwd;
    EditText ip;
    EditText user;
    Button save;
    public static String RSSID = MainActivity.DEF_RSSID;
    public static String RPWD = MainActivity.DEF_RPWD;
    public static String ROUTERIP = MainActivity.DEF_ROUTERIP;
    public static String ROUTERUSER = MainActivity.DEF_ROUTERUSER;
    public static String MYSQLHOSTUSER = MainActivity.DEF_MYSQLHOSTUSER;
    public static String MYSQLHOSTIP = MainActivity.DEF_MYSQLHOSTIP;
    public static String MYSQLHOSTPASSWORD = MainActivity.DEF_MYSQLHOSTPASSWORD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        ssid = (EditText) findViewById(R.id.device_subtype);
        pwd = (EditText) findViewById(R.id.pwd);
        ip = (EditText) findViewById(R.id.ip);
        user = (EditText) findViewById(R.id.user);
        save = (Button) findViewById(R.id.save);

        ssid.setOnFocusChangeListener(this);
        pwd.setOnFocusChangeListener(this);
        ip.setOnFocusChangeListener(this);
        user.setOnFocusChangeListener(this);
        save.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == save) {
            if (ssid.getText().toString().equals("") || pwd.getText().toString().equals("") ||
                    ip.getText().toString().equals("") || user.getText().toString().equals("")) {
                Toast t = Toast.makeText(this, R.string.blank, Toast.LENGTH_SHORT);
                t.show();
                return;
            } else {
                RSSID = ssid.getText().toString();
                RPWD = pwd.getText().toString();
                ROUTERIP = ip.getText().toString();
                ROUTERUSER = user.getText().toString();
                finish();
            }
        }
    }


    @Override
    public void onFocusChange(View view, boolean hasFocus) {//function not to modify randomly generated password for newly registered device
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (hasFocus) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        } else {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
