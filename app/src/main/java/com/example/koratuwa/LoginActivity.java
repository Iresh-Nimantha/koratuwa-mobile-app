package com.example.koratuwa;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends BaseActivity {

    private EditText etEmail, etPassword;
    private ImageView ivShowHide;
    private Button btnLogin;
    private TextView tvReset, tvRegister, tvForgotPassword;
    private boolean isPasswordVisible = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences sessionPrefs;

    private AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sessionPrefs = getSharedPreferences("session_prefs", MODE_PRIVATE);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        ivShowHide = findViewById(R.id.ivShowHide);
        btnLogin = findViewById(R.id.btnLogin);
        tvReset = findViewById(R.id.tvReset);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        ivShowHide.setOnClickListener(v -> togglePasswordVisibility());

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateLoginForm(email, password)) {
                loginUser(email, password);
            }
        });

        tvReset.setOnClickListener(v ->
                showToastWithLogo(getString(R.string.reset) + " " + getString(R.string.clicked))
        );

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToMain();
        }
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            ivShowHide.setImageResource(R.drawable.ic_eye);
        } else {
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            ivShowHide.setImageResource(R.drawable.ic_eye_open);
        }
        isPasswordVisible = !isPasswordVisible;
        etPassword.setSelection(etPassword.getText().length());
    }

    private boolean validateLoginForm(String email, String password) {
        boolean isValid = true;

        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getString(R.string.email_hint) + " " + getString(R.string.is_required));
            etEmail.requestFocus();
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.please_enter_valid_email));
            etEmail.requestFocus();
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.password_hint) + " " + getString(R.string.is_required));
            etPassword.requestFocus();
            isValid = false;
        } else if (password.length() < 6) {
            etPassword.setError(getString(R.string.password_min_length));
            etPassword.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    private void showToastWithLogo(String message) {
        View layout = LayoutInflater.from(this).inflate(R.layout.toast_with_image, findViewById(android.R.id.content), false);

        ImageView toastLogo = layout.findViewById(R.id.toastLogo);
        toastLogo.setImageResource(R.drawable.log); // Your logo drawable here

        TextView toastText = layout.findViewById(R.id.toastText);
        toastText.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    private void loginUser(String email, String password) {
        btnLogin.setEnabled(false);
        btnLogin.setText(getString(R.string.logging_in));

        showLoading();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText(getString(R.string.login));
                    hideLoading();

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            showSuccessDialog(getString(R.string.login_successful), () -> loadUserDataAndNavigate(user));
                        }
                    } else {
                        String errorMessage = getLoginErrorMessage(task.getException());
                        showErrorDialog(errorMessage);
                    }
                });
    }

    private void loadUserDataAndNavigate(FirebaseUser user) {
        String uid = user.getUid();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        SharedPreferences.Editor editor = sessionPrefs.edit();
                        editor.putString("email", user.getEmail());
                        editor.putString("uid", uid);
                        editor.putString("fullName", documentSnapshot.getString("fullName"));
                        editor.putString("district", documentSnapshot.getString("district"));
                        editor.putBoolean("is_logged_in", true);
                        editor.apply();

                        navigateToMain();
                    } else {
                        showToastWithLogo(getString(R.string.user_data_not_found));
                        FirebaseAuth.getInstance().signOut();
                    }
                })
                .addOnFailureListener(e -> {
                    showToastWithLogo(getString(R.string.failed_to_load_user_data));
                    FirebaseAuth.getInstance().signOut();
                });
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String getLoginErrorMessage(Exception exception) {
        if (exception == null) {
            return getString(R.string.login_failed) + ". " + getString(R.string.please_try_again);
        }

        String errorCode = exception.getMessage();
        if (errorCode.contains("user-not-found")) {
            return getString(R.string.no_account_found);
        } else if (errorCode.contains("wrong-password")) {
            return getString(R.string.incorrect_password);
        } else if (errorCode.contains("invalid-email")) {
            return getString(R.string.invalid_email_format);
        } else if (errorCode.contains("user-disabled")) {
            return getString(R.string.account_disabled);
        } else if (errorCode.contains("too-many-requests")) {
            return getString(R.string.too_many_attempts);
        } else if (errorCode.contains("network-request-failed")) {
            return getString(R.string.network_error);
        } else {
            return getString(R.string.login_failed) + ": " + errorCode;
        }
    }

    private void showLoading() {
        if (loadingDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.SpinnerDialog);
            View view = getLayoutInflater().inflate(R.layout.progress_overlay, null);
            builder.setView(view);
            builder.setCancelable(false);
            loadingDialog = builder.create();

            // Remove white corners
            if (loadingDialog.getWindow() != null) {
                loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
        }
        loadingDialog.show();
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_error, null);

        TextView tvMessage = view.findViewById(R.id.tvErrorMessage);
        Button btnOk = view.findViewById(R.id.btnOk);

        tvMessage.setText(message);

        builder.setView(view);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnOk.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showSuccessDialog(String message, Runnable onDismissAction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_success, null); // ✅ use separate success layout

        TextView tvMessage = view.findViewById(R.id.tvSuccessMessage);
        Button btnOk = view.findViewById(R.id.btnOkSuccess);

        tvMessage.setText(message);

        builder.setView(view);
        AlertDialog dialog = builder.create();

        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            if (onDismissAction != null) onDismissAction.run();
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
    }

}
