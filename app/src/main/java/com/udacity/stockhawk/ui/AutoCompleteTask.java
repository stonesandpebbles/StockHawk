package com.udacity.stockhawk.ui;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.udacity.stockhawk.R;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.github.mikephil.charting.charts.Chart.LOG_TAG;

/**
 * Created by Abhijeet on 06-Mar-17.
 */

public class AutoCompleteTask extends AsyncTask<String, Void, List<String>> {
    private AutoCompleteTextView stockSuggestionTextView;
    private ArrayAdapter<String> suggestAdapter;
    private Context mContext;
    //private static final String YAHOO_BASE_URL = "https://query.yahooapis.com/v1/public/yql";
    private static final String SQL_QUERY = "select * from pm.finance.autocomplete where auto_complete_str=";
    private static final String QUOTE="'";
    private static final String QUERY_PARAM = "q";
    private static final String FORMAT_PARAM = "format";
    private static final String JSON = "xml";
    private static final String ENV = "env";
    private static final String ENV_VAL = "store://datatables.org/alltableswithkeys";
    private static final String CALLBACK_PARM = "callback";
    private static final String YAHOO_BASE_URL = "http://d.yimg.com/aq/autoc";
    private static final String QUERY = "query";
    private static final String REGION_PARAM = "region";
    private static final String LANGUAGE_PARAM = "lang";
    private static final String US = "US";

    public AutoCompleteTask(AutoCompleteTextView stockEditText, Context context){
        this.stockSuggestionTextView = stockEditText;
        this.mContext = context;
    }
    @Override
    protected List<String> doInBackground(String... params) {
        String input = (String) params[0];
        List<String> suggestStockList = new ArrayList<>();
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String stockSuggestJsonStr = null;

        try {
            Uri builtUri = Uri.parse(YAHOO_BASE_URL).buildUpon().appendQueryParameter(QUERY, input)
                    .appendQueryParameter(REGION_PARAM, "")
                    .appendQueryParameter(LANGUAGE_PARAM, "")
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            //urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.connect();
            if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST){
                InputStream errorStream = urlConnection.getErrorStream();
                reader = new BufferedReader(new InputStreamReader(errorStream));
                String line;
                Timber.d("Bad Request:\n");
                while ((line = reader.readLine()) != null) {
                    Timber.d(line+"\n");
                }
            }
            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            stockSuggestJsonStr = buffer.toString();
            suggestStockList = new AutoCompleteJSONParser().getStockDataFromJson(stockSuggestJsonStr);
        } catch (IOException e) {
            Timber.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the stock data, there's no point in attempting
            // to parse it.
        }
        catch (JSONException e) {
            Timber.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return suggestStockList;
    }

    @Override
    protected void onPostExecute(List<String> o) {
        super.onPostExecute(o);
        suggestAdapter = new ArrayAdapter<String>(mContext, R.layout.support_simple_spinner_dropdown_item, o);
        stockSuggestionTextView.setAdapter(suggestAdapter);
        suggestAdapter.notifyDataSetChanged();

    }

}
