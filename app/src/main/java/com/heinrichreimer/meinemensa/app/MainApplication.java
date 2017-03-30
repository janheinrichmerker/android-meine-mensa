package com.heinrichreimer.meinemensa.app;

import android.app.Application;

import com.heinrichreimer.meinemensa.realm.DefaultMigration;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainApplication extends Application {

    private static final String REALM_NAME = "meinemensa.realm";
    private static final int REALM_SCHEMA_VERSION = 2;

    @Override
    public void onCreate() {
        super.onCreate();
        RealmConfiguration config = new RealmConfiguration.Builder(this)
                .name(REALM_NAME)
                .schemaVersion(REALM_SCHEMA_VERSION)
                .migration(new DefaultMigration())
                //.deleteRealmIfMigrationNeeded() // for debugging
                .build();
        Realm.setDefaultConfiguration(config);
    }
}
