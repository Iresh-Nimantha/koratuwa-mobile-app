package com.example.koratuwa;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class KoratuwaApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
