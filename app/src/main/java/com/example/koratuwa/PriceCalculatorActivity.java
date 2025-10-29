package com.example.koratuwa;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class PriceCalculatorActivity extends BaseActivity {
    private AutoCompleteTextView actvCrop;
    private EditText etQuantity;
    private TextView tvTotalPrice;
    private Button btnCalculate, btnReset;
    private ImageButton backArrow;

    private ArrayList<String> cropNames = new ArrayList<>();
    private HashMap<String, Double> cropPriceMap = new HashMap<>();
    private String selectedCrop;
    private double pricePerKg = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_calculator);

        actvCrop = findViewById(R.id.actv_crop);
        etQuantity = findViewById(R.id.et_quantity);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        btnCalculate = findViewById(R.id.btn_calculate);
        btnReset = findViewById(R.id.btn_reset);
        backArrow = findViewById(R.id.back_arrow);

        backArrow.setOnClickListener(v -> onBackPressed());

        fetchMarketPrices();

        actvCrop.setOnItemClickListener((parent, view, position, id) -> {
            selectedCrop = cropNames.get(position);
            pricePerKg = cropPriceMap.getOrDefault(selectedCrop, 0.0);
            tvTotalPrice.setText(getString(R.string.default_price_text)); // Reset display
        });

        actvCrop.setOnClickListener(v -> {
            if (!cropNames.isEmpty()) {
                actvCrop.showDropDown();
            }
        });

        btnCalculate.setOnClickListener(v -> calculateTotal());

        btnReset.setOnClickListener(v -> {
            actvCrop.setText("");
            etQuantity.setText("");
            selectedCrop = "";
            pricePerKg = 0;
            tvTotalPrice.setText(getString(R.string.default_price_text));
        });
    }

    private void fetchMarketPrices() {
        FirebaseFirestore.getInstance().collection("marketPrices")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    cropPriceMap.clear();
                    cropNames.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String cropName = doc.getString("cropName");
                        Double latestPrice = doc.getDouble("latestPrice");
                        Log.d("PriceCalculator", "Fetched crop: " + cropName + ", latestPrice: " + latestPrice);
                        if (!TextUtils.isEmpty(cropName) && latestPrice != null) {
                            cropNames.add(cropName);
                            cropPriceMap.put(cropName, latestPrice);
                        }
                    }
                    Log.d("PriceCalculator", "Total crops fetched: " + cropNames.size());
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_dropdown_item_1line, cropNames);
                    actvCrop.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Log.e("PriceCalculator", "Error fetching crops", e);
                    showToastWithLogo(getString(R.string.failed_fetch_prices));
                });
    }

    private void calculateTotal() {
        String quantityStr = etQuantity.getText().toString().trim();
        String crop = actvCrop.getText().toString().trim();

        if (TextUtils.isEmpty(crop)) {
            actvCrop.setError(getString(R.string.select_crop_type));
            return;
        }
        if (TextUtils.isEmpty(quantityStr)) {
            etQuantity.setError(getString(R.string.enter_quantity));
            return;
        }
        double quantity;
        try {
            quantity = Double.parseDouble(quantityStr);
        } catch (NumberFormatException e) {
            etQuantity.setError(getString(R.string.invalid_quantity));
            return;
        }
        if (!cropPriceMap.containsKey(crop)) {
            showToastWithLogo(getString(R.string.crop_price_not_found));
            return;
        }
        double total = cropPriceMap.get(crop) * quantity;
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        String totalStr = nf.format(total);
        tvTotalPrice.setText(getString(R.string.rs_format, totalStr));
    }

    private void showToastWithLogo(String message) {
        View layout = LayoutInflater.from(this).inflate(R.layout.toast_with_image, findViewById(android.R.id.content), false);

        ImageView toastLogo = layout.findViewById(R.id.toastLogo);
        toastLogo.setImageResource(R.drawable.log); // Replace with your actual logo drawable resource name

        TextView toastText = layout.findViewById(R.id.toastText);
        toastText.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
}
