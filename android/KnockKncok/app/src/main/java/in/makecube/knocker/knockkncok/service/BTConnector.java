package in.makecube.knocker.knockkncok.service;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

public class BTConnector extends IntentService {

    public BTConnector() {
        super(BTConnector.class.getName());
    }

    public BTConnector(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Observable.interval(1000, TimeUnit.MILLISECONDS).take(5).subscribe(n -> {
            Log.d("iamchiwon", "onHandleIntent : "+n);
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("iamchiwon", "destroy");
    }
}
