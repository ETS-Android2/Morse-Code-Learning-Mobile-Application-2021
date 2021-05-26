package com.example.aibasedmorsecode.home;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.aibasedmorsecode.R;
import com.example.aibasedmorsecode.database.controller.DBHandler;
import com.example.aibasedmorsecode.ownerusername.UsernameActivity;
import com.example.aibasedmorsecode.profile.ProfileActivity;
import com.example.aibasedmorsecode.model.User;
import com.example.aibasedmorsecode.morsecodelist.MorseCodeListActivity;
import com.example.aibasedmorsecode.multiuser.MultiUserMenuActivity;
import com.example.aibasedmorsecode.translator.TranslatorActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class MainActivity extends AppCompatActivity{

    public User user;

    /**
     * Initialize MainActivity class
     * @param savedInstanceState as Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = performDeserialization();
        if(user == null) goUsername();
        setContentView(R.layout.activity_main);
    }

    /**
     * @return Stored User object
     */
    private User performDeserialization() {
        System.out.println(getApplicationContext().getFilesDir().getPath());
        System.out.println("Deserialization started");
        try{
            FileInputStream fis=new FileInputStream(new File(getApplicationContext().getFilesDir().getPath(),"SaveFile.ser"));
            ObjectInputStream ois=new ObjectInputStream(fis);
            return (User) ois.readObject();
        }catch(IOException e){
            System.out.println("IOException!!");
            System.out.println("File not found...");
        }catch(ClassNotFoundException e){
            System.out.println("ClassNotFoundException!!");
        }
        return null;
    }

    /**
     * To go to username activity (Only when user file not found)
     */
    private void goUsername() {
        Intent intent = new Intent(getApplicationContext(), UsernameActivity.class);
        intent.putExtra("Message","Note that if you later wish to change your username, all status will be reset!");
        startActivity(intent);
        finish();
    }

    /**
     * To go to profile activity
     * @param view
     */
    public void goProfile(View view){
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        intent.putExtra("user",user);
        startActivity(intent);
    }

    /**
     * To go to Morse code list activity
     * @param view
     */
    public void goMorseCodeList(View view){
        Intent intent = new Intent(getApplicationContext(), MorseCodeListActivity.class);
        startActivity(intent);
    }

    /**
     * To go to Multi-User Menu activity
     * @param view
     */
    public void goMultiUser(View view){
        final WifiManager manager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if(manager.getWifiState() != WifiManager.WIFI_STATE_ENABLED){
            Toast.makeText(getApplicationContext(),"Please Turn On Wi-Fi",Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getApplicationContext(), MultiUserMenuActivity.class);
        intent.putExtra("user",user);
        startActivity(intent);
    }

    /**
     * To go to translator activity
     * @param view
     */
    public void goTranslator(View view){
        Intent intent = new Intent(getApplicationContext(), TranslatorActivity.class);
        startActivity(intent);
    }
}