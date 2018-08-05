package io.pslab;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by viveksb007 on 4/8/17.
 */

public class PSLabApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
