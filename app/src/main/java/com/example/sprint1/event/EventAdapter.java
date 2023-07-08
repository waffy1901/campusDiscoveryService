package com.example.sprint1.event;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sprint1.EventInfoPage;
import com.example.sprint1.R;
import com.example.sprint1.User;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.MyViewHolder> {

    final Context context;
    final User currUser;
    final List<Event> eventList;

    public EventAdapter(Context context, List<Event> eventList, User currUser) {
        this.eventList = eventList;
        this.context = context;
        this.currUser = currUser;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate our custom view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_card, parent,false);
        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // Bind all custom views by its position
        Event event = eventList.get(position);

        try {
            holder.tvTitle.setText(event.getName());
            holder.tvDescription.setText(event.getDescription());
            holder.tvDateTime.setText(event.getDay());
            holder.tvLocation.setText(event.getLocation().toString());
            holder.tvHost.setText("Host: " + event.getHostname());
        } catch (Exception e) { e.printStackTrace(); }

        // Listener
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EventInfoPage.class);
                intent.putExtra("editTools", event.getOwner().equals(currUser.getId()) || currUser.isModerator());
                intent.putExtra("currUser", currUser);
                intent.putExtra("event", event);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    //all the custom view will be hold here or initialize here inside MyViewHolder
    static class MyViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout linearLayout;
        final TextView tvTitle;
        final TextView tvDescription;
        final TextView tvDateTime;
        final TextView tvLocation;
        final TextView tvHost;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.eventCard_layout);
            tvTitle = itemView.findViewById(R.id.eventCard_header);
            tvDescription = itemView.findViewById(R.id.eventCard_shortDescription);
            tvDateTime = itemView.findViewById(R.id.eventCard_dateTime);
            tvLocation = itemView.findViewById(R.id.eventCard_location);
            tvHost = itemView.findViewById(R.id.eventCard_host);
        }
    }
}
