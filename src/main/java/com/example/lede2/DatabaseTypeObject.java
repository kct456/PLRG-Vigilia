package com.example.lede2;

import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

/**
 * Created by Brian on 3/5/2018.
 */

public class DatabaseTypeObject {
    private int numSubtypes;
    private Hashtable<String, DatabaseSubtypeObject> subtypes;
    protected  String tag;
    private String name;

    public DatabaseTypeObject(String name, String tag){
        numSubtypes = 0;
        subtypes = new Hashtable<String, DatabaseSubtypeObject>();
        this.name = name;
        this.tag = tag;
    }

    public void addSubtype(String name, DatabaseSubtypeObject subtypeObject){
        if(!subtypes.contains(name)){
            subtypes.put(name, subtypeObject);
            numSubtypes++;
        }
    }
    public void deleteSubtype(String name){
        if(subtypes.contains(name)){
            subtypes.remove(name);
            numSubtypes--;
        }
    }
    public DatabaseSubtypeObject getSubtypeObject(String name){
        return subtypes.get(name);
    }
    public Set<String> getKeySet(){
        return subtypes.keySet();
    }
    public String getName(){
        return this.name;
    }
    public void setTag(String tag){
        this.tag = tag;
    }
    public String getTag(){
        return  tag;
    }
}
