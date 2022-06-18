package hcmute.spkt.truongminhhoang.zaloclone.services.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import hcmute.spkt.truongminhhoang.zaloclone.services.notifications.Token;

public class FirebaseLoginInstance {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = mAuth.getCurrentUser();



    public MutableLiveData<FirebaseUser> getFirebaseUserLoginStatus() {
        //get user login status from firebase => if user has login before but not logout
        final MutableLiveData<FirebaseUser> firebaseUserLoginStatus = new MutableLiveData<>();

        firebaseUserLoginStatus.setValue(firebaseUser); //set value into to trigger observe event
        Log.e("TAG", "authStatus "+firebaseUserLoginStatus.getValue());

        return firebaseUserLoginStatus;

    }

    public MutableLiveData<FirebaseAuth> getFirebaseAuth() {
        //get specific user data from database using firebase Auth API
        final MutableLiveData<FirebaseAuth> firebaseAuth = new MutableLiveData<>();
        firebaseAuth.setValue(mAuth);//set value into to trigger observe event
        return firebaseAuth;
    }

    public MutableLiveData<Boolean> successUpdateToken(String newToken) {
        //update token after login successfully
        final MutableLiveData<Boolean> successTokenUpdate = new MutableLiveData<>();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(newToken);
        assert firebaseUser != null;
        reference.child(firebaseUser.getUid()).setValue(token);
        return successTokenUpdate;
    }




    public MutableLiveData<Task> loginUser(String emailLogin, String pwdLogin) {
        final MutableLiveData<Task> taskLogin = new MutableLiveData<>();
        //login with firebase authentication feature => need providing email and password
        mAuth.signInWithEmailAndPassword(emailLogin, pwdLogin).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                taskLogin.setValue(task);
            }
        });

        return taskLogin;
    }

    public MutableLiveData<Task> resetPassword(String email) {
        final MutableLiveData<Task> successResetPassword = new MutableLiveData<>();
        //reset password provided by firebase library
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                successResetPassword.setValue(task); //set value into to trigger observe event
            }
        });


        return successResetPassword;
    }
}
