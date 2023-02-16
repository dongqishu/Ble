package com.plugin.ble;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.UUID;

/**
 * This class echoes a string called from JavaScript.
 */
public class Ble extends CordovaPlugin {

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothAdvertiser;
    private boolean isStart = false;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        }
        if (action.equals("startAdvertising")) {
            this.startAdvertising(args, callbackContext);
            return true;
        }
        if (action.equals("stopAdvertising")) {
            this.stopAdvertising(callbackContext);
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void startAdvertising(JSONArray args, CallbackContext callbackContext) {
        if (isStart) {
            return;
        }
        try {
            // 广播对象
            mBluetoothManager = (BluetoothManager) cordova.getContext().getSystemService(cordova.getContext().BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            mBluetoothAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

            // 广播设置
            AdvertiseSettings mAdvertiseSettings = new AdvertiseSettings.Builder()
                    // 设置广播模式，以控制广播的功率和延迟。
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                    // 发射功率级别
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                    // 不得超过180000毫秒。值为0将无时间限制广播。
                    .setTimeout(0)
                    // 设置是否可以连接
                    .setConnectable(true)
                    .build();

//            UUID BLE_ADV_SERVICE = UUID.fromString("1dc250bd-84b1-329f-149d-dd6fd3100f38");
//            short major = 29229;
//            short minor = (short) 43102;
//            byte txPower = (byte) -0x3b;

            UUID BLE_ADV_SERVICE = UUID.fromString(args.getString(0));
            short major = (short) args.get(1);
            short minor = (short) args.get(2);
            byte txPower = (byte) args.get(3);

            // 广播数据
            AdvertiseData mAdvertiseData = createIBeaconAdvertiseData(BLE_ADV_SERVICE, major, minor, txPower);

            // 开始广播
            mBluetoothAdvertiser.startAdvertising(mAdvertiseSettings, mAdvertiseData, new AdvertiseCallback() {
                public void onStartSuccess(android.bluetooth.le.AdvertiseSettings settingsInEffect) {
                    super.onStartSuccess(settingsInEffect);
                    callbackContext.success("广播成功");
                    isStart = true;
                }

                public void onStartFailure(int errorCode) {
                    super.onStartFailure(errorCode);
                    callbackContext.success("广播失败");
                }
            });
        } catch (SecurityException | JSONException e) {
            callbackContext.success("广播异常");
        }
    }

    private void stopAdvertising(CallbackContext callbackContext) {
        if (!isStart) {
            callbackContext.success("stoped");
            return;
        }
        mBluetoothAdvertiser.stopAdvertising(new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                callbackContext.success("stoped");
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
                callbackContext.error("stoped");
            }
        });
    }

    private AdvertiseData createIBeaconAdvertiseData(UUID proximityUuid, short major, short minor, byte txPower) {
        String[] uuidstr = proximityUuid.toString().replaceAll("-", "").toLowerCase().split("");
        byte[] uuidBytes = new byte[16];
        for (int i = 0, x = 0; i < uuidstr.length; x++) {
            uuidBytes[x] = (byte) ((Integer.parseInt(uuidstr[i++], 16) << 4) | Integer.parseInt(uuidstr[i++], 16));
        }
        byte[] majorBytes = { (byte) (major >> 8), (byte) (major & 0xff) };
        byte[] minorBytes = { (byte) (minor >> 8), (byte) (minor & 0xff) };
        byte[] mPowerBytes = { txPower };
        byte[] manufacturerData = new byte[0x17];
        byte[] flagibeacon = { 0x02, 0x15 };

        System.arraycopy(flagibeacon, 0x0, manufacturerData, 0x0, 0x2);
        System.arraycopy(uuidBytes, 0x0, manufacturerData, 0x2, 0x10);
        System.arraycopy(majorBytes, 0x0, manufacturerData, 0x12, 0x2);
        System.arraycopy(minorBytes, 0x0, manufacturerData, 0x14, 0x2);
        System.arraycopy(mPowerBytes, 0x0, manufacturerData, 0x16, 0x1);

        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        builder.addManufacturerData(0x004c, manufacturerData);

        AdvertiseData adv = builder.build();
        return adv;
    }
}
