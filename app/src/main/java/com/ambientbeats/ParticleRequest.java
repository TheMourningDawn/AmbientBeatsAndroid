package com.ambientbeats;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class ParticleRequest {

    boolean getVariable(String apiKey, String deviceId, String variableName) throws IOException, JSONException {
        Log.i("Caling URL: ", "https://api.particle.io/v1/devices/" + deviceId + "/" + variableName + "?access_token=" + apiKey);
        URL url = new URL("https://api.particle.io/v1/devices/" + deviceId + "/" + variableName + "?access_token="+ apiKey);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        if (connection.getResponseCode() != 200) {
            Log.e("GetCloudVariable",  "Failed : HTTP error code " + connection.getResponseCode() + " : " + connection.getResponseMessage() +"\n The device is probably offline.");
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));

        String output;
        while ((output = br.readLine()) != null) {
            JSONObject reader = null;
            try {
                reader = new JSONObject(output);
            } catch (JSONException e) {
                Log.e("GetCloudVariable",  "Failed parsing JSON: ", e);
            }
            return reader.getBoolean("result");
        }

        connection.disconnect();

        return false;
    }
}
