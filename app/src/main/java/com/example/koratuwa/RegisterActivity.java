package com.example.koratuwa;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends BaseActivity {

    private int currentStep = 1;

    private FirebaseAuth mAuth;
    private SharedPreferences sessionPrefs;

    private ImageView ivBackArrow;
    private TextView tvRegister;
    private Button btnNext;
    private Button btnRegister;

    private View progressOverlay;

    // Step 1 views
    private EditText etFullName, etAddress, etBirthday;
    private Spinner spinnerDistrict, spinnerStatus;

    // Step 2 views
    private EditText etGmail, etPassword, etRePassword, etPhoneNumber;

    private final Map<String, String> step1Data = new HashMap<>();

    private static final String PREFS_NAME = "secure_credentials";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_step1);

        mAuth = FirebaseAuth.getInstance();
        sessionPrefs = getSharedPreferences("session_prefs", MODE_PRIVATE);

        initializeViews();
        setupStep1();
        setupStep1ClickListeners();

        progressOverlay = null;
    }

    private void initializeViews() {
        ivBackArrow = findViewById(R.id.back_arrow);
        tvRegister = findViewById(R.id.tvRegister);
        btnNext = findViewById(R.id.btnNext);
    }

    private void setupStep1() {
        etFullName = findViewById(R.id.etFullName);
        etAddress = findViewById(R.id.etAddress);
        spinnerDistrict = findViewById(R.id.spinnerDistrict);
        etBirthday = findViewById(R.id.etBirthday);
        spinnerStatus = findViewById(R.id.spinnerStatus);

        setupDistrictSpinner();
        setupStatusSpinner();
        setupBirthdayPicker();

        tvRegister.setText(getString(R.string.register) + " - " + getString(R.string.step_1));
    }

    private void setupStep1ClickListeners() {
        ivBackArrow.setOnClickListener(v -> finish());

        btnNext.setOnClickListener(v -> {
            if (validateStep1()) {
                saveStep1Data();
                setContentView(R.layout.register_step2);
                setupStep2();
            }
        });
    }

    private void setupStep2() {
        ivBackArrow = findViewById(R.id.back_arrow);
        tvRegister = findViewById(R.id.tvRegister);
        btnRegister = findViewById(R.id.btnRegister);
        progressOverlay = findViewById(R.id.progressOverlay);
        if (progressOverlay != null) progressOverlay.setVisibility(View.GONE);

        etGmail = findViewById(R.id.etGmail);
        etPassword = findViewById(R.id.etPassword);
        etRePassword = findViewById(R.id.etRePassword);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);  // newly added

        tvRegister.setText(getString(R.string.register) + " - " + getString(R.string.step_2));

        setupStep2ClickListeners();
    }

    private void setupStep2ClickListeners() {
        ivBackArrow.setOnClickListener(v -> goToStep1());

        btnRegister.setOnClickListener(v -> {
            if (validateStep2()) {
                completeRegistration();
            }
        });
    }

    private void setupDistrictSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.district_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrict.setAdapter(adapter);
    }

    private void setupStatusSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.current_status_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);
    }

    private void setupBirthdayPicker() {
        etBirthday.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(this, (view, y, m, d) -> {
                etBirthday.setText(String.format("%02d/%02d/%04d", d, m + 1, y));
            }, year, month, day).show();
        });
    }

    private boolean validateStep1() {
        boolean valid = true;

        if (TextUtils.isEmpty(etFullName.getText().toString().trim())) {
            etFullName.setError(getString(R.string.full_name) + " " + getString(R.string.is_required));
            valid = false;
        }
        if (TextUtils.isEmpty(etAddress.getText().toString().trim())) {
            etAddress.setError(getString(R.string.address) + " " + getString(R.string.is_required));
            valid = false;
        }
        if (spinnerDistrict.getSelectedItemPosition() == 0) {
            showToastWithLogo(getString(R.string.please_select_district));
            valid = false;
        }
        if (TextUtils.isEmpty(etBirthday.getText().toString().trim())) {
            etBirthday.setError(getString(R.string.birthday) + " " + getString(R.string.is_required));
            valid = false;
        }
        if (spinnerStatus.getSelectedItemPosition() == 0) {
            showToastWithLogo(getString(R.string.please_select_status));
            valid = false;
        }

        return valid;
    }

    private boolean validateStep2() {
        boolean valid = true;

        String email = etGmail.getText().toString().trim();
        String phone = etPhoneNumber.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String rePassword = etRePassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etGmail.setError(getString(R.string.gmail) + " " + getString(R.string.is_required));
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etGmail.setError(getString(R.string.please_enter_valid_email));
            valid = false;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhoneNumber.setError(getString(R.string.please_enter_phone_number));
            valid = false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.password_hint) + " " + getString(R.string.is_required));
            valid = false;
        } else if (password.length() < 6) {
            etPassword.setError(getString(R.string.password_min_length));
            valid = false;
        }

        if (TextUtils.isEmpty(rePassword)) {
            etRePassword.setError(getString(R.string.please_reenter_password));
            valid = false;
        } else if (!password.equals(rePassword)) {
            etRePassword.setError(getString(R.string.passwords_do_not_match));
            valid = false;
        }

        return valid;
    }

    private void saveStep1Data() {
        step1Data.put("fullName", etFullName.getText().toString().trim());
        step1Data.put("address", etAddress.getText().toString().trim());
        step1Data.put("district", spinnerDistrict.getSelectedItem().toString());
        step1Data.put("birthday", etBirthday.getText().toString().trim());
        step1Data.put("status", spinnerStatus.getSelectedItem().toString());
    }

    private void goToStep2() {
        currentStep = 2;
        setupStep2();
    }

    private void goToStep1() {
        currentStep = 1;
        setContentView(R.layout.register_step1);
        initializeViews();
        setupStep1();
        setupStep1ClickListeners();

        etFullName.setText(step1Data.get("fullName"));
        etAddress.setText(step1Data.get("address"));
        etBirthday.setText(step1Data.get("birthday"));

        tvRegister.setText(getString(R.string.register) + " - " + getString(R.string.step_1));
    }

    private void completeRegistration() {
        String email = etGmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        btnRegister.setEnabled(false);
        if (progressOverlay != null) progressOverlay.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (progressOverlay != null) progressOverlay.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            String uid = user.getUid();

                            Map<String, Object> userData = new HashMap<>();
                            userData.put("fullName", step1Data.get("fullName"));
                            userData.put("address", step1Data.get("address"));
                            userData.put("district", step1Data.get("district"));
                            userData.put("birthday", step1Data.get("birthday"));
                            userData.put("status", step1Data.get("status"));
                            userData.put("email", email);
                            userData.put("phoneNumber", etPhoneNumber.getText().toString().trim());

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users").document(uid)
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        SharedPreferences.Editor editor = sessionPrefs.edit();
                                        editor.putString("email", email);
                                        editor.putString("uid", uid);
                                        editor.putString("fullName", step1Data.get("fullName"));
                                        editor.putBoolean("is_logged_in", true);
                                        editor.apply();

                                        new AlertDialog.Builder(RegisterActivity.this)
                                                .setTitle(getString(R.string.registration_successful))
                                                .setMessage(getString(R.string.account_created_successfully))
                                                .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                })
                                                .setCancelable(false)
                                                .show();
                                    })
                                    .addOnFailureListener(e -> {
                                        new AlertDialog.Builder(RegisterActivity.this)
                                                .setTitle(getString(R.string.registration_failed))
                                                .setMessage(getString(R.string.registration_failed) + ": " + e.getMessage())
                                                .setPositiveButton(getString(R.string.ok), null)
                                                .show();
                                    });
                        }
                    } else {
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : getString(R.string.registration_failed);
                        new AlertDialog.Builder(RegisterActivity.this)
                                .setTitle(getString(R.string.registration_failed))
                                .setMessage(errorMsg)
                                .setPositiveButton(getString(R.string.ok), null)
                                .show();
                    }
                });
    }

    // New method to show toast with logo
    private void showToastWithLogo(String message) {
        View layout = LayoutInflater.from(this).inflate(R.layout.toast_with_image, findViewById(android.R.id.content), false);

        ImageView toastLogo = layout.findViewById(R.id.toastLogo);
        toastLogo.setImageResource(R.drawable.log); // Replace with your logo drawable

        TextView toastText = layout.findViewById(R.id.toastText);
        toastText.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
}
