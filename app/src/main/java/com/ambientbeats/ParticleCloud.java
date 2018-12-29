package com.ambientbeats;

import android.util.Log;

import java.io.IOException;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException;
import io.particle.android.sdk.utils.Async;

class ParticleCloud {
    void callCloudFunction(ParticleDevice device, String functionName, List<String> arguments) {
        try {
            Async.executeAsync(device, new Async.ApiWork<ParticleDevice, Integer>() {

                public Integer callApi(ParticleDevice particleDevice) throws ParticleCloudException, IOException {
                    try {
                        Log.i("CallCloudFunction", "Calling " + functionName + " on " + device.getName());
                        return device.callFunction(functionName, arguments);
                    } catch (ParticleDevice.FunctionDoesNotExistException e) {
                        Log.e("CloudFunction", "Cloud function does not exist:  ", e);
                        e.printStackTrace();
                    }

                    return 0;
                }

                @Override
                public void onSuccess(Integer value) {

                }

                @Override
                public void onFailure(ParticleCloudException e) {
                    Log.e("CloudFunction", "Something went wrong calling a cloud function: ", e);
                }
            });
        } catch (ParticleCloudException e) {
            Log.e("CloudFunction", "Something went wrong calling a cloud function: ", e);
        }
    }
}
