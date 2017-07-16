package org.fossasia.pslab.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.fossasia.pslab.R;

/**
 * Created by viveksb007 on 12/7/17.
 */

public class ExperimentDocFragment extends Fragment {

    private WebView webView;
    private String htmlFile;

    public static ExperimentDocFragment newInstance(String htmlFile) {
        ExperimentDocFragment experimentDocFragment = new ExperimentDocFragment();
        experimentDocFragment.htmlFile = htmlFile;
        return experimentDocFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.experiment_doc, container, false);
        webView = (WebView) view.findViewById(R.id.perform_experiment_wv);
        configureWebView();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        webView.loadUrl("file:///android_asset/DOC_HTML/apps/" + htmlFile);
    }

    @SuppressWarnings("setJavaScriptEnabled")
    private void configureWebView() {
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new MyWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        //webView.setVerticalScrollBarEnabled(true);
        //webView.setHorizontalScrollBarEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
    }

    private class MyWebViewClient extends WebViewClient {

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }
    }

}
