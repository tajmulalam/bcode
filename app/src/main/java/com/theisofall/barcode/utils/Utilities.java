

package com.theisofall.barcode.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.theisofall.barcode.R;
import com.theisofall.barcode.listener.VolleyServiceResponseListener;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;


/**
 * Created by tajmulalam on 1/11/18.
 */

public class Utilities {

    public static Utilities utilitiesObj = null;
    ProgressDialog pDialog = null;
    private static final String REGEX_URL =
            "^[A-Za-z][A-Za-z0-9+.-]{1,120}:[A-Za-z0-9/](([A-Za-z0-9$_.+!*,;/?:@&~=-])"
                    + "|%[A-Fa-f0-9]{2}){1,333}(#([a-zA-Z0-9][a-zA-Z0-9$_.+!*,;/?:@&~=%-]{0,"
                    + "1000}))?$";
    private RequestQueue mRequestQueue;

    public static Utilities getInstance() {
        if (utilitiesObj == null) {
            utilitiesObj = new Utilities();
        }
        return utilitiesObj;
    }

    public HashMap<String, String> getRequestParams() {
        HashMap<String, String> mRequestParams = new HashMap<>();
        return mRequestParams;
    }


    public void showProgressDialog(Context ctx) {
        pDialog = new ProgressDialog(ctx);
        pDialog.setCancelable(true);
        pDialog.setMessage(ctx.getString(R.string.loader_loading));
        pDialog.show();
    }

    public void dismissProgressDialog() {
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }


    public RequestQueue getRequestQueue(Context mContext) {
        // If RequestQueue is null the initialize new RequestQueue
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        // Return RequestQueue
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request, Context mContext) {
        // Add the specified request to the request queue
        getRequestQueue(mContext).add(request);
        request.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    /// actual service calling method
    public void doServiceCall(Context context, final String url, final String pinModel,
                              final VolleyServiceResponseListener listener, final boolean showLoader) {
        if (showLoader) {
            showProgressDialog(context);
        }
        StringRequest sr = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(getClass().getSimpleName(), "_log : Response : " + response);
                        listener.onSuccessResponse(url, response);
                        if (showLoader) {
                            dismissProgressDialog();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(getClass().getSimpleName(), "_log : Error : " + error.toString());
                listener.onErrorResponse(url, error.toString());
                if (showLoader) {
                    dismissProgressDialog();
                }
            }
        }) ;
        Utilities.getInstance().addToRequestQueue(sr, context);
    }


    /**
     * It handles all service calls.
     *
     * @param mContext   - Context of calling activity
     * @param showLoader - Flag for to show progress loader
     * @param URL        - Service url
     * @param pinModel   - Input user model
     * @param listener   - Callback listeners
     */

    public void makeServiceCall(String URL, Context mContext, String pinModel,
                                VolleyServiceResponseListener listener, boolean showLoader) {
        try {
            if (ConnectionChecker.isOnline(mContext)) {
                doServiceCall(mContext, URL, pinModel, listener, showLoader);
            } else {
                if (!showLoader) {
                    listener.onNetWorkFailure(URL, "Internet error");
                } else {
                    dismissProgressDialog();
                    listener.onNetWorkFailure(URL, "Internet error");
//                    BlazeUtils.getInstance().showLongToast(mContext.getString(R.string
// .no_connection));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideKeyBoard(Context mContext) {
        try {
            View view = ((Activity) mContext).getCurrentFocus();
            if (view != null) {
                InputMethodManager inputManager = (InputMethodManager) mContext.getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e) {
            // Ignore exceptions if any
            Log.e("KeyBoardUtil", e.toString(), e);
        }
    }

    public void showKeyBoard(Context mContext) {
        try {
            InputMethodManager inputManager = (InputMethodManager) mContext.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            inputManager.toggleSoftInputFromWindow(
                    ((Activity) mContext).getCurrentFocus().getWindowToken(),
                    InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            // Ignore exceptions if any
            Log.e("KeyBoardUtil", e.toString(), e);
        }
    }


    public void setBackgroundDrawable(View view, Drawable drawable) {
        int sdk = Build.VERSION.SDK_INT;
        if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(drawable);
        } else {
            setBackground(view, drawable);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setBackground(View view, Drawable drawable) {
        view.setBackground(drawable);
    }


    public void printMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }


    public String optString(JSONObject json, String key) {
        if (json.isNull(key)) {
            return "";
        } else {
            return json.optString(key, "");
        }
    }

    public void hideKeyBoard(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(
                Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public byte[] getBytes(InputStream inputStream) throws IOException {
        int len;
        int size = 1024;
        byte[] buf;

        if (inputStream instanceof ByteArrayInputStream) {
            size = inputStream.available();
            buf = new byte[size];
            len = inputStream.read(buf, 0, size);
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            buf = new byte[size];
            while ((len = inputStream.read(buf, 0, size)) != -1) {
                bos.write(buf, 0, len);
            }
            buf = bos.toByteArray();
        }
        return buf;
    }

    public boolean isValidUrl(String url) {
        return url.matches(REGEX_URL);
    }


    public String getCurrentDateWithTimeAsString() {
        try {
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault());
            return outputFormat.format(Calendar.getInstance().getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public boolean isGooglePlayServicesAvailable(Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }


    public void clearApplicationData(Context context) {
        File cacheDirectory = context.getCacheDir();
        File applicationDirectory = new File(cacheDirectory.getParent());
        if (applicationDirectory.exists()) {
            String[] fileNames = applicationDirectory.list();
            for (String fileName : fileNames) {
                if (!fileName.equals("lib")) {
                    deleteFile(new File(applicationDirectory, fileName));
                }
            }
        }
    }

    public static boolean deleteFile(File file) {
        boolean deletedAll = true;
        if (file != null) {
            if (file.isDirectory()) {
                String[] children = file.list();
                for (String aChildren : children) {
                    deletedAll = deleteFile(new File(file, aChildren)) && deletedAll;
                }
            } else {
                deletedAll = file.delete();
            }
        }
        return deletedAll;
    }
}
