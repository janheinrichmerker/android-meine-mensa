package com.heinrichreimer.meinemensa.realm;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

public class DefaultMigration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();

        // Migrate to version 2: Add field "vegetarian" to class "Meal"

        if (oldVersion == 1) {
            schema.get("Meal").addField("vegetarian", boolean.class);
            //oldVersion++;
        }

    }
}
