package de.simon_dankelmann.apps.ledcontroller;

/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

/** For a good article on bluetooth BLE management
 * https://medium.com/@martijn.van.welie/making-android-ble-work-part-2-47a3cdaade07
 */

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BlunoBLE extends Service {
    private final static String TAG = BlunoBLE.class.getSimpleName();

    public final static UUID UUID_BLUNO_SERIAL_SERVICE =
            UUID.fromString("0000dfb0-0000-1000-8000-00805f9b34fb");

    public final static UUID UUID_BLUNO_SERIAL_CHARACTERISTIC =
            UUID.fromString("0000dfb1-0000-1000-8000-00805f9b34fb");

    //private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    //private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private BluetoothDevice mBluetoothDevice;

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public final static String ACTION_DEVICE_FOUND =
            "com.example.bluetooth.le.ACTION_DEVICE_FOUND";
    public final static String ACTION_DEVICE_NOT_FOUND =
            "com.example.bluetooth.le.ACTION_DEVICE_NOT_FOUND";
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";


    public int getConnectionState() {
        return mConnectionState;
    }

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                close();
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                /*setCharacteristicNotification(
                        mBluetoothGatt.getService(UUID_BLUNO_SERIAL_SERVICE).getCharacteristic(UUID_BLUNO_SERIAL_CHARACTERISTIC),
                        true);*/
                if (!writeThread.isAlive())
                    writeThread.start();
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.e(TAG, "onServicesDiscovered received: " + status);
                gatt.disconnect();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //super.onCharacteristicWrite(gatt, characteristic, status);
            synchronized (blockWrite) {
                blockWrite = false;
            }

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, String.format(Locale.ENGLISH,"ERROR: Write failed for characteristic: %s, status %d", characteristic.getUuid(), status));
            } else {
                Log.i(TAG, String.format(Locale.ENGLISH, "Write done %s.", new String(characteristic.getValue()), characteristic.getUuid()));
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            //super.onCharacteristicRead(gatt, characteristic, status);

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, String.format(Locale.ENGLISH,"ERROR: Read failed for characteristic: %s, status %d", characteristic.getUuid(), status));
            } else {
                final byte[] bytes = new byte[characteristic.getValue().length];
                System.arraycopy(characteristic.getValue(), 0, bytes, 0, characteristic.getValue().length );
                Log.e(TAG, String.format(Locale.ENGLISH, "Read done %s.", new String(bytes)));
            }
            synchronized (blockWrite) {
                blockWrite = false;
            }

            if ( status == BluetoothGatt.GATT_SUCCESS) {
                //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            final byte[] bytes = new byte[characteristic.getValue().length];
            System.arraycopy(characteristic.getValue(), 0, bytes, 0, characteristic.getValue().length );
            Log.e(TAG, "onCharacteristicChanged(): " + new String(bytes));
            //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);

        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            intent.putExtra(EXTRA_DATA, new String(data));
        }

        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        BlunoBLE getService() {
            return BlunoBLE.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        BluetoothManager mBluetoothManager = null;
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        commandQueue = new LinkedBlockingQueue<byte[]>();
        delayQueue = new LinkedBlockingQueue<Integer>();

        return true;
    }

    public void startBlunoScan() {

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Log.e(TAG, "BluetoothAdapter not initialized or unspecified address.");
            scanCallback.onScanFailed(-1);
            return;
        }

        BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();

        List<ScanFilter> filters = new ArrayList<ScanFilter>();
        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(UUID_BLUNO_SERIAL_SERVICE))
                .build();
        filters.add(filter);

        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH)//ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
                .setReportDelay(0L)
                .build();

        if (scanner != null) {
            //scanner.stopScan(scanCallback);
            scanner.startScan(filters, scanSettings, scanCallback);
            Log.e(TAG, "Scan started");
        }  else {
            Log.e(TAG, "could not get scanner object");
        }
    }

    // The scanner will now start looking for devices that match your filters and when it finds one, it will call your scanCallback
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            Log.e(TAG, "Device found: " + device.getName() + " " + device.getAddress() );
            mBluetoothDevice = device;
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
            broadcastUpdate(ACTION_DEVICE_FOUND);
            connect();

        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "Scan Failed");
            if (mBluetoothAdapter != null && mBluetoothAdapter.getBluetoothLeScanner() != null)
                mBluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
            broadcastUpdate(ACTION_DEVICE_NOT_FOUND);
        }


    };

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * //@param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect() {
        if (mBluetoothAdapter == null || mBluetoothDevice == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
//        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
//                && mBluetoothGatt != null) {
//            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
//            if (mBluetoothGatt.connect()) {
//                mConnectionState = STATE_CONNECTING;
//                return true;
//            } else {
//                return false;
//            }
//        }

        //final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mBluetoothDeviceAddress);
        if (mBluetoothDevice == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = mBluetoothDevice.connectGatt(this, false, mGattCallback, TRANSPORT_LE);
        Log.e(TAG, "Creating a new connection.");
        //mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        commandQueue.clear();
        delayQueue.clear();
        try {
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "Bluetooth is off or Gatt not connected");
            return;
        }
        mBluetoothGatt.disconnect();
        //mBluetoothAdapter.disable();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothGatt set to null");
            return;
        }
        Log.e(TAG, "Closing connection");
        mBluetoothGatt.close();
        mSerialCharacteristic = null;
        mBluetoothGatt = null;
        mBluetoothDevice = null;
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    private BlockingQueue<byte[]> commandQueue;
    private BlockingQueue<Integer> delayQueue;

    private Boolean blockWrite = false;
    private Boolean blockRead = true;
    private BluetoothGattCharacteristic mSerialCharacteristic;

    public boolean serialWrite(byte[] bytes) {
        return serialWrite(bytes, 50);
    }

    public boolean serialWrite(byte[] bytes, int delay) {

        if (mBluetoothGatt == null) {
            Log.e(TAG, "Gatt not connected");
            return false;
        }

        BluetoothGattService srvs = mBluetoothGatt.getService(UUID_BLUNO_SERIAL_SERVICE);
        if (srvs == null) {
            Log.e(TAG, "Serial service not found");
            return false;
        }

        final BluetoothGattCharacteristic characteristic = srvs.getCharacteristic(UUID_BLUNO_SERIAL_CHARACTERISTIC);
        if (characteristic == null) {
            Log.e(TAG, "Serial characteristic not found");
            return false;
        }

        try {
            commandQueue.put(bytes);
            delayQueue.put(delay);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }

    private BluetoothGattCharacteristic getSerialCharacteristic() {
        if (mSerialCharacteristic == null)
            try {
                mSerialCharacteristic = mBluetoothGatt.getService(UUID_BLUNO_SERIAL_SERVICE).getCharacteristic(UUID_BLUNO_SERIAL_CHARACTERISTIC);
            } catch (Exception e){
                return null;
            }
        return mSerialCharacteristic;
    }

    private Thread writeThread = new Thread() {
        @Override
        public void run() {
            int sleptTimeout = 0;
            boolean doSleep = false;

            while(true) {
                synchronized (blockWrite) {
                    doSleep = blockWrite;
                    sleptTimeout = doSleep ? sleptTimeout + 1 : 0;
                    if (sleptTimeout > 100) { //watchdog 500 ms
                        Log.e(TAG, String.format("Watchdog timeout"));
                        commandQueue.clear();
                        delayQueue.clear();
                        sleptTimeout = 0;
                        blockWrite = false;
                        continue;
                    }
                }

                BluetoothGattCharacteristic characteristic = getSerialCharacteristic();

                try {
                    if (doSleep) {
                        Thread.sleep(5);
                        continue;
                    }

                    byte[] bytes = commandQueue.take();
                    int delay = delayQueue.poll(10, TimeUnit.MILLISECONDS);
                    characteristic.setValue(bytes);
                    if (!mBluetoothGatt.writeCharacteristic(characteristic)) {
                        Log.e(TAG, String.format("ERROR: writeCharacteristic failed for characteristic: %s", characteristic.getUuid()));
                    } else {
                        synchronized (blockWrite) {
                            blockWrite = true;
                        }
                    }

                    Thread.sleep(delay);
                } catch (Exception e) {
                    Log.e(TAG, String.format("Write thread exception value: %d %d", commandQueue.size(), delayQueue.size() ) );
                    e.printStackTrace();
                    commandQueue.clear();
                    delayQueue.clear();
                    blockWrite = false;
                }
            }
        }
    };

    public void clearCommandQuery() {
        commandQueue.clear();
        delayQueue.clear();
    }

    public void setLength(int len) {
        String txt = "l-"+ len + "\n";
        Log.d(TAG, "setLength(): " + txt);
        serialWrite(txt.getBytes(StandardCharsets.UTF_8),50);
    }

    public void setBrightness(int b) {
        String txt = "b-"+ b + "\n";
        Log.d(TAG, "setBrightness(): " + txt);
        serialWrite(txt.getBytes(StandardCharsets.UTF_8),50);
    }

    public void setPixelColor(int p, int c) {
        String txt = "p-"+ p+"-"+Color.red(c)+"-"+Color.green(c)+"-"+Color.blue(c) + "\n";
        Log.d(TAG, "setPixelColor(): " + txt);
        serialWrite(txt.getBytes(StandardCharsets.UTF_8),50);
    }

    public void memSetPixelColor(int p, int c) {
        String txt = "m-"+ p+"-"+Color.red(c)+"-"+Color.green(c)+"-"+Color.blue(c) + "\n";
        Log.d(TAG, "memSetPixelColor(): " + txt);
        serialWrite(txt.getBytes(StandardCharsets.UTF_8),10);
    }

    public void clearPixels() {
        String txt = "c\n";
        Log.d(TAG, "clearPixels(): " + txt);
        serialWrite(txt.getBytes(StandardCharsets.UTF_8),10);
    }


    public void showPixels() {
        String txt = "s\n";
        Log.d(TAG, "showPixels(): " + txt);
        serialWrite(txt.getBytes(StandardCharsets.UTF_8),50);
    }


    public void fillColor(int c) {
        String txt = "f-"+ Color.red(c)+"-"+Color.green(c)+"-"+Color.blue(c) + "\n";
        Log.d(TAG, "fillColor(): " + txt);
        serialWrite(txt.getBytes(StandardCharsets.UTF_8),50);
    }

    public void setPresetMode(int preset) {
        String txt = "i-"+ preset + "\n";
        Log.d(TAG, "setPresetMode(): " + txt);
        serialWrite(txt.getBytes(StandardCharsets.UTF_8),50);
    }

    public BluetoothDevice getDevice() {
        return mBluetoothDevice;
    }

}
