package com.ambientbeats;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEventVisibility;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.Toaster;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CardRecyclerAdapter recyclerViewAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Switch powerAllSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cards);

        getDevices();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.actionBarMenu);
        View view = MenuItemCompat.getActionView(menuItem);
        powerAllSwitch = view.findViewById(R.id.switchForActionBar);

        powerAllSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.i("PowerAll", String.valueOf(isChecked));
            if (isChecked) {
                powerOnAllDevices();
            } else {
                powerOffAllDevices();
            }
        });
        return true;
    }

    private void powerOnAllDevices() {
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiProcedure<ParticleCloud>() {
            @Override
            public Void callApi(ParticleCloud particleCloud) throws ParticleCloudException {
                particleCloud.publishEvent("POWER_ON", "POWER_ON", ParticleEventVisibility.PRIVATE, 60);
                return null;
            }

            @Override
            public void onSuccess(Void voyd) {
                super.onSuccess(voyd);
                recyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(ParticleCloudException exception) {
                Log.e("PowerOnCall", "Something went wrong publishing an event: ", exception);
                Toaster.s(MainActivity.this, "Couldn't turn off some devices!");
            }
        });
    }

    private void powerOffAllDevices() {
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiProcedure<ParticleCloud>() {
            @Override
            public Void callApi(ParticleCloud particleCloud) throws ParticleCloudException {
                particleCloud.publishEvent("POWER_OFF", "POWER_OFF", ParticleEventVisibility.PRIVATE, 60);
                return null;
            }

            @Override
            public void onSuccess(Void voyd) {
                super.onSuccess(voyd);
                recyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(ParticleCloudException exception) {
                Log.e("PowerOffCall", "Something went wrong publishing an event: ", exception);
                Toaster.s(MainActivity.this, "Couldn't turn off some devices!");
            }
        });
    }

    private void getDevices() {
        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, List<ParticleDevice>>() {
            public List<ParticleDevice> callApi(ParticleCloud particleCloud) throws ParticleCloudException {
                return particleCloud.getDevices();
            }

            @Override
            public void onSuccess(List<ParticleDevice> devices) {
                List<ParticleDevice> particleDevices = new ArrayList<>();

                for (ParticleDevice device : devices) {
                    List<String> ignoreDevices = new ArrayList<>();
                    ignoreDevices.add("AudioServer");

                    if (ignoreDevices.contains(device.getName()) || !device.isConnected()) {
                        continue;
                    }

                    particleDevices.add(device);
                }

                particleDevices.sort(new Comparator<ParticleDevice>() {
                    public int compare(ParticleDevice o1, ParticleDevice o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });

                recyclerView = findViewById(R.id.recycler_view);

                // use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                recyclerView.setHasFixedSize(true);

                // use a linear layout manager
                layoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(layoutManager);

                // specify an adapter (see also next example)
                recyclerViewAdapter = new CardRecyclerAdapter(particleDevices);
                recyclerView.setAdapter(recyclerViewAdapter);


                powerAllSwitch.setChecked(recyclerViewAdapter.getAnyDeviceOn());
            }

            @Override
            public void onFailure(ParticleCloudException e) {
                Log.e("GetDevices", "Something went wrong getting particle devices: ", e);
                Toaster.s(MainActivity.this, "Couldn't access your devices!");
            }
        });
    }
}
