/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.theisofall.barcode.controller;

import android.content.Context;
import android.util.Log;

import com.theisofall.barcode.activities.MainActivity;
import com.theisofall.barcode.listener.VolleyServiceResponseListener;
import com.theisofall.barcode.models.ProductModel;
import com.theisofall.barcode.utils.Configs;
import com.theisofall.barcode.utils.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tajmulalam on 1/11/18.
 */

public class CommonController implements VolleyServiceResponseListener {
    private Context mContext;
    public static final int PRODUCT_INFO_REQUEST = 1;
    private int req = -1;
    private String barcode = "";
    private String productUrl = "";

    public CommonController(Context mContext) {
        this.mContext = mContext;
    }

    public void callApi(int req, String barcode, boolean showLoader) {
        this.req = req;
        this.barcode = barcode;
        this.productUrl = Configs.URL_URL_GET_PRODUCT_INFO_BY_BARCODE + barcode + Configs.API_KEY;
        switch (req) {
            case PRODUCT_INFO_REQUEST:
                Utilities.getInstance().makeServiceCall(productUrl, mContext, barcode,
                        this, showLoader);
                break;

        }
    }

    @Override
    public void onNetWorkFailure(String url, String msg) {
        url = url.replace(productUrl, Configs.URL_URL_GET_PRODUCT_INFO_BY_BARCODE);
        switch (url) {
            case Configs.URL_URL_GET_PRODUCT_INFO_BY_BARCODE:
                ((MainActivity) mContext).showErrorMsg(msg);
                break;


        }
    }

    @Override
    public void onSuccessResponse(String url, String response) {
        Log.e("response", response);
        JSONObject mainJsonObj = null;
        url = url.replace(productUrl, Configs.URL_URL_GET_PRODUCT_INFO_BY_BARCODE);
        try {
            mainJsonObj = new JSONObject(response);
            switch (url) {
                case Configs.URL_URL_GET_PRODUCT_INFO_BY_BARCODE:
                    JSONArray products = mainJsonObj.getJSONArray("products");
                    if (products.length() > 0) {
//                        for (int i=0;i<products.length();i++){
                        JSONObject singleProduct = products.getJSONObject(0);
                        ProductModel productModel = new ProductModel();
                        productModel.setBarcodeNumber(singleProduct.optString("barcode_number", ""));
                        productModel.setBarcodeType(singleProduct.optString("barcode_type", ""));
                        productModel.setBarcodeFormats(singleProduct.optString("barcode_formats", ""));
                        productModel.setMpn(singleProduct.optString("mpn", ""));
                        productModel.setModel(singleProduct.optString("model", ""));
                        productModel.setAsin(singleProduct.optString("asin", ""));
                        productModel.setProductName(singleProduct.optString("product_name", ""));
                        productModel.setTitle(singleProduct.optString("title", ""));
                        productModel.setCategory(singleProduct.optString("category", ""));
                        productModel.setManufacturer(singleProduct.optString("manufacturer", ""));
                        productModel.setBrand(singleProduct.optString("brand", ""));
                        productModel.setLabel(singleProduct.optString("label", ""));
                        productModel.setAuthor(singleProduct.optString("author", ""));
                        productModel.setPublisher(singleProduct.optString("publisher", ""));
                        productModel.setArtist(singleProduct.optString("artist", ""));
                        productModel.setActor(singleProduct.optString("actor", ""));
                        productModel.setDirector(singleProduct.optString("director", ""));
                        productModel.setStudio(singleProduct.optString("studio", ""));
                        productModel.setGenre(singleProduct.optString("genre", ""));
                        productModel.setAudienceRating(singleProduct.optString("audience_rating", ""));
                        productModel.setIngredients(singleProduct.optString("ingredients", ""));
                        productModel.setNutritionFacts(singleProduct.optString("nutrition_facts", ""));
                        productModel.setColor(singleProduct.optString("color", ""));
                        productModel.setFormat(singleProduct.optString("format", ""));
                        productModel.setPackageQuantity(singleProduct.optString("package_quantity", ""));
                        productModel.setSize(singleProduct.optString("size", ""));
                        productModel.setLength(singleProduct.optString("length", ""));
                        productModel.setWidth(singleProduct.optString("width", ""));
                        productModel.setHeight(singleProduct.optString("height", ""));
                        productModel.setWeight(singleProduct.optString("weight", ""));
                        productModel.setReleaseDate(singleProduct.optString("release_date", ""));
                        productModel.setDescription(singleProduct.optString("description", ""));
                        JSONArray photoArray = singleProduct.getJSONArray("images");
                        if (photoArray.length() > 0) {
                            productModel.setProductPhoto(photoArray.getString(0));
                        } else {
                            productModel.setProductPhoto("");
                        }
//                        }
                    ((MainActivity) mContext).showSuccessMsg(productModel);
                    }else {

                    ((MainActivity) mContext).showErrorMsg("No Product Found");
                    }
                    break;
            }
        } catch (
                JSONException e)

        {
            e.printStackTrace();
        }

    }

    @Override
    public void onErrorResponse(String url, String response) {
        url = url.replace(productUrl, Configs.URL_URL_GET_PRODUCT_INFO_BY_BARCODE);
        switch (url) {
            case Configs.URL_URL_GET_PRODUCT_INFO_BY_BARCODE:
                ((MainActivity) mContext).showErrorMsg(response);
                break;

        }
    }
}