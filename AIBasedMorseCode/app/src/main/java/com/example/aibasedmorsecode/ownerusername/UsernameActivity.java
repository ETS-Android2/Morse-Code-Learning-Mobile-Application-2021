package com.example.aibasedmorsecode.ownerusername;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aibasedmorsecode.R;
import com.example.aibasedmorsecode.home.MainActivity;
import com.example.aibasedmorsecode.model.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class UsernameActivity extends AppCompatActivity {

    TextView textViewMessage;
    EditText editTextUsername;

    /**
     * Initialize MultiUserMenuActivity class
     * @param savedInstanceState as Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);

        textViewMessage = findViewById(R.id.textViewUsernameMessage);
        editTextUsername = findViewById(R.id.editTextUsername);

        textViewMessage.setText(getIntent().getExtras().getString("Message")); // hint from the calling activity
    }

    /**
     * Save username (Only if the length of username > 0), then go to the main UI
     * @param view
     */
    public void saveUsername(View view){
        String username = editTextUsername.getText().toString().trim();
        if(username.isEmpty()){
            editTextUsername.setHint("Please Enter Username");
            editTextUsername.setHintTextColor(Color.parseColor("red"));
            return;
        }
        performSerialization(new User(username));
        Toast.makeText(getApplicationContext(),username,Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Perform serialization to user's object
     * @param user
     */
    private void performSerialization(User user) {
        System.out.println("Serialization started");
        try{
            FileOutputStream fos=new FileOutputStream(new File(getApplicationContext().getFilesDir().getPath(),"SaveFile.ser"));
            ObjectOutputStream oos=new ObjectOutputStream(fos);
            oos.writeObject(user);
        }catch(IOException e){
            System.out.println("IOException!!");
            System.out.println(e);
        }
    }
}