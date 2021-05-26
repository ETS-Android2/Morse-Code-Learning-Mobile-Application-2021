package com.example.aibasedmorsecode.multiuser.activesession;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.aibasedmorsecode.R;
import com.example.aibasedmorsecode.database.controller.DBHandler;
import com.example.aibasedmorsecode.home.MainActivity;
import com.example.aibasedmorsecode.camera.Camera2BasicFragment;
import com.example.aibasedmorsecode.model.Category;
import com.example.aibasedmorsecode.model.MultiUserSession;
import com.example.aibasedmorsecode.model.Receiver;
import com.example.aibasedmorsecode.model.User;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MultiUserSessionActivity extends AppCompatActivity {

    //---------------------------------- common -------------------------------------
    private TextView textViewSessionName, textViewWaitingMessage;
    private TextView textViewParticipantsList, textViewPointsList, textViewRoundNum;
    private boolean buttonTapped = false; // for any sending buttons
    private Handler handler = new Handler();
    private int isHost;

    //---------------------------------- sender -------------------------------------
    private Server serverThread;
    private EditText editTextMessage;
    private Button startButton, continueButton, endButton, sendButton;
    private boolean startButtonTapped, continueButtonTapped, endButtonTapped;

    //---------------------------------- receiver -------------------------------------
    private Client clientThread;
    private TextView tvJoinMessage;
    private TextView textViewMessage, textViewResult;
    private EditText etIPNum1, etIPNum2, etIPNum3, etIPNum4;
    private EditText editTextAnswer;
    private Button answerButton;
    private boolean joinButtonTapped;

    /**
     * Initialize MultiUserSessionActivity class
     * @param savedInstanceState as Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isHost = getIntent().getExtras().getInt("isHost");
        switch (isHost){
            case 1: // Host (Server)
                setContentView(R.layout.activity_session_waiting);
                textViewSessionName = findViewById(R.id.activeSessionName);
                textViewWaitingMessage = findViewById(R.id.text_waiting_on_ip);
                textViewParticipantsList = findViewById(R.id.sessionParticipantsList);
                startButton = findViewById(R.id.startButton);

                startButton.setVisibility(View.VISIBLE);
                startButton.setEnabled(false);

                serverThread = new Server();
                serverThread.start();
                break;

            case 0: // Not Host (Client)
                setContentView(R.layout.activity_session_joining);
                tvJoinMessage = findViewById(R.id.joiningMessage);
                etIPNum1 = findViewById(R.id.editTextNumber);
                etIPNum2 = findViewById(R.id.editTextNumber2);
                etIPNum3 = findViewById(R.id.editTextNumber3);
                etIPNum4 = findViewById(R.id.editTextNumber4);

                clientThread = new Client();
                clientThread.start();
                break;
        }
    }

    /**
     * Set ButtonTapped To (true)
     * @param view
     */
    public void setButtonTappedToTrue(View view){ buttonTapped = true; }

    /**
     * Set startButtonTapped To (true)
     * @param view
     */
    public void setStartButtonTappedToTrue(View view){ startButtonTapped = true; }

    /**
     * Set joinButtonTapped To (true)
     * @param view
     */
    public void setJoinButtonTappedToTrue(View view){ joinButtonTapped = true; }

    /**
     * Set continueButtonTapped To (true)
     * @param view
     */
    public void setContinueButtonTappedToTrue(View view){ continueButtonTapped = true; }

    /**
     * Set endButtonTapped To (true)
     * @param view
     */
    public void setEndButtonTappedToTrue(View view){ endButtonTapped = true; }

    /**
     * To close server/client thread (Called when closing the activity)
     */
    @Override
    public void onStop() {
        super.onStop();
        switch (isHost) {
            case 1: // Host (Server)
                if(!serverThread.isInterrupted()){
                    serverThread.closeConnection();
                    serverThread.interrupt();
                    System.out.println("serverThread: interrupted!");
                }
                break;
            case 0: // Not Host (Client)
                if(!clientThread.isInterrupted()){
                    clientThread.interrupt();
                    System.out.println("clientThread: interrupted!");
                }
                break;
        }
    }

    class Server extends Thread { // ----------------------- server thread -----------------------------

        private final int PORT = 9090;
        private Socket socket;
        private ServerSocket listener;
        private DBHandler dbHandler;
        private MultiUserSession multiUserSession;
        private User hostUser;
        private ArrayList<ClientHandler> clientHandlers;
        private HashMap<Character,String> textHashMap;
        private Category category;
        private String message, answer, result;

        /**
         * To perform server's operations and play as Sender or Receiver.
         * If "Training, then the host is the going to be a receiver.
         * If "Exam",  then the host is the going to be a sender, and the communication will be: sender's flashlight -> light signals -> receiver's camera.
         * When finish playing, the server store the new state of user's profile.
         */
        @Override
        public void run() {
            dbHandler = new DBHandler(getApplicationContext());
            dbHandler.init();
            message = ""; // initialization to avoid some errors
            answer = ""; // initialization to avoid some errors
            Intent intent = getIntent();
            multiUserSession = new MultiUserSession(intent.getExtras().getString("name"));
            hostUser = (User) intent.getExtras().getSerializable("user");
            int numP = intent.getExtras().getInt("numP");
            int numQ = intent.getExtras().getInt("numQ");
            String learningType = intent.getExtras().getString("learningType");
            String categoryType = intent.getExtras().getString("categoryType");
            multiUserSession.setLearningType(learningType);

            if(learningType.equals("Training")) {
                multiUserSession.getReceivers().add(new Receiver(hostUser.getUsername()));
                multiUserSession.setCategoryName(categoryType);
                multiUserSession.setCategoryid(dbHandler.getCategory(categoryType).getId());
            } else { // Exam
                multiUserSession.setSenderName(hostUser.getUsername());
            }

            clientHandlers = new ArrayList<>();
            textHashMap = new HashMap<>();
            setHashMapCharsToMorse();

            if(categoryType != null) {
                category = dbHandler.getCategory(categoryType);
            }

            createServerListener();

            // add host to the participant list
            StringBuilder participantListText = new StringBuilder();
            participantListText.append(hostUser.toString() + "\n");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    textViewParticipantsList.setText(participantListText.toString());   // add to the list of participants text view
                }
            });

            // add clients to the participant list
            for (int i = 0; i < numP; i++) {
                    Log.d("[server]", "Waiting...");
                try {
                    socket = listener.accept();
                    DataInputStream dis = new DataInputStream(socket.getInputStream());
                    String username = dis.readUTF(); // user name
                    Receiver participant = new Receiver(username);
                    ClientHandler clientHandler = new ClientHandler(socket, participant, dis);
                    multiUserSession.getReceivers().add(participant);
                    Log.d("[server]", participant + " -> Accepted!");

                    clientHandler.getDos().writeUTF(multiUserSession.getSessionName());  // session name
                    clientHandler.getDos().flush();
                    clientHandler.getDos().writeUTF("Waiting For Participants on IP: " + getIPAddress(true)); // Waiting message
                    clientHandler.getDos().flush();

                    participantListText.append(participant.toString() + "\n");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            textViewParticipantsList.setText(participantListText.toString());   // add to the list of participants text view
                        }
                    });

                    clientHandler.getDos().writeUTF(numP + "");
                    clientHandler.getDos().flush();
                    clientHandler.getDos().writeUTF(i + ""); // num. current participants connected to the host
                    clientHandler.getDos().flush();
                    clientHandler.getDos().writeUTF(numQ + "");
                    clientHandler.getDos().flush();
                    clientHandler.getDos().writeUTF(learningType); // Training or Exam
                    clientHandler.getDos().flush();
                    if(learningType.equals("Training")) {
                        clientHandler.getDos().writeUTF(multiUserSession.getCategoryName()); // host name
                        clientHandler.getDos().flush();
                    } else { // Exam
                        clientHandler.getDos().writeUTF(hostUser.getUsername()); // host name
                        clientHandler.getDos().flush();
                    }

                    clientHandlers.add(clientHandler);
                    new Thread(clientHandler).start();
                    // end of adding current participant

                    for (ClientHandler ch : clientHandlers) {
                        ch.getDos().writeUTF(participantListText.toString());  // add to the list of participants text view
                        ch.getDos().flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    textViewWaitingMessage.setText("Ready");
                    textViewWaitingMessage.setBackgroundColor(Color.parseColor("#79DC07"));
                    startButton.setEnabled(true);
                }
            });

            while (true) {
                if (startButtonTapped) {
                    sendSessionStartSignal();
                    startButtonTapped = false;
                    break;
                }
            }

            //-------------------------------- Start Session ----------------------------------
            switch (learningType){ // which learning type?
                case "Training": //----------------------------------- Training Mode
                    prepareReceiverScreenElements();
                    while (true) {
                        for (int i = 0; i < numQ; i++) {
                            setReceiverScreenElementsValue();
                            int min = 0;
                            int max = category.getQuestions().size() - 1;
                            int qNum = (int)(Math.random() * (max - min + 1) + min);
                            message = category.getQuestions().get(qNum).getMessage();
                            Log.d("[server]", "selected message: " + message);
                            sendMessage();
                            String morseMessage = parseText(message);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    answerButton.setEnabled(true);
                                    textViewMessage.setText(morseMessage);
                                }
                            });

                            while (true) {
                                if (buttonTapped) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            answer = editTextAnswer.getText().toString().trim();
                                        }
                                    });
                                    waitForMilliSeconds(500);
                                    if(answer.isEmpty()){
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                editTextAnswer.setHint("Please Write Answer");
                                                editTextAnswer.setHintTextColor(Color.parseColor("red"));
                                            }
                                        });
                                        buttonTapped = false;
                                        continue;
                                    } else {
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                answerButton.setEnabled(false);
                                            }
                                        });
                                    }
                                    waitForMilliSeconds(500);

                                    int j;
                                    while (true) { // check if all receivers sent answers
                                        j = 0; // num. received answers
                                        for (ClientHandler ch : clientHandlers) {
                                            if (!ch.getMessage().isEmpty()) { // if receiver has answered
                                                j++;
                                            }
                                        }
                                        if (j == clientHandlers.size()) { // if all receivers have answered
                                            checkParticipantsAnswersAndSetResults();
                                            checkHostAnswerAndSetResult();
                                            displayServerResult();
                                            setPointsListToAllUsers();
                                            buttonTapped = false;
                                            break;
                                        }
                                    }
                                    waitForMilliSeconds(5000); // wait for the next round
                                    break;
                                }
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    editTextAnswer.setHint("");
                                }
                            });
                        }

                        switchEndAndContinueButtonsValues();
                        while(true){
                            if(continueButtonTapped || endButtonTapped){
                                switchEndAndContinueButtonsValues();
                                waitForMilliSeconds(500);
                                continueButtonTapped = false;
                                break;
                            }
                        }
                        if(endButtonTapped){ // finish session
                            break;
                        }
                        for (ClientHandler ch: clientHandlers){ // continue
                            try {
                                ch.getDos().writeUTF("Continue");
                                ch.getDos().flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } // end of training session
                    break;
                case "Exam": //----------------------------------------- Exam Mode
                    prepareSenderScreenElements();
                    while(true) {
                        for (int i = 0; i < numQ; i++) {
                            setSenderScreenElementsValue();
                            while (true) {
                                if (buttonTapped) { // send button
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            message = editTextMessage.getText().toString().trim();
                                        }
                                    });
                                    waitForMilliSeconds(500);
                                    if(message.isEmpty()){
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                editTextMessage.setHint("Please Write Message");
                                                editTextMessage.setHintTextColor(Color.parseColor("red"));
                                            }
                                        });
                                        buttonTapped = false;
                                        continue;
                                    } else {
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                sendButton.setEnabled(false);
                                            }
                                        });
                                    }
                                    waitForMilliSeconds(500);
                                    String flashMessage = parseText(message);
                                    flashLightMessage(flashMessage);
                                    sendMessage();  // Sender's message
                                    buttonTapped = false;
                                    break;
                                }
                            }

                            int j; // counter for num. receivers who answer
                            while (true) { // check if all receivers sent answers
                                j = 0; // num. received answers
                                for (ClientHandler ch : clientHandlers) {
                                    if (!ch.getMessage().isEmpty()) { // if receiver has answered
                                        j++;
                                    }
                                }
                                if (j == clientHandlers.size()) { // if all receivers have answered
                                    checkParticipantsAnswersAndSetResults();
                                    setPointsListToParticipants();
                                    break;
                                }
                            }
                            waitForMilliSeconds(5000);
                        }

                        switchEndAndContinueButtonsValues();
                        while(true){
                            if(continueButtonTapped || endButtonTapped){
                                switchEndAndContinueButtonsValues();
                                waitForMilliSeconds(500);
                                continueButtonTapped = false;
                                break;
                            }
                        }
                        if(endButtonTapped){
                            break;
                        }
                        for (ClientHandler ch: clientHandlers){
                            try {
                                ch.getDos().writeUTF("Continue");
                                ch.getDos().flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } // end of Exam session
                    break;
            }

            closeClientHandlerConnection();
            closeConnection();

            if(learningType.equals("Training")) {
                increaseUserWinsXorLosses();
                performSerialization();
            }

            dbHandler.addMultiUserSession(multiUserSession);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                }
            });
        }

        /**
         * Increase User's Wins if he/she wins XOR Losses if he/she loses
         */
        private void increaseUserWinsXorLosses() {
            Receiver winner = multiUserSession.getReceivers().get(0); // assume this user (host) is the winner
            for(Receiver r: multiUserSession.getReceivers()){
                if(r.getPoints() > winner.getPoints()){
                    hostUser.increaseLosses();
                    return;
                }
            }
            hostUser.increaseWins(); // if he/she actually the winner
        }

        /**
         * Perform Serialization for hostUser object
         */
        private void performSerialization() {
            System.out.println("Serialization started");
            try{
                FileOutputStream fos=new FileOutputStream(new File(getApplicationContext().getFilesDir().getPath(),"SaveFile.ser"));
                ObjectOutputStream oos=new ObjectOutputStream(fos);
                oos.writeObject(hostUser);
            }catch(IOException e){
                System.out.println("IOException!!");
                System.out.println(e);
            }
        }

        /**
         * (Enable,VISIBLE) Toggle for (endButton,continueButton)
         */
        private void switchEndAndContinueButtonsValues() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    endButton.setEnabled(!endButton.isEnabled());
                    continueButton.setEnabled(!continueButton.isEnabled());
                    if(endButton.isEnabled()){
                        endButton.setVisibility(View.VISIBLE);
                        continueButton.setVisibility(View.VISIBLE);
                    } else{
                        endButton.setVisibility(View.INVISIBLE);
                        continueButton.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }

        /**
         * Set current points list to all Users
         */
        private void setPointsListToAllUsers() {
            StringBuilder points = new StringBuilder(); // points
            for(Receiver r: multiUserSession.getReceivers()){
                points.append(r.getPoints() + " " + ((r.getPoints() > 1)?"Points":"Point") + "\n");
            }

            for(ClientHandler ch: clientHandlers){
                try {
                    ch.getDos().writeUTF(points.toString());
                    ch.getDos().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    textViewPointsList.setText(points);
                }
            });
        }

        /**
         * Set current points list to participants
         */
        private void setPointsListToParticipants() {
            StringBuilder points = new StringBuilder();

            points.append("Sender:    (Host)\n"); // the first user is the host, who doesn't have points
            for(ClientHandler ch: clientHandlers){
                points.append("Receiver: " + ch.getReceiver().getPoints() + " " + ((ch.getReceiver().getPoints() > 1)?"Points":"Point") + "\n");
            }

            for(ClientHandler ch: clientHandlers){
                try {
                    ch.getDos().writeUTF(points.toString());
                    ch.getDos().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    textViewPointsList.setText(points);
                }
            });
        }

        /**
         * It uses Thread.sleep(ms)
         * @param ms (milliSeconds) as int
         */
        private void waitForMilliSeconds(int ms) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * Create Server Listener and set 'waitingMessage' with given IP address by getIPAddress() method
         */
        private void createServerListener() {
            try {
                listener = new ServerSocket(PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String waitingMessage = "Waiting For Participants on IP: " + getIPAddress(true);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    textViewSessionName.setText(multiUserSession.getSessionName());
                    textViewWaitingMessage.setText(waitingMessage);
                }
            });
        }

        /**
         * Prepare Receiver UI's Elements
         */
        private void prepareReceiverScreenElements() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    String participantListText = textViewParticipantsList.getText().toString();
                    setContentView(R.layout.activity_receiver);
                    textViewMessage = findViewById(R.id.message);
                    textViewResult = findViewById(R.id.result);
                    editTextAnswer = findViewById(R.id.receiverAnswer);
                    answerButton = findViewById(R.id.sendAnswer);
                    textViewSessionName = findViewById(R.id.activeSessionName);
                    textViewRoundNum = findViewById(R.id.roundNum);
                    textViewParticipantsList = findViewById(R.id.sessionParticipantsList);
                    textViewPointsList = findViewById(R.id.sessionPointsList);
                    continueButton = findViewById(R.id.continueButton);
                    endButton = findViewById(R.id.endButton);

                    answerButton.setEnabled(false);
                    textViewSessionName.setText(multiUserSession.getSessionName());
                    textViewParticipantsList.setText(participantListText);
                }
            });
            waitForMilliSeconds(500);
        }

        /**
         * Set Receiver's UI Elements' Value
         */
        private void setReceiverScreenElementsValue() {
            multiUserSession.increaseRoundNum();
            setPointsListToAllUsers();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    textViewRoundNum.setText("Round " + multiUserSession.getNumRounds());
                    answerButton.setEnabled(false);
                    textViewMessage.setText("");
                    textViewResult.setText("");
                    editTextAnswer.setText("");
                    editTextAnswer.setHint("");
                }
            });
        }

        /**
         * Prepare Sender UI's Elements
         */
        private void prepareSenderScreenElements() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    String participantListText = textViewParticipantsList.getText().toString();
                    setContentView(R.layout.activity_sender);
                    textViewSessionName = findViewById(R.id.activeSessionName);
                    textViewRoundNum = findViewById(R.id.roundNum);
                    textViewParticipantsList = findViewById(R.id.sessionParticipantsList);
                    textViewPointsList = findViewById(R.id.sessionPointsList);
                    editTextMessage = findViewById(R.id.senderMessage);
                    sendButton = findViewById(R.id.sendButton);
                    continueButton = findViewById(R.id.continueButton);
                    endButton = findViewById(R.id.endButton);

                    textViewSessionName.setText(multiUserSession.getSessionName());
                    textViewParticipantsList.setText(participantListText);
                    editTextMessage.setText("");
                    sendButton.setEnabled(true);
                }
            });
        }

        /**
         * Close clientHandlers' connection
         */
        private void closeClientHandlerConnection() {
            try {
                for (ClientHandler ch : clientHandlers) {
                    Log.d("[server]", "close " + ch.getReceiver());
                    ch.getDos().writeUTF("CLOSE_CONNECTION");
                    ch.getDos().flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Close server's connection
         */
        public void closeConnection() {
            try {
                if(!listener.isClosed()) {
                    listener.close();
                    Log.d("[server]", "Connection Closed!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * source -> https://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device-from-code
         * by -> Whome (answered Oct 22 '12 at 8:12) & exploitr (edited May 11 '18 at 17:14)
         * @param useIPv4 as boolean
         * @return IP address as String
         */
        public String getIPAddress(boolean useIPv4) {
            try {
                List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface intf : interfaces) {
                    List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                    for (InetAddress addr : addrs) {
                        if (!addr.isLoopbackAddress()) {
                            String sAddr = addr.getHostAddress();
                            //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                            boolean isIPv4 = sAddr.indexOf(':')<0;

                            if (useIPv4) {
                                if (isIPv4)
                                    return sAddr;
                            } else {
                                if (!isIPv4) {
                                    int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                    return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) { } // for now eat exceptions
            return "";
        }

        /**
         * Set Sender's UI Elements' Value
         */
        private void setSenderScreenElementsValue() {
            setPointsListToParticipants();
            multiUserSession.increaseRoundNum();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    textViewRoundNum.setText("Round " + multiUserSession.getNumRounds());
                    editTextMessage.setText("");
                    editTextMessage.setHint("");
                    sendButton.setEnabled(true);
                }
            });
        }

        /**
         * Check participants' answers , then set results
         */
        private void checkParticipantsAnswersAndSetResults() {
            // for clients:
            for (ClientHandler ch : clientHandlers) {
                if(ch.getMessage().equalsIgnoreCase(message)){
                    try {
                        Log.d("[server]", "client "+"["+ch.getReceiver()+"]"+" said: " + ch.getMessage());
                        Log.d("[server]", "Sending Result...");
                        ch.getReceiver().increasePoints(); // by 5 for now...
                        ch.getDos().writeUTF("T"); // true
                        ch.getDos().flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Log.d("[server]", "client "+"["+ch.getReceiver()+"]"+" said: " + ch.getMessage());
                        Log.d("[server]", "Sending Result...");
                        ch.getDos().writeUTF("F"); // false
                        ch.getDos().flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                ch.setMessage(""); // reset
            }
        }

        /**
         * Check host's answer, then set result
         */
        private void checkHostAnswerAndSetResult(){
            // for server:
            Log.d("[server]", "I said: " + answer);
            if(answer.equalsIgnoreCase(message)){
                multiUserSession.getReceivers().get(0).increasePoints(); // the hostUser user itself
                result = "T"; // true
            } else {
                result = "F"; // true
            }
        }

        /**
         * Display server's result
         */
        private void displayServerResult() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (result.equals("T")) {
                        String resultText;
                        textViewResult.setTextColor(Color.parseColor("#89FF00"));
                        int min = 0;
                        int max = 2;
                        switch ((int)(Math.random() * (max - min + 1) + min)){
                            case 0:resultText = "Great Job!"; break;
                            case 1:resultText = "Impressive!"; break;
                            default:resultText = "Splendid!";
                        }
                        textViewResult.setText(resultText);
                    } else {
                        String resultText;
                        textViewResult.setTextColor(Color.parseColor("red"));
                        int min = 0;
                        int max = 2;
                        switch ((int)(Math.random() * (max - min + 1) + min)){
                            case 0:resultText = "Wrong, Try Next Time..."; break;
                            case 1:resultText = "Wrong, You Can Do Better!"; break;
                            default:resultText = "Wrong, Do Your Best Next Time";
                        }
                        textViewResult.setText(resultText);
                    }
                }
            });
        }

        /**
         * Send message to connected clients
         */
        private void sendMessage() {
            for (ClientHandler ch : clientHandlers) {
                try {
                    ch.getDos().writeUTF(parseText(message));
                    ch.getDos().flush();
                    Log.d("[server]", "Sending: " + message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Send session's start signal (".") to connected clients
         */
        private void sendSessionStartSignal() {
            for (ClientHandler ch : clientHandlers) {
                try {
                    ch.getDos().writeUTF("."); // session's start signal to receiver
                    ch.getDos().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Parse Text to get corresponding Morse code
         * @param message
         * @return morse as String
         */
        private String parseText(String message) {
            StringBuilder morse = new StringBuilder();

            for (int i = 0;i < message.length();i++){
                char c = message.charAt(i);
                if(textHashMap.get(c) != null) {
                    morse.append(textHashMap.get(c) + " ");
                } else{
                    morse = new StringBuilder("Not Valid!");
                    return morse.toString();
                }
            }
            return morse.toString().trim();
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
         * To turn off the flashlight
         */
        private void flashLightOff() {
            CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

            try {
                String cameraId = cameraManager.getCameraIdList()[0];
                cameraManager.setTorchMode(cameraId, false);
            } catch (CameraAccessException e) {
            }
        }

        /**
         * To turn on the flashlight
         */
        private void flashLightOn() {
            CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

            try {
                String cameraId = cameraManager.getCameraIdList()[0];
                cameraManager.setTorchMode(cameraId, true);
            } catch (CameraAccessException e) {
            }
        }

        /**
         * Convert Morse code into light signals using flashlight
         * @param message
         */
        private void flashLightMessage(String message)
        {
            message = message.trim();
            for (int i = 0; i < message.length(); i++) {
                waitForMilliSeconds(500);
                if (message.charAt(i) == '.')
                {
                    flashLightOn();
                    waitForMilliSeconds(500);
                    flashLightOff();
                }
                else if(message.charAt(i) == '/')
                {
                    waitForMilliSeconds(3000);
                }
                else
                {
                    flashLightOn();
                    waitForMilliSeconds(1500);
                    flashLightOff();
                }
            }
            flashLightOff();
        }
    } // ------------------------------ server thread --------------------------------

    class Client extends Thread { // --------------------------- client thread ---------------------------

        private String ip, ipPart1, ipPart2, ipPart3, ipPart4;
        private final int PORT = 9090;
        private Socket socket;
        private DBHandler dbHandler;
        private DataInputStream dis;
        private DataOutputStream dos;
        private MultiUserSession multiUserSession;
        private User clientUser;
        private String message, answer;

        /**
         * To perform client's operations and play as Receiver.
         * If "Training, then the client will not use the camera, the communication will be only through networking.
         * If "Exam",  then the client will use the camera, and the communication will be: sender's flashlight -> light signals -> receiver's camera.
         * When finish playing, the client store the new state of user's profile.
         */
        @Override
        public void run() {
            dbHandler = new DBHandler(getApplicationContext());
            dbHandler.init();
            answer = ""; // initialization to avoid some errors
            multiUserSession = new MultiUserSession();
            clientUser  = (User) getIntent().getExtras().getSerializable("user");
            socket = new Socket();
            while (true) {
                if(joinButtonTapped) {
                    ip = getEnteredIPText();
                    setUIJoinMessage("Looking For: " + ip + " ...");
                    try {
                        socket.connect(new InetSocketAddress(ip, PORT), 5000);
                        joinButtonTapped = false;
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                        try {
                            socket.close();
                            socket = new Socket();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                        joinButtonTapped = false;
                        setUIJoinMessage("Not Found... Try Again");
                    }
                }
            } // Connected

            try {
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            writeToServerBuffer(clientUser.getUsername()); // send username

            multiUserSession.setSessionName(readServerMessage()); // session name
            String waitingMessage = readServerMessage(); // waiting message

            handler.post(new Runnable() { // Waiting Screen
                @Override
                public void run() {
                    setContentView(R.layout.activity_session_waiting);
                    textViewSessionName = findViewById(R.id.activeSessionName);
                    textViewWaitingMessage = findViewById(R.id.text_waiting_on_ip);
                    textViewParticipantsList = findViewById(R.id.sessionParticipantsList);
                    textViewPointsList = findViewById(R.id.sessionPointsList);

                    textViewSessionName.setText(multiUserSession.getSessionName());
                    textViewWaitingMessage.setText(waitingMessage); // Waiting Screen
                }
            });

            waitForMilliSeconds(500);

            int numP = Integer.parseInt(readServerMessage());
            int numCurrentP = Integer.parseInt(readServerMessage()); // so, you know... things work somehow... (^_^)
            int numQ = Integer.parseInt(readServerMessage());
            String learningType = readServerMessage();
            multiUserSession.setLearningType(learningType);
            if(learningType.equals("Training")) {
                multiUserSession.setCategoryName(readServerMessage());
                multiUserSession.setCategoryid(dbHandler.getCategory(multiUserSession.getCategoryName()).getId());
            } else { // Exam
                multiUserSession.setSenderName(readServerMessage());
            }

            String participantListText = null;
            for (;numCurrentP < numP; numCurrentP++){
                String participantCurrentListText = readServerMessage(); // list of participants
                participantListText = participantCurrentListText;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        textViewParticipantsList.setText(participantCurrentListText);
                    }
                });
            }

            String[] players = participantListText.trim().split("[\n]");
            if(learningType.equals("Training")){ // then add the host
                for(String player: players){
                    multiUserSession.getReceivers().add(new Receiver(player));
                }
            } else { // if Exam mode, then don't add the host
                for(int i = 1;i < players.length;i++){
                    multiUserSession.getReceivers().add(new Receiver(players[i]));
                }
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    textViewWaitingMessage.setText("Ready");
                    textViewWaitingMessage.setBackgroundColor(Color.parseColor("#79DC07"));
                }
            });


            Log.d("[client]", "received start sign: " + readServerMessage()); // start sign

            //------------------------- Start Session ------------------------

            prepareReceiverScreenElements();
            if(learningType.equals("Exam")) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, Camera2BasicFragment.newInstance())
                        .commit();
            }
            while (true) {
                for (int i = 0; i < numQ; i++) {
                    setReceiverScreenElementsValue();
                    message = readServerMessage();
                    // todo camera (object detection)
                    Log.d("[client]", "received: " + message);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            textViewMessage.setText(message);
                            answerButton.setEnabled(true);
                        }
                    });

                    while (true) {
                        if (buttonTapped) { // answer button
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    answer = editTextAnswer.getText().toString().trim();
                                }
                            });
                            waitForMilliSeconds(500);
                            if(answer.isEmpty()){
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        editTextAnswer.setHint("Please Write Answer");
                                        editTextAnswer.setHintTextColor(Color.parseColor("red"));
                                    }
                                });
                                buttonTapped = false;
                                continue;
                            } else {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        answerButton.setEnabled(false);
                                    }
                                });
                            }
                            waitForMilliSeconds(500);

                            writeToServerBuffer(answer);
                            Log.d("[client]", "Sent: " + answer);

                            String result = readServerMessage();
                            displayResult(result);

                            String pointsText = readServerMessage().trim();
                            String points[] = pointsText.split("[\n]");
                            int j = (learningType.equals("Training"))?0:1; // index for 'points'
                            if(learningType.equals("Training")){
                                for(Receiver r: multiUserSession.getReceivers()){
                                    r.setPoints(Integer.parseInt(points[j].substring(0,points[j].indexOf(' ')))); //e.g. 10 points -> 10
                                    j++;
                                }
                            } else { // Exam mode
                                for(Receiver r: multiUserSession.getReceivers()){
                                    r.setPoints(Integer.parseInt(points[j].substring(points[j].indexOf(' ') + 1,points[j].lastIndexOf(' ')))); //e.g. 10 points -> 10
                                    j++;
                                }
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    textViewPointsList.setText(pointsText);
                                }
                            });

                            buttonTapped = false;
                            break;
                        }
                    }
                    waitForMilliSeconds(5000); // wait for the next round
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            editTextAnswer.setHint("");
                        }
                    });
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        textViewResult.setTextColor(Color.parseColor("gray"));
                        textViewResult.setText("Waiting Host's Decision...");
                    }
                });
                String serverDecision = readServerMessage();
                if(serverDecision.equals("CLOSE_CONNECTION")){
                    break;
                }
            } // end of training/exam mode

            dbHandler.addMultiUserSession(multiUserSession);
            increaseUserWinsXorLosses();
            performSerialization();
            closeConnection();
            Log.d("[client]", "Closed!");

            handler.post(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                }
            });
        }

        /**
         * Increase User's Wins if he/she wins XOR Losses if he/she Loses
         */
        private void increaseUserWinsXorLosses() {
            Receiver winner = multiUserSession.getReceivers().get(0); // assume the first receiver is the winner
            for(Receiver r: multiUserSession.getReceivers()){
                if(r.getPoints() > winner.getPoints()){
                    clientUser.increaseLosses();
                    return;
                }
            }
            clientUser.increaseWins(); // if he/she actually the winner
        }

        /**
         * Perform serialization to clientUser's object
         */
        private void performSerialization() {
            System.out.println("Serialization started");
            try{
                FileOutputStream fos=new FileOutputStream(new File(getApplicationContext().getFilesDir().getPath(),"SaveFile.ser"));
                ObjectOutputStream oos=new ObjectOutputStream(fos);
                oos.writeObject(clientUser);
            }catch(IOException e){
                System.out.println("IOException!!");
                System.out.println(e);
            }
        }

        /**
         * @return received message from server
         */
        private String readServerMessage() {
            try {
                return dis.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }

        /**
         * Send message to server
         * @param message as String
         */
        private void writeToServerBuffer(String message) {
            try {
                dos.writeUTF(message);
                dos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Display client's result
         * @param result as String
         */
        private void displayResult(String result) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (result.equals("T")) {
                        String resultText;
                        textViewResult.setTextColor(Color.parseColor("#89FF00"));
                        int min = 0;
                        int max = 2;
                        switch ((int)(Math.random() * (max - min + 1) + min)){
                            case 0:resultText = "Great Job!"; break;
                            case 1:resultText = "Impressive!"; break;
                            default:resultText = "Splendid!";
                        }
                        textViewResult.setText(resultText);
                    } else {
                        String resultText;
                        textViewResult.setTextColor(Color.parseColor("red"));
                        int min = 0;
                        int max = 2;
                        switch ((int)(Math.random() * (max - min + 1) + min)){
                            case 0:resultText = "Wrong, Try Next Time..."; break;
                            case 1:resultText = "Wrong, You Can Do Better!"; break;
                            default:resultText = "Wrong, Do Your Best Next Time";
                        }
                        textViewResult.setText(resultText);
                    }
                }
            });
        }

        /**
         * Prepare receiver's UI elements
         */
        private void prepareReceiverScreenElements() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    String participantListText = textViewParticipantsList.getText().toString();
                    setContentView(R.layout.activity_receiver);
                    textViewMessage = findViewById(R.id.message);
                    textViewResult = findViewById(R.id.result);
                    editTextAnswer = findViewById(R.id.receiverAnswer);
                    answerButton = findViewById(R.id.sendAnswer);
                    textViewSessionName = findViewById(R.id.activeSessionName);
                    textViewRoundNum = findViewById(R.id.roundNum);
                    textViewParticipantsList = findViewById(R.id.sessionParticipantsList);
                    textViewPointsList = findViewById(R.id.sessionPointsList);

                    answerButton.setEnabled(false);
                    textViewSessionName.setText(multiUserSession.getSessionName());
                    textViewParticipantsList.setText(participantListText);
                }
            });
            waitForMilliSeconds(500);
        }

        /**
         * Set UI join message
         * @param message as String
         */
        private void setUIJoinMessage(String message) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ip = getEnteredIPText();
                    tvJoinMessage.setText(message);
                }
            });
            waitForMilliSeconds(500);
        }

        /**
         * To get entered IP text from UI
         * @return ip address as String
         */
        private String getEnteredIPText() {
            ipPart1 = etIPNum1.getText().toString();
            ipPart2 = etIPNum2.getText().toString();
            ipPart3 = etIPNum3.getText().toString();
            ipPart4 = etIPNum4.getText().toString();
            return ipPart1 + "." + ipPart2 + "." + ipPart3 + "." + ipPart4;
        }

        /**
         * Close client's connection
         */
        public void closeConnection() {
            try {
                if(socket.isConnected()) {
                    dos.writeUTF("CLOSE_SOCKET");
                    dos.flush();
                    dos.close();
                    dis.close();
                    socket.close();
                    Log.d("[client]", "Connection Closed!");
                } else {
                    socket.close();
                    Log.d("[client]", "Socket Closed!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * It uses Thread.sleep(ms)
         * @param ms (milliseconds) as int
         */
        private void waitForMilliSeconds(int ms) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * Set receiver's UI Elements' Value
         */
        private void setReceiverScreenElementsValue() {
            multiUserSession.increaseRoundNum();
            String pointsText = readServerMessage(); // points of each participant
            handler.post(new Runnable() {
                @Override
                public void run() {
                    textViewRoundNum.setText("Round " + multiUserSession.getNumRounds());
                    answerButton.setEnabled(false);
                    textViewPointsList.setText(pointsText);
                    textViewMessage.setText("");
                    textViewResult.setText("");
                    editTextAnswer.setText("");
                    editTextAnswer.setHint("");
                }
            });
        }
    } // ------------------------------- client thread --------------------------------
}