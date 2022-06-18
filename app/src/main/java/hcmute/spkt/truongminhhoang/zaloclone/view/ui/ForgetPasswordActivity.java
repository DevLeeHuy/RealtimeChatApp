package hcmute.spkt.truongminhhoang.zaloclone.view.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Objects;

import hcmute.spkt.truongminhhoang.zaloclone.R;
import hcmute.spkt.truongminhhoang.zaloclone.viewModel.LogInViewModel;

public class ForgetPasswordActivity extends AppCompatActivity {

    ImageView iv_back_button;
    EditText et_email_to_reset;
    Button btn_reset;
    LogInViewModel logInViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        init();// inflate view components
        listeners(); // listen to view component events

    }

    private void listeners() {
        final AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F); // initialize animation value when clicking

        iv_back_button.setOnClickListener(v -> finish()); // return to previous screen

        btn_reset.setOnClickListener(v -> {
            String email = et_email_to_reset.getText().toString().trim();
            et_email_to_reset.clearFocus();
            v.startAnimation(buttonClick);
            dismissKeyboard(); // hide keyboard

            if(email.isEmpty()){  // check if input field is empty
                et_email_to_reset.setError("Please enter your authorised Email Id.");
                Toast.makeText(ForgetPasswordActivity.this, "Field is empty", Toast.LENGTH_SHORT).show();
                et_email_to_reset.requestFocus();
            }else{
                et_email_to_reset.setClickable(false);
                resetPassword(email);
            }

        });
    }

    private void resetPassword(String email) {
        logInViewModel.addPasswordResetEmail(email); // send email reset password to specific email
        logInViewModel.successPasswordReset.observe(this, task -> { //listen to this event
            if(!task.isSuccessful()){  // if request fail => show error
                et_email_to_reset.setClickable(true);
                et_email_to_reset.setText("");
                String error= Objects.requireNonNull(task.getException()).getMessage();
                et_email_to_reset.requestFocus();
                Toast.makeText(ForgetPasswordActivity.this, error, Toast.LENGTH_SHORT).show();

            }else{ //if nothing wrong => back to login screen
                Toast.makeText(ForgetPasswordActivity.this, "Please check your Email.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ForgetPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    public void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

    private void init() {
        et_email_to_reset = findViewById(R.id.et_email_to_reset);
        iv_back_button = findViewById(R.id.iv_back_button_forget_pwd_layout);
        btn_reset = findViewById(R.id.btn_reset);

        logInViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication()))
                .get(LogInViewModel.class);

    }


}