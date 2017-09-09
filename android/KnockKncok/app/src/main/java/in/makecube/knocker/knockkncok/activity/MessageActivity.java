package in.makecube.knocker.knockkncok.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

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

    @BindView(R.id.messageTextView)
    TextView messageTextView;
    @BindView(R.id.closeButton)
    Button closeButton;

    private CompositeDisposable disposeBag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        ButterKnife.bind(this);

        String code = getIntent().getStringExtra("code");
        String message = convertCodeToMessage(code);
        messageTextView.setText(message);

        disposeBag = new CompositeDisposable();
        disposeBag.add(
                Observable.interval(1, TimeUnit.SECONDS)
                        .map(s -> (int) (CLOSE_SECONDS - s))
                        .map(s -> String.format(getString(R.string.close_countdown), s))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(text -> closeButton.setText(text))
        );
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

        if (disposeBag != null) {
            disposeBag.dispose();
            disposeBag = null;
        }
    }

    @OnClick(R.id.closeButton)
    public void onClose() {
        finish();
    }

    private String convertCodeToMessage(String code) {
        //template demo
        if (code.equals("A")) {
            return "B병동 2층 피부과에서 노크만님께서 호출하셨습니다";
        }
        return "호출 되었습니다";
    }
}