package io.pslab.widgets.loggedData;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import io.pslab.R;
import io.pslab.activity.DataLoggerActivity;

public class LoggedDataWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; i++) {
            Intent intent = new Intent(context, logDataWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.logged_data_widget);
            rv.setRemoteAdapter(android.R.id.list, intent);
            rv.setEmptyView(android.R.id.list, R.id.data_logger_list_view);
            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}

