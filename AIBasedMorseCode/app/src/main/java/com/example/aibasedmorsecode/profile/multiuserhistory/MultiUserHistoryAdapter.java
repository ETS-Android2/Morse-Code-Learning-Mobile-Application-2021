package com.example.aibasedmorsecode.profile.multiuserhistory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aibasedmorsecode.R;
import com.example.aibasedmorsecode.model.MultiUserSession;
import com.example.aibasedmorsecode.model.User;

import java.util.ArrayList;

public class MultiUserHistoryAdapter extends RecyclerView.Adapter<MultiUserHistoryAdapter.MLHViewHolder> {

    ArrayList<MultiUserSession> multiUserSessions;

    /**
     * Create MultiUserHistoryAdapter object with one parameter
     * @param multiUserSessions as ArrayList<MultiUserSession>
     */
    public MultiUserHistoryAdapter(ArrayList<MultiUserSession> multiUserSessions) {
        this.multiUserSessions = multiUserSessions;
    }

    @NonNull
    @Override
    public MLHViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MLHViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.multi_user_history_list_row, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MLHViewHolder holder, int position) {
        MultiUserSession multiUserSession = multiUserSessions.get(position);
        holder.textViewSessionName.setText(multiUserSession.getSessionName());
        holder.textViewLearningType.setText(multiUserSession.getLearningType()
                + " " + ((multiUserSession.getCategoryName() == null)?"":("(" + multiUserSession.getCategoryName() + ")")));
        holder.textViewNumRounds.setText(multiUserSession.getNumRounds() + " " + ((multiUserSession.getNumRounds() > 1)?"Rounds":"Round"));
        holder.textViewSender.setText(multiUserSession.getSenderName() + " (Sender)");

        int points = -1;
        String winner = "";
        String flag = "";
        for(User receiver: multiUserSession.getReceivers()){
            holder.textViewReceivers.append(receiver.getUsername() + "\n");
            holder.textViewPoints.append(receiver.getPoints() + "\n");
            if(receiver.getPoints() > points){
                points = receiver.getPoints();
                winner = receiver.getUsername();
            } else if(receiver.getPoints() == points){
                winner += " & " + receiver.getUsername();
            }
        }
        holder.textViewWinner.setText("Winner: " + winner);
        holder.textViewDate.setText("" + multiUserSession.getDate());
    }

    @Override
    public int getItemCount() {
        return multiUserSessions.size();
    }

    public class MLHViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSessionName, textViewLearningType, textViewNumRounds, textViewSender, textViewReceivers, textViewPoints, textViewWinner, textViewDate;

        public MLHViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSessionName = itemView.findViewById(R.id.sessionName);
            textViewLearningType = itemView.findViewById(R.id.textViewLearningType);
            textViewNumRounds = itemView.findViewById(R.id.textViewNumRounds);
            textViewSender = itemView.findViewById(R.id.textViewSender);
            textViewReceivers = itemView.findViewById(R.id.name);
            textViewPoints = itemView.findViewById(R.id.points);
            textViewWinner = itemView.findViewById(R.id.winner);
            textViewDate = itemView.findViewById(R.id.sessionDate);
        }
    }
}
