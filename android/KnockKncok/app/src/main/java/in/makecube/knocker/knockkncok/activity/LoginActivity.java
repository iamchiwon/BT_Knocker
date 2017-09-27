package in.makecube.knocker.knockkncok.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.makecube.knocker.knockkncok.R;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.editId)
    EditText editId;
    @BindView(R.id.editPassword)
    EditText editPassword;
    @BindView(R.id.buttonLogin)
    Button loginButton;
    @BindView(R.id.save_id)
    ImageView saveIdCheck;
    @BindView(R.id.save_auto)
    ImageView saveAutoCheck;

    private CompositeDisposable disposeBag = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        disposeBag.add(
                Observable.combineLatest(
                        RxTextView.textChangeEvents(editId).map(edit -> edit.text().length() > 0),
                        RxTextView.textChangeEvents(editPassword).map(edit -> edit.text().length() > 0),
                        (inputId, inputPw) -> inputId & inputPw
                )
                        .doOnNext(enable -> loginButton.setAlpha(enable ? 1.0f : 0.5f))
                        .subscribe(enable -> loginButton.setEnabled(enable))
        );

        disposeBag.add(
                RxView.clicks(saveIdCheck)
                        .map(v -> saveIdCheck.isSelected())
                        .map(selected -> !selected)
                        .distinctUntilChanged()
                        .doOnNext(RxView.selected(saveIdCheck))
                        .map(isSelected -> isSelected ? R.drawable.checked : R.drawable.unchecked)
                        .subscribe(drawable -> saveIdCheck.setBackgroundResource(drawable))
        );

        disposeBag.add(
                RxView.clicks(saveAutoCheck)
                        .map(v -> saveAutoCheck.isSelected())
                        .map(selected -> !selected)
                        .distinctUntilChanged()
                        .doOnNext(RxView.selected(saveAutoCheck))
                        .map(isSelected -> isSelected ? R.drawable.checked : R.drawable.unchecked)
                        .subscribe(drawable -> saveAutoCheck.setBackgroundResource(drawable))
        );

        disposeBag.add(
                Observable.just(loadLoginInfo())
                        .filter(isAuto -> isAuto)
                        .delay(1, TimeUnit.SECONDS)
                        .subscribe(iaAuto -> loginButton.callOnClick())
        );
    }

    @OnClick(R.id.buttonLogin)
    public void onLogin() {
        saveLoginInfo();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void saveLoginInfo() {
        SharedPreferences prefs = getSharedPreferences("LOGIN_INFO", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("save_id", saveIdCheck.isSelected());
        editor.putBoolean("save_auto", saveAutoCheck.isSelected());
        editor.putString("id", editId.getText().toString());
        editor.putString("pw", editPassword.getText().toString());
        editor.commit();
    }

    private boolean loadLoginInfo() {
        SharedPreferences prefs = getSharedPreferences("LOGIN_INFO", MODE_PRIVATE);

        boolean isSaveId = prefs.getBoolean("save_id", false);
        saveIdCheck.setSelected(isSaveId);
        if (isSaveId) {
            String id = prefs.getString("id", "");
            editId.setText(id);
            saveIdCheck.setSelected(!TextUtils.isEmpty(id));
        }
        saveIdCheck.setBackgroundResource(saveIdCheck.isSelected() ? R.drawable.checked : R.drawable.unchecked);

        boolean isSavePw = prefs.getBoolean("save_auto", false);
        saveAutoCheck.setSelected(isSavePw);
        if (isSavePw) {
            String pw = prefs.getString("pw", "");
            editPassword.setText(pw);
            saveAutoCheck.setSelected(!TextUtils.isEmpty(pw));
        }
        saveAutoCheck.setBackgroundResource(saveAutoCheck.isSelected() ? R.drawable.checked : R.drawable.unchecked);

        return saveAutoCheck.isSelected();
    }
}
