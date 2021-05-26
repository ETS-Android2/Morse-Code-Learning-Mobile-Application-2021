package com.example.aibasedmorsecode.multiuser;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.aibasedmorsecode.R;
import com.example.aibasedmorsecode.model.User;
import com.example.aibasedmorsecode.multiuser.activesession.MultiUserSessionActivity;

public class MultiUserMenuActivity extends AppCompatActivity {

    User user;

    /**
     * Initialize MultiUserMenuActivity class
     * @param savedInstanceState as Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_user_menu);

        user = (User) getIntent().getExtras().getSerializable("user");
    }

    /**
     * Go to session setting activity
     * @param view
     */
    public void goSessionSettingActivity(View view){
        Intent intent = new Intent(getApplicationContext(), SessionSettingActivity.class);
        intent.putExtra("user",user);
        startActivity(intent);
    }

    /**
     * Go to session joining activity
     * @param view
     */
    public void goSessionJoiningActivity(View view){
        Intent intent = new Intent(getApplicationContext(), MultiUserSessionActivity.class);
        intent.putExtra("user",user);
        intent.putExtra("isHost",0);
        startActivity(intent);
    }
}