package com.example.aibasedmorsecode.multiuser;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.aibasedmorsecode.R;
import com.example.aibasedmorsecode.model.User;
import com.example.aibasedmorsecode.multiuser.activesession.MultiUserSessionActivity;

public class SessionSettingActivity extends AppCompatActivity {

    EditText etSessionName;
    Spinner sNumP, sNumQ, sCategory;
    RadioGroup radioGroup;
    RadioButton radioButton;
    User user;

    /**
     * Initialize SessionSettingActivity class
     * @param savedInstanceState as Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_setting);

        user = (User) getIntent().getExtras().getSerializable("user");

        etSessionName = findViewById(R.id.editTextSessionName);
        sNumP = findViewById(R.id.spinnerNumP);
        sNumQ = findViewById(R.id.spinnerNumQ);
        sCategory = findViewById(R.id.spinnerCategory);
        radioGroup = findViewById(R.id.radioGroup);
        radioButton = findViewById(R.id.radioButton); // training option as default

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioButton = findViewById(checkedId);
                sCategory.setEnabled(!sCategory.isEnabled());
            }
        });
    }

    /**
     * Go to session waiting activity (Only if etSessionName is set)
     * @param view
     */
    public void goSessionWaitingActivity(View view){
        String name = etSessionName.getText().toString().trim();
        if(name.length() == 0) {
            etSessionName.setHint("Please Write Session Name");
            etSessionName.setHintTextColor(Color.parseColor("red"));
            return;
        }

        Intent intent = new Intent(getApplicationContext(), MultiUserSessionActivity.class);
        intent.putExtra("name",name);
        intent.putExtra("numP",Integer.parseInt(sNumP.getSelectedItem().toString()));
        intent.putExtra("numQ",Integer.parseInt(sNumQ.getSelectedItem().toString()));
        intent.putExtra("learningType",radioButton.getText().toString());
        if(sCategory.isEnabled()){
            intent.putExtra("categoryType", sCategory.getSelectedItem().toString());
            Toast.makeText(getApplicationContext(), sCategory.getSelectedItem().toString(),Toast.LENGTH_LONG).show();
        }
        intent.putExtra("user",user);
        intent.putExtra("isHost",1);
        startActivity(intent);
    }
}