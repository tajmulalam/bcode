package com.theisofall.barcode.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;
import com.theisofall.barcode.R;
import com.theisofall.barcode.controller.CommonController;
import com.theisofall.barcode.models.ProductModel;
import com.theisofall.barcode.utils.ConnectionChecker;
import com.theisofall.barcode.utils.LocationManagerAI;
import com.theisofall.materialbarcodescanner.MaterialBarcodeScanner;
import com.theisofall.materialbarcodescanner.MaterialBarcodeScannerBuilder;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNotNull;

public class MainActivity extends AppCompatActivity implements LocationManagerAI.LocationFoundListener {

    public static final String BARCODE_KEY = "BARCODE";

    private Barcode barcodeResult;

    private TextView result;
    private TextView tvAddress;
    private ImageView ivProductImage;
    private LocationManagerAI lm;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        result = (TextView) findViewById(R.id.barcodeResult);
        result.setMovementMethod(new ScrollingMovementMethod());
        tvAddress = (TextView) findViewById(R.id.tvAddress);
        ivProductImage = (ImageView) findViewById(R.id.ivProductImage);
        ivProductImage.setVisibility(View.GONE);
        lm = new LocationManagerAI(MainActivity.this, this);
        lm.enableLocationApi();
        lm.startTracking();
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assertNotNull(result);
        assertNotNull(fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScan();
            }
        });
        if (savedInstanceState != null) {
            Barcode restoredBarcode = savedInstanceState.getParcelable(BARCODE_KEY);
            if (restoredBarcode != null) {
                result.setText(restoredBarcode.rawValue);
                barcodeResult = restoredBarcode;
            }
        }
    }

    private void startScan() {
        /**
         * Build a new MaterialBarcodeScanner
         */
        final MaterialBarcodeScanner materialBarcodeScanner = new MaterialBarcodeScannerBuilder()
                .withActivity(MainActivity.this)
                .withEnableAutoFocus(true)
                .withBleepEnabled(true)
                .withBackfacingCamera()
                .withCenterTracker()
                .withText("Scanning...")
                .withResultListener(new MaterialBarcodeScanner.OnResultListener() {
                    @Override
                    public void onResult(Barcode barcode) {
                        barcodeResult = barcode;
                        if (ConnectionChecker.isOnline(MainActivity.this)) {
                            new CommonController(MainActivity.this).callApi(CommonController.PRODUCT_INFO_REQUEST, barcode.rawValue, true);
                        } else {
                            Toast.makeText(MainActivity.this, "No internet connection available", Toast.LENGTH_SHORT).show();
                            result.setText(barcode.rawValue);
                        }
                    }
                })
                .build();
        materialBarcodeScanner.startScan();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(BARCODE_KEY, barcodeResult);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != MaterialBarcodeScanner.RC_HANDLE_CAMERA_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScan();
            return;
        }
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(android.R.string.ok, listener)
                .show();
    }

    public void showErrorMsg(String msg) {
        Toast.makeText(this, "No Product found", Toast.LENGTH_SHORT).show();
    }

    public void showSuccessMsg(ProductModel productModel) {
        ivProductImage.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(productModel.getProductPhoto()))
            Picasso.get().load(productModel.getProductPhoto()).error(R.mipmap.ic_launcher).into(ivProductImage);
        result.setText(
                "Product Name: " + productModel.getProductName() + "\n" +
                        "Bar code: " + productModel.getBarcodeNumber() + "\n" +
                        "Category: " + productModel.getCategory() + "\n" +
                        "Manufacturer: " + productModel.getManufacturer() + "\n" +
                        "Product color: " + productModel.getColor() + "\n" +
                        "Description: " + productModel.getDescription()
        );
        getAddressFromLocation();

    }

    private Location location;

    @Override
    public void locationFound(Location location) {
        this.location = location;

    }

    private void getAddressFromLocation() {
        if (Geocoder.isPresent()) {
            try {
                Geocoder gc = new Geocoder(MainActivity.this);
                List<Address> addresses = gc.getFromLocation(location.getLatitude(), location.getLongitude(), 5); // get the found Address Objects

                List<LatLng> ll = new ArrayList<LatLng>(addresses.size()); // A list to save the coordinates if they are available
                for (Address a : addresses) {
                    if (a.hasLatitude() && a.hasLongitude()) {
                        ll.add(new LatLng(a.getLatitude(), a.getLongitude()));
                    }
                }
                tvAddress.setText("Address:\n\n" + addresses.get(0).getAddressLine(0));
            } catch (IOException e) {
            }
        } else {

        }
    }

    @Override
    protected void onDestroy() {
        lm.stopLocationUpdates();
        lm.stopTracking();
        lm = null;
        super.onDestroy();
    }
}
