package spartansaferide.sjsu.edu.driver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;


public class BarcodeScannerActivity extends AppCompatActivity {

    TextView barcodeResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        barcodeResult = (TextView) findViewById(R.id.barcode_result);
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
                } else {
                    barcodeResult.setText("No barcode found");
                }
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
