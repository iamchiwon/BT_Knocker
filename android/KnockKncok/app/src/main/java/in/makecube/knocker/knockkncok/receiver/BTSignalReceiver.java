package in.makecube.knocker.knockkncok.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import in.makecube.knocker.knockkncok.activity.MessageActivity;

/**
 * Created by iamchiwon on 2017. 9. 9..
 */

public class BTSignalReceiver extends BroadcastReceiver {
    private final String TAG = BTSignalReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String code = intent.getStringExtra("code");
        Log.d(TAG, "onReceive(code, " + code + ")");

        Intent messageIntent = new Intent(context, MessageActivity.class);
        messageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        messageIntent.putExtra("code", code);
        context.startActivity(messageIntent);
    }
}
