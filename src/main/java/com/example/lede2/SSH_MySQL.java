package com.example.lede2;

/**
 * Created by rtrimana on 9/25/17.
 */

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



import java.io.IOException;
import java.io.InputStream;
import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// AsyncTask input : command line
// AysncTask output : output from a command
public class SSH_MySQL extends AsyncTask<String, Void, List<String>> {

    // variables used for connection
    private Session session;
    private Channel channel;
    private ChannelExec ce;
    // in this project, we supposed we use fixed host, username, password
    private String host;
    private String username;
    private String password;
    ProgressDialog dialog;

    //use this to see the output of the command used
    private List<String> resultLines = new ArrayList<String>();

    // host, username, password initialize
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        host = ConfigActivity.MYSQLHOSTIP;
        username = ConfigActivity.MYSQLHOSTUSER;
        password = ConfigActivity.MYSQLHOSTPASSWORD;

    }

	/*
	The functions below are mainly from :
	https://stackoverflow.com/questions/25789245/how-to-get-jsch-shell-command-output-in-string
	*/

    // open the connection using username, password, and hostname
    public boolean open() throws JSchException {

        JSch jSch = new JSch();

        session = jSch.getSession(username, host, 22);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");  // not recommended
        session.setPassword(password);
        session.setConfig(config);


        Log.d("SSH CONNECT OPEN", "Connecting SSH to " + host + " - Please wait for few seconds... ");
        session.connect();
        if (session.isConnected()) {
            Log.d("SSH CONNECT", "router connected!");
            return true;
        } else {
            Log.d("SSH NOT CONNECT", "router NOT connected!");
            return false;
        }
    }

    // send a command
    public void runCommand(String command) throws JSchException, IOException {

        if (!session.isConnected())
            throw new RuntimeException("Not connected to an open session.  Call open() first!");

        System.out.println("command: " + command);
        channel = session.openChannel("exec");
        ce = (ChannelExec) channel;
        ce.setCommand(command);
        ce.connect();
        Log.d("SSH RUN COMMAND", command);
    }

    // get output from a command
    private List<String> getChannelOutput(Channel channel) throws IOException {

        byte[] buffer = new byte[8192];
        List<String> output_lines = new ArrayList<String>();
        try {
            InputStream in = channel.getInputStream();
            String line = new String();
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(buffer, 0, 8192);
                    if (i < 0) {
                        break;
                    }
                    line = new String(buffer, 0, i);
                    // add the read line to the return value list.
                    output_lines = new ArrayList(Arrays.asList(line.split("\\n")));
                }

                if(line.contains("logout")) {
                    break;
                }
                if (channel.isClosed()) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception e){}
            }
        } catch(Exception e) {
            Log.d("SSH READOUTPUT ERROR", "Error while reading channel output: "+ e);
        }

        return output_lines;
    }


    /*
    usage : execute commands through SSH for database MySQL
    */
    @Override
    protected List<String> doInBackground(String... params) {

        String cmd;
        // Get into the path and create the config file
        //starts at iot2/bin/iotinstaller
        cmd = "cd " + MainActivity.DEF_ADD_DEVICE_TO_MYSQL + ";";
        cmd = cmd + params[0];
        //Log.d("yoyo", cmd);

        // now the command is set, so send it.
        try {
            // try open the connection
            if (!open()) {
                Log.d("SSH CONNECTION CLOSE", "open failed.");
                return null;
            }
            runCommand(cmd);
            ce.setCommand(cmd);
            ce.connect();
            resultLines = getChannelOutput(ce);
        } catch (Exception e) {
        } // done

        channel.disconnect();
        return null;
    }

    public List<String> getResultLines() {
        return resultLines;
    }
	/*
	@Override
	protected  onPostExecute(Void param) {
		Log.d("POST", "in post execute");
	}
	*/
}
