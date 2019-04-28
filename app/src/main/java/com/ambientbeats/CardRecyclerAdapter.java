package com.ambientbeats;

import android.os.AsyncTask;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.ValueBar;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.utils.Async;

import static io.particle.android.sdk.utils.Py.list;

public class CardRecyclerAdapter extends RecyclerView.Adapter<CardRecyclerAdapter.DeviceCardViewHolder> {
    private List<ParticleDevice> particleDevices;
    private ParticleCloud particleCloud = new ParticleCloud();
    private boolean anyDeviceOn = false;

    public static class DeviceCardViewHolder extends RecyclerView.ViewHolder {
        CardView deviceCardView;
        TextView deviceNameTextView;

        public Switch powerSwitch;

        public SeekBar animationSpeedSeekBar;
        public TextView animationSpeedTextView;
        public SeekBar audioSensitivitySeekBar;
        public TextView audioSensitivityTextView;

        public TableLayout deviceManagementTableLayout;
        public ImageButton deviceManagementShowHideButton;

        public ImageButton nextAnimationButton;
        public ImageButton previousAnimationButton;
        public ImageButton toggleAudioReactiveButton;
        public ImageButton cycleFrequencyButton;

        public Button resetButton;
        public Button enterSafeModeButton;

        int previousHue = 0;
        int previousBrightness = 0;
        int previousSaturation = 0;
        int previousColor = 0;

        ColorPicker colorPicker;
        ValueBar brightnessValueBar;

        private DeviceCardViewHolder(View v) {
            super(v);
            powerSwitch = v.findViewById(R.id.powerSwitch);
            deviceCardView = v.findViewById(R.id.card_view);
            deviceNameTextView = v.findViewById(R.id.device_name_textview);

            animationSpeedSeekBar = v.findViewById(R.id.animationSpeedSeekBar);
            animationSpeedTextView = v.findViewById(R.id.animationSpeedTextView);
            audioSensitivitySeekBar = v.findViewById(R.id.audioSensitivitySeekBar);
            audioSensitivityTextView = v.findViewById(R.id.audioSensitivityTextView);

            deviceManagementTableLayout = v.findViewById(R.id.deviceManagementTableLayout);
            deviceManagementShowHideButton = v.findViewById(R.id.settingsExpandButton);

            nextAnimationButton = v.findViewById(R.id.nextAnimationButton);
            previousAnimationButton = v.findViewById(R.id.previousAnimationButton);
            toggleAudioReactiveButton = v.findViewById(R.id.toggleAudioReactiveButton);
            cycleFrequencyButton = v.findViewById(R.id.cycleFrequencyButton);
            resetButton = v.findViewById(R.id.resetButton);
            enterSafeModeButton = v.findViewById(R.id.enterSafeModeButton);

            colorPicker = (ColorPicker) v.findViewById(R.id.picker);
            brightnessValueBar = (ValueBar) v.findViewById(R.id.valuebar);

            colorPicker.addValueBar(brightnessValueBar);
        }
    }

    CardRecyclerAdapter(List<ParticleDevice> particleDeviceList) {
        particleDevices = particleDeviceList;
    }

