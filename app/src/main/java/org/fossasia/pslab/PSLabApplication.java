package org.fossasia.pslab;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import io.realm.Realm;

/**
 * Created by viveksb007 on 4/8/17.
 */

public class PSLabApplication extends Application {

    public RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);

        initializeLeakCanary();
    }

    private void initializeLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        refWatcher = LeakCanary.install(this);
    }
}
