package com.example.lede2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Brian on 2/16/2018.
 */

public class DriversSelectActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener {
    Spinner typeSpinner;
    Button doneButton;
    Properties prop;
    ProgressDialog dialog;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drivers);
        SSH_MySQL getListOfDrivers = new SSH_MySQL();
        typeSpinner = (Spinner) findViewById(R.id.spinnerDrivers);
        doneButton = (Button) findViewById(R.id.doneDrivers);
        String getListOfDriversCommand  = this.getString(R.string.getListOfDriversCommand);
        List<String> results = new ArrayList<>();
        String driversListFilename = this.getString(R.string.driversListFilename);

        //get list of all driver types
        try {
            getListOfDrivers.execute(getListOfDriversCommand);
            Thread.sleep(1000);//To execute asyntask in ssh object, we have to sleep main thread
            results = getListOfDrivers.getResultLines();
            while(results.size() == 0){
                Thread.sleep(500);
                results = getListOfDrivers.getResultLines();
            }
            System.out.println(results);
        } catch (Exception e) {
            Log.d("Sleep exception", "exception at getSetsAndRelations");
        }

        addSSHResultsToConfig(results, driversListFilename);
        System.out.println("read from file: " + ConfigFileIO.readFromFile(driversListFilename, this));
        prop = new Properties();
        File file = new File(getApplicationContext().getFilesDir(), driversListFilename);
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(file);
            prop.load(fis);
            fis.close();
        }
        catch (IOException ex){
            System.out.println("Error when reading drivers list file ");
            ex.printStackTrace();
        }

        ArrayList driverTypeList = new ArrayList();
        //add sets relations to the relation hashmap
        int numFields  = Integer.parseInt(prop.getProperty("FIELD_NUMBER"));
        for(int i = 0; i < numFields; i++){
            driverTypeList.add(prop.getProperty("FIELD_" + i));
        }
        ArrayAdapter typeAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, driverTypeList);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);


        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        doneButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if (view == doneButton) {
            dialog = new ProgressDialog(this);
            dialog.setMessage("Please Wait");
            dialog.setCancelable(false);
            dialog.setInverseBackgroundForced(false);
            dialog.show();

            Log.d("CLICKING", "Clicking on done button!");
            //determine the where to cat based on user selection and go to next screen
            //looks at config file for the command based on the user selection
            String userSelection = typeSpinner.getSelectedItem().toString();
            String fromProperty = prop.getProperty(userSelection);
            String command2 = prop.getProperty("COMMAND_PREFIX") + fromProperty;
                Intent i = new Intent(this, DriversAddActivity.class);
                i.putExtra("getConfigCommand", command2);
                startActivity(i);
                finish();
        }

    }
    @Override
    protected void onStop() {
        super.onStop();
        if(dialog != null && dialog.isShowing()){
            dialog.dismiss();
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

    protected void addSSHResultsToConfig(List<String> results, String filename) {
        ConfigFileIO.writeToNewFile(filename,"", this );
        File dir = getFilesDir();
        File file = new File(dir, filename);
        file.delete();
        for (int i = 0; i < results.size(); i++) {
            ConfigFileIO.writeToFile(filename, results.get(i), this);
        }
    }
}