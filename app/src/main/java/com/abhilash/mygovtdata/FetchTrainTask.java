package com.abhilash.mygovtdata;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by abhil on 09-01-2017.
 */

public class FetchTrainTask extends AsyncTask<String, Void, Void> {
    private final String LOG_TAG = FetchTrainTask.class.getSimpleName();

    @Override
    protected Void doInBackground(String... params) {

        if (params.length == 0) {
            return null;
        }

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String trainJsonString = null;
        List<Train> trains;

        try {
            final String BASE_URL = "https://data.gov.in/api/datastore/resource.json?resource_id=b46200c1-ca9a-4bbe-92f8-b5039cc25a12";
            final String API_KEY = "api-key";
            final String OFFSET = "offset";

            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(API_KEY, BuildConfig.OPEN_GOV_DATA_API_KEY)
                    .appendQueryParameter(OFFSET, params[0])
                    .build();

            URL url = new URL(builtUri.toString());

            Log.v(LOG_TAG, "Built URI " + builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                throw new Exception("No data received. Stream is null.");
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                throw new Exception("No data received. Buffer length was 0.");
            }

            trainJsonString = buffer.toString();

            Log.i("trainJsonString: ", trainJsonString);

            trains = new ArrayList<>();
            JSONObject trainJsonObject = new JSONObject(trainJsonString);
            JSONArray trainJsonArray = trainJsonObject.getJSONArray("records");
            for (int i = 0; i < trainJsonArray.length(); i++) {
                String trainNumber = trainJsonArray.getJSONObject(i)
                        .getString("Train No.").replace("'", "").trim();
                String trainName = trainJsonArray.getJSONObject(i)
                        .getString("train Name").trim();

                boolean trainExists = false;
                for (Train train : trains) {
                    if (train.getTrainNumber().equals(trainNumber)) {
                        trainExists = true;
                    }
                }
                if (!trainExists) {
                    trains.add(new Train(trainNumber, trainName));
                }
            }
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error ", exception);
            return null;
        } catch (IOException exception) {
            Log.e(LOG_TAG, "Error ", exception);
            return null;
        } catch (Exception exception) {
            Log.e(LOG_TAG, "Error ", exception);
            return null;
        }

        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error ", e);
                }
            }
        }
        return null;
    }
}
