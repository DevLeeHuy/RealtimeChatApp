package hcmute.spkt.truongminhhoang.zaloclone.view.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import hcmute.spkt.truongminhhoang.zaloclone.R;
import hcmute.spkt.truongminhhoang.zaloclone.services.model.Chats;
import hcmute.spkt.truongminhhoang.zaloclone.services.model.Users;
import hcmute.spkt.truongminhhoang.zaloclone.services.notifications.Client;
import hcmute.spkt.truongminhhoang.zaloclone.services.notifications.Data;
import hcmute.spkt.truongminhhoang.zaloclone.services.notifications.MyResponse;
import hcmute.spkt.truongminhhoang.zaloclone.services.notifications.Sender;
import hcmute.spkt.truongminhhoang.zaloclone.services.notifications.Token;
import hcmute.spkt.truongminhhoang.zaloclone.utils.ImageConvert;
import hcmute.spkt.truongminhhoang.zaloclone.view.adapters.MessageAdapter;
import hcmute.spkt.truongminhhoang.zaloclone.view.fragments.APIService;
import hcmute.spkt.truongminhhoang.zaloclone.view.fragments.BottomSheetProfileDetailUser;
import hcmute.spkt.truongminhhoang.zaloclone.viewModel.DatabaseViewModel;
import hcmute.spkt.truongminhhoang.zaloclone.viewModel.LogInViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {
    LogInViewModel logInViewModel;
    DatabaseViewModel databaseViewModel;

    CircleImageView iv_profile_image;
    TextView tv_profile_user_name;
    ImageView iv_back_button;
    ImageView iv_user_status_message_view;

    String profileUserNAme;
    String profileImageURL;
    String bio;
    FirebaseUser currentFirebaseUser;

    EditText et_chat;
    ImageView btn_sendIv;
    ImageView btn_collections;
    ImageView btn_record;
    ImageView btn_camera;
    static final int PICK_IMAGE = 1;
    static final int CAPTURE_IMAGE=2;
    String chat;
    String timeStamp;
    String userId_receiver; // userId of other user who'll receive the text // Or the user id of profile currently opened
    String userId_sender;  // current user id
    String type;
    String user_status;
    MessageAdapter messageAdapter;
    ArrayList<Chats> chatsArrayList;
    RecyclerView recyclerView;
    Context context;
    BottomSheetProfileDetailUser bottomSheetProfileDetailUser;
    Uri imageUri;
    APIService apiService;
    boolean notify = false;
    private static int MICROPHONE_PERMISSION_CODE = 200;
    MediaRecorder mediaRecorder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        userId_receiver = getIntent().getStringExtra("userid");
        Log.e("TAG", userId_receiver);
        init();
        getCurrentFirebaseUser();
        fetchAndSaveCurrentProfileTextAndData();


        iv_profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBottomSheetDetailFragment(profileUserNAme, profileImageURL, bio);
            }
        });


        btn_sendIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;

                chat = et_chat.getText().toString().trim();
                if (!chat.equals("")) {
                    type = "text";
                    addChatInDataBase();
                } else {
                    Toast.makeText(MessageActivity.this, "Message can't be empty.", Toast.LENGTH_SHORT).show();
                }
                et_chat.setText("");
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                type = "image";
                chat = ImageConvert.getEncoded64ImageStringFromBitmap(bitmap);
                addChatInDataBase();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(requestCode==CAPTURE_IMAGE && resultCode==RESULT_OK){
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            type = "image";
            chat = ImageConvert.getEncoded64ImageStringFromBitmap(bitmap);
            addChatInDataBase();
        }
    }

    private void init() {
        databaseViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(DatabaseViewModel.class);
        logInViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(LogInViewModel.class);
        context = MessageActivity.this;

//        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        iv_user_status_message_view = findViewById(R.id.iv_user_status_message_view);
        iv_profile_image = findViewById(R.id.iv_user_image);

        tv_profile_user_name = findViewById(R.id.tv_profile_user_name);
        iv_back_button = findViewById(R.id.iv_back_button);

        iv_back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        btn_collections = findViewById(R.id.iv_collections);
        btn_collections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gallery, "Sellect Picture"), PICK_IMAGE);
            }
        });
        btn_camera=findViewById(R.id.iv_camera);
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(MessageActivity.this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MessageActivity.this,new String[]{
                            Manifest.permission.CAMERA
                    },CAPTURE_IMAGE);
                }
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,CAPTURE_IMAGE);

            }
        });
        btn_record = findViewById(R.id.iv_record);
        btn_record.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (isMicrophonePresent()) {
                        try {
                            getMicrophonePermission();
                            mediaRecorder = new MediaRecorder();
                            mediaRecorder.setAudioSource((MediaRecorder.AudioSource.MIC));
                            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                            mediaRecorder.setOutputFile(getRecordingFilePath());
                            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                            mediaRecorder.prepare();
                            mediaRecorder.start();
                        }catch (Exception e){
                            Log.e("Error", e.toString());
                        }
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    mediaRecorder = null;
                    chat=convert(getRecordingFilePath());
                    type="audio";
                    addChatInDataBase();

                }
                return true;
            }
        });



        et_chat = findViewById(R.id.et_chat);
        btn_sendIv = findViewById(R.id.iv_send_button);

        recyclerView = findViewById(R.id.recycler_view_messages_record);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        chatsArrayList = new ArrayList<>();


    }


    public String convert(String path) {
        byte[] audioBytes;
        try {

            // Just to check file size.. Its is correct i-e; Not Zero
            File audioFile = new File(path);
            long fileSize = audioFile.length();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileInputStream fis = new FileInputStream(new File(path));
            byte[] buf = new byte[1024];
            int n;
            while (-1 != (n = fis.read(buf)))
                baos.write(buf, 0, n);
            audioBytes = baos.toByteArray();

            // Here goes the Base64 string
            String audioBase64 = Base64.encodeToString(audioBytes, Base64.DEFAULT);
        return audioBase64;
        } catch (Exception e) {
            Log.e("TAG", e.toString() );
            return null;
        }
    }
    private String getRecordingFilePath() {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(musicDirectory, "testRecordingFIle" + ".mp3");
        return file.getPath();
    }

    private boolean isMicrophonePresent() {
        return this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
    }

    private void getMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MICROPHONE_PERMISSION_CODE);

        }
    }

    private void openBottomSheetDetailFragment(String username, String imageUrl, String bio) {
        bottomSheetProfileDetailUser = new BottomSheetProfileDetailUser(username, imageUrl, bio, context);
        assert getSupportActionBar() != null;
        bottomSheetProfileDetailUser.show(getSupportFragmentManager(), "edit");
    }

    private void getCurrentFirebaseUser() {
        logInViewModel.getFirebaseUserLogInStatus();
        logInViewModel.firebaseUserLoginStatus.observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                currentFirebaseUser = firebaseUser;
                userId_sender = currentFirebaseUser.getUid();
            }
        });
    }


    private void fetchAndSaveCurrentProfileTextAndData() {
        if (userId_receiver == null) {
            userId_receiver = getIntent().getStringExtra("userId");
        }
        databaseViewModel.fetchSelectedUserProfileData(userId_receiver);
        databaseViewModel.fetchSelectedProfileUserData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);

                assert user != null;
                profileUserNAme = user.getUsername();
                profileImageURL = user.getImageUrl();
                bio = user.getBio();
                user_status = user.getStatus();

                try {
                    if (user_status.contains("online") && isNetworkConnected()) {
                        iv_user_status_message_view.setBackgroundResource(R.drawable.online_status);
                    } else {
                        iv_user_status_message_view.setBackgroundResource(R.drawable.offline_status);
                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }

                tv_profile_user_name.setText(profileUserNAme);
                if (profileImageURL.equals("default")) {
                    iv_profile_image.setImageResource(R.drawable.sample_img);
                } else {
                    Glide.with(getApplicationContext()).load(profileImageURL).into(iv_profile_image);
                }
                fetchChatFromDatabase(userId_receiver, userId_sender);
            }
        });

        addIsSeen();
    }

    public void addIsSeen() {
        String isSeen = "seen";
        databaseViewModel.fetchChatUser();
        databaseViewModel.fetchedChat.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    Chats chats = dataSnapshot1.getValue(Chats.class);
                    assert chats != null;
                    if (chats.getSenderId().equals(userId_receiver) && chats.getReceiverId().equals(userId_sender)) {
                        databaseViewModel.addIsSeenInDatabase(isSeen, dataSnapshot1);
                    }
                }

            }
        });

    }


    public boolean isNetworkConnected() throws InterruptedException, IOException {   //check internet connectivity
        final String command = "ping -c 1 google.com";
        return Runtime.getRuntime().exec(command).waitFor() == 0;
    }

    private void fetchChatFromDatabase(String myId, String senderId) {
        databaseViewModel.fetchChatUser();
        databaseViewModel.fetchedChat.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(DataSnapshot dataSnapshot) {
                chatsArrayList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Chats chats = snapshot.getValue(Chats.class);
                    assert chats != null;
                    if (chats.getReceiverId().equals(senderId) && chats.getSenderId().equals(myId) || chats.getReceiverId().equals(myId) && chats.getSenderId().equals(senderId)) {
                        chatsArrayList.add(chats);
                    }
                    messageAdapter = new MessageAdapter(chatsArrayList, context, userId_sender);
                    recyclerView.setAdapter(messageAdapter);
                }
            }
        });
    }

    private void addChatInDataBase() {

        long tsLong = System.currentTimeMillis();
        timeStamp = Long.toString(tsLong);
        databaseViewModel.addChatDb(userId_receiver, userId_sender, chat, timeStamp, type);
        databaseViewModel.successAddChatDb.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    // Toast.makeText(MessageActivity.this, "Sent.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MessageActivity.this, "Message can't be sent.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        final String msg = chat;
        databaseViewModel.fetchingUserDataCurrent();
        databaseViewModel.fetchUserCurrentData.observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(DataSnapshot dataSnapshot) {
                Users users = dataSnapshot.getValue(Users.class);
                assert users != null;
                if (notify) {
//                    sendNotification(userId_receiver, users.getUsername(), msg);

                }
                notify = false;
            }
        });
    }

    private void sendNotification(String userId_receiver, String username, String msg) {
        databaseViewModel.getTokenDatabaseRef();
        databaseViewModel.getTokenRefDb.observe(this, new Observer<DatabaseReference>() {
            @Override
            public void onChanged(DatabaseReference databaseReference) {
                Query query = databaseReference.orderByKey().equalTo(userId_receiver);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Token token = snapshot.getValue(Token.class);
                            Data data = new Data(userId_sender, String.valueOf(R.drawable.ic_baseline_textsms_24), username + ": " + msg, "New Message", userId_receiver);

                            assert token != null;
                            Sender sender = new Sender(data, token.getToken());

                            apiService.sendNotification(sender)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if (response.code() == 200) {
                                                assert response.body() != null;
                                                if (response.body().success != 1) {

                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {

                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }


    private void currentUser(String userid) {
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }


    private void addStatusInDatabase(String status) {
        databaseViewModel.addStatusInDatabase("status", status);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addStatusInDatabase("online");
        currentUser(userId_receiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        addStatusInDatabase("offline");
        currentUser("none");
    }

}