package com.example.lede2;

import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.Pair;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by Brian on 2/28/2018.
 */

public class ConfigFileIO {

    //appends information to the bottom of file
    public static void writeToFile(String filename, String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.
                    openFileOutput(filename,
                            Context.MODE_APPEND | Context.MODE_PRIVATE));
            BufferedWriter writer = new BufferedWriter(outputStreamWriter);
            writer.write(data);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    //returns contents of file
    public static String readFromFile(String filename, Context context) {

        String strscan = "";

        try {
            File scanfile = context.getFileStreamPath(filename);
            Scanner scanner = new Scanner(scanfile);
            while (scanner.hasNextLine()) {
                strscan += scanner.nextLine() + "\n";
            }

        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        }

        return strscan;
    }



    //creates the local copy of the sql database object
    public static DatabaseObject createDatabaseObject(Context context, String idInformation){
        Properties prop = new Properties();
        String filename = context.getString(R.string.device_param_config_filename);
        File file = new File(context.getFilesDir() , filename);
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(file);
            prop.load(fis);
            fis.close();
        }
        catch (IOException ex){
            System.out.println("Error when reading SpeakerController Config file ");
            ex.printStackTrace();
        }

        int numTypes = Integer.parseInt(prop.getProperty("NUM_OF_TYPES"));
        System.out.println("numtypes: " + numTypes);
        DatabaseObject databaseObject = new DatabaseObject();
        //add all types into databaseObject
        for(int i = 0; i < numTypes; i++){
            String type = prop.getProperty("TYPE_" + i);
            String tag = prop.getProperty("TAG_" + i);
            //generates a type object, fill it with subtype objects
            DatabaseTypeObject databaseTypeObject = new DatabaseTypeObject(type, tag);
            int numSubtypes = Integer.parseInt(prop.getProperty("SUBTYPE_" + i));

            //for each subtype, create the appropriate fields
            for(int j = 0; j < numSubtypes; j++){
                String currentSubtype = "TYPE_" + i + "_" + j;
                String subtypeName = prop.getProperty(currentSubtype);
                int numAddresses = Integer.parseInt(prop.getProperty(currentSubtype + "_NUM_OF_ADDRESSES"));
                int numZigbeeAddresses = Integer.parseInt(prop.getProperty(currentSubtype + "_NUM_OF_ZBADDRESSES"));

                DatabaseSubtypeObject databaseSubtypeObject = new DatabaseSubtypeObject(subtypeName, tag, numAddresses, numZigbeeAddresses);

                if(numAddresses > 0) {
                    String temp = prop.getProperty("TYPE_" + i + "_" + j + "_ADDRESS_FIELDS");
                    Scanner scanner = new Scanner(temp);
                    ArrayList<String> addressFieldNames = new ArrayList<>();
                    while (scanner.hasNext()) {
                        addressFieldNames.add(scanner.next());
                    }
                    //fill in the address fields for each subtype. May have to do multiple times for some subtypes
                    System.out.println("num addresses" + numAddresses);
                    for (int k = 0; k < numAddresses; k++) {
                        ArrayList<Pair<String, String>> addressParamList = new ArrayList<>();
                        for (int l = 0; l < addressFieldNames.size(); l++) {
                            System.out.println(prop.getProperty(addressFieldNames.get(l) + "_" + i + "_" + j + "_ADD_" + k));
                            String fieldValue = prop.getProperty(addressFieldNames.get(l) + "_" + i + "_" + j + "_ADD_" + k);
                            Pair<String, String> fieldPair = new Pair<>(addressFieldNames.get(l), fieldValue);
                            addressParamList.add(fieldPair);
                        }
                        databaseSubtypeObject.getAddressParams().add(addressParamList);
                        databaseSubtypeObject.setNumAddressParams(addressFieldNames.size());
                    }
                }

                if(numZigbeeAddresses > 0) {
                    String temp = prop.getProperty("TYPE_" + i + "_" + j + "_ZBADDRESS_FIELDS");
                    Scanner scanner = new Scanner(temp);
                    ArrayList<String> zigbeeAddressFieldNames = new ArrayList<>();
                    while (scanner.hasNext()) {
                        zigbeeAddressFieldNames.add(scanner.next());
                    }
                    //fill in the address fields for each subtype. May have to do multiple times for some subtypes
                    System.out.println("num addresses" + numZigbeeAddresses);
                    for (int k = 0; k < numZigbeeAddresses; k++) {
                        ArrayList<Pair<String, String>> zigbeeAddressParamList = new ArrayList<>();
                        for (int l = 0; l < zigbeeAddressFieldNames.size(); l++) {
                           // System.out.println(prop.getProperty(zigbeeAddressFieldNames.get(l) + "_" + i + "_" + j + "_ADD_" + k));
                            String fieldValue = prop.getProperty(zigbeeAddressFieldNames.get(l) + "_" + i + "_" + j + "_ADD_" + k);
                            //System.out.println("prop command:  " + " FIELD VALUE " + fieldValue);
                            Pair<String, String> fieldPair = new Pair<>(zigbeeAddressFieldNames.get(l), fieldValue);
                            zigbeeAddressParamList.add(fieldPair);
                        }
                        databaseSubtypeObject.getZigbeeAddressParams().add(zigbeeAddressParamList);
                        databaseSubtypeObject.setNumZigbeeParams(zigbeeAddressFieldNames.size());
                    }
                }









                //fill in the device fields for each subtype. There should only be up to 1 set of device fields
                int numDevFields = Integer.parseInt(prop.getProperty("TYPE_" + i + "_" + j + "_NUM_OF_DEVICE_INFO"));
                if(numDevFields > 0) {
                    String temp = prop.getProperty("TYPE_" + i + "_" + j + "_" + "DEVICE_FIELDS");
                    Scanner scanner = new Scanner(temp);
                    ArrayList<String> addressFieldNames = new ArrayList<>();
                    while (scanner.hasNext()) {
                        addressFieldNames.add(scanner.next());
                    }
                    for (int l = 0; l < addressFieldNames.size(); l++) {
                        String fieldValue = prop.getProperty(addressFieldNames.get(l) + "_" + i + "_" + j);
                        Pair<String, String> fieldPair = new Pair<>(addressFieldNames.get(l), fieldValue);
                        databaseSubtypeObject.addParam(fieldPair);
                    }
                    databaseSubtypeObject.setNumParams(addressFieldNames.size());
                }
                databaseTypeObject.addSubtype(subtypeName, databaseSubtypeObject);
            }
            System.out.println();
            databaseObject.addTypeObject( type, databaseTypeObject);
        }
        //Now add individual instances of each device


