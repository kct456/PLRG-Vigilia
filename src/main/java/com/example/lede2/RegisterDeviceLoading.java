package com.example.lede2;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RegisterDeviceLoading extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener {

    private static final int REQUEST_RESULT = 1001;
    Button done;//Done button in UI
    Button wifi;//wifi button in UI
    SSH ssh;//Connection object between Android & Router
    Context context;
    TextView psk;//red letter in UI
    String newpsk;//same as psk (different data type)
    EditText name;//device name newly registered in UI
    String deviceName;//same as name(different data type)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_device);

        ssh = new SSH();
        psk = (TextView) findViewById(R.id.add_psk);
        done = (Button) findViewById(R.id.done);
        wifi = (Button) findViewById(R.id.wifi);
        name = (EditText) findViewById(R.id.name);

        done.setOnClickListener(this);
        wifi.setOnClickListener(this);
        name.setOnFocusChangeListener(this);

        //Make random password and show the password through EditText
        newpsk = generateRandomPassword();
        psk.setText(newpsk);

        try {//To execute asyntask in ssh object, we have to sleep main thread
            ssh.execute("-ch " + newpsk);
            Thread.sleep(1000);
        } catch (Exception e) {
            Log.d("SLEEP EXCEPTION", "SLEEP EXCEPTION occurs in onCreate method of EnrollDeviceActivity");
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {//function not to modify randomly generated password for newly registered device
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (hasFocus) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        } else {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    boolean isNetworkAvailable() {//check whether wifi connection is or not
        Context context = getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
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
                context = getApplicationContext();
                if (getWifiName(context).equals("\""+ ConfigActivity.RSSID +"\"")) {//if wifi name is LEDE2
                    String networkPass = psk.getText().toString();//random password
                    ssh = new SSH();

                    // execute shell script  (script's function -> Save contents(Mac,Ip,Key,Name) on hostapd-psk)
                    ssh.execute("-co " + networkPass + " " + deviceName + " " + ConfigActivity.RPWD);//review!!!!!!
                    try {//To execute asyntask in ssh object, we have to sleep main thread
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        Log.d("SLEEP EXCEPTION", "SLEEP EXCEPTION occurs in onClick method of EnrollDeviceActivity");
                    }
                    finish();//Go back to the Main Activity
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
        }
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
}
