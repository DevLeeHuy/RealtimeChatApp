package hcmute.spkt.truongminhhoang.zaloclone.view.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import hcmute.spkt.truongminhhoang.zaloclone.R;
import hcmute.spkt.truongminhhoang.zaloclone.viewModel.DatabaseViewModel;
import hcmute.spkt.truongminhhoang.zaloclone.viewModel.SignUpViewModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class SignupActivity extends AppCompatActivity {

    EditText et_usernameSignIn;
    EditText et_phoneSignIn;
    EditText et_emailIdSignIn;
    EditText et_pwdSignIn;
    Button btn_signIn;
    TextView textToLogin;
    SignUpViewModel signUpViewModel;
    DatabaseViewModel databaseViewModel;
    String emailId;
    String pwd;
    String userName;
    String phoneNumber;
    Context context;
    String userId;
    String imageUrl;
    String timeStamp;
    FirebaseUser currentUser;
    FrameLayout progressBarSignInFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        init();
        listeners();
    }
    private void init() {
        et_usernameSignIn = findViewById(R.id.et_signin_username);
        et_phoneSignIn = findViewById(R.id.et_signin_phone);
        et_emailIdSignIn = findViewById(R.id.et_signin_email);
        et_pwdSignIn = findViewById(R.id.et_signin_password);
        btn_signIn = findViewById(R.id.btn_signin);
        textToLogin = findViewById(R.id.text_to_login);
        context = SignupActivity.this;
        signUpViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication()))
                .get(SignUpViewModel.class);
        databaseViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication()))
                .get(DatabaseViewModel.class); //
        progressBarSignInFrame = findViewById(R.id.progress_bar_signIn);
    }
    private void listeners() {

        btn_signIn.setOnClickListener(new View.OnClickListener() {
            final AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);

            @Override
            public void onClick(View v) {
                et_usernameSignIn.clearFocus();
                et_phoneSignIn.clearFocus();
                et_emailIdSignIn.clearFocus();
                et_pwdSignIn.clearFocus();
                v.startAnimation(buttonClick);

                emailId = et_emailIdSignIn.getText().toString();
                pwd = et_pwdSignIn.getText().toString();
                userName = et_usernameSignIn.getText().toString();
                phoneNumber = et_phoneSignIn.getText().toString();
                if ((pwd.isEmpty() && emailId.isEmpty() && userName.isEmpty() && phoneNumber.isEmpty())) {
                    Toast.makeText(SignupActivity.this, "Fields are empty!", Toast.LENGTH_SHORT).show();
                    et_usernameSignIn.requestFocus();
                } else if (userName.isEmpty()) {
                    et_usernameSignIn.setError("Please enter a username.");
                    et_usernameSignIn.requestFocus();
                } else if (phoneNumber.isEmpty()) {
                    et_phoneSignIn.setError("Please set your phone.");
                    et_phoneSignIn.requestFocus();
                } else if (emailId.isEmpty()) {
                    et_emailIdSignIn.setError("Please enter your Email Id.");
                    et_emailIdSignIn.requestFocus();
                } else if (pwd.isEmpty()) {
                    et_pwdSignIn.setError("Please set your password.");
                    et_pwdSignIn.requestFocus();
                } else {
                    progressBarSignInFrame.setVisibility(View.VISIBLE);
                    et_usernameSignIn.setClickable(false);
                    et_phoneSignIn.setClickable(false);
                    et_emailIdSignIn.setClickable(false);
                    et_pwdSignIn.setClickable(false);
                    textToLogin.setClickable(false);
                    dismissKeyboard();
                    signInUsers();
                }
            }
        });

        textToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                finish();
            }
        });
    }

    public void signInUsers() {
        signUpViewModel.userSignIn(userName, phoneNumber, emailId, pwd);
        signUpViewModel.signInUser.observe(this, new Observer<Task>() {
            @Override
            public void onChanged(Task task) {
                if (!task.isSuccessful()) {
                    et_usernameSignIn.setClickable(true);
                    et_phoneSignIn.setClickable(true);
                    et_emailIdSignIn.setClickable(true);
                    et_pwdSignIn.setClickable(true);
                    textToLogin.setClickable(true);

                    progressBarSignInFrame.setVisibility(View.GONE);
                    et_emailIdSignIn.setText("");
                    et_pwdSignIn.setText("");
                    et_usernameSignIn.setText("");
                    et_phoneSignIn.requestFocus();

                    try {
                        throw Objects.requireNonNull(task.getException());
                    } catch (FirebaseAuthUserCollisionException existEmail) {
                        Toast.makeText(context, "Email Id already exists.", Toast.LENGTH_SHORT).show();
                    } catch (FirebaseAuthWeakPasswordException weakPassword) {
                        Toast.makeText(context, "Password length should be more then six characters.", Toast.LENGTH_SHORT).show();
                    } catch (FirebaseAuthInvalidCredentialsException malformedEmail) {
                        Toast.makeText(context, "Invalid credentials, please try again.", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(context, "SignUp unsuccessful. Try again.", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    getUserSession();
                    addUserInDatabase(userName, phoneNumber, emailId, userId);
                    Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

        });
    }

    private void addUserInDatabase(String userName, String phoneNumber, String email, String idUser) {
        long tsLong = System.currentTimeMillis();
        timeStamp = Long.toString(tsLong);
        imageUrl = "default";
        userId = currentUser.getUid();
        databaseViewModel.addUserDatabase(userId, userName, phoneNumber, email, timeStamp, imageUrl);
        databaseViewModel.successAddUserDb.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean)
                    Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
                else {
                    Toast.makeText(context, "ERROR WHILE ADDING DATA IN DATABASE.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void getUserSession() {
        signUpViewModel.getUserFirebaseSession();
        signUpViewModel.userFirebaseSession.observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                currentUser = firebaseUser;
            }
        });

    }

    public void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }



}
