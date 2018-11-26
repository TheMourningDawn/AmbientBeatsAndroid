package com.ambientbeats;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Telephony;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    String currentDeviceId = "";
    Map<String, String> deviceNameIds = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getDevices();

        final Button nextAnimationButton = (Button) findViewById(R.id.nextAnimationButton);
        nextAnimationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                nextPattern(v);
            }
        });

        final Button previousAnimationButton = (Button) findViewById(R.id.previousAnimationButton);
        previousAnimationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                previousPattern(v);
            }
        });

        final Button toggleAudioReactiveButton = (Button) findViewById(R.id.toggleAudioButton);
        toggleAudioReactiveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                toggleAudioReactive(v);
            }
        });

        final Button powerOnButton = (Button) findViewById(R.id.powerOnButton);
        powerOnButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                powerOn(v);
            }
        });

        final Button powerOffButton = (Button) findViewById(R.id.powerOffButton);
        powerOffButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                powerOff(v);
            }
        });

        final Button resetButton = (Button) findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resetDevice(v);
            }
        });

        final Button enterSafeModeButton = (Button) findViewById(R.id.safeModeButton);
        enterSafeModeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                enterSafeMode(v);
            }
        });

        final Button cycleFrequencyButton = (Button) findViewById(R.id.cycleFrequencyButton);
        cycleFrequencyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cycleFrequency(v);
            }
        });

        final SeekBar hueSeekBar = (SeekBar) findViewById(R.id.hueSeekBar);
        final TextView hueTextView = (TextView) findViewById(R.id.hueTextView);
        hueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // updated continuously as the user slides the thumb
                hueTextView.setText("Hue: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // called when the user first touches the SeekBar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // called after the user finishes moving the SeekBar
                new ParticleFunction(getApplicationContext()).execute(currentDeviceId, "set-hue", String.valueOf(seekBar.getProgress()));
            }
        });

        final SeekBar brightnessSeekBar = (SeekBar) findViewById(R.id.brightnessSeekBar);
        final TextView brightnessTextView = (TextView) findViewById(R.id.brightnessTextView);
        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // updated continuously as the user slides the thumb
                brightnessTextView.setText("Brightness: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // called when the user first touches the SeekBar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // called after the user finishes moving the SeekBar
                new ParticleFunction(getApplicationContext()).execute(currentDeviceId, "set-brightness", String.valueOf(seekBar.getProgress()));
            }
        });

        final SeekBar saturationSeekBar = (SeekBar) findViewById(R.id.saturationSeekBar);
        final TextView saturationTextView = (TextView) findViewById(R.id.saturationTextView);
        saturationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // updated continuously as the user slides the thumb
                saturationTextView.setText("Saturation: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // called when the user first touches the SeekBar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // called after the user finishes moving the SeekBar
                new ParticleFunction(getApplicationContext()).execute(currentDeviceId, "set-saturation", String.valueOf(seekBar.getProgress()));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.actionBarMenu);
        View view = MenuItemCompat.getActionView(menuItem);
        Switch powerAllSwitch = (Switch) view.findViewById(R.id.switchForActionBar);
        powerAllSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("PowerAll", String.valueOf(isChecked));
                if (isChecked) {
                    powerOnAllDevices();
                } else {
                    powerOffAllDevices();
                }
            }
        });
        return true;
    }

    private void nextPattern(View view) {
        new ParticleFunction(getApplicationContext()).execute(currentDeviceId, "next-animation");
    }

    private void previousPattern(View view) {
        new ParticleFunction(getApplicationContext()).execute(currentDeviceId, "previous-animation");
    }

    private void toggleAudioReactive(View view) {
        new ParticleFunction(getApplicationContext()).execute(currentDeviceId, "toggle-audio-reactive");
    }

    private void cycleFrequency(View view) {
        new ParticleFunction(getApplicationContext()).execute(currentDeviceId, "cycle-frequency");
    }

    private void powerOn(View view) {
        new ParticleFunction(getApplicationContext()).execute(currentDeviceId, "power-on");
    }

    private void powerOff(View view) {
        new ParticleFunction(getApplicationContext()).execute(currentDeviceId, "power-off");
    }

    private void enterSafeMode(View view) {
        new ParticleFunction(getApplicationContext()).execute(currentDeviceId, "enter-safe-mode");
    }

    private void resetDevice(View view) {
        new ParticleFunction(getApplicationContext()).execute(currentDeviceId, "reset-device");
    }

    private void getDevices() {
        new ParticleGetDevices().execute("This takes no params...");
    }

    private void powerOnAllDevices() {
        for(String device : deviceNameIds.keySet()) {
            new ParticleFunction(getApplicationContext()).execute(deviceNameIds.get(device), "power-on");
        }
    }

    private void powerOffAllDevices() {
        for(String device : deviceNameIds.keySet()) {
            new ParticleFunction(getApplicationContext()).execute(deviceNameIds.get(device), "power-off");
        }
    }

    public class ParticleGetDevices extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String...urls) {
            try {
                URL url = new URL("https://api.particle.io/v1/devices?access_token=33758e91bd70b14d10de5eab575bd65416fac6a2");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() != 200) {
                    Log.i("Response message: ", connection.getResponseMessage());
                    MainActivity.Toaster.get().showToast(getApplicationContext(), "Failed : HTTP error code : " + connection.getResponseCode() + " : " + connection.getResponseMessage() +"\n\n The device is probably offline. ", Toast.LENGTH_LONG);
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (connection.getInputStream())));

                String output = null;
                String toReturn = null;
                while ((output = br.readLine()) != null) {
                    toReturn = output;
                }

                connection.disconnect();
                return toReturn;

            } catch (MalformedURLException e) {
                MainActivity.Toaster.get().showToast(getApplicationContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT);
            } catch (IOException e) {
                MainActivity.Toaster.get().showToast(getApplicationContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT);
            }

            return "balls to you, sir";
        }

        @Override
        protected void onPostExecute(String result) {
            Spinner spinner = (Spinner) findViewById(R.id.deviceSpinner);
            JSONArray devices = null;
            try {
                devices = new JSONArray(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            java.util.ArrayList<String> deviceNames = new java.util.ArrayList<>();

            try {
                //Iterate the jsonArray and print the info of JSONObjects
                for(int i=0; i < devices.length(); i++){
                    JSONObject jsonObject = devices.getJSONObject(i);

                    String name = jsonObject.optString("name");

                    if(name.equals("AudioServer") || name.equals("Bedroom") || name.equals("Remote")) {
                        continue;
                    }

                    deviceNames.add(name);
                    deviceNameIds.put(name, jsonObject.optString("id"));
                }

                spinner.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, deviceNames));
            } catch (JSONException e) {e.printStackTrace();}

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                    Spinner localSpinner = (Spinner) findViewById(R.id.deviceSpinner);
                    currentDeviceId = deviceNameIds.get(localSpinner.getItemAtPosition(localSpinner.getSelectedItemPosition()).toString());
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    // TODO Auto-generated method stub
                }
            });

        }
    }

    public enum Toaster {
        INSTANCE;

        private final Handler handler = new Handler(Looper.getMainLooper());

        public void showToast(final Context context, final String message, final int length) {
            handler.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, message, length).show();
                        }
                    }
            );
        }

        public static Toaster get() {
            return INSTANCE;
        }
    }
}
