package org.fossasia.pslab.others;

import android.app.Activity;
import android.content.ComponentName;
import android.net.Uri;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import org.fossasia.pslab.R;

/**
 * Created by nikit on 28/9/17.
 */

public class CustomTabService {
    private CustomTabsClient mCustomTabsClient;
    private CustomTabsSession mCustomTabsSession;
    private CustomTabsServiceConnection mCustomTabsServiceConnection;
    private CustomTabsIntent mCustomTabsIntent;

    private Activity activity;

    public CustomTabService (Activity currentActivity) {
        this.activity = currentActivity;
        init();
    }

    private void init() {
        mCustomTabsServiceConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
                mCustomTabsClient = customTabsClient;
                mCustomTabsClient.warmup(0L);
                mCustomTabsSession = mCustomTabsClient.newSession(null);
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mCustomTabsClient= null;
            }
        };
        CustomTabsClient.bindCustomTabsService(activity, activity.getPackageName(), mCustomTabsServiceConnection);
        mCustomTabsIntent = new CustomTabsIntent.Builder(mCustomTabsSession)
                .setShowTitle(true)
                .setToolbarColor(ContextCompat.getColor(activity, R.color.colorPrimary))
                .build();
    }

    public void launchUrl(String Url){
        try{
            mCustomTabsIntent.launchUrl(activity, Uri.parse(Url));
        }catch (Exception e){
            Toast.makeText(activity.getApplication(), "Error: "+e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
