package com.ambientbeats;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ParticleFunction  extends AsyncTask<String, Void, String> {
    Context currentContext;

    public ParticleFunction(Context context) {
        currentContext = context;
    }

    @Override
    protected String doInBackground(String...device_and_function) {
        try {
            String functionArgs = "";
            if(device_and_function.length == 3) {
                functionArgs = "&args=" + device_and_function[2];
            }

            String urlParams = "access_token=33758e91bd70b14d10de5eab575bd65416fac6a2" + functionArgs;
            byte[] postData = urlParams.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;

            Log.i("Caling URL: ", "https://api.particle.io/v1/devices/" + device_and_function[0] + "/" + device_and_function[1] + "?access_token=33758e91bd70b14d10de5eab575bd65416fac6a2" + functionArgs);
            URL url = new URL("https://api.particle.io/v1/devices/" + device_and_function[0] + "/" + device_and_function[1]);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            try(DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write( postData );
            }

            if (connection.getResponseCode() != 200) {
                MainActivity.Toaster.get().showToast(currentContext, "Failed : HTTP error code : " + connection.getResponseCode() + " : " + connection.getResponseMessage() +"\n\n The device is probably offline. ", Toast.LENGTH_LONG);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));

            String output;
            Log.i("ParticleFunction", "Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                Log.i("output", output);
            }

            connection.disconnect();
        } catch (MalformedURLException e) {
            MainActivity.Toaster.get().showToast(currentContext, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT);
            e.printStackTrace();
        } catch (IOException e) {
            MainActivity.Toaster.get().showToast(currentContext, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT);
        }

        return "balls to you, sir";
    }
}
