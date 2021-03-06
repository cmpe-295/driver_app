package spartansaferide.sjsu.edu.driver;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

/**
 * Created by balaji.byrandurga on 9/9/16.
 */
public class ScanBarcodeActivity extends AppCompatActivity {
    SurfaceView cameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scan_barcode);

        cameraPreview = (SurfaceView) findViewById(R.id.camera_preview);
        createCameraSource();
    }

    public void createCameraSource() {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this).build();

        final CameraSource cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(1600, 1024)
                .build();

        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                //Start Camera Source. Camera Permission check is needed and the start method throws an IOException, so it should be surrounded by a try-cathc block.
                //Dont forget to add camera permissions in the Manifest.xml file
                try {
                    if (ActivityCompat.checkSelfPermission(ScanBarcodeActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    cameraSource.start(cameraPreview.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();
            }
        });

        //Set the barcode detector's processor to get the detected barcodes in the recieveDetections() method.
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>(){
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections detections) {

                //Get the detected barcodes. Barcodes can be multiple. So, use an Array
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                //After successful detection of barcode, send it to mainactivity through Intent.
                if(barcodes.size() > 0){
                    Intent intent = new Intent();
                    //Get the latest barcode from the array
                    intent.putExtra("barcodes",barcodes.valueAt(0));
                    setResult(CommonStatusCodes.SUCCESS,intent);

                    //Finish the activity and return back to DriverMapsActivity.java
                    finish();
                }
            }
        });
    }
}