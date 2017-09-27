package in.makecube.knocker.knockkncok.activity;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding2.view.RxView;

import java.util.ArrayList;
import java.util.List;
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
import lombok.AllArgsConstructor;
import lombok.Data;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.buttonReconnect)
    ImageView buttonReconnect;
    @BindView(R.id.listView)
    ListView listView;

    private long backPressedAt;
    private BehaviorSubject<Integer> stateSubject = BehaviorSubject.createDefault(BTConnector.BT_STATUS_READY);
    private BroadcastReceiver statusReceiver = null;
    private CompositeDisposable disposeBag = new CompositeDisposable();

    private ListAdapter listAdapter;

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
                        .subscribe(text -> Snackbar.make(buttonReconnect, text, Snackbar.LENGTH_LONG).show())
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

        listAdapter = new ListAdapter();
        listView.setAdapter(listAdapter);

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

    ////////////////////////////

    @AllArgsConstructor
    @Data
    class Patient {
        String name;
        String division;
    }

    class ListAdapter extends BaseAdapter {

        List<Patient> patients;
        int highlightIndex = -1;

        public ListAdapter() {
            patients = new ArrayList<>();
            patients.add(new Patient("신유경", "OS"));
            patients.add(new Patient("김지연", "GS"));
            patients.add(new Patient("장민경", "OS"));
            patients.add(new Patient("박용덕", "OBGY"));
            patients.add(new Patient("김호수", "OS"));
            patients.add(new Patient("신유경", "OS"));
            patients.add(new Patient("김지연", "GS"));
            patients.add(new Patient("장민경", "OS"));
            patients.add(new Patient("박용덕", "OBGY"));
            patients.add(new Patient("김호수", "OS"));
            patients.add(new Patient("신유경", "OS"));
            patients.add(new Patient("김지연", "GS"));
            patients.add(new Patient("장민경", "OS"));
            patients.add(new Patient("박용덕", "OBGY"));
            patients.add(new Patient("김호수", "OS"));
        }

        @Override
        public int getCount() {
            return patients.size();
        }

        @Override
        public Object getItem(int i) {
            return patients.get(i);
        }

        @Override
        public long getItemId(int i) {
            return getItem(i).hashCode();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View row = view;

            if (row == null) {
                LayoutInflater inflator = LayoutInflater.from(viewGroup.getContext());
                row = inflator.inflate(R.layout.list_item_layout, viewGroup, false);

                ViewHolder vh = new ViewHolder();
                vh.setName(row.findViewById(R.id.name));
                vh.setDivision(row.findViewById(R.id.division));
                vh.setLocation(row.findViewById(R.id.location));
                vh.setCall(row.findViewById(R.id.call));

                row.setTag(vh);
            }

            ViewHolder vh = (ViewHolder) row.getTag();
            Patient p = (Patient) getItem(i);

            vh.getName().setText(p.getName());
            vh.getDivision().setText(p.getDivision());
            vh.getLocation().setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("환자 위치를 조회하시겠습니까?");
                builder.setPositiveButton("예", (dialog, which) -> {
                    Intent messageIntent = new Intent(MainActivity.this, MessageActivity.class);
                    messageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MainActivity.this.startActivity(messageIntent);

                    highlightIndex = i;
                    notifyDataSetChanged();
                });
                builder.setNegativeButton("아니오", (dialog, which) -> dialog.dismiss());
                builder.show();
            });
            vh.getCall().setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("환자를 호출하시겠습니까?");
                builder.setPositiveButton("예", (dialog, which) -> {
                    Intent sendIntent = new Intent(BTConnector.BROADCAST_MESSAGE_CALL);
                    sendBroadcast(sendIntent);

                    highlightIndex = i;
                    notifyDataSetChanged();

                    Snackbar.make(buttonReconnect, "호출하였습니다.", Snackbar.LENGTH_LONG).show();
                });
                builder.setNegativeButton("아니오", (dialog, which) -> dialog.dismiss());
                builder.show();
            });

            if (i == highlightIndex) {
                row.setBackgroundColor(0xFF65D0BC);
                vh.getName().setTextColor(0xFFFFFFFF);
            } else {
                row.setBackgroundColor(0xFFFFFFFF);
                vh.getName().setTextColor(0xFF585858);
            }

            return row;
        }

        @Data
        class ViewHolder {
            TextView name;
            TextView division;
            Button location;
            Button call;
        }
    }
}
