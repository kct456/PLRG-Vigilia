package com.example.lede2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Created by Brian on 4/16/2018.
 */

public class DriversAddActivity extends ApplicationWithRelations{

    private SSH_MySQL getSetsAndRelations;
    private SSH_MySQL getSetList;
    private ArrayList<String> relationNames; //relations in this application

    Context context;
    private HashMap<String, ArrayList<String>> availableSets; //all possible sets pulled from pi


    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drivers_add);
        super.context = this;
        context = super.context;
        getSetsAndRelations = new SSH_MySQL();
        getSetList = new SSH_MySQL();
        Intent intent = getIntent();
        String getSetsCommand = intent.getExtras().getString("getConfigCommand");


        String getIoTDeviceAddressListCommand = this.getString(R.string.getIoTDeviceAddress); //edit this to new file
        List<String> results = new ArrayList<>();
        final String driverConfigFilename = this.getString(R.string.driverConfigFilename);
        relations = new HashMap<>();
        relationNames = new ArrayList<>();
        relationNameSpinner = (Spinner) findViewById(R.id.selectedRelation);
        relationLeftSpinner = (Spinner) findViewById(R.id.relationLeftSpinner);
        relationRightSpinner = (Spinner) findViewById(R.id.relationRightSpinner);
        addRelationButton = (Button) findViewById(R.id.addRelationButton);
        doneRelationButton = (Button) findViewById(R.id.addRelationDoneButton);
        deleteRelationButton = (Button) findViewById(R.id.addRelationDeleteButton);
        setOrRelationSpinner = (Spinner) findViewById(R.id.setsOrRelations);
        super.hasIndependentSets = true;
        super.independentSetNames = new ArrayList<>();
        super.independentSetList = new HashMap<>();
        super.addedSetList = new HashMap<>();
        super.addedIndependentSets = new HashMap<>();

        super.hasRelations = false;

        addRelationButton.setOnClickListener(this);
        doneRelationButton.setOnClickListener(this);
        deleteRelationButton.setOnClickListener(this);
        addRelationsListView = (ListView) findViewById(R.id.selectedRelationsList);
        addRelationsListView.setFocusable(false);
        super.addedRelations = new HashMap<>();
        HashSet<Pair<String, String>> addedrelationsSet = new HashSet<>();
        //addedRelations.put("", addedrelationsSet);
        super.applicationSetList = new HashSet<>();
        setHashMap = new HashMap<>();
        System.out.println("THIS COMMAND  " + getSetsCommand);

        //pull data from config file on iotuser
        try {
            getSetsAndRelations.execute(getSetsCommand);
            Thread.sleep(1000);//To execute asyntask in ssh object, we have to sleep main thread
            results = getSetsAndRelations.getResultLines();
            while(results.size() == 0){
                Thread.sleep(500);
                results = getSetsAndRelations.getResultLines();
            }
            System.out.println(results);
        } catch (Exception e) {
            Log.d("Sleep exception", "exception at getSetsAndRelations");
        }

        addSSHResultsToConfig(results, driverConfigFilename);
        System.out.println("read from file: " + ConfigFileIO.readFromFile(driverConfigFilename, this));
        super.prop = new Properties();
        File file = new File(getApplicationContext().getFilesDir(), driverConfigFilename);
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(file);
            super.prop.load(fis);
            fis.close();
        }
        catch (IOException ex){
            System.out.println("Error when reading SpeakerController Config file ");
            ex.printStackTrace();
        }

        //add sets relations to the relation hashmap
        super.numFields = Integer.parseInt(super.prop.getProperty("FIELD_NUMBER"));
        for(int i = 0; i < super.numFields; i++){
            String name = (super.prop.getProperty("FIELD_" + i));
            if(super.prop.getProperty("FIELD_TYPE_" + i).equals("IoTRelation")) {
                String relationPair = super.prop.getProperty("FIELD_CLASS_0_" + i) + "," + super.prop.getProperty("FIELD_CLASS_1_" + i);
                addPair(name, relationPair);
                super.hasRelations = true;
                relationNames.add(name);
            }
            if(super.prop.getProperty("FIELD_TYPE_" + i).equals("IoTSet")) {
                if(super.prop.getProperty("FIELD_INDEPENDENT_" + i).equals("TRUE")){
                    independentSetList.put(name, super.prop.getProperty("FIELD_CLASS_" + i));
                    independentSetNames.add(name);
                }
                else{
                    setHashMap.put(name, super.prop.getProperty("FIELD_CLASS_" + i));

                }
            }
        }
        //pull availableSets file from iotuser
        try {
            getSetList.execute(getIoTDeviceAddressListCommand);
            Thread.sleep(1000);//To execute asyntask in ssh object, we have to sleep main thread
            results = getSetList.getResultLines();
            while(results.size() == 0){
                Thread.sleep(500);
                results = getSetList.getResultLines();
            }
            System.out.println(results);
        } catch (Exception e) {
            Log.d("Sleep exception", "exception at getSetsAndRelations");
        }
        String setListFilename = this.getString(R.string.setListFilename);
        addSSHResultsToConfig(results, setListFilename);
        availableSets =  createSetList(setListFilename);
        System.out.println();

        ArrayList<String> setsOrRelationArrayList = new ArrayList<>();
        if(super.hasIndependentSets){
            setsOrRelationArrayList.add("SETS");

        }
        if(super.hasRelations) {
            setsOrRelationArrayList.add("RELATIONS");
        }
        ArrayAdapter setsOrRelationsArrayAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, setsOrRelationArrayList);
        setOrRelationSpinner.setAdapter(setsOrRelationsArrayAdapter);



        setOrRelationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //set array adapter values = listed relations
                if(setOrRelationSpinner.getSelectedItem().toString().equals("RELATIONS")) {
                    ArrayAdapter nameAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, relationNames);
                    nameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    relationNameSpinner.setAdapter(nameAdapter);
                }
                else{
                    ArrayAdapter nameAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, independentSetNames);
                    nameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    relationNameSpinner.setAdapter(nameAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });




        //sets left and right possible values to registered devices in the sets
        relationNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {


                if(setOrRelationSpinner.getSelectedItem().toString().equals("RELATIONS")) {
                    relationRightSpinner.setVisibility(View.VISIBLE);
                    String selectedRelation = relationNameSpinner.getSelectedItem().toString();
                    ArrayList availableDevicesLeft = availableSets.get(relations.get(selectedRelation).first);
                    ArrayList availableDevicesRight = availableSets.get(relations.get(selectedRelation).second);

                    ArrayAdapter leftAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, availableDevicesLeft);
                    ArrayAdapter rightAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, availableDevicesRight);

                    leftAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    rightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    relationLeftSpinner.setAdapter(leftAdapter);
                    relationRightSpinner.setAdapter(rightAdapter);
                }
                else{
                    relationRightSpinner.setVisibility(View.GONE);

                    String selectedSet = relationNameSpinner.getSelectedItem().toString();
                    ArrayList availableDevicesLeft = availableSets.get(independentSetList.get(selectedSet));
                    Collections.sort(availableDevicesLeft);
                    ArrayAdapter leftAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, availableDevicesLeft);
                    leftAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    relationLeftSpinner.setAdapter(leftAdapter);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        ArrayList<String> addedRelationsArrayList = new ArrayList<>();
        Set<String> relationTypes = super.addedRelations.keySet();
        for(String s: relationTypes) {
            for (Pair<String, String> temp : super.addedRelations.get(s)) {
                addedRelationsArrayList.add(temp.first + " " + temp.second);
            }
        }
        adapter = new ArrayAdapter(context, android.R.layout.simple_list_item_multiple_choice, addedRelationsArrayList);
        addRelationsListView.setAdapter(adapter);
        addRelationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }});
        for(int i= 0; i < relationNames.size(); i++){
            Pair<String, String> tempPair =  relations.get(relationNames.get(i));
            applicationSetList.add(tempPair.first);
            applicationSetList.add(tempPair.second);
        }
    }


}

