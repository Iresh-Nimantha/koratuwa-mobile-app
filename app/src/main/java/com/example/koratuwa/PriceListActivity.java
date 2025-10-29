package com.example.koratuwa;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PriceListActivity extends BaseActivity {

    private EditText etSearch;
    private Button btnPopular, btnLowest, btnHighest;
    private ImageButton backArrow;
    private RecyclerView rvMarketList;
    private MarketPriceAdapter adapter;
    private List<MarketPrice> marketPrices = new ArrayList<>();
    private List<MarketPrice> filteredPrices = new ArrayList<>();
    private String currentFilter = "popular";

    private FrameLayout progressOverlay;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_list);

        backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> onBackPressed());

        etSearch = findViewById(R.id.et_search);
        btnPopular = findViewById(R.id.btnPopular);
        btnLowest = findViewById(R.id.btnLowestRate);
        btnHighest = findViewById(R.id.btnHighestRate);
        rvMarketList = findViewById(R.id.rv_market_list);

        progressOverlay = findViewById(R.id.progressOverlay);
        progressBar = findViewById(R.id.progressBar);

        adapter = new MarketPriceAdapter(this, filteredPrices);
        rvMarketList.setLayoutManager(new LinearLayoutManager(this));
        rvMarketList.setAdapter(adapter);

        fetchMarketPrices();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAndSortPrices();
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        btnPopular.setOnClickListener(v -> {
            currentFilter = "popular";
            filterAndSortPrices();
        });

        btnLowest.setOnClickListener(v -> {
            currentFilter = "lowest";
            filterAndSortPrices();
        });

        btnHighest.setOnClickListener(v -> {
            currentFilter = "highest";
            filterAndSortPrices();
        });
    }

    private void fetchMarketPrices() {
        progressOverlay.setVisibility(FrameLayout.VISIBLE);

        FirebaseFirestore.getInstance()
                .collection("marketPrices")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressOverlay.setVisibility(FrameLayout.GONE);

                    marketPrices.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String cropName = doc.getString("cropName");
                        String imageUrl = doc.getString("imageUrl");
                        Double latestPrice = doc.getDouble("latestPrice");
                        Double oldPrice = doc.getDouble("oldPrice");

                        if (cropName != null && latestPrice != null) {
                            marketPrices.add(new MarketPrice(cropName, imageUrl, latestPrice, oldPrice != null ? oldPrice : 0));
                        }
                    }
                    filterAndSortPrices();
                })
                .addOnFailureListener(e -> {
                    progressOverlay.setVisibility(FrameLayout.GONE);
                    showToastWithLogo(getString(R.string.failed_to_load_prices));
                    e.printStackTrace();
                });
    }

    private void filterAndSortPrices() {
        String search = etSearch.getText().toString().trim().toLowerCase();
        filteredPrices.clear();

        for (MarketPrice mp : marketPrices) {
            if (mp.getCropName().toLowerCase().contains(search)) {
                filteredPrices.add(mp);
            }
        }

        switch (currentFilter) {
            case "lowest":
                Collections.sort(filteredPrices, Comparator.comparingDouble(MarketPrice::getLatestPrice));
                break;
            case "highest":
                Collections.sort(filteredPrices, (a,b) -> Double.compare(b.getLatestPrice(), a.getLatestPrice()));
                break;
            case "popular":
            default:
                Collections.sort(filteredPrices, Comparator.comparing(MarketPrice::getCropName));
                break;
        }
        adapter.notifyDataSetChanged();
    }

    private void showToastWithLogo(String message) {
        View layout = LayoutInflater.from(this).inflate(R.layout.toast_with_image, findViewById(android.R.id.content), false);

        ImageView toastLogo = layout.findViewById(R.id.toastLogo);
        toastLogo.setImageResource(R.drawable.log); // Replace with your actual logo drawable resource name

        TextView toastText = layout.findViewById(R.id.toastText);
        toastText.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
