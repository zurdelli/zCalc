package com.zurdelli.calculator;

import org.json.JSONException;
import org.json.JSONObject;

public class CurencyKiller {

    // Helpers
    private static JSONObject getObject(String tagName, JSONObject jObj) throws JSONException {
        JSONObject subObj = jObj.getJSONObject(tagName);
        return subObj;
    }

    private static String getString(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getString(tagName);
    }

    private static float getFloat(String tagName, JSONObject jObj) throws JSONException {
        return (float) jObj.getDouble(tagName);
    }

    private static int getInt(String tagName, JSONObject jObj) throws JSONException {
        return jObj.getInt(tagName);
    }






//    private void getCurrencies(String datas){
//        // We create out JSONObject from the data
//        try {
//            JSONObject jObj = new JSONObject(datas);
//            JSONObject ratesObj = getObject("rates");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
}
