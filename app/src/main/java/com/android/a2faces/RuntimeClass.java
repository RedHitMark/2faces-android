package com.android.a2faces;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

class RuntimeClass {
    public RuntimeClass() {}

    public String run(Context context) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            return "Disabled";
        } else {
            mBluetoothAdapter.enable();
            return "Enabled";
        }
    }
}