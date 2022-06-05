package hcmute.spkt.truongminhhoang.zaloclone.view.ui;


import androidx.appcompat.app.AppCompatActivity;
import hcmute.spkt.truongminhhoang.zaloclone.R;

import android.content.Intent;
import android.os.Bundle;
public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}