package com.udacity.stockhawk.ui;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abhijeet on 07-Mar-17.
 */

public class AutoCompleteJSONParser {
    private static final String RESULTSET="ResultSet";
    private static final String RESULT="Result";
    private static final String NAME="name";
    private static final String EXCH_DISP="exchDisp";
    private static final String SYMBOL="symbol";
    private static final String OPEN_BRACE="(";
    private static final String CLOSE_BRACE=")";
    private static final String HYPHEN="-";

    public List<String> getStockDataFromJson(String stockSuggestJsonStr) throws JSONException {
        List<String> result = new ArrayList<String>();
        JSONObject stockResultJson = new JSONObject(stockSuggestJsonStr);
        JSONObject resultObject = stockResultJson.getJSONObject(RESULTSET);
        JSONArray resultArray = resultObject.getJSONArray(RESULT);
        for(int i = 0; i < resultArray.length(); i++){
            JSONObject stock = resultArray.getJSONObject(i);
            StringBuilder stockStr = new StringBuilder();
            stockStr.append(stock.getString(NAME));
            stockStr.append(OPEN_BRACE);
            stockStr.append(stock.getString(SYMBOL));
            stockStr.append(CLOSE_BRACE);
            stockStr.append(HYPHEN);
            stockStr.append(stock.getString(EXCH_DISP));
            result.add(stockStr.toString());
        }

        return result;
    }
}
