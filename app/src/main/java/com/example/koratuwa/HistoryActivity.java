package com.example.koratuwa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends BaseActivity {

    private RecyclerView rvHistory;
    private HistoryAdapter adapter;
    private List<SellPost> postList = new ArrayList<>();

    private FirebaseFirestore db;

    private Button btnAll, btnApproved, btnPending, btnRejected, btnSell;
    private ImageView ivBack;

    private Button activeFilterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        db = FirebaseFirestore.getInstance();

        rvHistory = findViewById(R.id.rv_history);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(postList);
        rvHistory.setAdapter(adapter);

        btnAll = findViewById(R.id.btn_all);
        btnApproved = findViewById(R.id.btn_approved);
        btnPending = findViewById(R.id.btn_pending);
        btnRejected = findViewById(R.id.btn_rejected);
        btnSell = findViewById(R.id.btnSell);
        ivBack = findViewById(R.id.back_arrow);

        View.OnClickListener filterClickListener = v -> {
            Button clickedButton = (Button) v;

            updateFilterButtonsStyle(clickedButton);

            String filter;
            if (clickedButton == btnAll) filter = "all";
            else filter = clickedButton.getText().toString().toLowerCase();

            fetchPosts(filter);
        };

        btnAll.setOnClickListener(filterClickListener);
        btnApproved.setOnClickListener(filterClickListener);
        btnPending.setOnClickListener(filterClickListener);
        btnRejected.setOnClickListener(filterClickListener);

        // Initialize filters - set btnAll active style, others default
        updateFilterButtonsStyle(btnAll);
        fetchPosts("all");

        btnSell.setOnClickListener(v -> {
            startActivity(new Intent(HistoryActivity.this, SellingActivity.class));
            finish();
        });

        ivBack.setOnClickListener(v -> {
            startActivity(new Intent(HistoryActivity.this, MainActivity.class));
            finish();
        });
    }

    private void updateFilterButtonsStyle(Button activeButton) {
        Button[] buttons = {btnAll, btnApproved, btnPending, btnRejected};
        for (Button btn : buttons) {
            if (btn == activeButton) {
                btn.setBackgroundResource(R.drawable.rounded_green_button);
                btn.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            } else {
                btn.setBackgroundResource(R.drawable.rounded_button_default);
                btn.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            }
        }
        activeButton = activeButton;
    }


    private void fetchPosts(String filter) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Query query = db.collection("posts")
                .whereEqualTo("userId", user.getUid());

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            postList.clear();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                SellPost sellPost = doc.toObject(SellPost.class);
                if ("all".equalsIgnoreCase(filter) || sellPost.getStatus().equalsIgnoreCase(filter)) {
                    postList.add(sellPost);
                }
            }
            adapter.notifyDataSetChanged();
        });
    }
}
