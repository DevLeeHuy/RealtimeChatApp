package hcmute.spkt.truongminhhoang.zaloclone.view.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import hcmute.spkt.truongminhhoang.zaloclone.R;
import hcmute.spkt.truongminhhoang.zaloclone.services.repository.FirebaseInstanceDatabase;

public class SettingActivity extends AppCompatActivity {

    private EditText editTextDate;
    private EditText editTextSecond;
    private FirebaseInstanceDatabase instanceDatabase = new FirebaseInstanceDatabase();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_setting);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_baseline_arrow_back_24));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        this.editTextDate = (EditText) this.findViewById(R.id.editText_date);
        this.editTextSecond = (EditText) this.findViewById(R.id.editText_second);
        Button buttonSave = (Button) this.findViewById(R.id.button_save);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        getSetting();
    }

    private void save() {
        instanceDatabase
                .addSettingInDatabase(this.editTextDate.getText().toString(), this.editTextSecond.getText().toString())
                .observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean)
                            Toast.makeText(SettingActivity.this, "Save successfully", Toast.LENGTH_SHORT).show();
                        else {
                            Toast.makeText(SettingActivity.this, "ERROR WHILE ADDING DATA IN DATABASE.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void getSetting() {
        instanceDatabase.fetchSettingDataCurrent().observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(DataSnapshot dataSnapshot) {
                HashMap<String, String>setting = (HashMap<String, String>)dataSnapshot.getValue();
                assert setting != null;
                editTextDate.setText(setting.get("date"));
                editTextSecond.setText(setting.get("second"));
            }
        });

    }
}
