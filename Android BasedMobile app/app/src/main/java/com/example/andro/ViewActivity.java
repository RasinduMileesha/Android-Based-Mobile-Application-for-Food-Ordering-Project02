package com.example.andro;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ViewActivity extends AppCompatActivity {
    private WebView webView;
    private Button button2;
    private Handler handler = new Handler(); // Handler to post delayed tasks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_new); // Use the layout XML file

        // Initialize WebView and Button
        WebView webView = findViewById(R.id.webView);
        Button button2 = findViewById(R.id.button2);

        // Set up WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Enable JavaScript if needed
        webSettings.setLoadWithOverviewMode(true); // Load pages in overview mode
        webSettings.setUseWideViewPort(true); // Use wide viewport
        webSettings.setDomStorageEnabled(true); // Enable DOM storage
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); // Cache resources

        // Enable hardware acceleration if not already enabled
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        // Load URL
        webView.loadUrl("http://srfoodtruck.ddns.net:5025/");

        // Make the button invisible initially
        button2.setVisibility(View.INVISIBLE);

        // Set up Button click listener
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to start MainActivity
                Intent intent = new Intent(ViewActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Make the button visible after a delay of 15 seconds (15000 milliseconds)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                button2.setVisibility(View.VISIBLE);
            }
        }, 15000); // 15000 milliseconds = 15 seconds
    }
}
