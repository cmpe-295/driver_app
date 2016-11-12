package spartansaferide.sjsu.edu.driver;

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
import java.util.regex.*;


public class BarcodeScannerActivity extends AppCompatActivity {

    TextView barcodeResult;
    EditText studentid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        barcodeResult = (TextView) findViewById(R.id.barcode_result);
        studentid = (EditText) findViewById(R.id.studentid);

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

        String sid=  studentid.getText().toString();

        validateStudentId(sid);

    }

    public void validateStudentId(String id){

        Pattern p = Pattern.compile("[0-9]{9}");
        Matcher m = p.matcher(id);

        if(m.matches()){

            Toast.makeText(getBaseContext(),"Valid studentid",Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getBaseContext(),"Invalid studentid",Toast.LENGTH_SHORT).show();
        }
    }
}
