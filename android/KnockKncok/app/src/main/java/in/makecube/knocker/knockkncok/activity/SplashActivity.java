package in.makecube.knocker.knockkncok.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

import in.makecube.knocker.knockkncok.R;
import io.reactivex.Observable;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Observable.just(0)
                .delay(3, TimeUnit.SECONDS)
                .subscribe(n -> startActivity(new Intent(SplashActivity.this, LoginActivity.class)),
                        e -> e.printStackTrace(),
                        () -> finish());
    }
}
