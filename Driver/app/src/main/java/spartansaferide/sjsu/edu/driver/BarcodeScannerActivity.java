package spartansaferide.sjsu.edu.driver;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.regex.*;

import cz.msebera.android.httpclient.Header;


public class BarcodeScannerActivity extends AppCompatActivity {

    TextView barcodeResult;
    EditText studentid;
    String sid="";
    String sname="";
    ProgressDialog prgDialog;
    RequestParams params = new RequestParams();

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
        sname =i.getStringExtra("name");


        getSupportActionBar().setTitle("Scan/Enter Student Barcode");
    }

    /* Add a click event to the scan_barcode button to launch the ScanBarcodeActivity */
    public void scanBarCode(View v) {
        Intent intent = new Intent(this,ScanBarcodeActivity.class);
        startActivityForResult(intent,0);
    }

    /* Override onActivityResult to get the barcode from ScanBarcodeActivity */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    //Check for the barcode and Display the value
                    Barcode barcode = data.getParcelableExtra("barcodes");
                    Log.d("test",barcode.displayValue);

                    barcodeResult.setText("Student ID : " + barcode.displayValue);
                    validateStudentId(barcode.toString());

                } else {
                    barcodeResult.setText("No barcode found");
                }
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    public void pickedStudent(View view){

        String student_id=  studentid.getText().toString();

        validateStudentId(student_id);

    }

    public void validateStudentId(String id){

        Pattern p = Pattern.compile("[0-9]{9}");
        Matcher m = p.matcher(id);

        if(m.matches() && id.equals(sid)){


            AlertDialog.Builder alert = new AlertDialog.Builder(this).setTitle("Confirm Pick-up")
                    .setMessage("Picked up "+sname+"?").setCancelable(true)
                    .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            updatePickUp();
                            finish();
                        }
                    })
                    .setNegativeButton("No",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            // if this button is clicked, just close the dialog box and do nothing
                            dialog.cancel();
                        }
                    });
            ;
            final AlertDialog dialog= alert.create();
            dialog.show();

        }
        else {
            //Toast.makeText(getBaseContext(),"Invalid studentid",Toast.LENGTH_SHORT).show();
            AlertDialog.Builder alert = new AlertDialog.Builder(this).setTitle("Invalid Student ID")
                    .setMessage("Enter Correct Student ID").setCancelable(true)
                    .setNegativeButton("OK",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            // if this button is clicked, just close the dialog box and do nothing
                            dialog.cancel();
                        }
                    });
            ;
            final AlertDialog dialog= alert.create();
            dialog.show();
        }
    }

    public void updatePickUp(){
        params.put("sjsu_id",sid);
        AsyncHttpClient client = new AsyncHttpClient();
        client.post("http://saferide.nagkumar.com/pickup_client/", params, new AsyncHttpResponseHandler() {


            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                //Hide Progress Bar after Successful Login
                //prgDialog.hide();
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                // Hide Progress Dialog
                //prgDialog.hide();
                // When Http response code is '404'
                if(statusCode == 404){
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if(statusCode == 500){
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else{
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }
}
