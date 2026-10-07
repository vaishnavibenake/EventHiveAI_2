package com.example.eventhiveai.coordinator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventhiveai.R;
import com.example.eventhiveai.models.WinnerModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class EventGalleryActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView recyclerWinners;
    private ProgressBar progressBarWinners;
    private TextView tvEmptyWinners;

    private ArrayList<WinnerModel> winnerList;
    private WinnerAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_gallery);

        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        recyclerWinners = findViewById(R.id.recyclerWinners);
        progressBarWinners = findViewById(R.id.progressBarWinners);
        tvEmptyWinners = findViewById(R.id.tvEmptyWinners);

        recyclerWinners.setLayoutManager(new LinearLayoutManager(this));
        winnerList = new ArrayList<>();
        adapter = new WinnerAdapter(winnerList);
        recyclerWinners.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        loadWinners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWinners();
    }

    private void loadWinners() {
        progressBarWinners.setVisibility(View.VISIBLE);
        tvEmptyWinners.setVisibility(View.GONE);

        db.collection("winners")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBarWinners.setVisibility(View.GONE);
                    winnerList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        WinnerModel winner = doc.toObject(WinnerModel.class);
                        if (winner == null) winner = new WinnerModel();
                        winner.setWinnerId(doc.getId());
                        if (doc.contains("eventName")) winner.setEventName(doc.getString("eventName"));
                        if (doc.contains("clubName")) winner.setClubName(doc.getString("clubName"));
                        if (doc.contains("firstPlace")) winner.setFirstPlace(doc.getString("firstPlace"));
                        if (doc.contains("secondPlace")) winner.setSecondPlace(doc.getString("secondPlace"));
                        if (doc.contains("thirdPlace")) winner.setThirdPlace(doc.getString("thirdPlace"));

                        winnerList.add(winner);
                    }

                    adapter.notifyDataSetChanged();

                    if (winnerList.isEmpty()) {
                        tvEmptyWinners.setVisibility(View.VISIBLE);
                    } else {
                        tvEmptyWinners.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBarWinners.setVisibility(View.GONE);
                    Toast.makeText(EventGalleryActivity.this, "Failed to load winners: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private static class WinnerAdapter extends RecyclerView.Adapter<WinnerAdapter.ViewHolder> {
        private final ArrayList<WinnerModel> items;

        WinnerAdapter(ArrayList<WinnerModel> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.winner_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WinnerModel item = items.get(position);
            holder.tvWinnerEventName.setText(item.getEventName());
            holder.tvWinnerClub.setText(item.getClubName().isEmpty() ? "Campus Club" : item.getClubName());

            holder.tvWinnerFirst.setText(item.getFirstPlace().isEmpty() ? "🥇 1st: TBA" : item.getFirstPlace());

            if (!item.getSecondPlace().isEmpty()) {
                holder.tvWinnerSecond.setVisibility(View.VISIBLE);
                holder.tvWinnerSecond.setText(item.getSecondPlace());
            } else {
                holder.tvWinnerSecond.setVisibility(View.GONE);
            }

            if (!item.getThirdPlace().isEmpty()) {
                holder.tvWinnerThird.setVisibility(View.VISIBLE);
                holder.tvWinnerThird.setText(item.getThirdPlace());
            } else {
                holder.tvWinnerThird.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvWinnerEventName, tvWinnerClub, tvWinnerFirst, tvWinnerSecond, tvWinnerThird;

            ViewHolder(@NonNull View v) {
                super(v);
                tvWinnerEventName = v.findViewById(R.id.tvWinnerEventName);
                tvWinnerClub = v.findViewById(R.id.tvWinnerClub);
                tvWinnerFirst = v.findViewById(R.id.tvWinnerFirst);
                tvWinnerSecond = v.findViewById(R.id.tvWinnerSecond);
                tvWinnerThird = v.findViewById(R.id.tvWinnerThird);
            }
        }
    }
}
