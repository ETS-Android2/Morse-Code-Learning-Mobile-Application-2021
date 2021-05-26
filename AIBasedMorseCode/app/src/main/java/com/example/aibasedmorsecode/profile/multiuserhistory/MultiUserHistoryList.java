package com.example.aibasedmorsecode.profile.multiuserhistory;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.aibasedmorsecode.R;
import com.example.aibasedmorsecode.database.controller.DBHandler;
import com.example.aibasedmorsecode.model.MultiUserSession;
import com.example.aibasedmorsecode.model.Receiver;
import com.example.aibasedmorsecode.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MultiUserHistoryList extends AppCompatActivity {

    ArrayList<MultiUserSession> multiUserSessions = new ArrayList();
    RecyclerView recyclerView;
    MultiUserHistoryAdapter multiUserHistoryAdapter;
    DBHandler dbHandler;

    /**
     * Initialize MultiUserHistoryList class
     * @param savedInstanceState as Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_user_history_list);

        recyclerView = findViewById(R.id.recyclerview);

        multiUserHistoryAdapter = new MultiUserHistoryAdapter(multiUserSessions);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(multiUserHistoryAdapter);

        dbHandler = new DBHandler(getApplicationContext());
        dbHandler.init();
        setGuiData();
    }

    /**
     * Set GUI data from Database
     */
    private void setGuiData() {
        String sessionName, learningType;
        String categoryName = null;
        int numRounds;
        String sender;
        ArrayList<Receiver> receivers;
        Date date;
        ArrayList<MultiUserSession> sessions = dbHandler.getAllMultiUserSessions();

        for(MultiUserSession session: sessions){
            sessionName = session.getSessionName();
            learningType = session.getLearningType();
            categoryName = session.getCategoryName();
            numRounds = session.getNumRounds();
            sender = session.getSenderName();
            receivers = session.getReceivers();
            date = session.getDate();
            multiUserSessions.add(new MultiUserSession(sessionName, learningType, numRounds, sender, receivers, date, categoryName));
        }

        multiUserHistoryAdapter.notifyDataSetChanged();
    }
}