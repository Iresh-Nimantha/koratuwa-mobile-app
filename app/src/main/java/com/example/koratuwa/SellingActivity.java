package com.example.koratuwa;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SellingActivity extends BaseActivity {

    private static final int PICK_IMAGE_REQUEST = 101;

    private Spinner spinnerCropType;
    private EditText etQuantity, etHarvestedDate, etDescription;
    private Button btnUploadImage, btnConfirm, btnSell, btnHistory;
    private ImageView ivBack;
    private TextView tvImagePath;

    private Uri imageUri;
    private String imagePath;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selling);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        spinnerCropType = findViewById(R.id.spinnerCropType);
        etQuantity = findViewById(R.id.etQuantity);
        etHarvestedDate = findViewById(R.id.etHarvestedDate);
        etDescription = findViewById(R.id.etDescription);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnConfirm = findViewById(R.id.btnConfirm);
        tvImagePath = findViewById(R.id.tvImagePath);
        btnSell = findViewById(R.id.btnSell);
        btnHistory = findViewById(R.id.btnHistory);
        ivBack = findViewById(R.id.back_arrow);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.variants_fruits, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCropType.setAdapter(adapter);

        etHarvestedDate.setOnClickListener(v -> showDatePicker());

        btnUploadImage.setOnClickListener(v -> openImagePicker());

        ivBack.setOnClickListener(v -> finish());

        btnConfirm.setOnClickListener(v -> savePostData());

        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(SellingActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        btnSell.setOnClickListener(v -> {
            // Implement sell logic if needed
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> etHarvestedDate.setText(String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)),
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imagePath = getFileName(imageUri);
            tvImagePath.setText(imagePath);
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null && uri.getPath() != null) {
            int cut = uri.getPath().lastIndexOf('/');
            if (cut != -1) result = uri.getPath().substring(cut + 1);
        }
        return result != null ? result : "image";
    }

    private void savePostData() {
        String cropType = spinnerCropType.getSelectedItem() != null ? spinnerCropType.getSelectedItem().toString() : "";
        String quantityStr = etQuantity.getText().toString().trim();
        String harvestedDate = etHarvestedDate.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (cropType.isEmpty() || cropType.equals(getString(R.string.select_crop))) {
            showToastWithLogo(getString(R.string.select_crop_error));
            return;
        }
        if (TextUtils.isEmpty(quantityStr)) {
            etQuantity.setError(getString(R.string.quantity_required));
            return;
        }
        if (TextUtils.isEmpty(harvestedDate)) {
            etHarvestedDate.setError(getString(R.string.harvested_date_required));
            return;
        }
        if (TextUtils.isEmpty(description)) {
            etDescription.setError(getString(R.string.description_required));
            return;
        }
        if (TextUtils.isEmpty(imagePath)) {
            showToastWithLogo(getString(R.string.upload_image_required));
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                etQuantity.setError(getString(R.string.invalid_quantity));
                return;
            }
        } catch (NumberFormatException e) {
            etQuantity.setError(getString(R.string.invalid_quantity));
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            showToastWithLogo(getString(R.string.user_not_logged_in));
            return;
        }

        if (progressBar == null) {
            progressBar = new ProgressBar(this);
            addContentView(progressBar, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }
        progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> postData = new HashMap<>();
        postData.put("userId", user.getUid());
        postData.put("cropType", cropType);
        postData.put("quantity", quantity);
        postData.put("harvestedDate", harvestedDate);
        postData.put("description", description);
        postData.put("imagePath", imagePath);
        postData.put("status", "pending");
        postData.put("timestamp", System.currentTimeMillis());

        db.collection("posts")
                .add(postData)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    showToastWithLogo(getString(R.string.post_saved_successfully));
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    showToastWithLogo(getString(R.string.failed_to_save_post));
                });
    }

    private void showToastWithLogo(String message) {
        View layout = LayoutInflater.from(this).inflate(R.layout.toast_with_image, findViewById(android.R.id.content), false);

        ImageView toastLogo = layout.findViewById(R.id.toastLogo);
        toastLogo.setImageResource(R.drawable.log); // Change to your logo drawable

        TextView toastText = layout.findViewById(R.id.toastText);
        toastText.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
