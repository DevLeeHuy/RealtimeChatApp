package hcmute.spkt.truongminhhoang.zaloclone.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

import hcmute.spkt.truongminhhoang.zaloclone.services.repository.FirebaseSignUpInstance;

public class SignUpViewModel extends ViewModel {

    private FirebaseSignUpInstance signUpInstance;
    public LiveData<Task> signInUser;
    public  LiveData<FirebaseUser> userFirebaseSession;

    public SignUpViewModel() {
        signUpInstance = new FirebaseSignUpInstance();
    }

    public void userSignIn(String userNameSignIn, String phoneNumber, String emailSignIn, String passwordSignIn) {
        signInUser = signUpInstance.signInUser(userNameSignIn, phoneNumber, emailSignIn, passwordSignIn);
    }


    public void getUserFirebaseSession(){
        userFirebaseSession = signUpInstance.firebaseUsers;
    }

}
