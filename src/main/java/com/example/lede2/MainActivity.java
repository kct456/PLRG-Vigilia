package com.example.lede2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button databaseButton;
    Button registerButton;
    Button listButton;
    Button applicationsButton;
    Button configButton;
    Button driversButton;
    TextView ssid;
    DatabaseObject databaseObject;
    ProgressDialog dialog;
    protected static Context context;
    protected static String DEF_RSSID;
    protected static String DEF_RPWD;
    protected static String DEF_ROUTERIP;
    protected static String DEF_ROUTERUSER;
    protected static String DEF_MYSQLHOSTUSER;
    protected static String DEF_MYSQLHOSTIP;
    protected static String DEF_MYSQLHOSTPASSWORD;
    protected static String DEF_CHANGE_DEFAULT_SCRIPT;
    protected static String DEF_CONNECT_DEVICE_SCRIPT;
    protected static String DEF_REGISTER_DEVICE_SCRIPT;

    protected static String DEF_INSTALL_DEVICE_FILE;
    protected static String DEF_INSTALL_DEVICE_ADDRESS_FILE;
    protected static String DEF_INSTALL_DEVICE_COMM_FILE;
    protected static String DEF_INSTALL_ADDRESS_FILE;
    protected static String DEF_INSTALL_TWO_DEVICES_AND_COMM_FILE;
    protected static String DEF_INSTALL_ZIGBEE_DEVICE_ADDRESS_FILE;
    protected static String DEF_INSTALL_HOST_FILE;
    protected static String DEF_DELETE_ENTITY_FILE;
    protected static String DEF_DELETE_COMM_PATTERN_FILE;
    protected static String DEF_DELETE_ADDRESS_FILE;
    protected static String DEF_DELETE_DEVICE_ADDRESS_FILE;
    protected static String DEF_DELETE_ZIGBEE_DEVICE_ADDRESS_FILE;
    protected static String DEF_DELETE_HOST_FILE;


    protected static String DEF_ADD_DEVICE_TO_MYSQL;
    protected static String DEF_MYSQL_CONFIG_FILE;
    protected static String DEF_INSTALL_CMD;
    protected static String DEF_DELETE_CMD;
    protected static String DEF_INSTALL_ADDRESS_CMD;
    protected static String DEF_INSTALL_ZBADDRESS_CMD;
    protected static String DEF_DELETE_ADDRESS_CMD;
    protected static String DEF_INSTALL_RELATION_CMD;
    protected static String DEF_DELETE_RELATION_CMD;
    protected static String DEF_DATABASE_NAME;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerButton = (Button) findViewById(R.id.register);
        listButton = (Button) findViewById(R.id.list);
        databaseButton = (Button) findViewById(R.id.database);
        applicationsButton = (Button) findViewById(R.id.applications);
        configButton = (Button) findViewById(R.id.config);
        driversButton = (Button) findViewById(R.id.drivers);
        ssid = (TextView)findViewById(R.id.device_subtype);


        registerButton.setOnClickListener(this);
        listButton.setOnClickListener(this);
        databaseButton.setOnClickListener(this);
        applicationsButton.setOnClickListener(this);
        configButton.setOnClickListener(this);
        driversButton.setOnClickListener(this);

        context = getApplicationContext();
        DEF_RSSID = context.getResources().getString(R.string.default_rssid);
        DEF_RPWD = context.getResources().getString(R.string.default_rpwd);
        DEF_ROUTERIP = context.getResources().getString(R.string.default_routerip);
        DEF_ROUTERUSER = context.getResources().getString(R.string.default_routeruser);
        DEF_CHANGE_DEFAULT_SCRIPT = context.getResources()
                .getString(R.string.change_default_script);
        DEF_CONNECT_DEVICE_SCRIPT = context.getResources()
                .getString(R.string.connect_device_script);
        DEF_REGISTER_DEVICE_SCRIPT = context.getResources()
                .getString(R.string.register_device_script);
        DEF_INSTALL_DEVICE_FILE = context.getResources().getString(R.string.install_device_file);
        DEF_INSTALL_DEVICE_ADDRESS_FILE = context.getResources().getString
                (R.string.install_device_address_file);
        DEF_INSTALL_TWO_DEVICES_AND_COMM_FILE = context.getResources()
                .getString(R.string.install_two_devices_and_comm_file);
        DEF_INSTALL_ADDRESS_FILE = context.getResources().getString(R.string.install_address_file);
        DEF_INSTALL_DEVICE_COMM_FILE = context.getResources().getString(R.string.install_device_comm_file);
        DEF_INSTALL_ZIGBEE_DEVICE_ADDRESS_FILE = context.getResources()
                .getString(R.string.install_zigbee_device_address_file);
        DEF_INSTALL_HOST_FILE = context.getResources().getString(R.string.install_host_file);
        DEF_DELETE_ENTITY_FILE = context.getResources().getString(R.string.delete_entity_file);
        DEF_DELETE_COMM_PATTERN_FILE = context.getResources()
                .getString(R.string.delete_comm_pattern_file);
        DEF_DELETE_ADDRESS_FILE = context.getResources().getString(R.string.delete_address_file);
        DEF_DELETE_DEVICE_ADDRESS_FILE = context.getResources()
                .getString(R.string.delete_device_address_file);
        DEF_DELETE_ZIGBEE_DEVICE_ADDRESS_FILE = context.getResources()
                .getString(R.string.delete_zigbee_device_address_file);
        DEF_DELETE_HOST_FILE = context.getResources().getString(R.string.delete_host);

        DEF_MYSQLHOSTUSER = context.getResources().getString(R.string.mysql_hostuser);
        DEF_MYSQLHOSTIP = context.getResources().getString(R.string.mysql_hostip);
        DEF_MYSQLHOSTPASSWORD = context.getResources().getString(R.string.mysql_hostpassword);
        DEF_ADD_DEVICE_TO_MYSQL = context.getResources().getString(R.string.add_device_to_mysql);


        DEF_MYSQL_CONFIG_FILE = context.getResources().getString(R.string.mysql_config_file);
        DEF_INSTALL_CMD = context.getResources().getString(R.string.install_cmd);
        DEF_DELETE_CMD = context.getResources().getString(R.string.delete_cmd);
        DEF_INSTALL_ADDRESS_CMD = context.getResources().getString(R.string.install_address_cmd);
        DEF_INSTALL_ZBADDRESS_CMD = context.getResources().getString(R.string.install_zb_address_cmd);
        DEF_DELETE_ADDRESS_CMD = context.getResources().getString(R.string.delete_address_cmd);
        DEF_INSTALL_RELATION_CMD = context.getResources().getString(R.string.add_comm_cmd);
        DEF_DELETE_RELATION_CMD = context.getResources().getString(R.string.delete_comm_cmd);

        DEF_DATABASE_NAME = context.getResources().getString(R.string.database_name);

        ssid.setText(DEF_RSSID);
        databaseObject = new DatabaseObject();
        //Check if the user has wifi connection
        if (isNetworkAvailable() == false) {//without wifi connection
            Toast t = Toast.makeText(this, R.string.connect, Toast.LENGTH_SHORT);
            t.show();
            Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
            intent.putExtra("only_access_points", true);
            intent.putExtra("extra_prefs_show_button_bar", true);
            intent.putExtra("wifi_enable_next_on_connect", true);
            startActivityForResult(intent,1);
        }
        //sends to another page to update/retrieve information about the database from the pi
        startActivity(new Intent(this, UpdateLocalConfigFiles.class));

    }

    public void onClick(View v) {

        if(v == configButton){
            startActivityForResult(new Intent(this,ConfigActivity.class),1000);
        }
        //Users try to execute shell scripts by pushing button,
        //but problem could occur(application stop) if user push the button without wifi connection
        //As a result, below function have to be implemented.
        if (isNetworkAvailable() == false) {//without wifi connection
            Toast t = Toast.makeText(this, R.string.connect, Toast.LENGTH_SHORT);
            t.show();
            Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
            intent.putExtra("only_access_points", true);
            intent.putExtra("extra_prefs_show_button_bar", true);
            intent.putExtra("wifi_enable_next_on_connect", true);
            startActivityForResult(intent,1);
            System.out.println("STOPPPPP");
            //startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK)); // Not as good as the lines above
        } else {//with wifi connection

            dialog = new ProgressDialog(this);
            dialog.setMessage("Please Wait");
            dialog.setCancelable(false);
            dialog.setInverseBackgroundForced(false);
            dialog.show();

            if (v == registerButton) {
                Log.d("CLICKING", "Clicking on register!");
                startActivity(new Intent(this, RegistrationRouterCheck.class));
//                dialog.hide();
                //startActivity(new Intent(this, AddDeviceActivity.class));
            } else if (v == listButton) {
                Log.d("CLICKING", "Clicking on list activity!");
                startActivity(new Intent(this, ListActivity.class));
//                dialog.hide();
            } else if (v == databaseButton) {
                //Need to change. Not every install option needs its own page.
                Log.d("CLICKING", "Clicking on add device!");
                startActivity(new Intent(this, DatabaseActivity.class));
//                dialog.hide();
            } else if (v == applicationsButton) {
                Log.d("CLICKING", "Clicking on delete device!");
                startActivity(new Intent(this, ApplicationsList.class));
//                dialog.hide();
            } else if (v == driversButton) {
                Log.d("CLICKING", "Clicking on add communication!");
                startActivity(new Intent(this, DriversSelectActivity.class));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(dialog != null && dialog.isShowing()){
            dialog.dismiss();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1000){
            ssid.setText(ConfigActivity.RSSID);
        }
    }
}