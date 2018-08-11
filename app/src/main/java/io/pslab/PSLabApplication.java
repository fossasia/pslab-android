package io.pslab;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by viveksb007 on 4/8/17.
 */

public class PSLabApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration.Builder v = new RealmConfiguration.Builder().name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded();
        Realm.setDefaultConfiguration(v.build());
    }
}
