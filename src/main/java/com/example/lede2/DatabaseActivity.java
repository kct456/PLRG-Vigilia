package com.example.lede2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

/**
 * Created by Brian on 2/16/2018.
 */

public class DatabaseActivity extends AppCompatActivity implements View.OnClickListener,
        View.OnFocusChangeListener {
    Button insertToDatabase;
    Button deleteFromDatabase;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.database);

        insertToDatabase = (Button) findViewById(R.id.doneDrivers);

        deleteFromDatabase = (Button) findViewById(R.id.deleteFromDatabase);
        insertToDatabase.setOnClickListener(this);
        deleteFromDatabase.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if (view == insertToDatabase) {
            Log.d("CLICKING", "Clicking on add device!");
            startActivity(new Intent(this, AddDeviceActivity.class));
        }
        if (view == deleteFromDatabase) {
            Log.d("CLICKING", "Clicking on install zigbee!");
            startActivity(new Intent(this, DeleteDeviceActivity.class));
        }

    }

    public void onFocusChange(View view, boolean hasFocus) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (hasFocus) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        } else {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}