package com.example.connectassistantforandroid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	// device details
	private String MODEL = android.os.Build.MODEL;
	private String MAC;
	private String MAC_raw;
	// array list that will contain multiple Devices
	private List<Devices> dev_devices = new ArrayList<Devices>();
	private List<Devices> tt_devices = new ArrayList<Devices>();
	// device icon drawables
	private int laptop = R.drawable.dev_laptop;
	private int desktop = R.drawable.dev_desktop;
	private int smartphone = R.drawable.dev_smartphone;
	private int tablet = R.drawable.dev_tablet;
	private int pda = R.drawable.dev_pda;
	private int console = R.drawable.dev_console;
	// volley declarations
	private RequestQueue requestQueue;
	private StringRequest request;
	// URLs to the PHP scripts that volley will POST to and receive responses from
	private static final String URL_users = "http://mazerveli.com/connectassistant/user_details.php";
	private static final String URL_devices = "http://mazerveli.com/connectassistant/user_devices.php";
	private static final String URL_register = "http://mazerveli.com/connectassistant/user_register.php";
	// get user name entered in Login screen
    private final String username = LoginActivity.username.getText().toString();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		//********** TABHOST HANDLING**********//
		// set up tabs for the TabHost in layout
		final TabHost tabHost = (TabHost)findViewById(R.id.tabhost);
        tabHost.setup();
        // set tab text
        TabSpec spec1 = tabHost.newTabSpec("Tab 1");
        // set tab icon
        spec1.setIndicator("", getResources().getDrawable(R.drawable.ic_connect));
        // set tab content layout
        spec1.setContent(R.id.tab_connect);
        TabSpec spec2 = tabHost.newTabSpec("Tab 2");
        spec2.setIndicator("", getResources().getDrawable(R.drawable.ic_devices));
        spec2.setContent(R.id.tab_devices);
        TabSpec spec3 = tabHost.newTabSpec("Tab 3");
        spec3.setIndicator("", getResources().getDrawable(R.drawable.ic_diagnostics));
        spec3.setContent(R.id.tab_troubleshoot);
        TabSpec spec4 = tabHost.newTabSpec("Tab 4");
        spec4.setIndicator("", getResources().getDrawable(R.drawable.ic_account));
        spec4.setContent(R.id.tab_account);
        TabSpec spec5 = tabHost.newTabSpec("Tab 5");
        spec5.setIndicator("", getResources().getDrawable(R.drawable.ic_help));
        spec5.setContent(R.id.tab_help);
        // adds tabs to TabHost
        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        tabHost.addTab(spec3);
        tabHost.addTab(spec4);
        tabHost.addTab(spec5);

        
        //********** HORIZONTAL SCROLL HANDLING**********//
        // <<experimental code>>
        // identifier for ScrollView within TabHost
    	ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
    	// actions to do when scrollView is swiped left or right
    	scrollView.setOnTouchListener(new OnSwipeTouchListener(this) {
    		@Override
    	    public void onSwipeLeft() {
    			int tab = tabHost.getCurrentTab();
    			if(tab == 4){
    				// do nothing on last tab
    			} else {
    				// change to next tab
    		    	tabHost.setCurrentTab(tab + 1);
    			}
    	    }
    	    @Override
    	    public void onSwipeRight() {
    	    	int tab = tabHost.getCurrentTab();
    			if(tab == 0){
    				// do nothing on first tab
    			} else {
    				// change to previous tab
    		    	tabHost.setCurrentTab(tab - 1);
    			}
    	    }
    	});
    	
    	
        //********** CONNECT TAB HANDLING**********//
    	// inbuilt class for wireless service management
    	WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    	WifiInfo info = manager.getConnectionInfo();
    	MAC = info.getMacAddress().toUpperCase(Locale.UK);
    	
    	// reference to EditText
		final EditText MAC_entry = (EditText) findViewById(R.id.MAC_entry);
		// reference to TextView
		final TextView text = (TextView) findViewById(R.id.MAC_address);
		// if device is running Android 6.0 user has to enter MAC address manually
		if(MAC.equals("02:00:00:00:00:00")){
			// << experimental code >>
			text.setText(getResources().getString(R.string.not_supported));
		} else {
			// sets default text of EditText and TextView declared above
			text.setText("This Device's WiFi MAC Address:");
			MAC_entry.setText(MAC);
		}
		
		// references to 'SHOW MAC' button view
		Button showMAC = (Button) findViewById(R.id.showMAC);
		// Sets behaviour for when showMAC button is clicked
		showMAC.setOnClickListener(new OnClickListener(){
			public void onClick(View view){
				if(MAC.equals("02:00:00:00:00:00")){
    				// << experimental code >>
					Toast.makeText(MainActivity.this, "This feature is not supported on Android 6.0", Toast.LENGTH_SHORT).show();
				} else {
					// sets EditText to device's MAC address
					MAC_entry.setText(MAC);
				}
			}
		});
		
		// references to 'REGISTER' button view
		Button register = (Button) findViewById(R.id.postMAC);
		// Sets behaviour for when postMAC button is clicked
		register.setOnClickListener(new OnClickListener(){
			public void onClick(View view){
				// Error checks for MAC Address format entered in EditText
				MAC_raw = MAC_entry.getText().toString().replace(":", "").toUpperCase(Locale.UK);
				// return true if string contains hexadecimal characters
				boolean match = MAC_raw.matches("[0-9a-fA-F]+");
				if((MAC_raw.length() == 12) && match){
					// confirm whether user wants to register device
					confirmRegistration();
				} else {
					Toast.makeText(MainActivity.this, "Please enter a valid MAC address.", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		
		//********** DEVICES & TROUBLESHOOT HANDLING **********//
		// get list of devices from server
		//GetDevices();

        // Populate device lists for offline use
        String[] types = {"Laptop", "Desktop", "Pda", "Mobile", "Console", "Tablet"};
        for(int i = 0; i < 10; i++){
            Random rand = new Random();
            int id = rand.nextInt(6);
            populateLists(types[id], i, "Name " + i, "Pass");
        }
        for(int i = 0; i < 5; i++){
            Random rand = new Random();
            int id = rand.nextInt(6);
            populateLists(types[id], i, "Name " + i, "Fail");
        }
        populateListViews();
		// set action for when a listed device has been clicked
		registerClickCallBack();
		
		
		//********** ACCOUNT TAB HANDLING**********//
		// References to TextViews
		final TextView acc_id = (TextView) findViewById(R.id.acc_id);
		final TextView acc_username = (TextView) findViewById(R.id.acc_username);
		final TextView acc_firstname = (TextView) findViewById(R.id.acc_firstname);
		final TextView acc_lastname = (TextView) findViewById(R.id.acc_lastname);
		final TextView acc_email = (TextView) findViewById(R.id.acc_email);
		// Reference to ImageView
        final ImageView avatar = (ImageView) findViewById(R.id.acc_avatar);
        // Retrieves user details from database
        GetUserDetails(acc_id, acc_username, acc_firstname, acc_lastname, acc_email, avatar);


		//********** HELP TAB HANDLING **********//
        // reference to contact Tech Zone text
		final TextView support = (TextView) findViewById(R.id.support);
		// set actions for when the TextView is clicked
        support.setOnClickListener(new OnClickListener(){
			public void onClick(View view){
				// intent for mail
				final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
				// put data into the intent to be passed to mail app
				emailIntent.setType("plain/text");
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"computing-support@brunel.ac.uk"});
				// opens mail app options for user
				startActivity(Intent.createChooser(emailIntent, "Send mail..."));
			}
		});
        // reference to contact developer text
		final TextView developer = (TextView) findViewById(R.id.developer);
		// set actions for when the TextView is clicked
		developer.setOnClickListener(new OnClickListener(){
        	public void onClick(View view){
        		// intent for mail
				final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
				// put data into the intent to be passed to mail app
				emailIntent.setType("plain/text");
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"1214728@my.brunel.ac.uk"});
				// opens mail app options for user
				startActivity(Intent.createChooser(emailIntent, "Send mail..."));
			}
		});
	}
	
	// This method gets confirmation from the user if they want to register their device
	private void confirmRegistration() {
		// Display dialog confirming device registration
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int clicked) {
		        switch (clicked){
		        case DialogInterface.BUTTON_POSITIVE:
		            // If "Yes" get model of device
		        	String model = getModel();
					if(isTablet()){
						// register if device is a tablet
						registerDevice("Tablet", model + " Tablet");
					} else {
						// register if the device is a mobile
						registerDevice("Mobile", model + " Smartphone");
					}
		            break;
		        case DialogInterface.BUTTON_NEGATIVE:
		            // If "No" do nothing
		            break;
		        }
		    }
		};
		// Message to display in confirmation window
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to register this device?")
			.setNegativeButton("No", dialogClickListener)
		    .setPositiveButton("Yes", dialogClickListener).show();
	}
	
	// This method returns true if the device is a tablet and false if otherwise
	public boolean isTablet(){
		boolean isTablet = getResources().getBoolean(R.bool.isTablet);
		if (isTablet) {
		    return true;
		} else {
		    return false;
		}
	}
	
	// This method detects the device's model
	// and returns it as a string variable
	public String getModel(){
		String model;
		if(MODEL.contains("ACER")){
			model = "Acer";
		} else if(MODEL.contains("ARCHOS")){
			model = "Archos";
		} else if(MODEL.contains("ASUS")){
			model = "Asus";
		} else if(MODEL.contains("DELL")){
			model = "Dell";
		} else if(MODEL.contains("HTC")){
			model = "HTC";
		} else if(MODEL.contains("HUAWEI")){
			model = "Huawei";
		} else if(MODEL.contains("LENOVO")){
			model = "Lenovo";
		} else if(MODEL.contains("LG")){
			model = "LG";
		} else if(MODEL.contains("MOTOROLA")){
			model = "Motorola";
		} else if(MODEL.contains("O2 XDA")){
			model = "O2 Xda";
		} else if(MODEL.contains("SAMSUNG")){
			model = "Samsung";
		} else if(MODEL.contains("SONY")){
			model = "Sony Ericson";
		} else if(MODEL.contains("VIEWSONIC")){
			model = "Viewsonic";
		} else {
			model = "Unlisted";
		}
		return model;
	}
	
	// This method processes the device registration
	// It uses volley libraries to receive responses from a PHP script
	protected void registerDevice(final String kind, final String model) {
		// a queue of requests for volley to process
		requestQueue = Volley.newRequestQueue(this);
        // this is the request that will be added to the queue
		// it contains what to do when a response is received,
		// an error listener for error handling
		// and the data to POST to the PHP script as a HashMap
        request = new StringRequest(Request.Method.POST, URL_register, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					// if the response is "true" then registration has been successful
					// else the error message received from the PHP script will be displayed
					if(response.equals("true")){
						Toast.makeText(MainActivity.this, "Registration successful", Toast.LENGTH_LONG).show();
					}else{
						Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError volleyError) {
				// default error handling
			}
		})
		{
        	@Override
			protected Map<String, String> getParams() throws AuthFailureError {
        		// HashMap contains a string key-value pair
				HashMap<String, String> hashMap = new HashMap<String, String>();
				// key-value pairs to put in HashMap and POST to the PHP script
				hashMap.put("owner", username);
				hashMap.put("MAC", MAC_raw);
				hashMap.put("kind", kind);
				hashMap.put("model", model);
				hashMap.put("name", "My " + model);
				hashMap.put("os", "Google Android");
				hashMap.put("status", "Pass");
				return hashMap;
			}
		};
		// adds request to the queue
		requestQueue.add(request);
	}
	
	// This method retrieves the users account details
	// It uses volley libraries to receive responses from a PHP script
	public void GetUserDetails(final TextView acc_id, final TextView acc_username, final TextView acc_firstname, final TextView acc_lastname, final TextView acc_email, final ImageView avatar){
		// a queue of requests for volley to process
		requestQueue = Volley.newRequestQueue(this);
        // this is the request that will be added to the queue
		// it contains what to do when a response is received,
		// an error listener for error handling
		// and the data to POST to the PHP script as a HashMap
        request = new StringRequest(Request.Method.POST, URL_users, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				// When a response is received the user's account details are stored
				// and applied to the application accordingly
				try {
					// Create JSONObject containing key-value pairs from the response
                	JSONObject jsonObject = new JSONObject(response);
                	// Set variables to corresponding values in jsonObject
            		String id = jsonObject.getString("id");
            		String firstname = jsonObject.getString("firstname");
            		String lastname = jsonObject.getString("lastname");
            		String email = jsonObject.getString("email");
            		String gender = jsonObject.getString("gender");
            		// Set text in TextViews in the accounts tab to corresponding values
            		acc_id.setText(id);
                    acc_username.setText(username);
                    acc_firstname.setText(firstname);
                    acc_lastname.setText(lastname);
                    acc_email.setText(email);
                    // Set the user's avatar depending on their gender
                    if(gender.equals("male")){
                    	avatar.setImageDrawable(getResources().getDrawable(R.drawable.male));
                    } else if (gender.equals("female")){
                    	avatar.setImageDrawable(getResources().getDrawable(R.drawable.female));
                    } else {
                    	avatar.setImageDrawable(getResources().getDrawable(R.drawable.male));
        			}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError volleyError) {
				// default error handling
			}
		})
		{
        	@Override
			protected Map<String, String> getParams() throws AuthFailureError {
        		// HashMap contains a string key-value pair
				HashMap<String, String> hashMap = new HashMap<String, String>();
				// key-value pairs to put in HashMap and POST to the PHP script
				hashMap.put("username", username);
				return hashMap;
			}
		};
		// adds request to the queue
		requestQueue.add(request);
	}
	
	// This method retrieves a list of the user's registered devices
	// It uses volley libraries to receive responses from a PHP script
	public void GetDevices(){
		// a queue of requests for volley to process
		requestQueue = Volley.newRequestQueue(this);
        // this is the request that will be added to the queue
		// it contains what to do when a response is received,
		// an error listener for error handling
		// and the data to POST to the PHP script as a HashMap
        request = new StringRequest(Request.Method.POST, URL_devices, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					// Create JSONArray containing key-value pairs in response
                	JSONArray jsonArray = new JSONArray(response);
                	// for each response in the array,
                	// get the jsonObject and apply the key-value strings to their
                	// corresponding fields in the device list
                	for (int i = 0; i < jsonArray.length(); i++) {
                	    JSONObject row = jsonArray.getJSONObject(i);
                	    // populate the list of device
                	    populateLists(row.getString("kind"), row.getInt("deviceID"), row.getString("name"), row.getString("status"));
                	}
                	// add the the list of device to the ListView
            		populateListViews();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError volleyError) {
				// default error handling
			}
		})
		{
        	@Override
			protected Map<String, String> getParams() throws AuthFailureError {
        		// HashMap contains a string key-value pair
				HashMap<String, String> hashMap = new HashMap<String, String>();
				// key-value pairs to put in HashMap and POST to the PHP script
				hashMap.put("username", username);
				return hashMap;
			}
		};
		// adds request to the queue
		requestQueue.add(request);
	}
	
	// This method populates a list of devices
	// devices with a status of "Fail" will be added to the Troubleshoot list
	private void populateLists(String type, int id, String name, String status){
		// Add devices that have failed security checks to troubleshoot list
		if(status.equals("Fail")){
			if(type.equals("Laptop")){
				tt_devices.add(new Devices(laptop, id, name, status));
			}else if(type.equals("Desktop")){
				tt_devices.add(new Devices(desktop, id, name, status));
			}else if(type.equals("Pda")){
				tt_devices.add(new Devices(pda, id, name, status));
			}else if(type.equals("Mobile")){
				tt_devices.add(new Devices(smartphone, id, name, status));
			}else if(type.equals("Tablet")){
				tt_devices.add(new Devices(tablet, id, name, status));
			}else if(type.equals("Console")){
				tt_devices.add(new Devices(console, id, name, status));
			}
		}
		// Add every device registered by user to device list
		if(type.equals("Laptop")){
			dev_devices.add(new Devices(laptop, id, name, status));
		}else if(type.equals("Desktop")){
			dev_devices.add(new Devices(desktop, id, name, status));
		}else if(type.equals("Pda")){
			dev_devices.add(new Devices(pda, id, name, status));
		}else if(type.equals("Mobile")){
			dev_devices.add(new Devices(smartphone, id, name, status));
		}else if(type.equals("Tablet")){
			dev_devices.add(new Devices(tablet, id, name, status));
		}else if(type.equals("Console")){
			dev_devices.add(new Devices(console, id, name, status));
		}
	}
	
	// This method applies each list of devices to their corresponding ListView
	private void populateListViews(){
		// Populate device ListView using device list adapter
		ArrayAdapter<Devices> dev_adapter = new DeviceListAdapter();
		ListView deviceList = (ListView) findViewById(R.id.devicesListView);
		deviceList.setAdapter(dev_adapter);
		// Populate troubleshoot ListView using troubleshoot list adapter
		ArrayAdapter<Devices> tt_adapter = new TroubleshootListAdapter();
		ListView troubleshootList = (ListView) findViewById(R.id.troubleshootListView);
		troubleshootList.setAdapter(tt_adapter);
	}
	
	// This class acts as an adapter for the Devices tab ListView
	// It sets the device icon, ID, status and name of each list element
	public class DeviceListAdapter extends ArrayAdapter<Devices>{
		public DeviceListAdapter() {
			super(MainActivity.this, R.layout.item_view, dev_devices);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// make sure there is view to work with
			View itemView = convertView;
			if(itemView == null){
				itemView = getLayoutInflater().inflate(R.layout.item_view, parent, false);
			}
			// current device selected to set up as an instance/object of the Devices class
			Devices currentDevice = dev_devices.get(position);
			// set which image to use as device icon
			ImageView icon = (ImageView) itemView.findViewById(R.id.item_icon);
			icon.setImageResource(currentDevice.getIconId());
			// set which text to show as device ID
			TextView deviceId = (TextView) itemView.findViewById(R.id.item_id);
			deviceId.setText(currentDevice.getDeviceId() + "");
			// set which text to show as device name
			TextView name = (TextView) itemView.findViewById(R.id.item_name);
			name.setText(currentDevice.getName());
			// set which text to show as device status
			TextView status = (TextView) itemView.findViewById(R.id.item_status);
			status.setText(currentDevice.getStatus());
			return itemView;
		}
	}
	
	// This class acts as an adapter for the Troubleshoot tab ListView
	// It sets the device icon, ID, status and name of each list element
	public class TroubleshootListAdapter extends ArrayAdapter<Devices>{
		public TroubleshootListAdapter() {
			super(MainActivity.this, R.layout.item_view, tt_devices);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// make sure there is view to work with
			View itemView = convertView;
			if(itemView == null){
				itemView = getLayoutInflater().inflate(R.layout.item_view, parent, false);
			}
			// current device selected to set up as an instance/object of the Devices class
			Devices currentDevice = tt_devices.get(position);
			// set which image to use as device icon
			ImageView icon = (ImageView) itemView.findViewById(R.id.item_icon);
			icon.setImageResource(currentDevice.getIconId());
			// set which text to show as device ID
			TextView deviceId = (TextView) itemView.findViewById(R.id.item_id);
			deviceId.setText(currentDevice.getDeviceId() + "");
			// set which text to show as device name
			TextView name = (TextView) itemView.findViewById(R.id.item_name);
			name.setText(currentDevice.getName());
			// set which text to show as device status
			TextView status = (TextView) itemView.findViewById(R.id.item_status);
			status.setText(currentDevice.getStatus());
			return itemView;
		}
	}
	
	// This method controls what to do when an element in a ListView is clicked
	// << experimental code >>
	private void registerClickCallBack() {
		ListView dev_list = (ListView) findViewById(R.id.devicesListView);
		dev_list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View viewClicked, int pos, long id){
				// Devices clickedDevice = devices.get(pos);
				// String message = "you clicked the " + clickedDevice.getName() + " which is position " + pos;
				// Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
			}
		});
		ListView tt_list = (ListView) findViewById(R.id.troubleshootListView);
		tt_list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View viewClicked, int pos, long id){
				// Devices clickedDevice = devices.get(pos);
				// String message = "you clicked the " + clickedDevice.getName() + " which is position " + pos;
				// Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	// This method controls the actions to do when the back button has been pressed
	@Override
	public void onBackPressed() {
		// Display dialog confirming exit of the application when back button is clicked
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int clicked) {
		        switch (clicked){
		        case DialogInterface.BUTTON_POSITIVE:
		            // If "Yes" close activity
		        	finish();
		            break;
		        case DialogInterface.BUTTON_NEGATIVE:
		            // If "No" do nothing
		            break;
		        }
		    }
		};
		// Message to display when leave application
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Do you wish to exit the application?")
			.setNegativeButton("No", dialogClickListener)
		    .setPositiveButton("Yes", dialogClickListener).show();
	}
}
