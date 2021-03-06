package spartansaferide.sjsu.edu.driver;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

import static spartansaferide.sjsu.edu.driver.DriverMapsActivity.context;


public class BarcodeScannerActivity extends AppCompatActivity {

    TextView barcodeResult;
    EditText studentid;
    String sid = "";
    String sname = "";
    ProgressDialog prgDialog;
    ConnectivityManager cm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        barcodeResult = (TextView) findViewById(R.id.barcode_result);
        studentid = (EditText) findViewById(R.id.studentid);

        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Please wait...");
        prgDialog.setCancelable(false);

        Intent i = getIntent();
        sid = i.getStringExtra("id");
        sname = i.getStringExtra("name");

        getSupportActionBar().setTitle("Scan/Enter Student Barcode");
    }

    /* Add a click event to the scan_barcode button to launch the ScanBarcodeActivity */
    public void scanBarCode(View v) {
        Intent intent = new Intent(this, ScanBarcodeActivity.class);
        startActivityForResult(intent, 0);
    }

    /* Override onActivityResult to get the barcode from ScanBarcodeActivity */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    //Check for the barcode and Display the value
                    Barcode barcode = data.getParcelableExtra("barcodes");
                    Log.d("test", barcode.displayValue);

                    barcodeResult.setText("Student ID : " + barcode.displayValue);
                    validateStudentId(barcode.displayValue.toString());

                } else {
                    barcodeResult.setText("No barcode found");
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void pickedStudent(View view) {

        String student_id = studentid.getText().toString();

        validateStudentId(student_id);
    }

    public void validateStudentId(String id) {

        Pattern p = Pattern.compile("[0-9]{9}");
        Matcher m = p.matcher(id);

        if (m.matches() && id.equals(sid)) {

            AlertDialog.Builder alert = new AlertDialog.Builder(this).setTitle("Confirm Pick-up")
                    .setMessage("Picked up " + sname + "?").setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                updatePickUp("ride/pickup_client/");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, just close the dialog box and do nothing
                            dialog.cancel();
                        }
                    });
            final AlertDialog dialog = alert.create();
            dialog.show();

        } else {
            //Toast.makeText(getBaseContext(),"Invalid studentid",Toast.LENGTH_SHORT).show();
            AlertDialog.Builder alert = new AlertDialog.Builder(this).setTitle("Invalid Student ID")
                    .setMessage("Enter Correct Student ID").setCancelable(true)
                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, just close the dialog box and do nothing
                            dialog.cancel();
                        }
                    });
            final AlertDialog dialog = alert.create();
            dialog.show();
        }
    }

    public void updatePickUp(String api) throws JSONException, UnsupportedEncodingException {
        if(checkInternetExists()) {

            JSONObject student = new JSONObject();
            student.put("sjsu_id", sid);
            AsyncHttpClient client = new AsyncHttpClient();
            StringEntity entity = new StringEntity(student.toString());
            client.addHeader("Authorization", "Token " + DriverMapsActivity.authObj.getString("token"));
            client.addHeader("Content-Type", "application/json");
            client.addHeader("Accept", "application/json");

            client.put(context, DriverMapsActivity.baseUrl + api, entity, "application/json", new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject responseBody) {

                    try {
                        JSONArray response = responseBody.getJSONArray("route");
                        setResult(RESULT_OK, getIntent().putExtra("response", responseBody.toString()));

                    } catch (JSONException e) {
                        Intent i = new Intent();
                        setResult(2, i);
                    }

                    Log.d("Return Status", "Status Code:" + statusCode);
                    finish();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {

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
        else{
            AlertDialog.Builder dialog = new AlertDialog.Builder(BarcodeScannerActivity.this);
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

    public boolean checkInternetExists() {
        cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            return true;
        }
        else
            return false;
    }

    @Override
    public void onBackPressed(){

        setResult(RESULT_OK, getIntent().putExtra("response","none"));
        super.onBackPressed();
    }
}
