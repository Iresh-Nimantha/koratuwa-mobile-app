package com.example.koratuwa;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends BaseActivity {

    private CardView cardChangeLanguage, cardPersonalInfo, cardChangePassword;
    private Button btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences sessionPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sessionPrefs = getSharedPreferences("session_prefs", MODE_PRIVATE);

        ImageView back_arrow = findViewById(R.id.back_arrow);
        back_arrow.setOnClickListener(view -> {
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        cardPersonalInfo = findViewById(R.id.cardPersonalInfo);
        cardPersonalInfo.setOnClickListener(view -> showPersonalDetailsDialog());

        cardChangePassword = findViewById(R.id.cardChangePassword);
        cardChangePassword.setOnClickListener(view -> showPasswordChangeDialog());

        cardChangeLanguage = findViewById(R.id.cardChangeLanguage);
        cardChangeLanguage.setOnClickListener(view -> {
            Intent intent = new Intent(SettingsActivity.this, LanguageSelectionActivity.class);
            intent.putExtra("change_button_text", true);
            startActivity(intent);
        });

        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(view -> logout());
    }

    private void logout() {
        // Sign out from Firebase
        mAuth.signOut();

        // Clear all SharedPreferences
        clearAllSharedPreferences();
        // Clear cache files
        clearCache();
        // Navigate to LanguageSelectionActivity clearing back stack
        Intent intent = new Intent(SettingsActivity.this, LanguageSelectionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void clearAllSharedPreferences() {
        getSharedPreferences("koratuwa_prefs", MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences("session_prefs", MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences("secure_credentials", MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences("app_prefs", MODE_PRIVATE).edit().clear().apply();
    }

    private void clearCache() {
        try {
            File cacheDir = getCacheDir();
            if (cacheDir != null && cacheDir.isDirectory()) {
                deleteDir(cacheDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return dir.delete();
    }

    private void showPasswordChangeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        EditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirmNewPassword = dialogView.findViewById(R.id.etConfirmNewPassword);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelPassword);
        Button btnUpdate = dialogView.findViewById(R.id.btnUpdatePassword);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnUpdate.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmNewPassword.getText().toString().trim();

            if (validatePasswordChange(currentPassword, newPassword, confirmPassword)) {
                updatePassword(currentPassword, newPassword, dialog);
            }
        });
    }

    private void showPersonalDetailsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_personal_details, null);
        builder.setView(dialogView);

        EditText etFullName = dialogView.findViewById(R.id.etFullName);
        EditText etAddress = dialogView.findViewById(R.id.etAddress);
        Spinner spinnerDistrict = dialogView.findViewById(R.id.spinnerDistrict);
        EditText etBirthday = dialogView.findViewById(R.id.etBirthday);
        Spinner spinnerStatus = dialogView.findViewById(R.id.spinnerStatus);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelPersonal);
        Button btnUpdate = dialogView.findViewById(R.id.btnUpdatePersonal);

        loadCurrentUserData(etFullName, etAddress, spinnerDistrict, etBirthday, spinnerStatus);

        etBirthday.setOnClickListener(v -> showDatePickerDialog(etBirthday));

        AlertDialog dialog = builder.create();
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnUpdate.setOnClickListener(v -> {
            if (validatePersonalDetails(etFullName, etAddress, spinnerDistrict, etBirthday, spinnerStatus)) {
                updatePersonalDetails(etFullName, etAddress, spinnerDistrict, etBirthday, spinnerStatus, dialog);
            }
        });
    }

    private boolean validatePasswordChange(String currentPassword, String newPassword, String confirmPassword) {
        boolean isValid = true;

        if (TextUtils.isEmpty(currentPassword)) {
            showToastWithLogo(getString(R.string.current_password_required));
            isValid = false;
        }

        if (TextUtils.isEmpty(newPassword)) {
            showToastWithLogo(getString(R.string.new_password_required));
            isValid = false;
        } else if (newPassword.length() < 6) {
            showToastWithLogo(getString(R.string.password_min_length));
            isValid = false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            showToastWithLogo(getString(R.string.confirm_password_required));
            isValid = false;
        } else if (!newPassword.equals(confirmPassword)) {
            showToastWithLogo(getString(R.string.passwords_do_not_match));
            isValid = false;
        }

        return isValid;
    }

    private boolean validatePersonalDetails(EditText etFullName, EditText etAddress, Spinner spinnerDistrict,
                                            EditText etBirthday, Spinner spinnerStatus) {
        boolean isValid = true;

        if (TextUtils.isEmpty(etFullName.getText().toString().trim())) {
            etFullName.setError(getString(R.string.full_name) + " " + getString(R.string.is_required));
            isValid = false;
        }

        if (TextUtils.isEmpty(etAddress.getText().toString().trim())) {
            etAddress.setError(getString(R.string.address) + " " + getString(R.string.is_required));
            isValid = false;
        }

        if (spinnerDistrict.getSelectedItemPosition() == 0) {
            showToastWithLogo(getString(R.string.please_select_district));
            isValid = false;
        }

        if (TextUtils.isEmpty(etBirthday.getText().toString().trim())) {
            etBirthday.setError(getString(R.string.birthday) + " " + getString(R.string.is_required));
            isValid = false;
        }

        if (spinnerStatus.getSelectedItemPosition() == 0) {
            showToastWithLogo(getString(R.string.please_select_status));
            isValid = false;
        }

        return isValid;
    }

    private void updatePassword(String currentPassword, String newPassword, AlertDialog dialog) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                        dialog.dismiss();
                        if (updateTask.isSuccessful()) {
                            showToastWithLogo(getString(R.string.password_changed_successfully));
                        } else {
                            showToastWithLogo(getString(R.string.password_change_failed));
                        }
                    });
                } else {
                    showToastWithLogo(getString(R.string.current_password_incorrect));
                }
            });
        }
    }

    private void updatePersonalDetails(EditText etFullName, EditText etAddress, Spinner spinnerDistrict,
                                       EditText etBirthday, Spinner spinnerStatus, AlertDialog dialog) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            Map<String, Object> userData = new HashMap<>();
            userData.put("fullName", etFullName.getText().toString().trim());
            userData.put("address", etAddress.getText().toString().trim());
            userData.put("district", spinnerDistrict.getSelectedItem().toString());
            userData.put("birthday", etBirthday.getText().toString().trim());
            userData.put("status", spinnerStatus.getSelectedItem().toString());
            userData.put("updatedAt", System.currentTimeMillis());

            db.collection("users").document(uid)
                    .update(userData)
                    .addOnSuccessListener(aVoid -> {
                        dialog.dismiss();
                        showToastWithLogo(getString(R.string.details_updated_successfully));

                        SharedPreferences.Editor editor = sessionPrefs.edit();
                        editor.putString("fullName", etFullName.getText().toString().trim());
                        editor.apply();
                    })
                    .addOnFailureListener(e -> {
                        showToastWithLogo(getString(R.string.details_update_failed));
                    });
        }
    }

    private void loadCurrentUserData(EditText etFullName, EditText etAddress, Spinner spinnerDistrict,
                                     EditText etBirthday, Spinner spinnerStatus) {
        // Load cached session data first
        String fullName = sessionPrefs.getString("fullName", "");
        etFullName.setText(fullName);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            etFullName.setText(documentSnapshot.getString("fullName"));
                            etAddress.setText(documentSnapshot.getString("address"));
                            etBirthday.setText(documentSnapshot.getString("birthday"));

                            setupSpinnerSelection(spinnerDistrict, R.array.district_array,
                                    documentSnapshot.getString("district"));

                            setupSpinnerSelection(spinnerStatus, R.array.current_status_array,
                                    documentSnapshot.getString("status"));
                        }
                    });
        }
    }

    private void setupSpinnerSelection(Spinner spinner, int arrayResId, String selectedValue) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                arrayResId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        if (selectedValue != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).toString().equals(selectedValue)) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void showDatePickerDialog(EditText etBirthday) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, y, m, d) -> etBirthday.setText(String.format("%02d/%02d/%04d", d, m + 1, y)),
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showToastWithLogo(String message) {
        View layout = LayoutInflater.from(this).inflate(R.layout.toast_with_image, findViewById(android.R.id.content), false);

        ImageView toastLogo = layout.findViewById(R.id.toastLogo);
        toastLogo.setImageResource(R.drawable.log); // Replace with your logo drawable

        TextView toastText = layout.findViewById(R.id.toastText);
        toastText.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
