package com.heinrichreimer.meinemensa.network;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.TaskParams;
import com.heinrichreimer.meinemensa.annotations.Location;
import com.heinrichreimer.meinemensa.model.Meal;
import com.heinrichreimer.meinemensa.parse.MenuParser;
import com.heinrichreimer.meinemensa.util.PreferencesUtils;
import com.heinrichreimer.meinemensa.widget.UpdateMenuWidgetService;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SyncService extends GcmTaskService {

    private static final String DEBUG_TAG = "SyncService";

    public static final String TAG_PERIODICALLY = "com.heinrichreimer.meinemensa.SyncService.PERIODICALLY";
    public static final String TAG_IMMEDIATELY = "com.heinrichreimer.meinemensa.SyncService.IMMEDIATELY";

    public static final String ACTION_IMMEDIATELY = "com.heinrichreimer.meinemensa.action.IMMEDIATELY";

    private final OkHttpClient client = new OkHttpClient();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_IMMEDIATELY.equals(intent.getAction())) {
            new SyncImmediatelyTask(intent.getExtras()).execute();
            return START_NOT_STICKY;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public int onRunTask(TaskParams params) {
        Log.d(DEBUG_TAG, "Starting sync...");
        Realm realm = Realm.getDefaultInstance();
        EventBus.getDefault().post(SyncStatusChangedEvent.start());

        DateTime date = PreferencesUtils.getNextWeekdayDate(this);
        @Location int[] locations = PreferencesUtils.getLocations(this);

        DateTime day = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), 0, 0);

        final List<Meal> menu = new ArrayList<>();
        for (int location : locations) {
            menu.addAll(loadMenu(location, day));
        }
        Log.d(DEBUG_TAG, menu.size() + " meals for locations " + Arrays.toString(locations) + ".");

        //Delete old menu for this day
        RealmQuery<Meal> query = realm.where(Meal.class);
        //Locations
        query.beginGroup();
        boolean first = true;
        for (int location : locations) {
            if (Location.Converter.isLocation(location)) {
                if (first) {
                    first = false;
                } else {
                    query.or();
                }
                query.equalTo(Meal.LOCATION, location);
            }
        }
        query.endGroup();
        //Date
        query.equalTo(Meal.DATE, day.getMillis());
        final RealmResults<Meal> results = query.findAll();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                results.deleteAllFromRealm();
            }
        });

        //Add new menus for this day
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(menu);
            }
        });

        realm.close();

        EventBus.getDefault().post(SyncStatusChangedEvent.finish());

        Intent intent = new Intent(this, UpdateMenuWidgetService.class);
        intent.setAction(UpdateMenuWidgetService.ACTION_UPDATE);
        startService(intent);

        return GcmNetworkManager.RESULT_SUCCESS;
    }

    @NonNull
    private List<Meal> loadMenu(@Location int location, DateTime date) {
        Log.d(DEBUG_TAG, "Fetching menu for location " + location + "...");

        // Load the XML from server
        Response<ResponseBody> response = null;
        try {
            response = new Retrofit.Builder()
                    .baseUrl("http://meine-mensa.de")
                    .client(client)
                    .build()
                    .create(MenuService.class)
                    .loadMenu(date.getDayOfMonth(), date.getMonthOfYear(), date.getYear(), location)
                    .execute();
        } catch (IOException e) {
            // Network error
            e.printStackTrace();
        }
        if (response == null) {
            Log.d(DEBUG_TAG, "Network error while loading menu for location " + location + ".");
            return new ArrayList<>();
        }

        MenuParser parser = MenuParser.parse(response.body().byteStream(), "utf-8", date, location);
        if (parser == null) {
            Log.d(DEBUG_TAG, "Error while creating menu parser for location " + location + ".");
            return new ArrayList<>();
        }

        return parser.readMenu();
    }

    private class SyncImmediatelyTask extends AsyncTask<Void, Void, Integer> {
        private Bundle extras;

        private SyncImmediatelyTask(Bundle extras) {
            this.extras = extras;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            return onRunTask(new TaskParams(TAG_IMMEDIATELY, extras));
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == GcmNetworkManager.RESULT_RESCHEDULE) {
                OneoffTask syncTask = new OneoffTask.Builder()
                        .setService(SyncService.class)
                        .setTag(SyncService.TAG_IMMEDIATELY)
                        .setExecutionWindow(0, 10)
                        .setUpdateCurrent(true)
                        .setRequiredNetwork(OneoffTask.NETWORK_STATE_CONNECTED)
                        .build();

                GcmNetworkManager.getInstance(SyncService.this)
                        .schedule(syncTask);
            }
            stopSelf();
        }
    }
}
