package hcmute.spkt.truongminhhoang.zaloclone.view.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;

import de.hdodenhof.circleimageview.CircleImageView;
import hcmute.spkt.truongminhhoang.zaloclone.R;
import hcmute.spkt.truongminhhoang.zaloclone.services.model.Users;
import hcmute.spkt.truongminhhoang.zaloclone.services.repository.FirebaseInstanceDatabase;
import hcmute.spkt.truongminhhoang.zaloclone.view.adapters.ViewPagerAdapter;
import hcmute.spkt.truongminhhoang.zaloclone.view.fragments.ChatFragment;
import hcmute.spkt.truongminhhoang.zaloclone.view.fragments.ContactFragment;
import hcmute.spkt.truongminhhoang.zaloclone.view.fragments.ProfileFragment;
import hcmute.spkt.truongminhhoang.zaloclone.view.fragments.UserFragment;
import hcmute.spkt.truongminhhoang.zaloclone.viewModel.DatabaseViewModel;
import hcmute.spkt.truongminhhoang.zaloclone.viewModel.LogInViewModel;

public class HomeActivity extends AppCompatActivity {
    LogInViewModel logInViewModel;
    Toolbar toolbar;
    DatabaseViewModel databaseViewModel;

    LinearLayout linearLayout;
    ProgressBar progressBar;
    TextView currentUserName;
    CircleImageView profileImage;
    String username;
    String imageUrl;

    TabLayout tabLayout;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        init(); // inflate view components
        fetchCurrentUserdata(); // get current user data based on user's token
        setupPagerFragment();// setup paper
        onOptionMenuClicked();//setup menu & its events

    }

    private void setupPagerFragment() {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), ViewPagerAdapter.POSITION_UNCHANGED);

        viewPagerAdapter.addFragment(new ChatFragment(this), "Chats");
        viewPagerAdapter.addFragment(new ContactFragment(this), "Contacts");
        viewPagerAdapter.addFragment(new UserFragment(this), "Users");
        viewPagerAdapter.addFragment(new ProfileFragment(this), "Profile");

        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);

    }

    private void fetchCurrentUserdata() {
        databaseViewModel.fetchingUserDataCurrent(); //get user data
        databaseViewModel.fetchUserCurrentData.observe(this, new Observer<DataSnapshot>() { //listen to event
            @Override
            public void onChanged(DataSnapshot dataSnapshot) { //trigger when fetching/data changing
                Users user = dataSnapshot.getValue(Users.class); //get user data
                if (user != null) { //if user is valid/not null (
                    progressBar.setVisibility(View.GONE);
                    linearLayout.setVisibility(View.VISIBLE);
                    username = user.getUsername(); //store data into variable
                    imageUrl = user.getImageUrl();
                    //  Toast.makeText(HomeActivity.this, "Welcome back " + username + ".", Toast.LENGTH_SHORT).show();
                    currentUserName.setText(username);
                    if (imageUrl.equals("default")) { // check if user avatar is default => if not => need to convert in order to show it into screen
                        profileImage.setImageResource(R.drawable.sample_img);
                    } else {
                        Glide.with(getApplicationContext()).load(imageUrl).into(profileImage);
                    }
                } else { // if user not found => inform to user
                    Toast.makeText(HomeActivity.this, "User not found..", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void getUserAuthToSignOut() { // logout function
        logInViewModel.getFirebaseAuth();
        logInViewModel.firebaseAuthLiveData.observe(this, new Observer<FirebaseAuth>() {
            @Override
            public void onChanged(FirebaseAuth firebaseAuth) {
                firebaseAuth.signOut();
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void onOptionMenuClicked() {
        toolbar.inflateMenu(R.menu.logout);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.log_out_home) { // when choosing logout option
                    getUserAuthToSignOut();
                    Toast.makeText(HomeActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (item.getItemId() == R.id.setting) { //when choosing setting option
                    Intent intent = new Intent(HomeActivity.this, SettingActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else {
                    return false;
                }
            }

        });
    }





    private void init() {

        logInViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication()))
                .get(LogInViewModel.class);

        toolbar = findViewById(R.id.toolbar);

        databaseViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication()))
                .get(DatabaseViewModel.class);

        currentUserName = findViewById(R.id.tv_username);
        profileImage = findViewById(R.id.iv_profile_image);
        linearLayout = findViewById(R.id.linearLayout);
        progressBar = findViewById(R.id.progress_bar);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(2); // to go to profile fragment
            }
        });

        new FirebaseInstanceDatabase().autoClearOutOfDateItems(); //auto delete audio/image mechanism function

    }

    private void status(String status){
        databaseViewModel.addStatusInDatabase("status", status);
    }

    @Override
    protected void onResume() { //change user status back to online when user get back to app
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() { // change user status to offline when user quit without logout
        super.onPause();
        status("offline");
    }
}