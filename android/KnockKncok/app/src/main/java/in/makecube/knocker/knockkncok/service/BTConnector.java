package in.makecube.knocker.knockkncok.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class BTConnector extends Service {
    public static final String CONNECTABLE_DEVICE_NAME = "KNOCKER_BT";
    public static final String BROADCAST_MESSAGE_STATUS = "in.makecube.knocker.BLUETOOTH_STATE";
    public static final String BROADCAST_MESSAGE_MESSAGE = "in.makecube.knocker.KNOCK_MESSAGE";
    public static final String BROADCAST_MESSAGE_CALL = "in.makecube.knocker.KNOCK_CALL";
    public static final int BT_STATUS_READY = 0;
    public static final int BT_STATUS_CONNECTING = 1;
    public static final int BT_STATUS_CONNECTED = 2;
    public static final int BT_STATUS_DISCONNECTED = 3;
    private final String TAG = BTConnector.class.getSimpleName();
    BroadcastReceiver callerReceiver;
    private CompositeDisposable disposable = new CompositeDisposable();
    private BluetoothAdapter btAdapter;
    private BluetoothSocket deviceSocket;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d(TAG, "Service onStart");

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        disposable.add(

                Observable.just(CONNECTABLE_DEVICE_NAME)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(name -> sendStatus(BT_STATUS_READY))
                        .observeOn(Schedulers.io())
                        .flatMap(devicename -> searchDeviceInBonded(BTConnector.this, devicename))
                        .map(device -> getSocket(device))
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(name -> sendStatus(BT_STATUS_CONNECTING))
                        .observeOn(Schedulers.io())
                        .map(socket -> connect(socket))
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(name -> sendStatus(BT_STATUS_CONNECTED))
                        .observeOn(Schedulers.io())
                        .flatMap(socket -> {
                            Observable<String> receive =
                                    Observable.create(emitter -> {
                                        try {
                                            InputStream in = socket.getInputStream();
                                            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                                            while (true) {
                                                emitter.onNext(reader.readLine());
                                            }
                                        } catch (Exception e) {
                                            emitter.onError(e);
                                        }
                                    });

                            return receive;
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(message -> sendMessage(message),
                                error -> {
                                    Log.e(TAG, error.toString());
                                    error.printStackTrace();
                                    sendStatus(BT_STATUS_DISCONNECTED);
                                },
                                () -> {
                                    Log.e(TAG, "complete Service");
                                })

        );

        registCallerReceiver();
    }

    private Observable<BluetoothDevice> searchDevice(Context context, String name) {
        final PublishSubject<BluetoothDevice> subject = PublishSubject.create();
        final BroadcastReceiver bReciever = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device.getName().toUpperCase().contains(name)) {
                        btAdapter.cancelDiscovery();
                        context.unregisterReceiver(this);

                        subject.onNext(device);
                        subject.onComplete();
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(bReciever, filter);
        btAdapter.startDiscovery();

        return subject;
    }

    private Observable<BluetoothDevice> searchDeviceInBonded(Context context, String name) {
        BluetoothDevice connectableDevice = null;

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().toUpperCase().contains(name)) {
                    connectableDevice = device;
                    break;
                }
            }
        }

        if (connectableDevice == null) return searchDevice(context, name);

        return Observable.just(connectableDevice);
    }

    private BluetoothSocket getSocket(BluetoothDevice device) throws IOException {
        final UUID KNOCK_BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(KNOCK_BT_UUID);
        this.deviceSocket = socket;
        return socket;
    }

    private BluetoothSocket connect(BluetoothSocket socket) throws IOException {
        socket.connect();
        return socket;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service onDestroy");
        unregistCallReceiver();
        if (deviceSocket != null) {
            try {
                deviceSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        disposable.dispose();
        super.onDestroy();
    }

    private void registCallerReceiver() {
        callerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    OutputStream out = deviceSocket.getOutputStream();
                    out.write('B');
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        registerReceiver(callerReceiver, new IntentFilter(BROADCAST_MESSAGE_CALL));
    }

    private void unregistCallReceiver() {
        if (callerReceiver == null) return;
        unregisterReceiver(callerReceiver);
    }

    private void sendStatus(int status) {
        Log.d(TAG, "sendStatus(" + status + ")");
        Intent sendIntent = new Intent(BROADCAST_MESSAGE_STATUS);
        sendIntent.putExtra("status", status);
        sendBroadcast(sendIntent);
    }

    private void sendMessage(String msg) {
        Log.d(TAG, "sendMessage(" + msg + ")");
        Intent sendIntent = new Intent(BROADCAST_MESSAGE_MESSAGE);
        sendIntent.putExtra("code", msg);
        sendBroadcast(sendIntent);
    }
}
