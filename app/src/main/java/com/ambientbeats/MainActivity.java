package com.ambientbeats;

import android.os.Bundle;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.Toaster;

import static io.particle.android.sdk.utils.Py.list;

public class MainActivity extends AppCompatActivity {

    ParticleDevice currentDevice = null;
    List<ParticleDevice> particleDevices = new ArrayList<>();

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
                try {
                    powerOn(v);
                } catch (ParticleCloudException e) {
                    e.printStackTrace();
                }
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
                callCloudFunction("set-hue", list(String.valueOf(seekBar.getProgress())));
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
                callCloudFunction("set-brightness", list(String.valueOf(seekBar.getProgress())));
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
                callCloudFunction("set-saturation", list(String.valueOf(seekBar.getProgress())));
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
        callCloudFunction("next-animation");
    }

    private void previousPattern(View view) {
        callCloudFunction("previous-animation");
    }

    private void toggleAudioReactive(View view) {
        callCloudFunction("toggle-audio-reactive");
    }

    private void cycleFrequency(View view) {
        callCloudFunction("cycle-frequency");
    }

    private void powerOn(View view) throws ParticleCloudException {
        callCloudFunction("power-on");
    }

    private void powerOff(View view) {
        callCloudFunction("power-off");
    }

    private void enterSafeMode(View view) {
        callCloudFunction("enter-safe-mode");
    }

    private void resetDevice(View view) {
        callCloudFunction("reset-device");
    }

    private void callCloudFunction(String functionName) {
        callCloudFunction(functionName, null);
    }

    private void powerOnAllDevices() {
//        for(String device : deviceNameIds.keySet()) {
//            new ParticleFunction(getApplicationContext()).execute(deviceNameIds.get(device), "power-on");
//        }
    }

    private void powerOffAllDevices() {
//        for(String device : deviceNameIds.keySet()) {
//            new ParticleFunction(getApplicationContext()).execute(deviceNameIds.get(device), "power-off");
//        }
    }

    private void updateSeekBarFromCloudVariable(String variableName, SeekBar seekbar, TextView textView, String textViewText) {
        try {
            Async.executeAsync(currentDevice, new Async.ApiWork<ParticleDevice, Integer>() {

                public Integer callApi(ParticleDevice particleDevice) throws ParticleCloudException, IOException {
                    try {
                        return currentDevice.getIntVariable(variableName);
                    } catch (ParticleDevice.VariableDoesNotExistException e) {
                        e.printStackTrace();
                    }

                    return -1;
                }

                @Override
                public void onSuccess(Integer value) {
                    seekbar.setProgress(value);
                    textView.setText(textViewText + ": " + value);
                }

                @Override
                public void onFailure(ParticleCloudException e) {
                    Log.e("some tag", "Something went wrong making an SDK call: ", e);
                    Toaster.l(MainActivity.this, "Couldn't get cloud variable.");
                }
            });
        } catch (ParticleCloudException e) {
            Toaster.l(MainActivity.this, "A Particle exception occurred: " + e.getBestMessage());
            e.printStackTrace();
        }
    }

    private void callCloudFunction(String functionName, List<String> arguments) {
        try {
            Async.executeAsync(currentDevice, new Async.ApiWork<ParticleDevice, Integer>() {

                public Integer callApi(ParticleDevice particleDevice) throws ParticleCloudException, IOException {
                    try {
                        return currentDevice.callFunction(functionName, arguments);
                    } catch (ParticleDevice.FunctionDoesNotExistException e) {
                        e.printStackTrace();
                    }

                    return 0;
                }

                @Override
                public void onSuccess(Integer value) { }

                @Override
                public void onFailure(ParticleCloudException e) {
                    Log.e("some tag", "Something went wrong making an SDK call: ", e);
                    Toaster.l(MainActivity.this, "Couldn't call cloud function.");
                }
            });
        } catch (ParticleCloudException e) {
            Toaster.l(MainActivity.this, "A Particle exception occurred: " + e.getBestMessage());
            e.printStackTrace();
        }
    }

    private void getDevices() {
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, List<ParticleDevice>>() {
            public List<ParticleDevice> callApi(ParticleCloud particleDevice) throws ParticleCloudException, IOException {
                return ParticleCloudSDK.getCloud().getDevices();
            }

            @Override
            public void onSuccess(List<ParticleDevice> devices) {
                List<String> deviceNames = new ArrayList<>();
                for (ParticleDevice device : devices) {
                    List<String> ignoreDevices = new ArrayList<>(); //TODO: init this  with the names

                    if (ignoreDevices.contains(device.getName())) {
                        continue;
                    }

                    deviceNames.add(device.getName());
                    particleDevices.add(device);
                }

                Spinner spinner = findViewById(R.id.deviceSpinner);
                spinner.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, deviceNames));
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                        currentDevice = particleDevices.stream().filter(item -> item.getName().equals(spinner.getSelectedItem())).findFirst().get();

                        updateSeekBarFromCloudVariable("hue", findViewById(R.id.hueSeekBar), findViewById(R.id.hueTextView), "Hue");
                        updateSeekBarFromCloudVariable("brightness", findViewById(R.id.brightnessSeekBar), findViewById(R.id.brightnessTextView), "Brightness");
                        updateSeekBarFromCloudVariable("saturation", findViewById(R.id.saturationSeekBar), findViewById(R.id.saturationTextView), "Saturation");
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        // TODO Auto-generated method stub
                    }
                });
            }

            @Override
            public void onFailure(ParticleCloudException e) {
                Log.e("some tag", "Something went wrong making an SDK call: ", e);
                Toaster.s(MainActivity.this, "Couldn't access your devices!");
            }
        });
    }
}
