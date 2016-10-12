package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.util.Log;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.exception.StockDoesNtExistException;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    private static String LOG_TAG = Utils.class.getSimpleName();

    public static boolean showPercent = true;

    final static public String NETWORK_STATE_CONNECTED = "CONNECTED";
    final static public String NETWORK_STATE_DISCONNECTED = "DISCONNECTED";
    final static public String RESULT_SUCCESS = "SUCCESS";
    final static public String RESULT_FAILURE = "FAILURE";
    // intent filter for updates to stock widgets.
    final static public String STOCK_WIDGET_INTENT_KEY = "STOCK_WIDGET_INTENT_KEY";

    public static ArrayList quoteJsonToContentVals(String JSON) throws StockDoesNtExistException{
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                //No information available for the stock
                if(count == 0)
                    throw new StockDoesNtExistException();

                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results")
                            .getJSONObject("quote");
                    batchOperations.add(buildBatchOperation(jsonObject));
                } else {
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            batchOperations.add(buildBatchOperation(jsonObject));
                        }
                    }
                }
            }
        } catch (JSONException | NumberFormatException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return batchOperations;
    }

    public static String truncateBidPrice(String bidPrice) {
        bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange) {
        String weight = change.substring(0, 1);
        String ampersand = "";
        if (isPercentChange) {
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }
        change = change.substring(1, change.length());
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
        change = String.format("%.2f", round);
        StringBuffer changeBuffer = new StringBuffer(change);
        changeBuffer.insert(0, weight);
        changeBuffer.append(ampersand);
        change = changeBuffer.toString();
        return change;
    }

    public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject) throws StockDoesNtExistException{
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);
        try {
            String change = jsonObject.getString("Change");
            builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
            builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
            builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                    jsonObject.getString("Change inPercent"), true));
            builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
            builder.withValue(QuoteColumns.ISCURRENT, 1);
            if (change.charAt(0) == '-') {
                builder.withValue(QuoteColumns.ISUP, 0);
            } else {
                builder.withValue(QuoteColumns.ISUP, 1);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "BuildBatchOperation failed: ", e);
        }catch (NumberFormatException e){
            throw new StockDoesNtExistException(e.getMessage());
        }
        return builder.build();
    }

    public static List<JSONObject> getQuoteFromResponse(String response){
        List<JSONObject> quote = new ArrayList<>();
        if(response==null || response.isEmpty()){
            return quote;
        }
        try {
            response = response.replace("finance_charts_json_callback( ", "");
            response = response.replace("\n", "");
            JSONObject answer = new JSONObject(response.substring(0, response.length() - 2));
            JSONArray quoteArray = answer.getJSONArray("series");
            quote = new ArrayList<>();
            for (int i = 0; i < quoteArray.length(); i++)
                quote.add(quoteArray.getJSONObject(i));


        } catch (JSONException e) {
            Log.e(LOG_TAG, "getQuoteFromResponse failed for response: "+response, e);
        }

        return quote;
    }
}
