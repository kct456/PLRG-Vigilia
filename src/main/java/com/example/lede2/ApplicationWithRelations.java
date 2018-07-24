package com.example.lede2;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.jcraft.jsch.HASH;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;


/**
 * Created by Brian on 4/27/2018.
 */

public class ApplicationWithRelations extends AppCompatActivity  implements View.OnClickListener {
    protected HashMap<String, ArrayList<String>> addedSetList; //list of sets that is currently being added and their values
    protected int numFields;
    protected Properties prop;
    protected HashMap<String, HashSet<Pair<String, String>>> addedRelations;
    protected HashMap<String, HashSet<String>> addedIndependentSets;
    protected Context context;
    protected HashMap<String, Pair<String, String>> relations;
    protected HashMap<String, String> setHashMap;
    protected Spinner relationNameSpinner;
    protected Spinner relationLeftSpinner;
    protected Spinner relationRightSpinner;
    protected Spinner setOrRelationSpinner;
    protected Button addRelationButton;
    protected Button doneRelationButton;
    protected Button deleteRelationButton;
    protected HashMap<String, String> independentSetList;
    protected ArrayList<String> independentSetNames;
    protected boolean hasIndependentSets;
    protected boolean hasRelations;
    ArrayList addedRelationsArrayList;
    ProgressDialog dialog;
    protected Scanner scanner;
    ArrayAdapter adapter;
    ListView addRelationsListView;
    protected HashSet<String> applicationSetList; //list of all sets that should be used




