package in.makecube.knocker.knockkncok.activity;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.buttonReconnect)
    Button buttonReconnect;
    @BindView(R.id.textStatus)
    TextView textStatus;

    private int currentStatus = BTConnector.BT_STATUS_READY;
    private BroadcastReceiver statusReceiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Observable<Boolean> isConnected =
                Observable.interval(2500, TimeUnit.MILLISECONDS)
                        .map(n -> isServiceRunning(BTConnector.class));

        isConnected
                .map(running -> {
                    if (!running) return getString(R.string.will_connect);
                    switch (currentStatus) {
                        case BTConnector.BT_STATUS_READY:
                            return getString(R.string.status_preparing);
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
                .subscribe(text -> textStatus.setText(text));

        isConnected
                .filter(running -> !running)
                .subscribe(r -> startService(serviceIntent()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver();
    }

    private static long backPressedAt;

    @Override
    public void onBackPressed() {
        if (backPressedAt + TimeUnit.SECONDS.toMillis(2) > System.currentTimeMillis()){
            super.onBackPressed();
        } else{
            Toast.makeText(getBaseContext(), R.string.press_one_more_to_finish, Toast.LENGTH_SHORT).show();
            backPressedAt = System.currentTimeMillis();
        }
    }

    @OnClick(R.id.buttonReconnect)
    public void onReconnect() {
        stopService(serviceIntent());
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
                currentStatus = intent.getIntExtra("status", BTConnector.BT_STATUS_READY);
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
