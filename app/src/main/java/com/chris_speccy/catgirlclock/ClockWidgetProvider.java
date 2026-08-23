package com.chris_speccy.catgirlclock;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ClockWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();

        // Atualização automática minuto a minuto acionada pelo sistema Android
        if (Intent.ACTION_TIME_TICK.equals(action) ||
                Intent.ACTION_TIME_CHANGED.equals(action) ||
                Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, ClockWidgetProvider.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        }
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_clock);

        Calendar cal = Calendar.getInstance();
        int hora = cal.get(Calendar.HOUR_OF_DAY);
        int minuto = cal.get(Calendar.MINUTE);

        int h1 = hora / 10;
        int h2 = hora % 10;
        int m1 = minuto / 10;
        int m2 = minuto % 10;

        // Renderiza o primeiro quadro de cada número para a Tela Inicial
        views.setImageViewResource(R.id.digit_h1, getStaticFrameResource(context, h1));
        views.setImageViewResource(R.id.digit_h2, getStaticFrameResource(context, h2));
        views.setImageViewResource(R.id.digit_m1, getStaticFrameResource(context, m1));
        views.setImageViewResource(R.id.digit_m2, getStaticFrameResource(context, m2));

        // Formata a data (ex: dom., 23 de agosto)
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d 'de' MMMM", new Locale("pt", "BR"));
        views.setTextViewText(R.id.text_date, sdf.format(new Date()));

        // Clique no widget abre a interface do aplicativo
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        views.setOnClickPendingIntent(R.id.digit_h1, pendingIntent);
        views.setOnClickPendingIntent(R.id.digit_h2, pendingIntent);
        views.setOnClickPendingIntent(R.id.digit_m1, pendingIntent);
        views.setOnClickPendingIntent(R.id.digit_m2, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static int getStaticFrameResource(Context context, int digito) {
        String name = String.format(Locale.US, "digit_%d_01", digito);
        return context.getResources().getIdentifier(name, "drawable", context.getPackageName());
    }
}