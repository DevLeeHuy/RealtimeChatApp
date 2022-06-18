package hcmute.spkt.truongminhhoang.zaloclone.view.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
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
    private final FirebaseInstanceDatabase instanceDatabase = new FirebaseInstanceDatabase();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // Add back button to toolbar
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

        editTextDate = (EditText) findViewById(R.id.editText_date);
        editTextSecond = (EditText) findViewById(R.id.editText_second);
        Button buttonSave = (Button) findViewById(R.id.button_save);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        getSetting();
    }

    // Save setting
    private void save() {
        instanceDatabase
                .addSettingInDatabase(editTextDate.getText().toString(), editTextSecond.getText().toString())
                .observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean) {
                            Toast.makeText(SettingActivity.this, "Save successfully", Toast.LENGTH_SHORT).show();

                            // Trigger auto clear out of date items when there is a new setting
                            instanceDatabase.autoClearOutOfDateItems();
                        }
                        else {
                            Toast.makeText(SettingActivity.this, "ERROR WHILE ADDING DATA IN DATABASE.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Get setting and smash the value to edit fields
    private void getSetting() {
        instanceDatabase.fetchSettingDataCurrent().observe(this, new Observer<DataSnapshot>() {
            @Override
            public void onChanged(DataSnapshot dataSnapshot) {
                HashMap<String, String>setting = (HashMap<String, String>)dataSnapshot.getValue();
                if (setting != null) {
                    editTextDate.setText(setting.get("date"));
                    editTextSecond.setText(setting.get("second"));
                }
            }
        });

    }
}