    protected void deleteFromSet(String device){
        Set<String> keyset = addedSetList.keySet();
        for(String s: keyset){
            ArrayList<String> tempSet =  addedSetList.get(s);
            tempSet.remove(device);
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

    protected void callSSHCommandAddComm(){
        //default already in bin/installer  (RELATION)
        String installCommPatternCommand = "";
        for(int i = 0; i < numFields; i++){
            if(prop.getProperty("FIELD_TYPE_" + i).equals("IoTRelation")){
                String writeToRelConfigFileCommand = "";
                writeToRelConfigFileCommand += this.getString(R.string.changeToSQLConfigFileLocation);

                String filename = prop.getProperty("FIELD_CONFIG_FILENAME_"+i);
                HashSet<Pair<String,String>> tempSet =  addedRelations.get(prop.getProperty("FIELD_" + i));

                //insert roomCameraReal.config writing
                writeToRelConfigFileCommand += "echo \"" + "SELECT RELATION FROM\n"+
                        "FIRST\n" +
                        prop.getProperty("FIELD_CLASS_0_" + i) + "\n" +
                        "OTHER\n" +
                        prop.getProperty("FIELD_CLASS_1_" + i) + "\n" +
                        "WHERE\n"; //need to add where clauses for each case
                int counter = 0;
                //iterates through all of the added relations under the current relationName
                //ex all relations under roomCameraRel
                for(Pair<String, String> pair: tempSet){
                    int size = tempSet.size();
                    Scanner scanner = new Scanner(pair.first);
                    String typeFirst = scanner.next();
                    String idFirst = scanner.next();
                    typeFirst += idFirst;
                    scanner = new Scanner(pair.second);
                    String typeSecond = scanner.next();
                    String idSecond = scanner.next();
                    typeSecond += idSecond;

                    writeToRelConfigFileCommand += "(TYPE_SOURCE='" + typeFirst + "' AND TYPE_DESTINATION='" + typeSecond + "')";


                    installCommPatternCommand += this.getString(R.string.changeToCommConfigFileLocation) + "echo \"" + idFirst + " " + typeFirst + "\n" +
                            idSecond + " " + typeSecond + "\n" +
                            "WRITE\" > " + filename + ";" +
                            "java -cp .:..:/usr/share/java/* iotinstaller.IoTInstaller -install_comm " + filename + ";" +
                            "rm " + filename + ";";

                    //if this is the last item, finish it by writing it into file
                    if(counter == size-1){
                        writeToRelConfigFileCommand += "\n;\" > " + prop.getProperty("FIELD_" + i) + ".config; \n";
                        installCommPatternCommand = writeToRelConfigFileCommand + installCommPatternCommand;
                        System.out.println("COMM"  + installCommPatternCommand);
                    }

                    //if it is not the last item, concatenate or and continue
                    else{
                        writeToRelConfigFileCommand += "\nOR\n";
                        System.out.println(installCommPatternCommand);
                    }

                    SSH_MySQL ssh_mySQL = new SSH_MySQL();
                    List<String> results = new ArrayList<>();
                    try {
                        ssh_mySQL.execute(installCommPatternCommand);
                        Thread.sleep(1000);//To execute asyntask in ssh object, we have to sleep main thread
                        results = ssh_mySQL.getResultLines();
                        while(results.size() == 0){
                            Thread.sleep(500);
                            results = ssh_mySQL.getResultLines();
                            System.out.println(results);
                        }
                        System.out.println(results);
                    } catch (Exception e) {
                        Log.d("Sleep exception", "exception at getSetsAndRelations");
                    }
                    installCommPatternCommand = "";
                    counter++;
                }

            }
        }
    }


    protected HashMap<String, ArrayList<String>> createSetList(String filename){
        HashMap<String, ArrayList<String>> setListHashMap = new HashMap<>();
        String setData = ConfigFileIO.readFromFile(filename, context);
        Scanner scanner = new Scanner(setData);
        while (scanner.hasNextLine()){
            String temp = scanner.nextLine();
            if(temp.equals("START")){
                String setName = scanner.nextLine();
                temp = scanner.nextLine();
                ArrayList<String> availableDevices = new ArrayList<>();
                while (!temp.equals("END")){
                    availableDevices.add(temp);
                    temp = scanner.nextLine();
                }
                setListHashMap.put(setName, availableDevices);
            }
        }
        return setListHashMap;
    }


    //relationofTwoItems comes in form of ex: RoomSmart,LightBulbSmart. Splits them by commma and adds them to hashmap
    protected void addPair(String name, String relationOfTwoItems){
        String firstItem = relationOfTwoItems.substring(0, relationOfTwoItems.indexOf(","));
        String secondItem = relationOfTwoItems.substring(relationOfTwoItems.indexOf(",")+1, relationOfTwoItems.length());
        Pair<String, String> pair = new Pair<>(firstItem, secondItem);
        relations.put(name,pair);
    }

    protected ArrayList<String> removeDuplicates(ArrayList<String> list){
        HashSet<String> set = new HashSet<>();
        set.addAll(list);
        ArrayList<String> noDups = new ArrayList<>();
        noDups.addAll(set);
        return noDups;
    }
    protected String generateSQLQuery(String setName, ArrayList<String> listOfDevices){
        String query = "";
        query = "SELECT * FROM\n" +
                setName + "\n" +
                "WHERE\n";
        for(int i = 0; i < listOfDevices.size(); i++){
            Scanner scanner = new Scanner(listOfDevices.get(i));
            String type = scanner.next();
            String id = scanner.next();
            query += "(TYPE='" + type + "' AND ID='" + id + "')";
            if(i == listOfDevices.size() -1){
                query +="\n;";
            }
            else{
                query += "\nOR\n";
            }
        }
        return query;
    }

    protected String generateSSHCommandRelation(){
        String command = "";
        //move to directory where the sql file should be located  (SET)
        command += this.getString(R.string.changeToSQLConfigFileLocation);
        //go through all sets, and find their set type.
        Set<String> keySet = setHashMap.keySet();
        for(String s: keySet){
            String setType = setHashMap.get(s);
            ArrayList<String> list = addedSetList.get(setType);
            list = removeDuplicates(list);
            command += "echo \"" + generateSQLQuery(setType, list) + "\" > " + s + ".config" + ";";
        }

        keySet = addedIndependentSets.keySet();
        for(String s:keySet){
            String setType = independentSetList.get(s);
            ArrayList<String> list = new ArrayList<>();
            HashSet<String> set = addedIndependentSets.get(s);
            list.addAll(set);
            command += "echo \"" + generateSQLQuery(setType, list) + "\" > " + s + ".config" + ";";
        }

        return command;
    }

    @Override
    public void onClick(View view) {
        if(view == addRelationButton){
            if(setOrRelationSpinner.getSelectedItem().toString().equals("RELATIONS")) {
                //adding the user selected relation into a relationset
                String leftValue = relationLeftSpinner.getSelectedItem().toString();
                String rightValue = relationRightSpinner.getSelectedItem().toString();
                Pair<String, String> pair = new Pair<>(leftValue, rightValue);
                if ((addedRelations.get(relationNameSpinner.getSelectedItem().toString())) == null) {
                    HashSet<Pair<String, String>> tempSet = new HashSet<>();
                    tempSet.add(pair);
                    addedRelations.put(relationNameSpinner.getSelectedItem().toString(), tempSet);
                    adapter.notifyDataSetChanged();
                } else {
                    (addedRelations.get(relationNameSpinner.getSelectedItem().toString())).add(pair);
                    adapter.notifyDataSetChanged();
                }

                //adding selected Objects into their respective sets
                String relationName = relationNameSpinner.getSelectedItem().toString();
                //get the left hand side of the generic selected relation
                // ex: IoTRelation<RoomSmart, CameraSmart> roomCameraRel, get "RoomSmart"
                Pair<String, String> tempPair = relations.get(relationName);
                ArrayList<String> tempArrayList = addedSetList.get(tempPair.first);
                //add the selected Left Value to RoomSmart set
                if (tempArrayList == null) {
                    tempArrayList = new ArrayList<>();
                    tempArrayList.add(leftValue);
                    addedSetList.put(tempPair.first, tempArrayList);
                } else {
                    tempArrayList.add(leftValue);
                    addedSetList.put(tempPair.first, tempArrayList);
                }

                //repeat for the right side
                tempArrayList = addedSetList.get(tempPair.second);
                //ex: add to the selected Right Value CameraSmart set
                if (tempArrayList == null) {
                    tempArrayList = new ArrayList<>();
                    tempArrayList.add(rightValue);
                    addedSetList.put(tempPair.second, tempArrayList);
                } else {
                    tempArrayList.add(rightValue);
                    addedSetList.put(tempPair.second, tempArrayList);
                }
                addedRelationsArrayList = new ArrayList<>();
                Set<String> relationTypes = addedRelations.keySet();
                for (String s : relationTypes) {
                    for (Pair<String, String> temp : addedRelations.get(s)) {
                        addedRelationsArrayList.add(temp.first + " " + temp.second);
                    }
                }
                if(hasIndependentSets){
                    Set<String> keys= addedIndependentSets.keySet();
                    for (String s : keys) {
                        for (String temp : addedIndependentSets.get(s)) {
                            addedRelationsArrayList.add(temp);
                        }
                    }
                    adapter = new ArrayAdapter(context, android.R.layout.simple_list_item_multiple_choice, addedRelationsArrayList);
                    addRelationsListView.setAdapter(adapter);
                }
                else {
                    adapter = new ArrayAdapter(context, android.R.layout.simple_list_item_multiple_choice, addedRelationsArrayList);
                    addRelationsListView.setAdapter(adapter);
                }

            }
            else {
                //user is adding to independent sets (sets that are not part of a relation)
                String leftValue = relationLeftSpinner.getSelectedItem().toString();
                if ((addedIndependentSets.get(relationNameSpinner.getSelectedItem().toString())) == null) {
                    HashSet<String> tempSet = new HashSet<>();
                    tempSet.add(leftValue);
                    addedIndependentSets.put(relationNameSpinner.getSelectedItem().toString(), tempSet);
                    adapter.notifyDataSetChanged();
                } else {
                    (addedIndependentSets.get(relationNameSpinner.getSelectedItem().toString())).add(leftValue);
                    adapter.notifyDataSetChanged();
                }
                addedRelationsArrayList = new ArrayList<>();
                Set<String> relationTypes = addedRelations.keySet();
                for (String s : relationTypes) {
                    for (Pair<String, String> temp : addedRelations.get(s)) {
                        addedRelationsArrayList.add(temp.first + " " + temp.second);
                    }
                }
                Set<String> keys= addedIndependentSets.keySet();
                for (String s : keys) {
                    for (String temp : addedIndependentSets.get(s)) {
                        addedRelationsArrayList.add(temp);
                    }
                }
                adapter = new ArrayAdapter(context, android.R.layout.simple_list_item_multiple_choice, addedRelationsArrayList);
                addRelationsListView.setAdapter(adapter);

            }
        }

        if(view == doneRelationButton){

            boolean allSetsHaveEntry = true;
            for(String s: applicationSetList){
                ArrayList<String> list = addedSetList.get(s);
                if(list== null || list.size() == 0){
                    allSetsHaveEntry = false;
                    System.out.println("NOT ALL RELATIONS HAVE ENTRIES");
                    Toast t = Toast.makeText(ApplicationWithRelations.this, "Insufficient Entries",Toast.LENGTH_SHORT);
                    t.show();
                    continue;
                }
            }
            if(hasIndependentSets) {
                for (int i = 0; i < independentSetNames.size(); i++) {
                    if (addedIndependentSets.get(independentSetNames.get(i)) == null ||
                            (addedIndependentSets.get(independentSetNames.get(i))).size() == 0) {
                        allSetsHaveEntry = false;
                        System.out.println("NOT ALL SETS HAVE ENTRIES");
                        continue;
                    }
                }
            }
            System.out.println("all sets have entry" + allSetsHaveEntry);
            if(allSetsHaveEntry){

                //the ssh execution is within the following command. Add comm patterns to database
                callSSHCommandAddComm();

                //adding relations and their derived sets, add to localconfig/mysql files
                String command = generateSSHCommandRelation();
                //System.out.println(command)
                SSH_MySQL addConfigFiles = new SSH_MySQL();
                addConfigFiles.execute(command);
                try {
                    Thread.sleep(1500);
                    List<String> result = addConfigFiles.getResultLines();
                    for (int i = 0; i < result.size(); i++) {
                        System.out.println("result" + result.get(i));
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }




                //add independent sets to their respective localconfig/mysql files
                finish();
            }

        }
        if (view == deleteRelationButton) {
            //SparseBooleanArray's data is True or False
            SparseBooleanArray checkedItems = addRelationsListView.getCheckedItemPositions();//to check which devices are checked in listview(check -> true, no check -> false)
            int count = adapter.getCount();//number of items in listview
            for (int i = count - 1; i >= 0; i--) {//scan from back
                //i : index of IoT device which will be removed in tmp array
                if (checkedItems.get(i)) {//if check
                    System.out.println("delete relation: " + i);
                    String selectedValue = (String)addRelationsListView.getItemAtPosition(i);
                    scanner = new Scanner(selectedValue);
                    String left = scanner.next() + " " + scanner.next();

                    if(scanner.hasNext()) {
                        String right = scanner.next() + " " + scanner.next();
                        Pair<String, String> pair = new Pair<>(left, right);
                        //remove from user-selected relations
                        Set<String> relationTypes = addedRelations.keySet();
                        for (String s : relationTypes) {
                            if (addedRelations.get(s).contains(pair)) {
                                addedRelations.get(s).remove(pair);
                            }
                        }
                        //remove from displaying on the check list
                        String temp = pair.first + " " + pair.second;
                        addedRelationsArrayList.remove(temp);
                        adapter.notifyDataSetChanged();
                        //remove from addedsetlist
                        deleteFromSet(left);
                        deleteFromSet(right);
                    }
                    else{
                        System.out.println("only has one word");
                        Set<String> keySet = addedIndependentSets.keySet();
                        for(String s: keySet){
                            HashSet<String> tempSet =  addedIndependentSets.get(s);
                            tempSet.remove(left);
                        }
                        addedRelationsArrayList.remove(left);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            //update
            adapter.notifyDataSetChanged();
            //delete checked mark in listview
            addRelationsListView.clearChoices();

        }

    }
}
