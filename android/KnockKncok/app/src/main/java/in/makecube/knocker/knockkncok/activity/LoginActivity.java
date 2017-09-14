package in.makecube.knocker.knockkncok.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;

import com.jakewharton.rxbinding2.widget.RxTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.makecube.knocker.knockkncok.R;
import io.reactivex.Observable;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.editId)
    EditText editId;
    @BindView(R.id.editPassword)
    EditText editPassword;
    @BindView(R.id.buttonLogin)
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        Observable.combineLatest(
                RxTextView.textChangeEvents(editId).map(edit -> edit.text().length() > 0),
                RxTextView.textChangeEvents(editPassword).map(edit -> edit.text().length() > 0),
                (inputId, inputPw) -> inputId & inputPw
        )
                .subscribe(enable -> loginButton.setEnabled(enable));

    }

    @OnClick(R.id.buttonLogin)
    public void onLogin() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
