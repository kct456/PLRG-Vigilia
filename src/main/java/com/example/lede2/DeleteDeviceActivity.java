package com.example.lede2;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class DeleteDeviceActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener {

    DatabaseObject databaseObject;
    protected String database_information_filename;
    protected String id_information_filename;
    protected String local_id_information_filename;
    Button deleteButton;
    Button doneButton;
    Spinner spinner1;
    Spinner spinner2;
    Spinner spinner3;
    Context context;
    private SSH_MySQL ssh;//Connection object between Android & Host

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_device);

        database_information_filename = this.getString(R.string.device_param_config_filename);
        id_information_filename = this.getString(R.string.device_id_config_filename);
        local_id_information_filename = this.getString(R.string.device_id_config_filename);
        deleteButton = (Button) findViewById(R.id.delDoneButton);
        spinner1 = (Spinner) findViewById(R.id.delspinner1);
        spinner2 = (Spinner) findViewById(R.id.delspinner2);
        spinner3 = (Spinner) findViewById(R.id.delspinner3);
        context = this;

        //similar to install format. Sets the subtypes to appear based on type chosen
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedType = spinner1.getSelectedItem().toString();
                Set<String> keysForSubType = (databaseObject.getTypeObject(selectedType)).getKeySet();
                ArrayList<String> subtypes = new ArrayList<String>();
                for(String key: keysForSubType){
                    subtypes.add(key);
                }
                Collections.sort(subtypes);
                ArrayAdapter subtypeAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, subtypes);
                subtypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner2.setAdapter(subtypeAdapter);
            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //sets the instances of devices to appear based on selected subtype
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                DatabaseSubtypeObject subtypeObject = getCurrentSubtypeObject();
                List<String> deviceList = subtypeObject.getDevices();
                Collections.sort(deviceList);
                ArrayList<String> devices = new ArrayList<>();
                for(String device: deviceList){
                    devices.add(device);
                }
                Collections.sort(devices);
                ArrayAdapter deviceAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, devices);
                deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner3.setAdapter(deviceAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });




        deleteButton.setOnClickListener(this);
        ssh = new SSH_MySQL();
        String databaseInformation = ConfigFileIO.readFromFile(
                database_information_filename, this);
        String idInformation = ConfigFileIO.readFromFile(local_id_information_filename, this);
        databaseObject = ConfigFileIO.createDatabaseObject(context, idInformation );
        Set<String> keysForType = databaseObject.getKeySet();
        ArrayList<String> types = new ArrayList<String>();
        for(String key: keysForType){
            types.add(key);
        }
        Collections.sort(types);
        ArrayAdapter typeAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(typeAdapter);
    }

    @Override
    public void onClick(View v) {
        if(v == deleteButton){
            DatabaseSubtypeObject subtypeObject = getSubtypeObject();
            // Remove the entry based on ID, type, and subtype
            String deleteCommand = MainActivity.DEF_DELETE_CMD + " " +
                    spinner3.getSelectedItem().toString() + " " +
                    spinner1.getSelectedItem().toString() + " " +
                    spinner2.getSelectedItem().toString() + ";";


            // Also remove the device address. Some need multiple address deletions such as ihome
            if(subtypeObject.getNumAddresses() > 1) {
                for(int i = 1; i <= subtypeObject.getNumAddresses(); i++){
                    deleteCommand += MainActivity.DEF_DELETE_ADDRESS_CMD + " " +
                            spinner3.getSelectedItem().toString() + " " +
                            spinner2.getSelectedItem().toString() + "Add" + i + ";";
                }
            }
            else{
                deleteCommand += MainActivity.DEF_DELETE_ADDRESS_CMD + " " +
                        spinner3.getSelectedItem().toString() + " " +
                        spinner2.getSelectedItem().toString() + "Add;";

            }

            deleteCommand += context.getResources().getString(R.string.delete_zb_cmd) + " " +
                    spinner3.getSelectedItem().toString() + " " +
                    spinner2.getSelectedItem().toString() + "ZBAdd;";
            //need to add delete from zigbee



            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("Please Wait");
            dialog.setCancelable(false);
            dialog.setInverseBackgroundForced(false);
            dialog.show();


            System.out.println(deleteCommand);
            ssh = new SSH_MySQL();
            ssh.execute(deleteCommand);

            deleteDeviceFromDatabase();
            updateIoTDeviceAddress();
            updateSetList();

            dialog.dismiss();
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
    public DatabaseSubtypeObject getCurrentSubtypeObject(){
        return databaseObject.getTypeObject(spinner1.
                getSelectedItem().toString()).getSubtypeObject(spinner2.getSelectedItem().toString());
    }

    //rewrites the local config file to not contain the deleted instance
    public void deleteDeviceFromDatabase(){
        DatabaseSubtypeObject subtypeObject = getCurrentSubtypeObject();
        String subtype = spinner2.getSelectedItem().toString();
        String id = spinner3.getSelectedItem().toString();
        subtypeObject.deleteDevice(id);
        String deviceToDelete = subtype + " " + id;
        //do it for the database file. also need
        String databaseInformation = ConfigFileIO.readFromFile(local_id_information_filename, this );
        Scanner scanner = new Scanner(databaseInformation);
        ConfigFileIO.writeToNewFile(local_id_information_filename, "", this);
        while (scanner.hasNextLine()){
            String temp = scanner.nextLine();
            if (!temp.equals(deviceToDelete)){
                ConfigFileIO.writeToFile(local_id_information_filename, temp, this);
            }
        }
    }


    private DatabaseSubtypeObject getSubtypeObject(){
        DatabaseSubtypeObject subtypeObject = databaseObject.getTypeObject(
                spinner1.getSelectedItem().toString()).getSubtypeObject(
                        spinner2.getSelectedItem().toString());
        return subtypeObject;
    }

    private  void addSSHResultsToConfig(List<String> results, String filename) {
        File dir = getFilesDir();
        File file = new File(dir, filename);
        file.delete();
        for (int i = 0; i < results.size(); i++) {
            ConfigFileIO.writeToFile(filename, results.get(i), this);
        }
        finish();
    }

    private void updateSetList(){
        SSH_MySQL ssh = new SSH_MySQL();
        String command = this.getString(R.string.updateSetList);
        List<String> results = new ArrayList<>();
        String setListFilename = this.getString(R.string.setListFilename);
        try {
            ssh.execute(command);
            Thread.sleep(1000);//To execute asyntask in ssh object, we have to sleep main thread
            results = ssh.getResultLines();
            while(results.size() == 0){
                Thread.sleep(500);
                results = ssh.getResultLines();
            }
            System.out.println(results);
        } catch (Exception e) {
            Log.d("Sleep exception", "exception at getSetsAndRelations");
        }
//        ssh = new SSH_MySQL();
//        command = this.getString(R.string.getSetList);
//        try {
//            ssh.execute(command);
//            Thread.sleep(1000);//To execute asyntask in ssh object, we have to sleep main thread
//            results = ssh.getResultLines();
//            while(results.size() == 0){
//                Thread.sleep(500);
//                results = ssh.getResultLines();
//            }
//            System.out.println(results);
//        } catch (Exception e) {
//            Log.d("Sleep exception", "exception at getSetsAndRelations");
//        }
//        addSSHResultsToConfig(results, setListFilename);

    }
    private void updateIoTDeviceAddress(){
        SSH_MySQL ssh = new SSH_MySQL();
        String command = this.getString(R.string.updateIoTDeviceAddress);
        List<String> results = new ArrayList<>();
        String iotDeviceAddressFilename = this.getString(R.string.updateIoTDeviceAddress);
        try {
            ssh.execute(command);
            Thread.sleep(1000);//To execute asyntask in ssh object, we have to sleep main thread
            results = ssh.getResultLines();
            while(results.size() == 0){
                Thread.sleep(500);
                results = ssh.getResultLines();
            }
            System.out.println(results);
        } catch (Exception e) {
            Log.d("Sleep exception", "exception at getSetsAndRelations");
        }
//        ssh = new SSH_MySQL();
//        command = this.getString(R.string.getIoTDeviceAddress);
//        try {
//            ssh.execute(command);
//            Thread.sleep(1000);//To execute asyntask in ssh object, we have to sleep main thread
//            results = ssh.getResultLines();
//            while(results.size() == 0){
//                Thread.sleep(500);
//                results = ssh.getResultLines();
//            }
//            System.out.println(results);
//        } catch (Exception e) {
//            Log.d("Sleep exception", "exception at getSetsAndRelations");
//        }
//        addSSHResultsToConfig(results, iotDeviceAddressFilename);
    }

}
