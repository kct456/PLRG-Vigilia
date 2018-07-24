package com.example.lede2;

import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

/**
 * Created by Brian on 2/28/2018.
 */
    /*hierarchy of this object is dbobject -> dbtypeobject -> dbsubtypeobject
      dbobject holds a hashtable of typeobject and typeobject holds a hashtable of subtypeobject
      structure is similar across all levels, subtype obejct also holds the individual instances */
public class DatabaseObject {
    private int numTypes;
    Hashtable<String, DatabaseTypeObject> types;

    public DatabaseObject(){
        numTypes = 0;
        types = new Hashtable<String, DatabaseTypeObject>();

    }

    public void addTypeObject(String name, DatabaseTypeObject typeObject){
        if(!types.contains(name)){
            types.put(name, typeObject);
            numTypes++;
        }
    }
    public void deleteTypeObject(String name){
        if(types.contains(name)){
            types.remove(name);
            numTypes--;
        }
    }
    public DatabaseTypeObject getTypeObject(String name){
        return types.get(name);
    }
    public Set<String> getKeySet(){
        return types.keySet();
    }
}
