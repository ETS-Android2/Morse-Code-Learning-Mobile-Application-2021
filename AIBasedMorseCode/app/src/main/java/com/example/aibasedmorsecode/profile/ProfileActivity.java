package com.example.aibasedmorsecode.profile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.aibasedmorsecode.R;
import com.example.aibasedmorsecode.model.User;
import com.example.aibasedmorsecode.ownerusername.UsernameActivity;
import com.example.aibasedmorsecode.profile.multiuserhistory.MultiUserHistoryList;


public class ProfileActivity extends AppCompatActivity {

    TextView textViewUsername, textViewWins, textViewLosses, textViewRank;
    User user;

    /**
     * Initialize ProfileActivity class
     * @param savedInstanceState as Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewWins = findViewById(R.id.textViewWins);
        textViewLosses = findViewById(R.id.textViewLosses);
        textViewRank  = findViewById(R.id.textViewRank);

        user = (User)getIntent().getExtras().getSerializable("user");

        textViewUsername.setText(user.getUsername());
        textViewWins.setText(user.getWins() + "");
        textViewLosses.setText(user.getLosses() + "");
        setTextViewRank();
    }

    /**
     * To go to username activity
     * @param view
     */
    public void goUsername(View view){
        Intent intent = new Intent(getApplicationContext(), UsernameActivity.class);
        intent.putExtra("Message","Warning: Any change to username will reset all status, including:\n - Num. Wins\n - Num. Losses\n - Rank");
        startActivity(intent);
    }

    /**
     * Set TextView's rank based on user's profile
     */
    private void setTextViewRank() {
        String rank = user.getCurrentRank();
        switch (rank){
            case "PLATINUM": textViewRank.setTextColor(Color.parseColor("purple")); break;
            case "SILVER": textViewRank.setTextColor(Color.parseColor("#6C7A86")); break;
            case "BRONZE": textViewRank.setTextColor(Color.parseColor("#FFA500")); break;
        }
        textViewRank.setText(rank);
    }

    /**
     * Go to Multi-User history activity
     * @param view
     */
    public void goMultiUserHistory(View view){
        Intent intent = new Intent(getApplicationContext(), MultiUserHistoryList.class);
        startActivity(intent);
    }
}