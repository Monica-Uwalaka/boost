package ca.ualberta.boost;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.blikoon.qrcodescanner.QrCodeActivity;

import ca.ualberta.boost.controllers.ActiveUser;
import ca.ualberta.boost.models.Driver;
import ca.ualberta.boost.models.Ride;
import ca.ualberta.boost.stores.RideStore;
import ca.ualberta.boost.stores.UserStore;

/**
 * QRActivity is responsible for
 * by making sure the username and password correspond to user information in firestore
 * the class also will launch the sign up page if that button is clicked
 */
public class ScannerActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_CODE_QR_SCAN = 101;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private Button scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        scanButton = findViewById(R.id.scan_button);
        scanButton.setOnClickListener(this);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String TAG = "ScannerActivity";
        Log.d(TAG, "onActivityResult");

        if (resultCode != Activity.RESULT_OK) {
            Log.d(TAG, "Could not get a good result");
            if (data == null)
                return;
            //Getting the passed result
            String result = data.getStringExtra("com.blikoon.qrcodescanner.error_decoding_image");
            if (result != null) {
                AlertDialog alertDialog = new AlertDialog.Builder(ScannerActivity.this).create();
                alertDialog.setTitle("Scan Error");
                alertDialog.setMessage("QR Code could not be scanned");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
            return;

        }
        if (requestCode == REQUEST_CODE_QR_SCAN) {
            if (data == null)
                return;
            //Getting the passed result
            final String result = data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
            Log.d(TAG, "Have scan result in your app activity :" + result);
            AlertDialog alertDialog = new AlertDialog.Builder(ScannerActivity.this).create();
            alertDialog.setTitle("You got paid:");
            alertDialog.setMessage(result);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            payDriver(Double.valueOf(result));
                            Intent intent = new Intent(ScannerActivity.this, DriverMainPage.class);
                            startActivity(intent);
                        }
                    });
            alertDialog.show();
        }
    }

    private void payDriver(double payment) {
        Driver driver = (Driver) ActiveUser.getUser();
        driver.addToBalance(payment);
        UserStore.saveUser(driver);

        Ride currentRide = ActiveUser.getCurrentRide();
        currentRide.payDriver();
        RideStore.saveRide(currentRide);
    }

    //FUNCTIONS FOR PROMPTING PERMISSIONS
    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        return true;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();

                    // main logic
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            showMessageOKCancel("You need to allow access permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermission();
                                            }
                                        }
                                    });
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(ScannerActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.scan_button) {
            if (checkPermission()) {
                Intent i = new Intent(ScannerActivity.this, QrCodeActivity.class);
                startActivityForResult(i, REQUEST_CODE_QR_SCAN);
            } else {
                // Permission has already been granted
                requestPermission();
            }
        }
    }
}

