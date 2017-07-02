package com.example.connectassistantforandroid;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class LoginActivity extends Activity {
	// volley declarations
	private RequestQueue requestQueue;
	private StringRequest request;
	// URL to the PHP script that volley will POST to and receive responses from
	private static final String URL = "http://mazerveli.com/connectassistant/user_control.php";
	
	// initialisations of views
	static EditText username;
	EditText password;
	TextView incorrect;
	Button login_button;
    boolean pass;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		// references to views on login screen
		username = (EditText) findViewById(R.id.login_username);
		password = (EditText) findViewById(R.id.login_password);
		incorrect = (TextView) findViewById(R.id.login_incorrect);
		login_button = (Button) findViewById(R.id.login_button);
		// default login details for testing
		username.setText("cs13ddw");
		password.setText("connect");
		
		// This method POSTs the user's login credentials to the PHP script for authentication
		// when the login button is clicked
		login_button.setOnClickListener(new OnClickListener (){
			public void onClick(View view){
				if(isServerAvailable()){
					// this is the request that will be added to the queue
					// it contains what to do when a response is received,
					// an error listener for error handling
					// and the data to POST to the PHP script as a HashMap
					request = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
						@Override
						public void onResponse(String response) {
							// if the user has been found start the main activity
							// else display relevant error messages
							if(response.substring(2, 9).equals("success")){
								// User exists
								startActivity(new Intent(getApplicationContext(), MainActivity.class));
								// Kills activity on exit
								finish();
							}else if(response.substring(2, 7).equals("error")){
								// User does not exist
								ErrorMessage("Incorrect Login Details");
							}else{
								// Database connection errors
								ErrorMessage(response.substring(1, response.length() - 1));
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
							hashMap.put("username", username.getText().toString());
							hashMap.put("password", password.getText().toString());
							return hashMap;
						}
					};
					// adds request to the queue
					requestQueue.add(request);
				} else {
					ErrorMessage("No Internet access");
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
			}
		});
		// a queue of requests for volley to process
		requestQueue = Volley.newRequestQueue(this);
	}

	// This method will display any error message passed through
	// as a red text error message on the login page
	private void ErrorMessage(String message){
		// Red text for error message passed into method
		incorrect.setText(message);
		// Displays TextView for 5 seconds before disappearing
		new CountDownTimer(5000, 1000){
			public void onTick(long millisUntilFinished){
			}
			public void onFinish(){
				// Sets TextView text to empty string when count down finishes
				incorrect.setText("");
			}
		}.start();
	}
	
	@Override
	public void onBackPressed() {
		// close activity when user presses back button
		finish();
	}

    // Checks if server is available
    // If server is false app goes into offline mode
    public boolean isServerAvailable(){
        request = new StringRequest(Request.Method.POST, URL,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    pass = true;
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    pass = false;
                }
            });
        return pass;
    }
	
	// attempts to ping a website and detects a response
	// if the ping was successful then there is an internet connection
	// else there is not or there is a network issue
    // <<experimental code>>
	public boolean ConnectionAvailable(){
	    try {
	        Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1    www.google.com");
	        int returnVal = p1.waitFor();
	        boolean reachable = (returnVal==0);
	        if(reachable){
	            // Internet Access
	            return reachable;
	        }else{
	        	// No Internet Access
	        	reachable = false;
	        	return reachable;
	        }
	    } catch (Exception e){
	    	e.printStackTrace();
	    }
	    return false;
	}
}