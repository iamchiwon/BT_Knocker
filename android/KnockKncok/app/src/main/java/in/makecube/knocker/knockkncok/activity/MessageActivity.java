package in.makecube.knocker.knockkncok.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.makecube.knocker.knockkncok.R;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class MessageActivity extends Activity {

    private static final int CLOSE_SECONDS = 8;

    @BindView(R.id.closeButton)
    Button closeButton;

    private CompositeDisposable disposeBag = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        ButterKnife.bind(this);

        String code = getIntent().getStringExtra("code");
        if (code != null) {
            disposeBag.add(
                    Observable.interval(1, TimeUnit.SECONDS)
                            .map(s -> (int) (CLOSE_SECONDS - s))
                            .map(s -> String.format(getString(R.string.close_countdown), s))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(text -> closeButton.setText(text))
            );
        }

        disposeBag.add(
                Observable.interval(1, TimeUnit.SECONDS)
                        .map(s -> (int) (CLOSE_SECONDS - s))
                        .filter(s -> s == 0)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(s -> finish())
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposeBag.dispose();
    }

    @OnClick(R.id.closeButton)
    public void onClose() {
        finish();
    }
}
