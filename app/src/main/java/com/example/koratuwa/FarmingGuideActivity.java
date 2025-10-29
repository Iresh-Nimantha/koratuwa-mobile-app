package com.example.koratuwa;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FarmingGuideActivity extends BaseActivity {

    private static final String TAG = "HFApiError";

    // TODO: Replace with your actual Hugging Face token
    private static final String HF_TOKEN = System.getenv("HF_TOKEN");
    private static final String MODEL = "deepseek-ai/DeepSeek-V3.2-Exp:novita";

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 2000; // 2 seconds

    private ImageView ivBack;
    private AutoCompleteTextView actvDesiredCrop, actvLandLocation, actvExistingCropType;
    private EditText etLandSize, etAvailableBudget;
    private RadioButton rbIrrigationYes, rbIrrigationNo, rbMachineryYes, rbMachineryNo;
    private Button btnCheck;

    // Overlay and spinner
    private FrameLayout progressOverlay;
    private ProgressBar progressBar;

    // Response layout
    private TextView tvZone, tvClimaticZone, tvAgroEcoZone, tvAgroEcoRegion, tvTerrain;
    private TextView tvMajorSoilType, tvLandUse, tvSecondaryLandUse, tvAnnualRainfall;
    private TextView tvFarmingAdvice;
    private Button btnNewSearch, btnShare;

    private JSONObject agroEcoProfile = null;
    private boolean isShowingResponse = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        if (HF_TOKEN.equals("YOUR_HF_TOKEN_HERE")) {
            showToastWithLogo("Please set your Hugging Face token in the code");
            finish();
            return;
        }

        setContentView(R.layout.activity_farming_guide);
        setupInputLayout();
    }

    private void setupInputLayout() {
        isShowingResponse = false;

        ivBack = findViewById(R.id.ivBack);
        actvDesiredCrop = findViewById(R.id.actvDesiredCrop);
        actvLandLocation = findViewById(R.id.actvLandLocation);
        actvExistingCropType = findViewById(R.id.actvExistingCropType);
        etLandSize = findViewById(R.id.etLandSize);
        etAvailableBudget = findViewById(R.id.etAvailableBudget);
        rbIrrigationYes = findViewById(R.id.rbIrrigationYes);
        rbIrrigationNo = findViewById(R.id.rbIrrigationNo);
        rbMachineryYes = findViewById(R.id.rbMachineryYes);
        rbMachineryNo = findViewById(R.id.rbMachineryNo);
        btnCheck = findViewById(R.id.btnCheck);
        progressOverlay = findViewById(R.id.progressOverlay);
        progressBar = findViewById(R.id.progressBar);

        // Load dropdown suggestions
        ArrayAdapter<String> fruitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.variants_fruits));
        actvDesiredCrop.setAdapter(fruitAdapter);
        actvDesiredCrop.setThreshold(1);
        actvExistingCropType.setAdapter(fruitAdapter);
        actvExistingCropType.setThreshold(1);

        ArrayAdapter<String> landAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.land_location_array));
        actvLandLocation.setAdapter(landAdapter);
        actvLandLocation.setThreshold(1);

        ivBack.setOnClickListener(v -> finish());
        btnCheck.setOnClickListener(v -> generateAndShowResponse());

        hideLoadingOverlay();
    }

    private void setupResponseLayout() {
        isShowingResponse = true;

        tvZone = findViewById(R.id.tvZone);
        tvClimaticZone = findViewById(R.id.tvClimaticZone);
        tvAgroEcoZone = findViewById(R.id.tvAgroEcoZone);
        tvAgroEcoRegion = findViewById(R.id.tvAgroEcoRegion);
        tvTerrain = findViewById(R.id.tvTerrain);
        tvMajorSoilType = findViewById(R.id.tvMajorSoilType);
        tvLandUse = findViewById(R.id.tvLandUse);
        tvSecondaryLandUse = findViewById(R.id.tvSecondaryLandUse);
        tvAnnualRainfall = findViewById(R.id.tvAnnualRainfall);
        tvFarmingAdvice = findViewById(R.id.tvFarmingAdvice);
        tvFarmingAdvice.setText(getLocalizedLoadingMessage());

        btnShare = findViewById(R.id.btnShare);
        progressOverlay = findViewById(R.id.progressOverlay);
        progressBar = findViewById(R.id.progressBar);

        btnShare.setOnClickListener(v -> {
            try {
                String fullReportText = buildFullReportString();
                File pdfFile = createMultiPagePdfFromText(fullReportText);

                if (pdfFile.exists()) {
                    Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", pdfFile);
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("application/pdf");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(shareIntent, "Share Full Farming Advice PDF"));
                } else {
                    showToastWithLogo("Failed to create PDF");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToastWithLogo("Error sharing PDF: " + e.getMessage());
            }
        });

        ivBack.setOnClickListener(v -> {
            setContentView(R.layout.activity_farming_guide);
            setupInputLayout();
        });

        hideLoadingOverlay();
    }

    private void showToastWithLogo(String message) {
        View layout = LayoutInflater.from(this).inflate(R.layout.toast_with_image, findViewById(android.R.id.content), false);

        ImageView toastLogo = layout.findViewById(R.id.toastLogo);
        toastLogo.setImageResource(R.drawable.log); // Replace 'my_logo' with your actual logo drawable resource name

        TextView toastText = layout.findViewById(R.id.toastText);
        toastText.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    private void showLoadingOverlay() {
        if (progressOverlay != null) progressOverlay.setVisibility(View.VISIBLE);
    }

    private void hideLoadingOverlay() {
        if (progressOverlay != null) progressOverlay.setVisibility(View.GONE);
    }

    private void generateAndShowResponse() {
        String location = actvLandLocation.getText().toString().trim();
        String desiredCrop = actvDesiredCrop.getText().toString().trim();

        if (location.isEmpty()) {
            showToastWithLogo("Land location is required for accurate advice.");
            return;
        }
        if (desiredCrop.isEmpty()) {
            showToastWithLogo("Desired crop is required for personalized advice.");
            return;
        }

        btnCheck.setEnabled(false);
        btnCheck.setText("Analyzing...");
        showLoadingOverlay();

        new AgroEcoProfileTask().execute(location);
    }

    private class AgroEcoProfileTask extends AsyncTask<String, Void, String> {
        private Exception exception;
        private int retryCount = 0;

        @Override
        protected String doInBackground(String... params) {
            String location = params[0];
            while (retryCount < MAX_RETRY_ATTEMPTS) {
                try {
                    String response = callHFApi(getAgroEcoSystemPrompt(location));
                    if (response != null && !response.isEmpty()) return response;
                } catch (Exception e) {
                    exception = e;
                    if (isRetryableError(e)) {
                        retryCount++;
                        try { Thread.sleep(RETRY_DELAY_MS * retryCount); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                        continue;
                    }
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            btnCheck.setEnabled(true);
            btnCheck.setText("Check");
            hideLoadingOverlay();

            if (exception != null) {
                showToastWithLogo("Error: " + exception.getMessage());
            } else if (response == null || response.isEmpty()) {
                showToastWithLogo("Failed to get agro profile. Retry.");
            } else {
                try {
                    agroEcoProfile = new JSONObject(response);
                    showLoadingOverlay();
                    new FarmingAdviceTask().execute(createComprehensivePrompt());
                } catch (Exception e) {
                    showToastWithLogo("Error parsing agro profile: " + e.getMessage());
                }
            }
        }
    }

    private class FarmingAdviceTask extends AsyncTask<String, Void, String> {
        private Exception exception;
        private int retryCount = 0;

        @Override
        protected String doInBackground(String... params) {
            String prompt = params[0];
            while (retryCount < MAX_RETRY_ATTEMPTS) {
                try {
                    String response = callHFApi(prompt);
                    if (response != null && !response.isEmpty()) return response;
                } catch (Exception e) {
                    exception = e;
                    if (isRetryableError(e)) {
                        retryCount++;
                        try { Thread.sleep(RETRY_DELAY_MS * retryCount); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                        continue;
                    }
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            btnCheck.setEnabled(true);
            btnCheck.setText("Check");
            hideLoadingOverlay();

            if (exception != null) {
                showToastWithLogo("Error getting farming advice: " + exception.getMessage());
            } else if (response == null || response.isEmpty()) {
                showToastWithLogo("Failed to get advice. Retry.");
            } else {
                showResponseInLayout(response);
            }
        }
    }

    private boolean isRetryableError(Exception e) {
        String message = e.getMessage();
        return message != null && (message.contains("503") || message.contains("overloaded") ||
                message.contains("UNAVAILABLE") || message.contains("timeout") || message.contains("connection"));
    }

    private void showResponseInLayout(String markdownResponse) {
        try {
            setContentView(R.layout.activity_farming_guide_response);
            setupResponseLayout();

            if (agroEcoProfile != null) {
                tvZone.setText(agroEcoProfile.optString("zone", "N/A"));
                tvClimaticZone.setText(agroEcoProfile.optString("climatic_Z", "N/A"));
                tvAgroEcoZone.setText(agroEcoProfile.optString("AGRO_ECO_Z", "N/A"));
                tvAgroEcoRegion.setText(agroEcoProfile.optString("AGRO_ECO_R", "N/A"));
                tvTerrain.setText(agroEcoProfile.optString("terrain", "N/A"));
                tvMajorSoilType.setText(agroEcoProfile.optString("major_soil_type", "N/A"));
                tvLandUse.setText(agroEcoProfile.optString("land_use", "N/A"));
                tvSecondaryLandUse.setText(agroEcoProfile.optString("secondary_land_use", "N/A"));
                tvAnnualRainfall.setText(agroEcoProfile.optString("annual_rainfall_mm", "N/A"));
            }

            tvFarmingAdvice.setText(formatMarkdownForDisplay(markdownResponse));

        } catch (Exception e) {
            Toast.makeText(this, "Error displaying response: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String formatMarkdownForDisplay(String markdownText) {
        if (markdownText == null || markdownText.isEmpty()) return "No farming advice available.";

        String formatted = markdownText;

        // Replace HTML <br> with " - " for better flow in tables/lists
        formatted = formatted.replaceAll("<br>", " - ");

        // Handle headings with emojis for better visual appeal
        formatted = formatted.replaceAll("(?m)^#\\s+(.*)$", "\n\n🌟 $1\n");  // H1 with star emoji
        formatted = formatted.replaceAll("(?m)^##\\s+(.*)$", "\n\n🔹 $1\n");  // H2 with dot
        formatted = formatted.replaceAll("(?m)^###\\s+(.*)$", "\n\n▪ $1\n");  // H3 with bullet

        // Handle bold and italic: Use Unicode bold/italic approximations or symbols
        formatted = formatted.replaceAll("\\*\\*(.*?)\\*\\*", "【$1】");  // Bold with brackets
        formatted = formatted.replaceAll("\\*(.*?)(?<!\\*)\\*", "_$1_");  // Italic with underscores

        // Handle unordered lists: Convert - or * to bullet points with indentation
        formatted = formatted.replaceAll("(?m)^\\s*[-*+]\\s+(.*)$", "\n• $1");

        // Handle ordered lists: Preserve numbers with spacing
        formatted = formatted.replaceAll("(?m)^\\s*(\\d+)\\.\\s+(.*)$", "\n$1. $2");

        // Handle code blocks: Wrap in monospace-like symbols
        formatted = formatted.replaceAll("```(.*?)```", "\n```\n$1\n```\n");  // Preserve code blocks
        formatted = formatted.replaceAll("`([^`]+)`", "`$1`");  // Inline code

        // Handle links: Convert to clickable text [text](url) -> text (url)
        formatted = formatted.replaceAll("\\[([^\\]]+)\\]\\(([^\\)]+)\\)", "$1 ($2)");

        // Clean up excessive newlines and trim
        formatted = formatted.replaceAll("\\n{3,}", "\n\n").trim();

        // Ensure proper paragraph breaks
        formatted = formatted.replaceAll("(?m)^\\s*\\n", "\n");  // Remove empty lines at start of paragraphs

        return formatted;
    }

    private String buildFullReportString() {
        StringBuilder fullReport = new StringBuilder();
        if (agroEcoProfile != null) {
            fullReport.append("AGRO-ECOLOGICAL PROFILE:\n\n");
            fullReport.append("Zone: ").append(agroEcoProfile.optString("zone", "N/A")).append("\n");
            fullReport.append("Climatic Zone: ").append(agroEcoProfile.optString("climatic_Z", "N/A")).append("\n");
            fullReport.append("Agro-Ecological Zone Code: ").append(agroEcoProfile.optString("AGRO_ECO_Z", "N/A")).append("\n");
            fullReport.append("Agro-Ecological Region: ").append(agroEcoProfile.optString("AGRO_ECO_R", "N/A")).append("\n");
            fullReport.append("Terrain: ").append(agroEcoProfile.optString("terrain", "N/A")).append("\n");
            fullReport.append("Major Soil Type: ").append(agroEcoProfile.optString("major_soil_type", "N/A")).append("\n");
            fullReport.append("Land Use: ").append(agroEcoProfile.optString("land_use", "N/A")).append("\n");
            fullReport.append("Secondary Land Use: ").append(agroEcoProfile.optString("secondary_land_use", "N/A")).append("\n");
            fullReport.append("Annual Rainfall (mm): ").append(agroEcoProfile.optString("annual_rainfall_mm", "N/A")).append("\n");
            fullReport.append("Notes: ").append(agroEcoProfile.optString("notes", "N/A")).append("\n\n");
        } else {
            fullReport.append("AGRO-ECOLOGICAL PROFILE: N/A\n\n");
        }

        fullReport.append("FARMING ADVICE:\n\n");
        fullReport.append(tvFarmingAdvice.getText().toString());
        return fullReport.toString();
    }

    // --- Hugging Face API prompt helpers ---
    private String getAgroEcoSystemPrompt(String location) {
        return "You are an expert consultant on Sri Lankan Agro-ecological Regions. " +
                "Provide a detailed JSON profile for location: " + location + ". " +
                "Keys: zone, climatic_Z, AGRO_ECO_Z, AGRO_ECO_R, terrain, major_soil_type, land_use, secondary_land_use, annual_rainfall_mm, notes. " +
                "Output must be valid minified JSON only.";
    }

    private String createComprehensivePrompt() {
        // Combine farmer profile + agro profile + instructions
        String location = actvLandLocation.getText().toString().trim();
        String desiredCrop = actvDesiredCrop.getText().toString().trim();
        String landSize = etLandSize.getText().toString().trim();
        String budget = etAvailableBudget.getText().toString().trim();
        String existingCrop = actvExistingCropType.getText().toString().trim();
        boolean hasIrrigation = rbIrrigationYes.isChecked();
        boolean hasMachinery = rbMachineryYes.isChecked();

        StringBuilder prompt = new StringBuilder();
        prompt.append(getFarmingAdviceSystemPrompt()).append("\n\n");
        prompt.append("AGRO-ECOLOGICAL PROFILE:\n");
        if (agroEcoProfile != null) {
            prompt.append(agroEcoProfile.toString()).append("\n\n");
        }
        prompt.append("FARMER PROFILE:\n");
        prompt.append("Location: ").append(location).append("\n");
        prompt.append("Desired Crop: ").append(desiredCrop).append("\n");
        prompt.append("Existing Crop: ").append(existingCrop.isEmpty() ? "None" : existingCrop).append("\n");
        prompt.append("Land Size: ").append(landSize.isEmpty() ? "Not specified" : landSize).append("\n");
        prompt.append("Budget: ").append(budget.isEmpty() ? "Not specified" : budget).append("\n");
        prompt.append("Irrigation: ").append(hasIrrigation ? "Yes" : "No").append("\n");
        prompt.append("Machinery: ").append(hasMachinery ? "Yes" : "No").append("\n\n");
        prompt.append("Provide detailed, crop-specific, step-by-step farming advice tailored for Sri Lanka.");
        return prompt.toString();
    }

    private String getFarmingAdviceSystemPrompt() {
        String language = getCurrentLanguageCode();
        String languageInstruction = "Generate entire response in English.";
        if ("si".equals(language)) languageInstruction = "Generate entire response in Sinhala.";
        else if ("ta".equals(language)) languageInstruction = "Generate entire response in Tamil.";
        return languageInstruction + " Provide comprehensive farming guidance with headings, bullet points, step-by-step instructions, budgets, timelines, and market insights. Use standard markdown for formatting, including tables and hyperlinks to relevant official sources like the Department of Agriculture[](https://doa.gov.lk/) and Export Development Board[](https://www.srilankabusiness.com/). Avoid HTML tags like <br>; use line breaks for multi-line cells if needed but keep single line.";
    }

    private String getCurrentLanguageCode() {
        return getCurrentLanguage(); // BaseActivity method
    }

    private String getLocalizedLoadingMessage() {
        switch (getCurrentLanguageCode()) {
            case "si": return "කෘෂිකර්ම උපදෙස් පූර්ණ විස්තර සමඟ ලබා ගනිමින්...";
            case "ta": return "விவசாய ஆலோசனை விரிவான விவரங்களுடன் பெறப்படுகிறது...";
            default: return "Loading comprehensive farming advice...";
        }
    }

    // --- Hugging Face API call ---
    private String callHFApi(String prompt) throws Exception {
        String endpoint = "https://router.huggingface.co/v1/chat/completions";

        Log.d(TAG, "Attempting to connect to: " + endpoint);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", MODEL);
        JSONArray messages = new JSONArray();
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.put(userMessage);
        requestBody.put("messages", messages);

        // Note: generationConfig might not be standard for HF Router; adjust if needed
        JSONObject generationConfig = new JSONObject();
        generationConfig.put("max_tokens", 8192);
        generationConfig.put("temperature", 0.3);
        requestBody.put("generationConfig", generationConfig);

        try {
            HttpURLConnection con = (HttpURLConnection) new URL(endpoint).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "Bearer " + HF_TOKEN);
            con.setDoOutput(true);
            con.setConnectTimeout(60000);  // Increased to 60s
            con.setReadTimeout(90000);     // Increased to 90s

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int code = con.getResponseCode();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    code >= 200 && code < 300 ? con.getInputStream() : con.getErrorStream(), "utf-8"));

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) response.append(line.trim());
            br.close();
            con.disconnect();

            if (code >= 200 && code < 300) return parseHFResponse(response.toString());
            else throw new Exception("API call failed: " + code + " " + response.toString());
        } catch (java.net.UnknownHostException e) {
            Log.e(TAG, "DNS Resolution Failed: " + e.getMessage());
            throw new Exception("Network error: Cannot reach Hugging Face server. Check internet/DNS.");
        } catch (java.net.SocketTimeoutException e) {
            Log.e(TAG, "Connection Timeout: " + e.getMessage());
            throw new Exception("Timeout: Slow network or server busy. Try again.");
        } catch (Exception e) {
            Log.e(TAG, "API Error: " + e.getMessage());
            throw e;
        }
    }

    private String parseHFResponse(String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            if (json.has("choices") && json.getJSONArray("choices").length() > 0) {
                JSONObject choice = json.getJSONArray("choices").getJSONObject(0);
                if (choice.has("message")) {
                    return choice.getJSONObject("message").getString("content");
                }
            }
            return "No response generated";
        } catch (Exception e) {
            Log.e(TAG, "Error parsing response", e);
            return "Error parsing response: " + e.getMessage();
        }
    }

    // Helper method to draw styled lines (handling bold 【text】)
    private void drawStyledLine(Canvas canvas, String lineText, float startX, float y, Paint defaultPaint) {
        float x = startX;
        int idx = 0;
        while (idx < lineText.length()) {
            int boldStart = lineText.indexOf("【", idx);
            if (boldStart == -1) {
                // Draw remaining as normal
                canvas.drawText(lineText.substring(idx), x, y, defaultPaint);
                break;
            }
            // Draw normal before bold
            if (boldStart > idx) {
                String normal = lineText.substring(idx, boldStart);
                canvas.drawText(normal, x, y, defaultPaint);
                x += defaultPaint.measureText(normal);
            }
            // Find bold end
            int boldEnd = lineText.indexOf("】", boldStart + 1);
            if (boldEnd == -1) {
                // No end, draw 【 as normal
                canvas.drawText("【", x, y, defaultPaint);
                x += defaultPaint.measureText("【");
                idx = boldStart + 1;
                continue;
            }
            String boldText = lineText.substring(boldStart + 1, boldEnd);
            Paint boldPaint = new Paint(defaultPaint);
            boldPaint.setFakeBoldText(true);
            canvas.drawText(boldText, x, y, boldPaint);
            x += boldPaint.measureText(boldText);
            idx = boldEnd + 1;
        }
    }

    // --- PDF generation ---
    private File createMultiPagePdfFromText(String text) throws Exception {
        PdfDocument pdfDocument = new PdfDocument();
        Paint basePaint = new Paint();
        basePaint.setTextSize(11);
        basePaint.setColor(0xFF000000); // Black
        int pageWidth = 595, pageHeight = 842, margin = 40, lineHeight = 16, reservedSpace = 60;
        int maxLinesPerPage = (pageHeight - 2 * margin - reservedSpace) / lineHeight;

        // Split into raw lines for processing
        String[] rawLines = text.split("\n");
        List<String> wrappedLines = new ArrayList<>();
        for (String rawLine : rawLines) {
            String paragraph = rawLine.trim();
            if (paragraph.isEmpty()) {
                wrappedLines.add("");
                continue;
            }
            Paint tempPaint = new Paint(basePaint);
            String[] words = paragraph.split(" ");
            StringBuilder currentLine = new StringBuilder();
            for (String word : words) {
                String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
                if (tempPaint.measureText(testLine) > (pageWidth - 2 * margin)) {
                    wrappedLines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    currentLine = new StringBuilder(testLine);
                }
            }
            if (currentLine.length() > 0) {
                wrappedLines.add(currentLine.toString());
            }
        }

        List<String> lines = wrappedLines;
        int totalPages = (int) Math.ceil(lines.size() / (float) maxLinesPerPage);
        int lineIndex = 0;

        for (int pageNum = 0; pageNum < totalPages; pageNum++) {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum + 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            float y = margin;
            if (pageNum == 0) {
                Paint headerPaint = new Paint();
                headerPaint.setTextSize(14);
                headerPaint.setFakeBoldText(true);
                String headerText = "AI POWERED KORATUWA FARMING ADVICE REPORT";
                float x = (pageWidth - headerPaint.measureText(headerText)) / 2f;
                canvas.drawText(headerText, x, y, headerPaint);
                y += 30;
            }

            int linesOnPage = 0;
            while (lineIndex < lines.size() && linesOnPage < maxLinesPerPage) {
                String line = lines.get(lineIndex);
                if (line.isEmpty()) {
                    y += lineHeight;
                    lineIndex++;
                    linesOnPage++;
                    continue;
                }

                // Check for table separator
                if (line.contains("---")) {
                    canvas.drawLine(margin, y + lineHeight / 2, pageWidth - margin, y + lineHeight / 2, basePaint);
                    y += lineHeight;
                    lineIndex++;
                    linesOnPage++;
                    continue;
                }

                // Prepare drawing
                float drawMargin = margin;
                Paint drawPaint = new Paint(basePaint);
                int currentLineHeight = lineHeight;
                String drawLine = line.trim();

                boolean isTableRow = drawLine.startsWith("|") && drawLine.endsWith("|");
                if (isTableRow) {
                    // Simple table column drawing
                    String[] cols = drawLine.split("\\|");
                    float colSpacing = 5f;
                    int numCols = cols.length - 1;
                    if (numCols > 0) {
                        float colW = (pageWidth - 2 * margin - (numCols - 1) * colSpacing) / numCols;
                        for (int c = 1; c < cols.length - 1; c++) {
                            String col = cols[c].trim();
                            Paint colP = new Paint(basePaint);
                            // Bold for first column if looks like header
                            if (c == 1 && drawLine.contains("Year")) {
                                colP.setFakeBoldText(true);
                            }
                            // Truncate if too long
                            if (basePaint.measureText(col) > colW) {
                                col = col.substring(0, Math.min(30, col.length())) + "...";
                            }
                            canvas.drawText(col, margin + (c - 1) * (colW + colSpacing), y, colP);
                        }
                        // Horizontal separator below table row
                        canvas.drawLine(margin, y + 2, pageWidth - margin, y + 2, basePaint);
                    }
                } else {
                    // Non-table: check styles
                    if (drawLine.startsWith("🌟")) {
                        drawPaint.setTextSize(16);
                        drawPaint.setFakeBoldText(true);
                        drawLine = drawLine.substring(2).trim();
                        float tw = drawPaint.measureText(drawLine);
                        drawMargin = (pageWidth - tw) / 2;
                        currentLineHeight = 20;
                    } else if (drawLine.startsWith("🔹")) {
                        drawPaint.setTextSize(14);
                        drawPaint.setFakeBoldText(true);
                        drawLine = drawLine.substring(2).trim();
                        currentLineHeight = 18;
                    } else if (drawLine.startsWith("▪")) {
                        drawMargin += 15;
                        drawLine = drawLine.substring(2).trim();
                        currentLineHeight = 16;
                    } else if (drawLine.startsWith("•")) {
                        drawMargin += 20;
                        drawLine = drawLine.substring(2).trim();
                        currentLineHeight = 16;
                    }
                    // Draw styled line (handles 【bold】)
                    drawStyledLine(canvas, drawLine, drawMargin, y, drawPaint);
                }

                y += currentLineHeight;
                lineIndex++;
                linesOnPage++;
            }

            if (pageNum == totalPages - 1) {
                y += 20;
                Paint disclaimerPaint = new Paint();
                disclaimerPaint.setTextSize(10);
                disclaimerPaint.setColor(0xFF555555);
                String disclaimer = "Disclaimer: This AI-generated Koratuwa farming advice is for guidance only. " +
                        "Consult experts before making decisions.";
                canvas.drawText(disclaimer, margin, y, disclaimerPaint);
            }

            String pageNumberText = "Page " + (pageNum + 1) + " of " + totalPages;
            Paint footerPaint = new Paint();
            footerPaint.setTextSize(11);
            float textWidth = footerPaint.measureText(pageNumberText);
            canvas.drawText(pageNumberText, pageWidth - margin - textWidth, pageHeight - 20, footerPaint);

            pdfDocument.finishPage(page);
        }

        File pdfFile = new File(getCacheDir(), "koratuwa_farming_advice_report.pdf");
        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            pdfDocument.writeTo(fos);
        }
        pdfDocument.close();
        return pdfFile;
    }
}