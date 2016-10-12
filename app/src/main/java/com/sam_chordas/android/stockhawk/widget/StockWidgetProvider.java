package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.DetailsActivity;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;


public class StockWidgetProvider extends AppWidgetProvider {

    private static String LOG_TAG = StockWidgetProvider.class.getSimpleName();

    /**
     * Updates the app widgets when an stock hawk is updated with latest data.
     */

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(LOG_TAG, "onReceive()");
        if (intent.getAction().equals(Utils.STOCK_WIDGET_INTENT_KEY)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName componentName = new ComponentName(context, this.getClass());

            int appWidgetIds[] = appWidgetManager.getAppWidgetIds(componentName);

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list_widget);

        }


    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d(LOG_TAG, "onUpdate()");
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_my_stocks);
            Intent intent = new Intent(context, MyStocksActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.widget_header, pendingIntent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                setRemoteAdapter(context, views);
            } else {
                setRemoteAdapterV11(context, views);
            }

            views.setEmptyView(R.id.list_widget, R.id.empty_text);

            Intent clickIntentTemplate = new Intent(context, DetailsActivity.class);
            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.list_widget, clickPendingIntentTemplate);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
        Log.d(LOG_TAG, "setRemoteAdapter()");
        views.setRemoteAdapter(R.id.list_widget,
                new Intent(context, StockWidgetService.class));
    }

    @Deprecated
    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
        Log.d(LOG_TAG, "setRemoteAdapter()");
        views.setRemoteAdapter(0, R.id.list_widget,
                new Intent(context, StockWidgetService.class));
    }


}
