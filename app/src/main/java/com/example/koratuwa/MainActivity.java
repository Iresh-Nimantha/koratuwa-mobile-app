package com.example.koratuwa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends BaseActivity {

    private View cardSettings, cardBell;
    private LinearLayout cardFarmingGuide, cardPriceList, cardSelling, cardGetHelp, cardPriceCalculator, cardMarketplace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cardSettings = findViewById(R.id.cardSettings);
        cardBell = findViewById(R.id.cardBell);

        cardFarmingGuide = findViewById(R.id.cardFarmingGuide);
        cardPriceList = findViewById(R.id.cardPriceList);
        cardSelling = findViewById(R.id.cardSelling);
        cardGetHelp = findViewById(R.id.cardGetHelp);
        cardPriceCalculator = findViewById(R.id.cardPriceCalculator);
        cardMarketplace = findViewById(R.id.cardMarketplace);

        cardSettings.setOnClickListener(view ->
                startActivity(new Intent(this, SettingsActivity.class))
        );
        cardBell.setOnClickListener(view ->
                startActivity(new Intent(this, NotificationsActivity.class))
        );
        cardFarmingGuide.setOnClickListener(view ->
                startActivity(new Intent(this, FarmingGuideActivity.class))
        );
        cardPriceList.setOnClickListener(view ->
                startActivity(new Intent(this, PriceListActivity.class))
        );
        cardSelling.setOnClickListener(view ->
                startActivity(new Intent(this, SellingActivity.class))
        );
        cardGetHelp.setOnClickListener(view ->
                startActivity(new Intent(this, GetHelpActivity.class))
        );
        cardPriceCalculator.setOnClickListener(view ->
                startActivity(new Intent(this, PriceCalculatorActivity.class))
        );
        cardMarketplace.setOnClickListener(view ->
                startActivity(new Intent(this, MarketplaceActivity.class))
        );


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // User not logged in, redirect to LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        // else user is logged in, continue
    }

    public static class NotificationItem {
        public String id;
        public String text;
        public String type;   // "post_action" or "alert"
        public String status; // for post_action notifications: "approved", "rejected", "pending"
        public long createdAt;

        public NotificationItem() { }

        public NotificationItem(String text, String type, String status, long createdAt) {
            this.text = text;
            this.type = type;
            this.status = status;
            this.createdAt = createdAt;
        }
    }

}
