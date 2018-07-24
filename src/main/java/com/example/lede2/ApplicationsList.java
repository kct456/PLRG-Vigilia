package com.example.lede2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by Brian on 4/16/2018.
 */

public class ApplicationsList extends AppCompatActivity implements View.OnClickListener {
    private Button homeSecurityButton;
    private Button irrigationButton;
    private Button lifxTestButton;
    private Button smartLightsButton;
    private Button speakerButton;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.applications);
        homeSecurityButton = (Button) findViewById(R.id.homeSecurityButton);
        irrigationButton = (Button) findViewById(R.id.irrigationButton);
        lifxTestButton = (Button) findViewById(R.id.lifxTestButton);
        smartLightsButton = (Button) findViewById(R.id.smartLightsButton);
        speakerButton = (Button) findViewById(R.id.speakerButton);

        homeSecurityButton.setOnClickListener(this);
        irrigationButton.setOnClickListener(this);
        lifxTestButton.setOnClickListener(this);
        smartLightsButton.setOnClickListener(this);
        speakerButton.setOnClickListener(this);



    }

    @Override
    public void onClick(View view) {
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please Wait");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.show();

        if(view == homeSecurityButton){
            startActivity(new Intent(this, HomeSecurity.class));
        }
        if(view == irrigationButton){
            startActivity(new Intent(this, Irrigation.class));

        }
        if(view == lifxTestButton){
            startActivity(new Intent(this, Lifxtest.class));

        }
        if(view == smartLightsButton){
            startActivity(new Intent(this, SmartLights.class));

        }
        if(view == speakerButton){
            startActivity(new Intent(this, Speaker.class));

        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
    }
}



