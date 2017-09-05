package in.makecube.knocker.knockkncok;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.makecube.knocker.knockkncok.service.BTConnector;
import lombok.Lombok;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.startButton)
    Button startButton;
    @BindView(R.id.stopButton)
    Button stopButton;
    @BindView(R.id.statusText)
    TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        startButton.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(MainActivity.this, BTConnector.class);
            startService(serviceIntent);
        });

        stopButton.setOnClickListener(v -> {
            //nothing
        });
    }
}
