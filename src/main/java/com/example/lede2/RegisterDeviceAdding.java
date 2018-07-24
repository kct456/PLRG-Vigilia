package com.example.lede2;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Brian on 4/3/2018.
 */

public class RegisterDeviceAdding extends AppCompatActivity implements View.OnClickListener {
    private String devdat_macaddress_filename;
    private String dhcp_macaddress_filename;
    private Spinner macAddressSpinner;
    private EditText name;
    private String deviceName;
    private Button wifi;//wifi button in UI
    private SSH ssh;//Connection object between Android & Router
    private Context context;
    private String newpsk;
    private WifiManager manager;

    private Button done;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_device_adding);
        context = getApplicationContext();
        manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        Bundle extras  = getIntent().getExtras();
        if(extras!= null){
            newpsk = extras.getString("newpsk");
        }
        macAddressSpinner = (Spinner) findViewById(R.id.registerMACs);
        wifi = (Button) findViewById(R.id.regAddWifi);
        wifi.setOnClickListener(this);
            devdat_macaddress_filename = this.getString(R.string.devices_dat_filename);
        dhcp_macaddress_filename = "temp_DHCP.txt";
        name = (EditText)findViewById(R.id.registerName);
        done = (Button)findViewById(R.id.registerDone);
        done.setOnClickListener(this);

        ArrayList<String> macAddressesdhcp = ConfigFileIO.getMACAddressesDHCP(dhcp_macaddress_filename, this);
        ArrayList<String> macAddressesdevdat = ConfigFileIO.getMACAddressesDevDat(devdat_macaddress_filename, this);
        ArrayList<String> macAddresses = new ArrayList<>();
        for(int i = 0; i < macAddressesdhcp.size();i++){
            if(!macAddressesdevdat.contains(macAddressesdhcp.get(i))){
                macAddresses.add(macAddressesdhcp.get(i));
            }
        }

        //sets value for macAddressesSpinner
        ArrayAdapter typeAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, macAddresses);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        macAddressSpinner.setAdapter(typeAdapter);




    }

    @Override
    public void onClick(View v) {
        if (v == done) {
            //Users try to execute shell scripts by pushing button,
            //but problem could occur(application stop) if user push the button without wifi connection
            //As a result, below function have to be implemented.
            if (isNetworkAvailable() == false) {//without wifi connection
                Toast t = Toast.makeText(this, R.string.connect, Toast.LENGTH_SHORT);
                t.show();
                //choosing wifi connection
                Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
                intent.putExtra("only_access_points", true);
                intent.putExtra("extra_prefs_show_button_bar", true);
                intent.putExtra("wifi_enable_next_on_connect", true);
                startActivityForResult(intent,1);
                //startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
            } else {//with wifi connection
                deviceName = name.getText().toString();
                if(deviceName.equals("")){
                    Toast t = Toast.makeText(this, R.string.empty_name,Toast.LENGTH_SHORT);
                    t.show();
                    return;
                }
                Scanner scanner = new Scanner(deviceName);
                scanner.next();
                if(scanner.hasNext()){
                    Snackbar oneWordName = Snackbar.make(findViewById(R.id.oneWordName),
                            "Please only use one word names", 2000);
                    oneWordName.show();
                    return;
                }
                String selectedMacAddress = macAddressSpinner.getSelectedItem().toString();
                context = getApplicationContext();
                if (getWifiName(context).equals("\""+ ConfigActivity.RSSID +"\"")) {//if wifi name is LEDE2
                    ssh = new SSH();
                    // execute shell script  (script's function -> Save contents(Mac,Ip,Key,Name) on hostapd-psk)
                    List<String> results = new ArrayList<>();
                    try {//To execute asyntask in ssh object, we have to sleep main thread
                        ssh.execute("-co " + newpsk + " " + selectedMacAddress + " " +
                                deviceName + " " + ConfigActivity.RPWD );
                        Thread.sleep(2000);

                        List<WifiConfiguration> list = manager.getConfiguredNetworks();
                        for( WifiConfiguration i : list ) {
                            if(i.SSID.equals("\"LEDE2\"")){
                                manager.removeNetwork(i.networkId);
                                manager.saveConfiguration();
                                System.out.println("deleted");
                            }
                        }
                        WifiConfiguration wifiConfig = new WifiConfiguration();
                        wifiConfig.SSID = String.format("\"%s\"", "LEDE2");
                        wifiConfig.preSharedKey = String.format("\"%s\"", context.getResources().getString(R.string.default_rpwd));

                        int netId = manager.addNetwork(wifiConfig);
                        manager.disconnect();
                        manager.enableNetwork(netId, true);
                        manager.reconnect();


                        //To execute asyntask in ssh object, we have to sleep main thread
//                        results = ssh.getResultLines();
//                        while(results.size() == 0){
//                            Thread.sleep(500);
//                            results = ssh.getResultLines();
//                            System.out.println(results);
//                        }
                    } catch (Exception e) {
                        Log.d("SLEEP EXCEPTION", "SLEEP EXCEPTION occurs in onClick method of EnrollDeviceActivity");
                    }
                } else { //if name of wifi is not LEDE -> go to wifi configuration screen to change wifi type
                    Toast t = Toast.makeText(this, R.string.try_again, Toast.LENGTH_SHORT);
                    t.show();
                    //Choosing wifi connection
                    Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
                    intent.putExtra("only_access_points", true);
                    intent.putExtra("extra_prefs_show_button_bar", true);
                    intent.putExtra("wifi_enable_next_on_connect", true);
                    startActivityForResult(intent,1);
                    //startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                }
            }
            // Call activity that adds device - not doing this flow because we haven't found
            // a way to change password without restarting WiFi
            //startActivity(new Intent(this, AddDeviceActivity.class));
            //finish();
        } else if (v == wifi) {
            Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
            intent.putExtra("only_access_points", true);
            intent.putExtra("extra_prefs_show_button_bar", true);
            intent.putExtra("wifi_enable_next_on_connect", true);
            startActivityForResult(intent,1);
            //startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("old password", context.getResources().getString(R.string.default_rpwd));
        clipboard.setPrimaryClip(clip);
        finish();
    }


    /******************************************************************************************************************************************************************/
    //Detect you are connected to a specific network.
    /******************************************************************************************************************************************************************/
    public String getWifiName(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (manager.isWifiEnabled()) {
            WifiInfo wifiInfo = manager.getConnectionInfo();
            if (wifiInfo != null) {
                NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
                if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    return wifiInfo.getSSID();
                }
            }
        }
        return null;
    }

    boolean isNetworkAvailable() {//check whether wifi connection is or not
        Context context = getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }


}
