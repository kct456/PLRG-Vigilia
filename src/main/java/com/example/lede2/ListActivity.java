package com.example.lede2;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class ListActivity extends AppCompatActivity implements View.OnClickListener {

    Button delete;//delete button in UI
    Button back;//select button in UI
    ListView listview;//listview in UI
    SSH ssh;//Connection object between Android & Router
    List<String> tmp;//data structure which has IoT device information already registered on LEDE2
    ArrayAdapter adapter;//adapter between tmp and listview
    String device_info;
    protected String deviceIp;
    ArrayList<String> deviceList;
    TextView ip;
    TextView mac;
    Hashtable<String, String> nameAndValues;
    Scanner scanner;
    Spinner selectedDevice;



    //have to check wifi before delete
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        nameAndValues = new Hashtable<>();
        selectedDevice = (Spinner) findViewById(R.id.selectedName);
        try{
            ssh = new SSH();
            ssh.execute("-ln");
            Thread.sleep(1000);
            tmp = ssh.getResultLines();
            while(tmp.size() == 0){
                Thread.sleep(500);
                tmp = ssh.getResultLines();
            }
        }
        catch (Exception e){
            e.printStackTrace();
            Log.d("cat device dat", "exception at cat device.dat");
        }
        tmp.remove(0);
        for(int i = 0; i < tmp.size(); i++){
            Scanner scanner = new Scanner(tmp.get(i));
            if(scanner.hasNext()){
                nameAndValues.put(scanner.next(), tmp.get(i));
            }
        }
        Set<String> nameAndValuesKeySet = nameAndValues.keySet();
         deviceList  = new ArrayList<String>();
        for(String key: nameAndValuesKeySet){
            deviceList.add(key);
        }


        delete = (Button) findViewById(R.id.delete);
        back = (Button) findViewById(R.id.list_back);
        listview = (ListView) findViewById(R.id.listView1);
        ip = (TextView) findViewById(R.id.txt_ip);
        mac = (TextView) findViewById(R.id.txt_mac);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, deviceList);//register tmp array to adapter

        delete.setOnClickListener(this);
        back.setOnClickListener(this);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                device_info = listview.getItemAtPosition(position).toString();
        }});

        ArrayAdapter typeAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, deviceList);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectedDevice.setAdapter(typeAdapter);

        selectedDevice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String nameIPMac = nameAndValues.get(selectedDevice.getSelectedItem().toString()    );
                scanner = new Scanner(nameIPMac);
                scanner.next();
                mac.setText(scanner.next());
                ip.setText(scanner.next());
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


    }

    @Override
    public void onClick(View v) {
        System.out.println("device info: " + device_info);
        if (v == delete && isNetworkAvailable()) {
            //SparseBooleanArray's data is True or False
            SparseBooleanArray checkedItems = listview.getCheckedItemPositions();//to check which devices are checked in listview(check -> true, no check -> false)
            int count = adapter.getCount();//number of items in listview
            String command = "-dn "; //after, +'name '

            for (int i = count - 1; i >= 0; i--) {//scan from back
                //i : index of IoT device which will be removed in tmp array
                if (checkedItems.get(i)) {//if check
                    String selectedValue = (String)listview.getItemAtPosition(i);
                    scanner = new Scanner(selectedValue);
                    String name = scanner.next();
                    command += name + " ";//complete command
                    //remove this information on the listview
                    nameAndValues.remove(name);
                    deviceList.remove(name);
                    //deviceIp = checkedItems.
                }
            }
            try {
                //delete IoT device information in the router by sending command line to router
                ssh = new SSH();
                ssh.execute(command);
                Thread.sleep(1000);//To execute asyntask in ssh object, we have to sleep main thread
            } catch (Exception e) {
                Log.d("SLEEP EXCEPTION", "SLEEP EXCEPTION occurs in onClick method of ListActivity");
            }
            //update
            adapter.notifyDataSetChanged();
            //delete checked mark in listview
            listview.clearChoices();

        }
        else if (v == back && isNetworkAvailable()){
            finish();
        }

    }

    boolean isNetworkAvailable() {//check whether wifi connection is or not
        Context context = getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

}
