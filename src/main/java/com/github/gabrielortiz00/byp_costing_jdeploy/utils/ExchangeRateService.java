package com.github.gabrielortiz00.byp_costing_jdeploy.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class ExchangeRateService {

    private static final String API_URL = "https://open.er-api.com/v6/latest/USD";

    //asynchronous call to get current exchange rate
    public static CompletableFuture<Double> getCurrentUsdToMxnRate() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                //open connection
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");


                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    //read response
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    //parse json response into string
                    String jsonResponse = response.toString();

                    int mxnIndex = jsonResponse.indexOf("\"MXN\":");
                    if (mxnIndex != -1) {

                        //extract MXN rate value
                        String rateString = jsonResponse.substring(mxnIndex + 6);

                        int commaIndex = rateString.indexOf(",");

                        if (commaIndex != -1) {
                            rateString = rateString.substring(0, commaIndex);
                        } else {
                            int bracketIndex = rateString.indexOf("}");
                            if (bracketIndex != -1) {
                                rateString = rateString.substring(0, bracketIndex);
                            }
                        }

                        //parse rate into double
                        return Double.parseDouble(rateString);
                    }

                    //rate parse failure
                    throw new IOException("Could not parse exchange rate from response");
                } else {
                    throw new IOException("API request failed");
                }
            } catch (Exception e) {
                System.err.println("Error fetching exchange rate");
                throw new RuntimeException("Failed to get exchange rate", e);
            }
        });
    }
}