    @Override
    public DeviceCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_card_layout, parent, false);
        return new DeviceCardViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(DeviceCardViewHolder holder, int position) {
        holder.deviceNameTextView.setText(particleDevices.get(position).getName());
        holder.deviceManagementTableLayout.setVisibility(View.GONE);

        updatePowerStateFromCloudVariable(holder, position);
        updateAudioReactiveFromCloudVariable(holder, position);

        setAudioSensitivitySeekBarListener(holder, position);
        setAnimationSpeedSeekBarListener(holder, position);

        setSettingsExpandButtonListener(holder, position);

        setActionButtonListeners(holder, position);
        setPowerSwitchListener(holder, position);

        setColorPickerListener(holder, position);

        updateColorFromCloudVariable(holder, position);
        updateSeekBarFromCloudVariable(holder, position, "speed", holder.animationSpeedSeekBar, holder.animationSpeedTextView, "Animation Speed");
        updateSeekBarFromCloudVariable(holder, position, "sensitivity", holder.audioSensitivitySeekBar, holder.audioSensitivityTextView, "Audio Sensitivity");
    }

    private void setColorPickerListener(DeviceCardViewHolder holder, int position) {
        holder.colorPicker.setOnColorSelectedListener(new ColorPicker.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                updateDeviceColor(color, holder, position);
            }
        });
    }

    private void updateDeviceColor(int color, DeviceCardViewHolder holder, int position) {
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(color, hsl);
        int hue = mapRangeTo255(hsl[0], 0, 360);
        int saturation = mapRangeTo255(hsl[1], 0, 1);
        int brightness = 2 * mapRangeTo255(hsl[2], 0, 1) - 1;

        if(hue != holder.previousHue) {
            particleCloud.callCloudFunction(particleDevices.get(position), "set-hue", list(String.valueOf(hue)));
        }
        if(brightness != holder.previousBrightness) {
            particleCloud.callCloudFunction(particleDevices.get(position),"set-brightness", list(String.valueOf(brightness)));
        }
        if(saturation != holder.previousSaturation) {
            particleCloud.callCloudFunction(particleDevices.get(position),"set-saturation", list(String.valueOf(saturation)));
        }

        holder.previousHue = hue;
        holder.previousBrightness = brightness;
        holder.previousSaturation = saturation;
        holder.colorPicker.setOldCenterColor(holder.previousColor);
        holder.previousColor = color;
    }

    @Override
    public int getItemCount() {
        return particleDevices.size();
    }

    public boolean getAnyDeviceOn() {
        return anyDeviceOn;
    }

    private int mapRangeTo255(double number, double rangeStart, double rangeEnd) {
        return Math.round(Math.round(map(number, rangeStart, rangeEnd, 0, 255)));
    }

    private double map(double number, double fromRangeStart, double fromRangeEnd, double toRangeStart, double toRangeEnd) {
        return (number - fromRangeStart)/(fromRangeEnd - fromRangeStart) * (toRangeEnd - toRangeStart) + toRangeStart;
    }

    private void setAnimationSpeedSeekBarListener(DeviceCardViewHolder holder, int position) {
        holder.animationSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // updated continuously as the user slides the thumb
                holder.animationSpeedTextView.setText("Animation Speed: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // called when the user first touches the SeekBar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // called after the user finishes moving the SeekBar
                particleCloud.callCloudFunction(particleDevices.get(position),"set-speed", list(String.valueOf(seekBar.getProgress())));
            }
        });
    }

    private void setAudioSensitivitySeekBarListener(DeviceCardViewHolder holder, int position) {
        holder.audioSensitivitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // updated continuously as the user slides the thumb
                holder.audioSensitivityTextView.setText("Audio Sensitivity: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // called when the user first touches the SeekBar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // called after the user finishes moving the SeekBar
                particleCloud.callCloudFunction(particleDevices.get(position),"set-sensitivity", list(String.valueOf(seekBar.getProgress())));
            }
        });
    }

    private void updateSeekBarFromCloudVariable(DeviceCardViewHolder holder, int position, String variableName, SeekBar seekbar, TextView textView, String textViewText) {
        try {
            Async.executeAsync(particleDevices.get(position), new Async.ApiWork<ParticleDevice, Integer>() {

                public Integer callApi(ParticleDevice particleDevice) throws ParticleCloudException, IOException {
                    try {
                        return particleDevice.getIntVariable(variableName);
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
                    Log.e("UpdateSeekBar", "Something went wrong making an SDK call: ", e);
                }
            });
        } catch (ParticleCloudException e) {
            Log.e("UpdateSeekBar", "Something went wrong making an SDK call: ", e);
        }
    }


    private void updateColorFromCloudVariable(DeviceCardViewHolder holder, int position) {
        try {
            Async.executeAsync(particleDevices.get(position), new Async.ApiWork<ParticleDevice, Integer>() {

                public Integer callApi(ParticleDevice particleDevice) throws ParticleCloudException, IOException {
                    try {
                        int hue = particleDevice.getIntVariable("hue");
                        int brightness = particleDevice.getIntVariable("brightness");
                        int saturation = particleDevice.getIntVariable("saturation");

                        float newHue = (float) map(hue, 0, 255, 0, 360);
                        float newBrightness = (float) map((float)(brightness/2), 0, 255, 0, 1);
                        float newSaturation = (float) map(saturation, 0, 255, 0, 1);

                        float[] hsl = {newHue, newSaturation, newBrightness};
                        return ColorUtils.HSLToColor(hsl);
                    } catch (ParticleDevice.VariableDoesNotExistException e) {
                        e.printStackTrace();
                    }

                    return -1;
                }

                @Override
                public void onSuccess(Integer value) {
                    holder.colorPicker.setColor(value);
                }

                @Override
                public void onFailure(ParticleCloudException e) {
                    Log.e("UpdateSeekBar", "Something went wrong making an SDK call: ", e);
                }
            });
        } catch (ParticleCloudException e) {
            Log.e("UpdateSeekBar", "Something went wrong making an SDK call: ", e);
        }
    }

    private void updatePowerStateFromCloudVariable(DeviceCardViewHolder holder, int position) {
        // TODO: For whatever reason I can't convert the boolean cloud variable into an int, so I did dumb shit to make this "work" for now :|
        new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... strings) {
                boolean result = false;
                try {
                    result = new ParticleRequest().getVariable(strings[0], strings[1], strings[2]);
                } catch (IOException e) {
                    Log.e("UpdatePowerState", "Something went wrong making an SDK call");
                } catch (JSONException e) {
                    Log.e("UpdatePowerState", "Something went wrong making an SDK call");
                }
                return result;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                holder.powerSwitch.setChecked(result);
                if(result) {
                    anyDeviceOn = true;
                }
            }
        }.execute("33758e91bd70b14d10de5eab575bd65416fac6a2", particleDevices.get(position).getID(), "powered-on");
    }

    private void updateAudioReactiveFromCloudVariable(DeviceCardViewHolder holder, int position) {
        // TODO: For whatever reason I can't convert the boolean into an int, so I did dumb shit to make this "work" for now :|
        new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... strings) {
                boolean result = false;
                try {
                    result = new ParticleRequest().getVariable(strings[0], strings[1], strings[2]);
                } catch (IOException e) {
                    Log.e("UpdateAudioReactive", "Something went wrong making an SDK call: ");
                } catch (JSONException e) {
                    Log.e("UpdateAudioReactive", "Something went wrong making an SDK call: ");
                }
                return result;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if(result) {
                    holder.toggleAudioReactiveButton.setImageResource(R.drawable.ic_volume_up_black_24dp);
                } else {
                    holder.toggleAudioReactiveButton.setImageResource(R.drawable.ic_volume_off_black_24dp);
                }
            }
        }.execute("33758e91bd70b14d10de5eab575bd65416fac6a2", particleDevices.get(position).getID(), "audio-on");
    }

    private void setSettingsExpandButtonListener(DeviceCardViewHolder holder, int position) {
        holder.deviceManagementShowHideButton.setOnClickListener(v -> {
            if(holder.deviceManagementTableLayout.getVisibility() == View.VISIBLE) {
                holder.deviceManagementTableLayout.setVisibility(View.GONE);
                holder.deviceManagementShowHideButton.setImageResource(R.drawable.ic_expand_more_black_24dp);
            } else {
                holder.deviceManagementTableLayout.setVisibility(View.VISIBLE);
                holder.deviceManagementShowHideButton.setImageResource(R.drawable.ic_expand_less_black_24dp);
            }
        });
    }

    private void setActionButtonListeners(DeviceCardViewHolder holder, int position) {
        ParticleDevice particleDevice = particleDevices.get(position);
        holder.nextAnimationButton.setOnClickListener(v ->
                particleCloud.callCloudFunction(particleDevice, "change-animation", list("NEXT"))
        );

        holder.previousAnimationButton.setOnClickListener(v ->
                particleCloud.callCloudFunction(particleDevice, "change-animation", list("PREVIOUS"))
        );

        holder.toggleAudioReactiveButton.setOnClickListener(v -> {
            particleCloud.callCloudFunction(particleDevice, "toggle-audio-reactive", null);
            updateAudioReactiveFromCloudVariable(holder, position);
        });

        holder.cycleFrequencyButton.setOnClickListener(v ->
                particleCloud.callCloudFunction(particleDevice, "cycle-frequency", null)
        );

        holder.resetButton.setOnClickListener(v ->
                particleCloud.callCloudFunction(particleDevice, "reset-device", null)
        );

        holder.enterSafeModeButton.setOnClickListener(v ->
                particleCloud.callCloudFunction(particleDevice, "enter-safe-mode", null)
        );
    }

    private void setPowerSwitchListener(DeviceCardViewHolder holder, int position) {
        ParticleDevice particleDevice = particleDevices.get(position);
        holder.powerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.i("POWER", "Device: " + particleDevice.getName() + " is being set to: " + String.valueOf(isChecked));
            if (isChecked) {
                particleCloud.callCloudFunction(particleDevice, "power", list("ON"));
            } else {
                particleCloud.callCloudFunction(particleDevice, "power", list("OFF"));
            }
        });
    }

}