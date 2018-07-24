package com.example.lede2;

import android.util.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import javax.sql.StatementEvent;

/**
 * Created by Brian on 3/5/2018.
 */

public class DatabaseSubtypeObject {

    //param and address params are the database column names
    //devices are the specific instances found on the database
    private int numParams;
    private int numAddresses;
    private int numAddressParams;
    private int numZigbeeParams;
    private int numZigbeeAddresses;
    private ArrayList<Pair <String, String>> params;
    //outer list = numOf addresses, inner list = num of params, inner pair = field name and value
    private ArrayList<ArrayList<Pair<String,String>>> addressParams;
    private ArrayList<ArrayList<Pair<String, String>>> zigbeeAddressParams;
    private List<String> devices;
    private String nextID;
    String name;
    private String tag;



    public DatabaseSubtypeObject(String name, String tag, int numAddresses, int numZigbeeAddresses){
        numParams = 0;
        numAddressParams = 0;
        this.numAddresses = numAddresses;
        this.numZigbeeAddresses = numZigbeeAddresses;
        params = new ArrayList<Pair<String, String>>();
        addressParams = new ArrayList<>();
        zigbeeAddressParams = new ArrayList<>();
        devices = new ArrayList<String>();
        this.name = name;
        this.tag = tag;
        if(devices.isEmpty()){
            nextID = tag+"1";
        }
    }
    public void addDevice(String device){
        devices.add(device);
        setNextID();
    }
    public void addParam(Pair<String, String> param){
        if(!params.contains(param)){
            params.add(param);
            numParams++;
        }
    }

    public void addAddressParam(int i, Pair<String, String> param){
        if(!addressParams.get(i).contains(param)){
            addressParams.get(i).add(param);
            numAddressParams++;
        }
    }


    public void deleteParam(String param){
        if(params.contains(param)){
            params.remove(param);
            numParams--;
        }
    }
    public ArrayList<Pair<String,String>> getParams(){
        return this.params;
    }
    public ArrayList<ArrayList<Pair<String, String>>> getAddressParams(){
        return this.addressParams;
    }
    public String getName(){
        return  this.name;
    }
    public int getNumParams() {
        return numParams;
    }

    public int getNumAddressParams() {
        return numAddressParams;
    }

    public List<String> getDevices() {
        return devices;
    }
    public String getNextID(){
        return nextID;
    }

    //sets the field which is used to generate the next available id
    //needs to fix so it it doesn't rely on the first instance in the list
    public void setNextID(){
        String id = "";
        ArrayList<String> ids = new ArrayList<>();
        if(devices.isEmpty()){
            nextID = tag+"1";
            return;
        }
        if(devices.size() > 0 && devices.get(0) != null) {
            for (int i = 0; i < devices.size(); i++) {
                ids.add(devices.get(i).substring(2));
            }
        }
        int count = 1;
        boolean doesNotContain = true;

        //should you set boolean to false or break;
        while(doesNotContain) {
            Integer idValue = new Integer(count);
            if(ids.contains(idValue.toString())){
                count += 1;
            }
            else
            {
                doesNotContain = false;
            }
        }

        id = tag + String.valueOf(count);
        nextID = id;
    }

    public void insertID(){
        devices.add(this.nextID);
        setNextID();
    }

    public void deleteDevice(String DeviceID){
        devices.remove(DeviceID);
        setNextID();
    }
    public void setNumAddressParams(int numParams){
        numAddressParams = numParams;
    }
    public void setNumParams(int numParams){
        numParams = numParams;
    }
    public void setNumZigbeeParams(int numParams){ this.numZigbeeParams = numParams; }
    public int getNumAddresses(){
        return this.numAddresses;
    }
    public int getNumZigbeeAddresses(){ return this.numZigbeeAddresses; }
    public ArrayList<ArrayList<Pair<String, String>>> getZigbeeAddressParams(){return this.zigbeeAddressParams; }

}
