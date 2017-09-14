package in.makecube.knocker.knockkncok.activity;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.makecube.knocker.knockkncok.R;
import in.makecube.knocker.knockkncok.service.BTConnector;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.buttonReconnect)
    Button buttonReconnect;
    @BindView(R.id.textStatus)
    TextView textStatus;

    private long backPressedAt;
    private BehaviorSubject<Integer> stateSubject = BehaviorSubject.createDefault(BTConnector.BT_STATUS_READY);
    private BroadcastReceiver statusReceiver = null;
    private CompositeDisposable disposeBag = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        disposeBag.add(
                stateSubject
                        .map(state -> {
                            switch (state) {
                                case BTConnector.BT_STATUS_READY:
                                    return getString(R.string.will_connect);
                                case BTConnector.BT_STATUS_CONNECTING:
                                    return getString(R.string.status_connecting);
                                case BTConnector.BT_STATUS_CONNECTED:
                                    return getString(R.string.status_connected);
                                case BTConnector.BT_STATUS_DISCONNECTED:
                                    return getString(R.string.status_disconnected);
                            }
                            return getString(R.string.unkown_error);
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(text -> textStatus.setText(text))
        );


        disposeBag.add(
                Observable.interval(3, TimeUnit.SECONDS)
                        .map(n -> isServiceRunning(BTConnector.class))
                        .filter(running -> !running)
                        .subscribeOn(Schedulers.newThread())
                        .subscribe(text -> {
                            Log.d(TAG, "request StartService");
                            startService(serviceIntent());
                        })
        );

        registerReceiver();
        checkBluetooth();
    }

    @Override
    protected void onDestroy() {
        disposeBag.dispose();
        unregisterReceiver();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (backPressedAt + TimeUnit.SECONDS.toMillis(2) > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            Toast.makeText(getBaseContext(), R.string.press_one_more_to_finish, Toast.LENGTH_SHORT).show();
            backPressedAt = System.currentTimeMillis();
        }
    }

    @OnClick(R.id.buttonReconnect)
    public void onReconnect() {
        Log.d(TAG, "request StopService");
        stopService(serviceIntent());
    }


    private void checkBluetooth() {
        final int REQUEST_BLUETOOTH = 1;

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle("미지원 기기")
                    .setMessage("이 기기는 블루투스 기능을 지원하지 않습니다")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            if (!btAdapter.isEnabled()) {
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT, REQUEST_BLUETOOTH);
            }
        }
    }

    private Intent serviceIntent() {
        Intent serviceIntent = new Intent(MainActivity.this, BTConnector.class);
        return serviceIntent;
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void registerReceiver() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BTConnector.BROADCAST_MESSAGE_STATUS);

        statusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int currentStatus = intent.getIntExtra("status", BTConnector.BT_STATUS_READY);
                Log.d(TAG, "onReceive(status, " + currentStatus + ")");
                stateSubject.onNext(currentStatus);
            }
        };

        registerReceiver(this.statusReceiver, intentFilter);
    }

    private void unregisterReceiver() {
        if (statusReceiver != null) {
            unregisterReceiver(statusReceiver);
            statusReceiver = null;
        }
    }
}
