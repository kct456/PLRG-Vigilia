package com.example.lede2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.util.Random;
import java.lang.Thread;


/**
 * Created by kevin on 4/3/2018.
 */

public class RegistrationRouterCheck extends AppCompatActivity implements View.OnClickListener{
    private SSH routerConnection;
    private SSH routerConnection2;
    private String command;
    public String temp_dhcp_filename;
    public String initial_dhcp_filename;
    public String initialDHCP;
    private String tempDHCP;
    private String newpsk;
    private TextView password;
    private boolean initialDHCPCall;
    public String commandRouter = "cat /tmp/dhcp.leases";
    private WifiManager manager;
    ProgressDialog dialog;
    Button back;
    Button check;
    Button load;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Context context = getApplicationContext();
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);



        password = (TextView) findViewById(R.id.newPassword);
        back = (Button) findViewById(R.id.back);
        load = (Button) findViewById(R.id.load);
        check = (Button) findViewById(R.id.check);

        back.setOnClickListener(this);
        load.setOnClickListener(this);
        check.setOnClickListener(this);

        command = "cat /tmp/dhcp.leases";
        temp_dhcp_filename = "temp_DHCP.txt";
        initial_dhcp_filename = this.getString(R.string.initial_DHCP);
        initialDHCPCall = false;
        newpsk = generateRandomPassword();
        password.setText(newpsk);

        Snackbar mySnackBar = Snackbar.make(findViewById(R.id.main_layout_id), "Please Wait Until Wifi is Connected!", 2000);
        mySnackBar.show();

        try{
            routerConnection = new SSH();
            routerConnection.execute("-ch " + newpsk);
            Thread.sleep(1000);
        } catch (Exception e) {
            Log.d("SLEEP EXCEPTION", "SLEEP EXCEPTION occurs in onCreate method of EnrollDeviceActivity");
        }

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
        wifiConfig.preSharedKey = String.format("\"%s\"", newpsk);

        int netId = manager.addNetwork(wifiConfig);
        manager.disconnect();
        manager.enableNetwork(netId, true);
        manager.reconnect();

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("new password", newpsk);
        clipboard.setPrimaryClip(clip);
    }

    boolean isNetworkAvailable() {//check whether wifi connection is or not
        Context context = getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    @Override
    public void onClick(View v){
        if(v == back){
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
            }
            else{
                List<String> routerOutput = new ArrayList<>();
                try{
                    routerConnection = new SSH();
                    routerConnection.execute("-ch 1qaz2wsx3edc");
                    Thread.sleep(1000);
                } catch(Exception ex){
                    ex.printStackTrace();
                    Log.d("Sleep exception", "exception at Router SSH");
                }
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("old password", "1qaz2wsx3edc");
                clipboard.setPrimaryClip(clip);

                Context context = getApplicationContext();
                WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
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
                finish();
            }
        }

        if(v == load){
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
            }
            else{
                List<String> routerOutput = new ArrayList<>();
                if(initialDHCP == null) {
                    try {
                        routerConnection = new SSH();
                        routerConnection.execute(commandRouter);
                        Thread.sleep(1000);
                        routerOutput = routerConnection.getResultLines();
                        while (routerOutput.size() == 0) {
                            Thread.sleep(500);
                            routerOutput = routerConnection.getResultLines();
                        }
                        addSSHResultsToConfig(routerOutput, initial_dhcp_filename);
                        initialDHCP = ConfigFileIO.readFromFile(initial_dhcp_filename, this);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                else{
                    Snackbar mySnackBar = Snackbar.make(findViewById(R.id.main_layout_id), "Already Loaded.", Snackbar.LENGTH_LONG);
                    mySnackBar.show();
                }
            }
        }


        if(v == check){
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
            }
            else{
                if(initialDHCP != null) {
                    List<String> routerOutput = new ArrayList<>();
                    try {
                        routerConnection = new SSH();
                        routerConnection.execute(command);
                        Thread.sleep(1000);
                        routerOutput = routerConnection.getResultLines();
                        while (routerOutput.size() == 0) {
                            Thread.sleep(500);
                            routerOutput = routerConnection.getResultLines();
                        }
                        addSSHResulToConfig(routerOutput, temp_dhcp_filename);
                        tempDHCP = ConfigFileIO.readFromFile(temp_dhcp_filename, this);
                    } catch (Exception ex) {
                        Log.d("SLEEP EXCEPTION", "SLEEP EXCEPTION occurs in onClick method of EnrollDeviceActivity");
                    }

                    if (!comparison(initialDHCP, tempDHCP)) {
                        Intent i = new Intent(this, RegisterDeviceAdding.class);
                        i.putExtra("newpsk", newpsk);
                        startActivity(i);
                        finish();
                    }
                    else{
                        Snackbar mySnackBar = Snackbar.make(findViewById(R.id.main_layout_id), "Please Connect a New Device Before Clicking Check.", Snackbar.LENGTH_LONG);
                        mySnackBar.show();
                    }
                }
                else{
                    Snackbar mySnackBar = Snackbar.make(findViewById(R.id.main_layout_id), "Please Click Load!", Snackbar.LENGTH_LONG);
                    mySnackBar.show();
                }
            }
        }


    }


    private boolean comparison(String initialDeviceDat, String tempDeviceDat){
        return initialDeviceDat.equals(tempDeviceDat);
    }

    private void addSSHResulToConfig(List<String> results, String filename){
        File dir = getFilesDir();
        File file = new File(dir, filename);
        file.delete();
        for (int i = 0; i < results.size(); i++) {
            ConfigFileIO.writeToFile(filename, results.get(i), this);
        }
    }

    /******************************************************************************************************************************************************************/
    // Generate a random password and return it
    /******************************************************************************************************************************************************************/
    public String generateRandomPassword() {
        String password = new String();

        StringBuffer rndpassword = new StringBuffer();
        Random rnd = new Random();
        int digitnum = 20;
        // Generate random 20digit password with upper / lower case alphabet + numbers
        // There are 10 int nums, 26 lower alphabets, 26 upper alphabets. Total 62
        // So 2/12 possiblity of int, 5/12 lower, 5/12 upper alphabets.
        for (int i = 0; i < digitnum; i++) {
            int rIndex = rnd.nextInt(12);
            if (rIndex >= 0 && rIndex < 2) { // 0 - 9
                rndpassword.append((rnd.nextInt(10)));
            } else if (rIndex >= 2 && rIndex < 7) { // a-z
                rndpassword.append((char) ((int) (rnd.nextInt(26)) + 97));
            } else {    // A-Z
                rndpassword.append((char) ((int) (rnd.nextInt(26)) + 65));
            }
        }
        password = rndpassword.toString();
        return password;
    }

    /******************************************************************************************************************************************************************/
    //Detect you are connected to a specific network.
    /******************************************************************************************************************************************************************/
    public String getWifiName(Context context) {
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

    public void addSSHResultsToConfig(List<String> results, String filename) {
        File dir = getFilesDir();
        File file = new File(dir, filename);
        file.delete();
        for (int i = 0; i < results.size(); i++) {
            ConfigFileIO.writeToFile(filename, results.get(i), this);
        }
    }


}
