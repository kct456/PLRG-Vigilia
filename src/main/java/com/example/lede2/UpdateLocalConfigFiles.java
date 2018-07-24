package com.example.lede2;

        import android.app.ProgressDialog;
        import android.content.Context;
        import android.os.Bundle;
        import android.support.v7.app.AppCompatActivity;
        import android.util.Log;
        import android.view.WindowManager;
        import android.widget.Spinner;

        import java.io.File;
        import java.util.ArrayList;
        import java.util.List;

/**
 * Created by Brian on 2/23/2018.
 */

public class UpdateLocalConfigFiles extends AppCompatActivity {
    private SSH_MySQL ssh_mySQL1;//Connection object between Android & Host
    private SSH_MySQL ssh_mySQL2;
    private SSH_MySQL ssh_mySQL3;
    private SSH sshDevDat;
    private SSH routerSSH;
    String filename;
    List<String> temp;//data structure which has IoT device information already registered on LEDE2
    String filename_device;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //  1)updates config files on the pi
        //  2)cats the structure of the database
        //  3)cats instances of devices on the database
        String command1 = this.getString(R.string.update_DBInformation_File);
        String command2 = this.getString(R.string.cat_device_types);
        String command3 = this.getString(R.string.cat_subtypes);
        String commandRouter = "cat /tmp/dhcp.leases";
        filename = this.getString(R.string.device_param_config_filename);
        filename_device = "initial_DHCP.txt";
        ssh_mySQL1 = new SSH_MySQL();
        ssh_mySQL2 = new SSH_MySQL();
        ssh_mySQL3 = new SSH_MySQL();
        routerSSH = new SSH();
        sshDevDat = new SSH();

        List<String> results = new ArrayList<>();


        try{
            //executes command and wait till result lines are no longer empty.
            //result lines are the lines that the pi outputs after running the command
            ssh_mySQL1.execute(command1);
            Thread.sleep(1000);//To execute asyntask in ssh object, we have to sleep main thread
            results = ssh_mySQL1.getResultLines();
            while(results.size() == 0){
                Thread.sleep(500);
                results = ssh_mySQL1.getResultLines();
                System.out.println(results);
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.d("Sleep exception", "exception at SSH3");
        }
        try {

            ssh_mySQL2.execute(command2);
            Thread.sleep(1000);//To execute asyntask in ssh object, we have to sleep main thread
            results = ssh_mySQL2.getResultLines();
            while(results.size() == 0){
                Thread.sleep(500);
                results = ssh_mySQL2.getResultLines();

            }
            addSSHResultsToConfig(results, filename);
            System.out.println(results);


        } catch (Exception e) {
            Log.d("Sleep exception", "exception at oncreate of update SSH2");
        }


        try {
            ssh_mySQL3.execute(command3);
            Thread.sleep(1000);
            results = ssh_mySQL3.getResultLines();
            while(results.size() == 0){
                Thread.sleep(500);
                results = ssh_mySQL3.getResultLines();
            }

            filename = this.getString(R.string.device_id_config_filename);
            addSSHResultsToConfig(results, filename);
        }
        catch (Exception e){
            e.printStackTrace();
            Log.d("Sleep exception", "exception at SSH3");

        }

        try{
            sshDevDat.execute("-ln");
            Thread.sleep(1000);
            results = sshDevDat.getResultLines();
            while(results.size() == 0){
                Thread.sleep(500);
                results = sshDevDat.getResultLines();
                System.out.println(results);
            }
            addSSHResultsToConfig(results,this.getString(R.string.devices_dat_filename));
        }
        catch (Exception e){
            e.printStackTrace();
            Log.d("cat device dat", "exception at cat device.dat");
        }
        finish();

    }
    //writes ssh result into a local config
    private void addSSHResultsToConfig(List<String> results, String filename) {
        File dir = getFilesDir();
        File file = new File(dir, filename);
        file.delete();
        for (int i = 0; i < results.size(); i++) {
            ConfigFileIO.writeToFile(filename, results.get(i), this);
        }
        finish();
    }


}







