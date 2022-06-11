package hcmute.spkt.truongminhhoang.zaloclone.viewModel;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import hcmute.spkt.truongminhhoang.zaloclone.services.repository.FirebaseInstanceDatabase;

public class DatabaseViewModel extends ViewModel {
    private FirebaseInstanceDatabase instance;
    public LiveData<Boolean> successAddUserDb;
    public LiveData<DataSnapshot> fetchUserCurrentData;
    public LiveData<DataSnapshot> fetchUserNames;
    public LiveData<DataSnapshot> fetchSelectedProfileUserData;
    public LiveData<Boolean> successAddChatDb;
    public LiveData<DataSnapshot> fetchedChat;
    public LiveData<StorageReference> imageFileReference;
    public LiveData<Boolean> successAddImageUrlInDatabase;
    public LiveData<Boolean> successAddUsernameInDatabase;
    public LiveData<Boolean> successAddBioInDatabase;
    public LiveData<Boolean> successAddStatusInDatabase;
    public List<LiveData<DataSnapshot>>fetchSearchUser = new ArrayList<>();
    public LiveData<Boolean> successAddIsSeen;
    public LiveData<DataSnapshot> getChaListUserDataSnapshot;
    public LiveData<DatabaseReference> getTokenRefDb;




    public DatabaseViewModel() {
        instance = new FirebaseInstanceDatabase();
    }

    public void addUserDatabase(String userId, String userName, String phoneNumber, String emailId, String timestamp, String imageUrl) {
        successAddUserDb = instance.addUserInDatabase(userId, userName, phoneNumber, emailId, timestamp, imageUrl);
    }

    public void fetchingUserDataCurrent() {
        fetchUserCurrentData = instance.fetchUserDataCurrent();
    }

    public void fetchUserByNameAll() {
        fetchUserNames = instance.fetchAllUserByNames();
    }

    public void fetchSelectedUserProfileData(String userId) {
        fetchSelectedProfileUserData = instance.fetchSelectedUserIdData(userId);
    }

    public void addChatDb(String receiverId,String senderId, String message, String timestamp,String type) {
        successAddChatDb = instance.addChatsInDatabase(receiverId, senderId, message, timestamp,type);
    }

    public void fetchChatUser() {
        fetchedChat = instance.fetchChatUser();
    }

    public void fetchImageFileReference(String timeStamp, Uri imageUri, Context context) {
        imageFileReference = instance.fetchFileReference(timeStamp, imageUri, context);
    }

    public void addImageUrlInDatabase(String imageUrl, Object mUri) {
        successAddImageUrlInDatabase = instance.addImageUrlInDatabase(imageUrl, mUri);
    }

    public void addBioInDatabase(String bio, Object bioUpdated) {
        successAddBioInDatabase = instance.addBioInDatabase(bio, bioUpdated);
    }

    public void addUsernameInDatabase(String usernameUpdated, Object username) {
        successAddUsernameInDatabase = instance.addUsernameInDatabase(usernameUpdated, username);
    }

    public void addStatusInDatabase(String statusUpdated,Object status){
        successAddStatusInDatabase = instance.addStatusInDatabase(statusUpdated, status);
    }

    public void fetchSearchedUser(String searchQuery){
        fetchSearchUser.add(instance.fetchSearchUser(searchQuery, "searchName"));
        fetchSearchUser.add(instance.fetchSearchUser(searchQuery, "searchPhone"));
    }

    public void addIsSeenInDatabase(String isSeen,DataSnapshot dataSnapshot){
        successAddIsSeen = instance.addIsSeenInDatabase(isSeen,dataSnapshot);
    }

    public void getChaListUserDataSnapshot(String currentUserId){
        getChaListUserDataSnapshot = instance.getChatList(currentUserId);
    }

    public void getTokenDatabaseRef(){
        getTokenRefDb = instance.getTokenRef();
    }


}