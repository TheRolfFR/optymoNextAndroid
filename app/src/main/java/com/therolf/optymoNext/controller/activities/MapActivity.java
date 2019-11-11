package com.therolf.optymoNext.controller.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.therolf.optymoNext.R;

import java.io.IOException;
import java.io.InputStream;

public class MapActivity extends TopViewActivity {

    @SuppressLint({"MissingSuperCall", "SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_map);

        WebView myWebView = findViewById(R.id.map_webview);

//      https://therolf.fr/tools/map.html
        WebSettings webSettings = myWebView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                injectScriptFile(view, "js/script.js"); // see below ...

                // test if the script was loaded
                view.loadUrl("javascript:setTimeout(test(), 2000)");
            }

            private void injectScriptFile(WebView view, String scriptFile) {
                InputStream input;
                try {
                    input = getAssets().open(scriptFile);
                    byte[] buffer = new byte[input.available()];
                    input.read(buffer);
                    input.close();

                    // String-ify the script byte-array using BASE64 encoding !!!
                    String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
                    view.loadUrl("javascript:(function() {" +
                            "var parent = document.getElementsByTagName('head').item(0);" +
                            "var script = document.createElement('script');" +
                            "script.type = 'text/javascript';" +
                            // Tell the browser to BASE64-decode the string into your script !!!
                            "script.innerHTML = window.atob('" + encoded + "');" +
                            "parent.appendChild(script)" +
                            "})()");
                } catch (IOException e) {
                    // Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        myWebView.loadUrl("https://www.optymo.fr/");
    }
}
