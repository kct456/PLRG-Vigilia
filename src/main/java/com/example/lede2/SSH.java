/**
 * Created by Tak and Bowon on 17. 7. 21.
 * SSH class can be used to make ssh connections and send command lines
 */

package com.example.lede2;

import android.content.Context;
import android.util.Log;
import android.os.AsyncTask;
import android.widget.TextView;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Channel;

import java.io.IOException;
import java.io.InputStream;
import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import android.provider.Settings;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import java.io.ByteArrayOutputStream;
//import java.util.Properties;
//import java.io.PrintStream;

// AsyncTask input : command line
// AysncTask output : output from a command
public class SSH extends AsyncTask<String, Void, List<String>> {

	// variables used for connection
	private Session session;
	private Channel channel;
	private ChannelExec ce;
	// in this project, we supposed we use fixed host, username, password
	private String host;
	private String username;
	public String password;
	private List<String> result_lines = new ArrayList<String>();

	// host, username, password initialize
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		host = ConfigActivity.ROUTERIP;
		username = ConfigActivity.ROUTERUSER;
		password = ConfigActivity.RPWD;




	}

	public void updatePassword(String newPass){
		password = newPass;
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

	// send a command 1523610518
	public void runCommand(String command) throws JSchException, IOException {

		if (!session.isConnected())
			throw new RuntimeException("Not connected to an open session.  Call open() first!");

		channel = session.openChannel("exec");
		ce = (ChannelExec) channel;
		ce.setCommand(command);
		ce.connect();
		Log.d("SSH RUN COMMAND", command);
	}

	// get output from a command
	private List<String> getChannelOutput(Channel channel) throws IOException {

		byte[] buffer = new byte[2048];
		List<String> output_lines = new ArrayList<String>();
		try {
			InputStream in = channel.getInputStream();
			String line = new String();
			while (true) {
				while (in.available() > 0) {
					int i = in.read(buffer, 0, 2048);
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
				} catch (Exception ee){}
			}
		} catch(Exception e) {
			Log.d("SSH READOUTPUT ERROR", "Error while reading channel output: "+ e);
		}

		return output_lines;
	}


	/*
	usage :
	0. params == "-ch <password>" : change default password into <password>
	1. params == "-co <password>" : add device to the database and hostapd file
	2. params == "-dn <password>" : delete devices by their names
	3. params == "-ln <password>" : list devices' names
	*/
	@Override
	protected List<String> doInBackground(String... params) {

		List<String> output = new ArrayList<String>();
		String cmd;

		if(params[0].substring(0,3).equals("-ch")) { // ./change_default_pw.sh -ch <password>
			cmd = MainActivity.DEF_CHANGE_DEFAULT_SCRIPT + " " + params[0];
		} else if(params[0].substring(0,3).equals("-co")) { // ./connect_device.sh -co <password> <device-name>
			cmd = MainActivity.DEF_CONNECT_DEVICE_SCRIPT + " " + params[0];
		} else if(params[0].substring(0,3).equals("-dn")) { // ./register_device.sh -dn <device-name>
			cmd = MainActivity.DEF_REGISTER_DEVICE_SCRIPT + " " + params[0];
		} else if(params[0].substring(0,3).equals("-ln")) { // ./register_device.sh -ln <device-name>
			// below block is a little different from others cause it needs to get output from the router
			try {
				// try open the connection
				if (!open()) {
					Log.d("SSH CONNECTION CLOSE", "open failed.");
					return null;
				}
				cmd = MainActivity.DEF_REGISTER_DEVICE_SCRIPT + " " + params[0];
				System.out.println(cmd);
				runCommand(cmd);
				ce.setCommand(cmd);
				ce.connect();
				result_lines = getChannelOutput(ce);
				//output = getChannelOutput(ce);
			} catch (Exception e) {
			}
			channel.disconnect();

			// only this block return meaningful value, which should be the names of devices.
			return output;
		} else if(params[0].substring(0,3).equals("cat")) {
			cmd = params[0];
		}
		else if(params[0].substring(0,4).equals("echo")) {
			cmd = params[0];
		}
		else {
			Log.d("SSH PARAM ERROR", "Wrong parameter used.");
			return null;
		}

		// now the command is set, so send it.
		try {
			// try open the connection
			if (!open()) {
				Log.d("SSH CONNECTION CLOSE", "open failed.");
				return null;
			}
			System.out.println(cmd);
			runCommand(cmd);
			ce.setCommand(cmd);
			ce.connect();
			result_lines = getChannelOutput(ce);
		} catch (Exception e) {
		} // done

		channel.disconnect();
		return null;
	}

	public List<String> getResultLines() {
		return result_lines;
	}

	/*
	@Override
	protected  onPostExecute(Void param) {
		Log.d("POST", "in post execute");
	}
	*/
}
