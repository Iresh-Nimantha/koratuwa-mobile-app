package com.example.koratuwa;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GetHelpActivity extends BaseActivity {
    private EditText etSearch;
    private RecyclerView rvGroups;
    private ExpandableOfficerAdapter adapter;
    private List<OfficerGroup> originalList = new ArrayList<>();
    private List<OfficerGroup> displayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 🚨 Force Light Mode, block Night Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_help);

        etSearch = findViewById(R.id.et_search);
        rvGroups = findViewById(R.id.rv_officer_groups);

        // Back button handler
        ImageButton backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> onBackPressed());

        adapter = new ExpandableOfficerAdapter(this, displayList);
        rvGroups.setLayoutManager(new LinearLayoutManager(this));
        rvGroups.setAdapter(adapter);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            rvGroups.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_RIGHT);
        }
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        rvGroups.addItemDecoration(new ExpandableOfficerAdapter.VerticalSpaceItemDecoration(spacingInPixels));

        fetchOfficers();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateFilter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchOfficers() {
        FirebaseFirestore.getInstance()
                .collection("officers")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    originalList.clear();
                    Map<String, List<Officer>> grouped = new LinkedHashMap<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Officer officer = doc.toObject(Officer.class);
                        String district = doc.getString("district");
                        if (!grouped.containsKey(district)) grouped.put(district, new ArrayList<>());
                        grouped.get(district).add(officer);
                    }
                    for (Map.Entry<String, List<Officer>> entry : grouped.entrySet()) {
                        OfficerGroup group = new OfficerGroup(entry.getKey(), entry.getValue());
                        group.setExpanded(false);
                        originalList.add(group);
                    }
                    updateFilter("");
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    private void updateFilter(String text) {
        String query = text.toLowerCase();
        displayList.clear();

        for (OfficerGroup group : originalList) {
            boolean matchesDistrict = group.getDistrict() != null && group.getDistrict().toLowerCase().contains(query);

            List<Officer> filteredOfficers = new ArrayList<>();
            for (Officer o : group.getOfficers()) {
                if (o.getName().toLowerCase().contains(query)
                        || o.getState().toLowerCase().contains(query)
                        || o.getPhoneNumber().contains(query)) {
                    filteredOfficers.add(o);
                }
            }

            if (matchesDistrict) {
                displayList.add(group);
            } else if (!filteredOfficers.isEmpty()) {
                OfficerGroup filteredGroup = new OfficerGroup(group.getDistrict(), filteredOfficers);
                filteredGroup.setExpanded(false);
                displayList.add(filteredGroup);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
