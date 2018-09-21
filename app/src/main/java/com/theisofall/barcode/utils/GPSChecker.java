package com.theisofall.barcode.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by tajmulalam on 1/11/18.
 */

public class GPSChecker {
    public static boolean checkGPS(final Context context) {
        LocationManager locationManager = (LocationManager) context
                .getSystemService(LOCATION_SERVICE);

        // get GPS status
        boolean checkGPS = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isPassiveEnabled = locationManager
                .isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
        // get network provider status
        boolean checkNetwork = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!checkGPS && !checkNetwork) {
            return false;
        } else {
            return true;
        }
    }

    public static void showSettingForGps(final Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("GPS is not Enabled!");
        alertDialog.setMessage("Do you want to turn on GPS?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        alertDialog.show();
    }
}