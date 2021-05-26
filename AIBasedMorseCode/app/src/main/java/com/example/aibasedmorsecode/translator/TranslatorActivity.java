package com.example.aibasedmorsecode.translator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aibasedmorsecode.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;

public class TranslatorActivity extends AppCompatActivity {

    private EditText editText;
    private TextView textView;
    private Button button;
    private boolean toMorse = true;
    private HashMap<Character,String> textHashMap;
    private HashMap<String,Character> morseHashMap;

    /**
     * Initialize TranslatorActivity class
     * @param savedInstanceState as Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translator);

        editText = findViewById(R.id.editText);
        textView = findViewById(R.id.textViewResult);
        button = findViewById(R.id.buttonTransType);

        textHashMap = new HashMap<>();
        morseHashMap = new HashMap<>();
        setHashMapCharsToMorse();
        setHashMapCharsToText();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(toMorse) parseText(); else parseMorse();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * To copy the result of the translation
     * @param view
     */
    public void copyText(View view){
        ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("TextView",textView.getText().toString());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this,"Copied!",Toast.LENGTH_SHORT).show();
    }

    /**
     * Switch translation modes between Morse code and normal text
     * @param v
     */
    public void switchTransMode(View v){
        toMorse = !toMorse;
        if(toMorse){
            button.setText("Text -> Morse");
            editText.setHint("Type Text Here");
        } else{
            button.setText("Morse -> Text");
            editText.setHint("Type Morse Here");
        }
        editText.setText("");
        textView.setText("");
    }

    /**
     * Parse Morse code to get normal text (also print 'Not Valid!' if the input is invalid)
     */
    private void parseMorse() {
        StringBuilder text = new StringBuilder(editText.getText().toString().trim() + "e"); // e = end of text
        StringBuilder chars = new StringBuilder();
        textView.setText("");

        int j;
        while(true){
            if(text.indexOf(" ") != -1 && text.indexOf(" ") != 0) {
                j = text.indexOf(" ");
                chars.replace(0,chars.length(),text.substring(0, j));
                text.delete(0,j + 1);
                if(morseHashMap.get(chars.toString()) != null) {
                    textView.append(morseHashMap.get(chars.toString()).toString());
                } else {
                    textView.setText("Not Valid!");
                    break;
                }
            } else if (text.indexOf(" ") == 0){
                text.deleteCharAt(0);
            } else if (text.length() > 1){
                if(morseHashMap.get(text.substring(0,text.length() - 1).toString()) != null) {
                    textView.append(morseHashMap.get(text.substring(0,text.length() - 1)).toString());
                } else {
                    textView.setText("Not Valid!");
                }
                break;
            } else {
                if(text.length() != 0 && text.charAt(0) != 'e') { // e = end of text
                    textView.setText("Not Valid!");
                }
                break;
            }
        }
        textView.setText(textView.getText().toString().trim());
    }

    /**
     * Parse normal text to get Morse code (also print 'Not Valid!' if the input is invalid)
     */
    private void parseText() {
        String text = editText.getText().toString().trim();
        textView.setText("");

        for (int i = 0;i < text.length();i++){
            char c = text.charAt(i);
            if(textHashMap.get(c) != null) {
                textView.append(textHashMap.get(c) + " ");
            } else{
                textView.setText("Not Valid!");
                break;
            }
        }
        textView.setText(textView.getText().toString().trim());
    }

    /**
     * Set textHashMap's values
     */
    private void setHashMapCharsToMorse() {
        textHashMap.put('A',".-");
        textHashMap.put('B',"-...");
        textHashMap.put('C',"-.-.");
        textHashMap.put('D',"-..");
        textHashMap.put('E',".");
        textHashMap.put('F',"..-.");
        textHashMap.put('G',"--.");
        textHashMap.put('H',"....");
        textHashMap.put('I',"..");
        textHashMap.put('J',".---");
        textHashMap.put('K',"-.-");
        textHashMap.put('L',".-..");
        textHashMap.put('M',"--");
        textHashMap.put('N',"-.");
        textHashMap.put('O',"---");
        textHashMap.put('P',".--.");
        textHashMap.put('Q',"--.-");
        textHashMap.put('R',".-.");
        textHashMap.put('S',"...");
        textHashMap.put('T',"-");
        textHashMap.put('U',"..-");
        textHashMap.put('V',"...-");
        textHashMap.put('W',".--");
        textHashMap.put('X',"-..-");
        textHashMap.put('Y',"-.--");
        textHashMap.put('Z',"--..");

        textHashMap.put('a',".-");
        textHashMap.put('b',"-...");
        textHashMap.put('c',"-.-.");
        textHashMap.put('d',"-..");
        textHashMap.put('e',".");
        textHashMap.put('f',"..-.");
        textHashMap.put('g',"--.");
        textHashMap.put('h',"....");
        textHashMap.put('i',"..");
        textHashMap.put('j',".---");
        textHashMap.put('k',"-.-");
        textHashMap.put('l',".-..");
        textHashMap.put('m',"--");
        textHashMap.put('n',"-.");
        textHashMap.put('o',"---");
        textHashMap.put('p',".--.");
        textHashMap.put('q',"--.-");
        textHashMap.put('r',".-.");
        textHashMap.put('s',"...");
        textHashMap.put('t',"-");
        textHashMap.put('u',"..-");
        textHashMap.put('v',"...-");
        textHashMap.put('w',".--");
        textHashMap.put('x',"-..-");
        textHashMap.put('y',"-.--");
        textHashMap.put('z',"--..");

        textHashMap.put('1',".----");
        textHashMap.put('2',"..---");
        textHashMap.put('3',"...--");
        textHashMap.put('4',"....-");
        textHashMap.put('5',".....");
        textHashMap.put('6',"-....");
        textHashMap.put('7',"--...");
        textHashMap.put('8',"---..");
        textHashMap.put('9',"----.");
        textHashMap.put('0',"-----");

        // Punctuation marks and miscellaneous signs
        textHashMap.put('.',".-.-.-");
        textHashMap.put(',',"--..--");
        textHashMap.put(':',"---...");
        textHashMap.put('?',"..--..");
        textHashMap.put('\'',".----.");
        textHashMap.put('-',"-....-");
        textHashMap.put('/',"-..-.");
        textHashMap.put('(',"-.--.");
        textHashMap.put(')',"-.--.-");
        textHashMap.put('\"',".-..-.");
        textHashMap.put('=',"-...-");
        textHashMap.put('+',".-.-.");
        textHashMap.put('@',".--.-.");
        textHashMap.put('\'',".----.");
        textHashMap.put('!',"-.-.--");
        textHashMap.put('&',".-...");
        textHashMap.put(';',"-.-.-.");
        textHashMap.put('_',"..--.-");
        textHashMap.put('\"',".-..-.");
        textHashMap.put('$',"...-..-");

        textHashMap.put(' ',"/"); // word space
    }

    /**
     * Set morseHashMap's values
     */
    private void setHashMapCharsToText() {
        // letters (Capital)
        morseHashMap.put(".-",'A');
        morseHashMap.put("-...",'B');
        morseHashMap.put("-.-.",'C');
        morseHashMap.put("-..",'D');
        morseHashMap.put(".",'E');
        morseHashMap.put("..-.",'F');
        morseHashMap.put("--.",'G');
        morseHashMap.put("....",'H');
        morseHashMap.put("..",'I');
        morseHashMap.put(".---",'J');
        morseHashMap.put("-.-",'K');
        morseHashMap.put(".-..",'L');
        morseHashMap.put("--",'M');
        morseHashMap.put("-.",'N');
        morseHashMap.put("---",'O');
        morseHashMap.put(".--.",'P');
        morseHashMap.put("--.-",'Q');
        morseHashMap.put(".-.",'R');
        morseHashMap.put("...",'S');
        morseHashMap.put("-",'T');
        morseHashMap.put("..-",'U');
        morseHashMap.put("...-",'V');
        morseHashMap.put(".--",'W');
        morseHashMap.put("-..-",'X');
        morseHashMap.put("-.--",'Y');
        morseHashMap.put("--..",'Z');

        // Digits
        morseHashMap.put(".----",'1');
        morseHashMap.put("..---",'2');
        morseHashMap.put("...--",'3');
        morseHashMap.put("....-",'4');
        morseHashMap.put(".....",'5');
        morseHashMap.put("-....",'6');
        morseHashMap.put("--...",'7');
        morseHashMap.put("---..",'8');
        morseHashMap.put("----.",'9');
        morseHashMap.put("-----",'0');

        // Punctuation marks and miscellaneous signs
        morseHashMap.put(".-.-.-",'.');
        morseHashMap.put("--..--",',');
        morseHashMap.put("---...",':');
        morseHashMap.put("..--..",'?');
        morseHashMap.put(".----.",'\'');
        morseHashMap.put("-....-",'-');
        morseHashMap.put("-..-.",'/');
        morseHashMap.put("-.--.",'(');
        morseHashMap.put("-.--.-",')');
        morseHashMap.put(".-..-.",'\"');
        morseHashMap.put("-...-",'=');
        morseHashMap.put(".-.-.",'+');
        morseHashMap.put(".--.-.",'@');
        morseHashMap.put(".----.",'\'');
        morseHashMap.put("-.-.--",'!');
        morseHashMap.put(".-...",'&');
        morseHashMap.put("-.-.-.",';');
        morseHashMap.put("..--.-",'_');
        morseHashMap.put(".-..-.",'\"');
        morseHashMap.put("...-..-",'$');

        morseHashMap.put("/",' '); // word space
    }
}