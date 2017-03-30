package com.heinrichreimer.meinemensa.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.view.View;
import android.widget.RemoteViews;

import com.heinrichreimer.meinemensa.R;
import com.heinrichreimer.meinemensa.annotations.Location;
import com.heinrichreimer.meinemensa.app.MainActivity;
import com.heinrichreimer.meinemensa.network.SyncService;
import com.heinrichreimer.meinemensa.util.PreferencesUtils;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class UpdateMenuWidgetService extends Service {

    private static final int REQUEST_CODE_REFRESH = 1;
    private static final int REQUEST_CODE_LAUNCH = 2;
    private static final int REQUEST_CODE_APP = 3;
    private static final int REQUEST_CODE_GRID_CLICK = 4;

    public static final String ACTION_UPDATE = "com.heinrichreimer.meinemensa.action.UPDATE";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_UPDATE.equals(intent.getAction())) {
            updateWidgets();
            stopSelf();
            return START_NOT_STICKY;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWidgets() {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(this.getApplicationContext());
        ComponentName widgetComponent = new ComponentName(this, MenuWidgetProvider.class);
        int[] allWidgetIds = widgetManager.getAppWidgetIds(widgetComponent);
        if (allWidgetIds == null) {
            return;
        }
        updateWidgets(widgetManager, allWidgetIds);
    }

    private void updateWidgets(AppWidgetManager widgetManager, int[] widgetIds) {
        for (int widgetId : widgetIds) {
            updateWidget(widgetManager, widgetId);
        }
    }

    private void updateWidget(AppWidgetManager widgetManager, int widgetId) {
        RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(),
                R.layout.widget_menu);

        //Title
        final int[] locations = PreferencesUtils.getLocations(this);

        final String title;
        if (locations.length > 1) {
            title = getString(R.string.title_activity_main_multiple_locations, locations.length);
        } else {
            title = getString(Location.Converter.toString(locations[0]));
        }
        remoteViews.setTextViewText(R.id.title, title);

        //Subtitle
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd.MM.yyyy");
        String subtitle = formatter.print(PreferencesUtils.getNextWeekdayDate(this));
        remoteViews.setTextViewText(R.id.subtitle, subtitle);
        remoteViews.setViewVisibility(R.id.subtitle, View.VISIBLE);

        //Fill grid
        Intent adapter = new Intent(this, MenuAdapterService.class);
        adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        remoteViews.setRemoteAdapter(R.id.grid, adapter);
        widgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.grid);

        final Intent gridIntent = new Intent(this, DetailActivityLauncher.class);
        final PendingIntent pendingGridIntent = PendingIntent.getBroadcast(
                this,
                REQUEST_CODE_GRID_CLICK,
                gridIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(R.id.grid, pendingGridIntent);

        //Launch click listener
        Intent launchIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingLaunchIntent = PendingIntent.getActivity(
                this,
                REQUEST_CODE_LAUNCH,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.launch, pendingLaunchIntent);

        //Refresh click listener
        Intent refreshIntent = new Intent(this, SyncService.class);
        refreshIntent.setAction(SyncService.ACTION_IMMEDIATELY);
        PendingIntent pendingRefreshIntent = PendingIntent.getService(
                this,
                REQUEST_CODE_REFRESH,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.refresh, pendingRefreshIntent);

        //Toolbar click listener
        Intent toolbarIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingToolbarIntent = PendingIntent.getActivity(
                this,
                REQUEST_CODE_APP,
                toolbarIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.toolbar, pendingToolbarIntent);
        //TODO scale up animation

        widgetManager.updateAppWidget(widgetId, remoteViews);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}