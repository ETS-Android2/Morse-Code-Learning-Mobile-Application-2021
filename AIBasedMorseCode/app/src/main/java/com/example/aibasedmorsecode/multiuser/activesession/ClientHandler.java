package com.example.aibasedmorsecode.multiuser.activesession;

import android.util.Log;

import com.example.aibasedmorsecode.model.Receiver;
import com.example.aibasedmorsecode.model.User;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{

    private Socket socket;
    private Receiver receiver;
    private String message;
    private boolean active;
    private DataInputStream dis;
    private DataOutputStream dos;

    /**
     * Create ClientHandler object with three parameters. It also initialize: (message, active, dos)
     * @param socket
     * @param receiver
     * @param dis
     */
    public ClientHandler(Socket socket, Receiver receiver, DataInputStream dis) {
        this.socket = socket;
        this.receiver = receiver;
        this.message = "";
        this.dis = dis;
        this.active = true;
        try {
            this.dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * To keep track of client's message
     */
    @Override
    public void run() {
        Log.d("[client handler]", "I'm Here! -> " + receiver);

        while (active){
            try {
                message = dis.readUTF();
                Log.d("[client handler]", "Message: " + message);
                if(message.equals("CLOSE_SOCKET")) {
                    Log.d("[client handler]", "Closing...");
                    dis.close();
                    dos.close();
                    socket.close();
                    active = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("[client handler]", "Something Wrong Happened...");
                try {
                    dis.close();
                    dos.close();
                    socket.close();
                    active = false;
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
        Log.d("[client handler]", "Finished!");
    }

    /**
     * @return receiver as Receiver
     */
    public Receiver getReceiver() {
        return receiver;
    }

    /**
     * @return message as String
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message as String
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return dos as DataOutputStream
     */
    public DataOutputStream getDos() {
        return dos;
    }
}
