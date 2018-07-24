package com.example.lede2;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.text.CollationElementIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class AddDeviceActivity extends AppCompatActivity implements View.OnClickListener,View.OnFocusChangeListener {

    DatabaseObject databaseObject;
    //DatabaseSubtypeObject databaseSubtypeObject;
    Button doneButton;
    EditText databaseInfo;
    Spinner spinner1;
    Spinner spinner2;
    Spinner spinner3;
    Spinner spinner4;
    ProgressBar progressBar;
    private ArrayAdapter subtypeAdapter;
    private ArrayAdapter paramAdapter;
    private ArrayAdapter fieldAdapter;
    String lastParamChosen;
    TextView databaseAddressInfo;
    protected String database_information_filename;
    protected String id_information_filename;
    Context context;
    HashMap<String, String> userInputs;
    ProgressDialog dialog;


    private SSH_MySQL ssh;//Connection object between Android & Host

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);


        doneButton = (Button) findViewById(R.id.doneButton);
        spinner1 = (Spinner) findViewById(R.id.spinner1);
        spinner2 = (Spinner) findViewById(R.id.spinner2);
        spinner3 = (Spinner) findViewById(R.id.spinner3);
        spinner4 = (Spinner) findViewById(R.id.spinner4);
        databaseInfo = (EditText)findViewById(R.id.textInfo);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        progressBar.setMax(10);
        //databaseAddressInfo = (EditText)findViewById(R.id.textInfoAddress);
        database_information_filename = this.getString(R.string.device_param_config_filename);
        id_information_filename = this.getString(R.string.device_id_config_filename);
        userInputs = new HashMap<String, String>();
        context = this;
        lastParamChosen = "";

        //looks through database to see possible subtypes for dropdown box 2
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //set spinner2 values = subtypes of type chosen for spinner 1
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                String selectedType = spinner1.getSelectedItem().toString();
                Set<String> keysForSubType = (databaseObject.getTypeObject(selectedType)).getKeySet();
                ArrayList<String> subtypes = new ArrayList<String>();
                for(String key: keysForSubType){
                    subtypes.add(key);
                }
                Collections.sort(subtypes);
                subtypeAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, subtypes);
                subtypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner2.setAdapter(subtypeAdapter);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //set spinner 3 values based on if the selected subtype has params and/or addressparams
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                DatabaseSubtypeObject subtypeObject = getCurrentSubtypeObject();

                ArrayList<String> paramType = new ArrayList<String>();
                if(subtypeObject.getParams().size() > 0){
                    paramType.add("Device");
                }
                if(subtypeObject.getAddressParams().size() > 0){
                    paramType.add("Address");
                }
                if(subtypeObject.getZigbeeAddressParams().size()>0){
                    paramType.add("Zigbee");
                }
                //if both empty(example room)
                if(subtypeObject.getAddressParams().size() == 0 && subtypeObject.getParams().size() == 0){
                    paramType.add("No Device or Address Parameters");
                }
                Collections.sort(paramType);
                paramAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, paramType);
                spinner3.setAdapter(paramAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //set spinner 4 value based on spinner1-3 values (actual params)
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                DatabaseSubtypeObject subtypeObject = getCurrentSubtypeObject();
                List<String> keysForSubType = new ArrayList<>();
                //for devices that have no fields ex Rooms
                if(spinner3.getSelectedItem().toString().equals("No Device or Address Parameters")) {
                    keysForSubType.add("No Fields");

                    databaseInfo.setVisibility(View.GONE);
                }
                if(spinner3.getSelectedItem().toString().equals("Device")) {
                    databaseInfo.setVisibility(View.VISIBLE);
                    if(subtypeObject.getNumParams() > 0){
                        //add only params that have USER value
                        ArrayList<Pair<String, String>> tempList = subtypeObject.getParams();
                        for(int j = 0; j < tempList.size(); j++){
                            if(tempList.get(j).second.equals("USER")){
                                keysForSubType.add(tempList.get(j).first);
                            }
                        }

                    }
                }
                else if(spinner3.getSelectedItem().toString().equals("Zigbee")){
                    //setting field name for zigbee
                    databaseInfo.setVisibility(View.VISIBLE);
                    keysForSubType.add("DEVICEADDRESS");
                }
                else{
                    //if selected value is address
                    if(subtypeObject.getNumAddressParams() > 0){
                        databaseInfo.setVisibility(View.VISIBLE);
                        //add only params that have USER value
                        ArrayList<ArrayList<Pair<String, String>>> tempList = subtypeObject.getAddressParams();
                        for(int j = 0; j < tempList.size(); j++){
                            for(int k = 0; k < tempList.get(j).size(); k++){
                                if(tempList.get(j).get(k).second.equals("USER")){
                                    if(!keysForSubType.contains(tempList.get(j).get(k).first)) {
                                        keysForSubType.add(tempList.get(j).get(k).first);
                                    }
                                }
                            }

                        }

                    }
                }
                //List<String> keysForSubType = (databaseObject.getTypeObject(selectedType)).getSubtypeObject(selectedSubtype).getParams();

                ArrayList<String> params = new ArrayList<String>();
                for(String key: keysForSubType){
                    params.add(key);
                }
                Collections.sort(params);
                fieldAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, params);
                fieldAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner4.setAdapter(fieldAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinner4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //saves user input into a hashmap that is used later to create ssh command
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if(!spinner3.getSelectedItem().toString().equals("No Device or Address Parameters")) {
                    if (lastParamChosen.equals("")) {
                        lastParamChosen = spinner2.getSelectedItem().toString() + spinner3.getSelectedItem().toString() + spinner4.getSelectedItem().toString();

                    } else {
                        String enteredText = databaseInfo.getText().toString();
                        userInputs.put(lastParamChosen, enteredText);
                        String currentSelections = spinner2.getSelectedItem().toString() + spinner3.getSelectedItem().toString() + spinner4.getSelectedItem().toString();
                        if (!userInputs.containsKey(currentSelections)) {
                            userInputs.put(currentSelections, "");
                        }
                        databaseInfo.setText(userInputs.get(currentSelections));
                        lastParamChosen = currentSelections;
                    }
                }
                else{
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        doneButton.setOnClickListener(this);
        // Set config text from file for device
        //pull information from config file and uses it to generate main database object

        String idInformation = ConfigFileIO.readFromFile(id_information_filename, this);
        databaseObject = ConfigFileIO.createDatabaseObject(this, idInformation );



        //ConfigFileIO.printDatabaseObject(databaseObject);
        Set<String> keysForType = databaseObject.getKeySet();
        ArrayList<String> types = new ArrayList<String>();
        for(String key: keysForType){
            types.add(key);
        }
        //sets value for spinner 1
        Collections.sort(types);
        ArrayAdapter typeAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(typeAdapter);


    }

    @Override
    public void onClick(View v) {
        if(v == doneButton){
            String enteredText = databaseInfo.getText().toString();
            //combines user input into a command for the pi
            String keyForUserInput = spinner2.getSelectedItem().toString() + spinner3.getSelectedItem().toString() + spinner4.getSelectedItem().toString();
            userInputs.put(keyForUserInput, enteredText);
            if(!sufficientEntries()) {
                Snackbar done = Snackbar.make(findViewById(R.id.done),
                        "Please Enter all required fields for selected device type", 2000);
                done.show();
            }
            else {
                Snackbar done = Snackbar.make(findViewById(R.id.done), "Updating IoTDeviceAddress.config", 2000);
                done.show();
                progressBar.setVisibility(View.VISIBLE);
                doneButton.setAlpha(.5f);
                doneButton.setClickable(false);





                String sqlCommand = generateSQLCommand();
                System.out.println(sqlCommand);
                Log.d("sqlcommand", sqlCommand);
                System.out.println("");

                ssh = new SSH_MySQL();
                ssh.execute(sqlCommand);
                try {
                    Thread.sleep(1000);
                    List<String> result = ssh.getResultLines();
                    for (int i = 0; i < result.size(); i++) {
                        System.out.println("result" + result.get(i));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                updateDatabase();

                updateIoTDeviceAddress();
//                done = Snackbar.make(findViewById(R.id.done),
//                        "Updating SetList.config", 2000);
//                done.show();
                updateSetList();
                finish();
            }
        }
    }
    //determines if all fields have been entered
    public boolean sufficientEntries(){
        boolean sufficient = true;
        DatabaseSubtypeObject subtypeObject = getCurrentSubtypeObject();
        ArrayList<Pair<String, String>> params = subtypeObject.getParams();
        ArrayList<ArrayList<Pair<String, String>>> addressParams = subtypeObject.getAddressParams();
        ArrayList<ArrayList<Pair<String, String>>> zigbeeAddressParams = subtypeObject.getZigbeeAddressParams();
        if(spinner3.getSelectedItem().toString().equals("No Device or Address Parameters")) {
            return true;
        }
        if(params.size()>0){
            for(int i = 0; i < params.size(); i++){
                if(!params.get(i).second.equals("USER")){
                    continue;
                }
                if(!userInputs.containsKey(spinner2.getSelectedItem().toString() + "Device" + params.get(i).first) ||
                        userInputs.get(spinner2.getSelectedItem().toString() + "Device" + params.get(i).first).equals("")){
                    sufficient = false;
                }
            }
        }
        if(addressParams.size()>0){
            for(int i = 0; i <  addressParams.size(); i++){
                for(int j = 0;j < addressParams.get(i).size(); j++) {
                    if(!addressParams.get(i).get(j).second.equals("USER")){
                        continue;
                    }
                    if (!userInputs.containsKey(spinner2.getSelectedItem().toString() + "Address" + addressParams.get(i).get(j).first) ||
                            userInputs.get(spinner2.getSelectedItem().toString() + "Address" + addressParams.get(i).get(j).first).equals("")) {
                        sufficient = false;
                    }
                }
            }
        }
        if(zigbeeAddressParams.size()>0){
            for(int i = 0; i <  zigbeeAddressParams.size(); i++){
                for(int j = 0;j < zigbeeAddressParams.get(i).size(); j++) {
                    if(!zigbeeAddressParams.get(i).get(j).second.equals("USER")){
                        continue;
                    }
                    if (!userInputs.containsKey(spinner2.getSelectedItem().toString() + "Zigbee" + zigbeeAddressParams.get(i).get(j).first) ||
                            userInputs.get(spinner2.getSelectedItem().toString() + "Zigbee" + zigbeeAddressParams.get(i).get(j).first).equals("")) {
                        sufficient = false;
                    }
                }
            }
        }

        return sufficient;
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (hasFocus) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        } else {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    //generates command for adding both device and address device. May need to expand for zigbee
    public String generateSQLCommand(){
        String command = "";
        String paramInfo = "";
        String addressParamInfo = "";
        DatabaseSubtypeObject subtypeObject = getCurrentSubtypeObject();
        int numAddresses = subtypeObject.getNumAddresses();
        int numZigbeeAddresses = subtypeObject.getNumZigbeeAddresses();
        // 1) Create a new file and insert the configuration
        // 2) Run iotinstaller code for device installation
        // 3) Remove the existing config file
        // 4) Repeat 1, 2, and 3 for device address
        command += "echo \"" + generateDevFields() + "\" >> " +
                MainActivity.DEF_MYSQL_CONFIG_FILE + ";" +
                MainActivity.DEF_INSTALL_CMD + " " + MainActivity.DEF_MYSQL_CONFIG_FILE + ";" +
                "rm -rf " + MainActivity.DEF_MYSQL_CONFIG_FILE + ";";
                    // repeat process for device address
                for(int i = 0; i <numAddresses; i++ ) {
                    boolean multiple = true;
                    if(numAddresses == 1){
                        multiple = false;
                    }
                    command += "echo \"" + generateAddressFields(i, multiple) + "\" >> " +
                            MainActivity.DEF_MYSQL_CONFIG_FILE + ";" +
                            MainActivity.DEF_INSTALL_ADDRESS_CMD + " " + MainActivity.DEF_MYSQL_CONFIG_FILE + ";"
                            + "rm -rf " + MainActivity.DEF_MYSQL_CONFIG_FILE + ";";
                }
                for(int i = 0; i < numZigbeeAddresses; i++){
                    boolean multiple = true;
                    if(numZigbeeAddresses == 1){
                        multiple = false;
                    }
                    command += "echo \"" + generateZigBeeAddressFields(i, multiple) + "\" >> " +
                            MainActivity.DEF_MYSQL_CONFIG_FILE + ";" +
                            MainActivity.DEF_INSTALL_ZBADDRESS_CMD + " " + MainActivity.DEF_MYSQL_CONFIG_FILE + ";"
                            + "rm -rf " + MainActivity.DEF_MYSQL_CONFIG_FILE + ";";
                }

        return command;

    }
    //specific device information to be inserted into the sql command. follows format seen in config file on pi
    public String generateDevFields(){
        String fields = "";
        DatabaseSubtypeObject subtypeObject = getCurrentSubtypeObject();
        //IotMain Table
        fields += context.getResources().getString(R.string.database_name) + "\n"
                + "ID " + subtypeObject.getNextID() + "\n"
                + "TYPE " + spinner1.getSelectedItem().toString()+ "\n"
                + "TYPESPECIFIC " + spinner2.getSelectedItem().toString()+ "\n"
                + "END" + "\n\n";
        //This sets up the param types and size. special case for devices with no params
        if(subtypeObject.getNumParams() == 0){
            fields += "Table 1" + "\n"
                    + "EMPTY VARCHAR 0 " + "\n";
        }

        //Case for when there are params
        else {
            fields += "Table " + subtypeObject.getNumParams() + "\n";
        }
        ArrayList<Pair<String,String>> params = subtypeObject.getParams();
        for(int i = params.size()-1; i >= 0; i--){
            fields += params.get(i).first + " VARCHAR 20 \n";
        }
        fields += "END\n\n";
        //data for params
        fields += "Data \n";
        for(int i = params.size()-1; i >= 0; i--){
            fields += userInputs.get(spinner2.getSelectedItem().toString() +
                    "Device" +
                    params.get(i).first)  + "\n";
        }
        fields += "END\n\n";
        return fields;
    }


    //refactor with string builder but works for now
    public String generateAddressFields(int i, boolean multiple){
        String fields = "";
        String addressNumber = "";
        if(multiple){
            addressNumber = Integer.toString(i+1);
        }
        DatabaseSubtypeObject subtypeObject = getCurrentSubtypeObject();
        fields += "ID=" + subtypeObject.getNextID() + "\n" ;
        fields += "ADDRESSFOR=" + subtypeObject.getName()+ "Add"+ addressNumber + "\n" ;
        ArrayList<ArrayList<Pair<String,String>>> addressParams = subtypeObject.getAddressParams();
        String currentAddressParam = "";
        for(int j = 0; j <addressParams.get(i).size(); j++) {
            //if the value for current address parameter is user, use the input values
            if(addressParams.get(i).get(j).second.equals("USER")){
                currentAddressParam = addressParams.get(i).get(j).first;
                fields += currentAddressParam + "=" + userInputs.get(spinner2.getSelectedItem().toString() + "Address" + currentAddressParam) + "\n";
            }
            //if the value for the current address is not user, use the predefined values
            else{
                currentAddressParam = addressParams.get(i).get(j).first;
                fields += currentAddressParam + "=" + addressParams.get(i).get(j).second + "\n";
            }
        }
        fields += "END\n\n";
        return fields;
    }
    public String generateZigBeeAddressFields(int i , boolean multiple){
        String fields = "";
        String addressNumber = "";
        if(multiple){
            addressNumber = Integer.toString(i+1);
        }
        DatabaseSubtypeObject subtypeObject = getCurrentSubtypeObject();
        fields += "ID=" + subtypeObject.getNextID() + "\n" ;
        fields += "ADDRESSFOR=" + subtypeObject.getName()+ "ZBAdd"+ addressNumber + "\n" ;
        ArrayList<ArrayList<Pair<String,String>>> zigbeeAddressParams = subtypeObject.getZigbeeAddressParams();
        String currentAddressParam = "";
        for(int j = 0; j < zigbeeAddressParams.get(i).size(); j++) {
            //if the value for current address parameter is user, use the input values
            if(zigbeeAddressParams.get(i).get(j).second.equals("USER")){
                currentAddressParam = zigbeeAddressParams.get(i).get(j).first;
                fields += currentAddressParam + "=" + userInputs.get(spinner2.getSelectedItem().toString() + "Zigbee" + currentAddressParam) + "\n";
            }
            //if the value for the current address is not user, use the predefined values
            else{
                currentAddressParam = zigbeeAddressParams.get(i).get(j).first;
                fields += currentAddressParam + "=" + zigbeeAddressParams.get(i).get(j).second + "\n";
            }
        }
        fields += "END\n\n";
        return fields;
    }

    //update local database object and local database config file
    public void updateDatabase(){
        DatabaseSubtypeObject subtypeObject = getCurrentSubtypeObject();
        String deviceID = subtypeObject.getName() + " " + subtypeObject.getNextID() + "\n";
        ConfigFileIO.writeToFile(id_information_filename, deviceID, this);
        subtypeObject.insertID();
    }
    private void updateSetList(){
        SSH_MySQL ssh = new SSH_MySQL();
        String command = this.getString(R.string.updateSetList);
        List<String> results = new ArrayList<>();
        String setListFilename = this.getString(R.string.setListFilename);
        try {
            ssh.execute(command);
            Thread.sleep(1000);//To execute asyntask in ssh object, we have to sleep main thread
            results = ssh.getResultLines();
            while(results.size() == 0){
                Thread.sleep(500);
                results = ssh.getResultLines();
            }
            System.out.println(results);
        } catch (Exception e) {
            Log.d("Sleep exception", "exception at getSetsAndRelations");
        }
    }
    private void updateIoTDeviceAddress(){
        SSH_MySQL ssh = new SSH_MySQL();
        String command = this.getString(R.string.updateIoTDeviceAddress);
        List<String> results = new ArrayList<>();
        String iotDeviceAddressFilename = this.getString(R.string.updateIoTDeviceAddress);
        try {
            ssh.execute(command);
            Thread.sleep(1000);//To execute asyntask in ssh object, we have to sleep main thread
            results = ssh.getResultLines();
            while(results.size() == 0){
                Thread.sleep(500);
                results = ssh.getResultLines();
            }
            System.out.println(results);
        } catch (Exception e) {
            Log.d("Sleep exception", "exception at getSetsAndRelations");
        }

    }

    protected void addSSHResultsToConfig(List<String> results, String filename) {
        ConfigFileIO.writeToNewFile(filename,"", this );
        File dir = getFilesDir();
        File file = new File(dir, filename);
        file.delete();
        for (int i = 0; i < results.size(); i++) {
            ConfigFileIO.writeToFile(filename, results.get(i), this);
        }
    }


    public DatabaseSubtypeObject getCurrentSubtypeObject(){
        return databaseObject.getTypeObject(spinner1.
                getSelectedItem().toString()).getSubtypeObject(spinner2.getSelectedItem().toString());
    }


    public class Progress extends AsyncTask<Void, Integer, Void> {
        boolean finished;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Log.d("progress", "after dialog.show");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for(int i = 0; i < 10; i++){
                try {
                    Thread.sleep(100);
                    publishProgress(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
        }




        public void finished(){

        }
    }

}
