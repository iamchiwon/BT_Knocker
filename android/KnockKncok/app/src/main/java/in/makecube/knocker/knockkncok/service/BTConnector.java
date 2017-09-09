package in.makecube.knocker.knockkncok.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

public class BTConnector extends Service {
    public static final UUID KNOCK_BT_UUID = UUID.randomUUID();
    public static final String CONNECTABLE_DEVICE_NAME = "KNOCKER_BT";
    public static final String BROADCAST_MESSAGE_STATUS = "in.makecube.knocker.BLUETOOTH_STATE";
    public static final String BROADCAST_MESSAGE_MESSAGE = "in.makecube.knocker.KNOCK_MESSAGE";
    public static final int BT_STATUS_READY = 0;
    public static final int BT_STATUS_CONNECTING = 1;
    public static final int BT_STATUS_CONNECTED = 2;
    public static final int BT_STATUS_DISCONNECTED = 3;

    private CompositeDisposable disposable;
    private BluetoothAdapter btAdapter;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!btAdapter.isEnabled()) btAdapter.enable();

        disposable = new CompositeDisposable();
        disposable.add(

                Observable.just(CONNECTABLE_DEVICE_NAME)
                        .doOnNext(name -> sendStatus(BT_STATUS_READY))
                        .map(devicename -> searchDevice(devicename))
                        .map(device -> getSocket(device))
                        .doOnNext(name -> sendStatus(BT_STATUS_CONNECTING))
                        .map(socket -> connect(socket))
                        .doOnNext(name -> sendStatus(BT_STATUS_CONNECTED))
                        .flatMap(socket -> {
                            Observable<String> receive =
                                    Observable.create(emitter -> {
                                        InputStream in = socket.getInputStream();
                                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                                        while (true) {
                                            emitter.onNext(reader.readLine());
                                        }
                                    });
                            return receive;
                        })
                        .subscribe(message -> sendMessage(message),
                                error -> sendStatus(BT_STATUS_DISCONNECTED))

        );
    }

    private BluetoothDevice searchDevice(String name) {
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

        return connectableDevice;
    }

    private BluetoothSocket getSocket(BluetoothDevice device) throws IOException {
        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(KNOCK_BT_UUID);
        return socket;
    }

    private BluetoothSocket connect(BluetoothSocket socket) throws IOException {
        socket.connect();
        return socket;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
    }

    private void sendStatus(int status) {
        Intent sendIntent = new Intent(BROADCAST_MESSAGE_STATUS);
        sendIntent.putExtra("status", status);
        sendBroadcast(sendIntent);
    }

    private void sendMessage(String msg) {
        Intent sendIntent = new Intent(BROADCAST_MESSAGE_MESSAGE);
        sendIntent.putExtra("code", msg);
        sendBroadcast(sendIntent);
    }
}
