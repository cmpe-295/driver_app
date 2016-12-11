package spartansaferide.sjsu.edu.driver;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

import static spartansaferide.sjsu.edu.driver.DriverMapsActivity.context;

public class DriverLoginActivity extends AppCompatActivity {

    private static final String TAG = "DriverLoginActivity";
    // Progress Dialog Object
    ProgressDialog prgDialog;
    // Error Msg TextView Object
    TextView errorMsg;
    // Email Edit View Object
    EditText driverEmail;
    // Passwprd Edit View Object
    EditText driverPassword;

    ImageView backgroundImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        // Find Error Msg Text View control by ID
        errorMsg = (TextView) findViewById(R.id.login_error);
        // Find Email Edit View control by ID
        driverEmail = (EditText) findViewById(R.id.loginEmail);
        // Find Password Edit View control by ID
        driverPassword = (EditText) findViewById(R.id.loginPassword);
        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        backgroundImage = (ImageView) findViewById(R.id.background_img);
        backgroundImage.setAlpha(50);
    }

    public void loginUser(View view) {

        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if(activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            int isWiFi = activeNetwork.getType();

            //Hide Keypad after clicking Login Button
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            // Get Email Edit View Value
            String email = driverEmail.getText().toString();
            // Get Password Edit View Value
            String password = driverPassword.getText().toString();
            // Instantiate Http Request Param Object
            RequestParams params = new RequestParams();
            // When Email Edit View and Password Edit View have values other than Null
            if (Validation.isNotNull(email) && Validation.isNotNull(password)) {
                // When Email entered is Valid
                if (Validation.validate(email)) {
                    // Put Http parameter username with value of Email Edit View control
                    params.put("username", email);
                    // Put Http parameter password with value of Password Edit Value control
                    params.put("password", password);
                    // Invoke RESTful Web Service with Http parameters
                    invokeWS(params);
                }
                // When Email is invalid
                else {
                    Toast.makeText(getApplicationContext(), "Please enter valid email", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Please enter email and password", Toast.LENGTH_LONG).show();
            }
        }
        else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(DriverLoginActivity.this);
            dialog.setMessage("No active internet connection. Turn on WiFi or enable mobile data connection");
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = dialog.create();
            alert.show();
        }
    }


    @Override
    public void onBackPressed() {
        prgDialog.dismiss();
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        driverEmail.setText("");
        driverPassword.setText("");
    }

    public void invokeWS(RequestParams params) {
        // Show Progress Dialog
        prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post("http://saferide.nagkumar.com/login/", params, new AsyncHttpResponseHandler() {


            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                //Hide Progress Bar after Successful Login
                prgDialog.hide();

                String authCode = new String(responseBody);
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("spartansaferide.sjsu.edu.driver", Context.MODE_PRIVATE);
                sharedPreferences.edit().putString("authcode", authCode).apply();

                Log.d("Status", "Auth Code in LoginActivity is" + sharedPreferences.getString("authcode", ""));

                navigatetoHomeActivity();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                // Hide Progress Dialog
                prgDialog.hide();
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Method which navigates from Login Activity to Home Activity
     */
    public void navigatetoHomeActivity() {
        Intent homeIntent = new Intent(getApplicationContext(), DriverMapsActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(homeIntent,1);

        Log.d(TAG, "Success Login");
    }
}
