package com.example.aibasedmorsecode.morsecodelist;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.aibasedmorsecode.R;

public class MorseCodeListActivity extends AppCompatActivity {

    /**
     * Initialize MorseCodeListActivity class
     * @param savedInstanceState as Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_morse_code_list);
    }
}