        Scanner scanner = new Scanner(idInformation);
        String temp = "";
        while (scanner.hasNext()) {
            temp = scanner.next();
            Set<String> typeset = databaseObject.getKeySet();
            for (String key : typeset) {
                DatabaseTypeObject typeObject = databaseObject.getTypeObject(key);
                Set<String> subtypeset = typeObject.getKeySet();
                if (subtypeset.contains(temp)) {
                    DatabaseSubtypeObject subtypeObject = typeObject.getSubtypeObject(temp);
                    subtypeObject.addDevice(scanner.next());
                    break;
                }
            }
        }




        return databaseObject;
    }



    //starts a new file instead of appending it
    public static void writeToNewFile(String filename, String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.
                    openFileOutput(filename, Context.MODE_PRIVATE));
            BufferedWriter writer = new BufferedWriter(outputStreamWriter);
            writer.write(data);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static ArrayList<String> getMACAddressesDevDat(String filename, Context context) {
        ArrayList<String> macAddresses = new ArrayList<>();
        String devicedat = ConfigFileIO.readFromFile(filename, context);
        System.out.println(devicedat);
        Scanner scanner = new Scanner(devicedat);
        if(scanner.hasNext()) {
            do {
                scanner.next();
                macAddresses.add(scanner.next());
                scanner.next();
            } while (scanner.hasNext());
            System.out.println(devicedat);
        }
        return macAddresses;
    }

    public static ArrayList<String> getMACAddressesDHCP(String filename, Context context) {
        ArrayList<String> macAddresses = new ArrayList<>();
        String dhcp = ConfigFileIO.readFromFile(filename, context);
        Scanner scanner = new Scanner(dhcp);
        do{
        scanner.next();
        macAddresses.add(scanner.next());
        scanner.next();
        scanner.next();
        scanner.next();
        }while(scanner.hasNext());
        System.out.println(dhcp);
        return macAddresses;
    }


}
