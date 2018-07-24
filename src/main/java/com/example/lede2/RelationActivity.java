package com.example.lede2;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.io.InputStream;

public class RelationActivity extends AppCompatActivity implements View.OnClickListener,View.OnFocusChangeListener {

    Button addButton;
    Button deleteButton;
    EditText databaseInfo;
    EditText idSource;
    EditText idDestination;
    private SSH_MySQL ssh;//Connection object between Android & Host

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relation);

        addButton = (Button) findViewById(R.id.addButton);
        deleteButton = (Button) findViewById(R.id.delButton);
        databaseInfo = (EditText)findViewById(R.id.textInfoComm);
        idSource = (EditText)findViewById(R.id.id_source);
        idDestination = (EditText)findViewById(R.id.id_destination);

        addButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        databaseInfo.setOnFocusChangeListener(this);
        idSource.setOnFocusChangeListener(this);
        idDestination.setOnFocusChangeListener(this);
        ssh = new SSH_MySQL();
        // Set config text from file for device
        try {
            InputStream is = getAssets().open(MainActivity.DEF_INSTALL_ADDRESS_FILE);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String text = new String(buffer);
            databaseInfo.setGravity(Gravity.LEFT);
            databaseInfo.setText(text);
            Log.d("LOADINGFILE", "Add comm info file is already loaded!");
        } catch (IOException ex) {
            Log.d("LOADINGFILE", "Add comm info file is NOT loaded!");
            ex.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if(v == addButton){
            // 1) Create a new file and insert the configuration
            // 2) Run iotinstaller code for communication/relation installation
            // 3) Remove the existing config file
            ssh.execute("echo \"" + databaseInfo.getText().toString() + "\" >> " +
                    MainActivity.DEF_MYSQL_CONFIG_FILE + ";" +
                    MainActivity.DEF_INSTALL_RELATION_CMD + " " + MainActivity.DEF_MYSQL_CONFIG_FILE + ";" +
                    "rm -rf " + MainActivity.DEF_MYSQL_CONFIG_FILE);
            finish();
        }
        if(v == deleteButton){
            // Delete a communication/relation entry
            ssh.execute(MainActivity.DEF_DELETE_RELATION_CMD + " " + idSource.getText().toString()
                    + " " + idDestination.getText().toString());
            finish();
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (hasFocus) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        } else {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
