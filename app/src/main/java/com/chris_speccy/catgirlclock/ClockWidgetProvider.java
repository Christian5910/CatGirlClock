package com.chris_speccy.catgirlclock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.appwidget.AppWidgetProviderInfo;

public class ClockWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_UPDATE_CLOCK = "com.chris_speccy.catgirlclock.UPDATE_CLOCK";

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

        if (ACTION_UPDATE_CLOCK.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                || Intent.ACTION_BOOT_COMPLETED.equals(action)) {

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, ClockWidgetProvider.class));
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Identifica qual layout este widget especifico esta usando
        AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetId);
        int layoutId = (info != null) ? info.initialLayout : R.layout.widget_clock;

        RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        int d1 = hour / 10;
        int d2 = hour % 10;
        int d3 = minute / 10;
        int d4 = minute % 10;

        setupFlipper(context, views, R.id.digit1_flipper, d1);
        setupFlipper(context, views, R.id.digit2_flipper, d2);
        setupFlipper(context, views, R.id.digit3_flipper, d3);
        setupFlipper(context, views, R.id.digit4_flipper, d4);

        // Atualiza a data apenas nos layouts que contem o campo date_text
        if (layoutId == R.layout.widget_clock || layoutId == R.layout.widget_clock_2x2) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, d 'de' MMMM", new Locale("pt", "BR"));
            String dataTexto = sdf.format(calendar.getTime());
            views.setTextViewText(R.id.date_text, dataTexto.toLowerCase());
        }

        // Clique para atualizar
        Intent clickIntent = new Intent(context, ClockWidgetProvider.class);
        clickIntent.setAction(ACTION_UPDATE_CLOCK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, appWidgetId, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);

        scheduleNextUpdate(context);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    // Método com a nova arquitetura de expansão de frames de 100ms
    private static void setupFlipper(Context context, RemoteViews views, int flipperId, int digit) {
        views.removeAllViews(flipperId);

        // A sua ideia: cravamos a velocidade global do ViewFlipper no menor tempo possível (100ms)
        views.setInt(flipperId, "setFlipInterval", 100);

        // Injetamos os dados exatos do seu script em arrays
        int[] delays;
        switch (digit) {
            case 0: delays = new int[]{500, 200, 100, 100, 200, 500, 200, 100, 100, 200}; break;
            case 1: delays = new int[]{500, 200, 100, 200, 100, 200, 100, 200, 100}; break;
            case 2: delays = new int[]{500, 200, 100, 200, 100, 100, 100, 100, 200}; break;
            case 3: delays = new int[]{500, 100, 200, 500, 100, 200, 500, 100, 200, 500, 100, 200}; break;
            case 4: delays = new int[]{100, 100, 100, 100, 100, 100, 100, 100, 100, 100}; break;
            case 5: delays = new int[]{500, 200, 200, 100, 500, 200, 500, 100}; break;
            case 6: delays = new int[]{100, 100, 200, 100, 100, 100, 100, 200, 100}; break;
            case 7: delays = new int[]{1000, 200, 200, 1000, 200, 1000, 200, 1000, 200}; break;
            case 8: delays = new int[]{2000, 100, 200, 200, 100, 500}; break;
            case 9: delays = new int[]{2000, 100, 1000, 100, 2000, 200, 1000, 100}; break;
            default: delays = new int[]{100}; break;
        }

        // Percorre cada frame do GIF original
        for (int i = 0; i < delays.length; i++) {
            // Carrega o nome do arquivo, ex: "digit_5_01" (lembrando que i começa em 0, então i+1)
            String frameName = String.format(Locale.US, "digit_%d_%02d", digit, i + 1);
            int resId = context.getResources().getIdentifier(frameName, "drawable", context.getPackageName());

            if (resId != 0) {
                // Aqui a mágica acontece: calcula quantas "cópias" de 100ms este frame precisa
                int repeticoes = delays[i] / 100;

                // Adiciona o MESMO frame o número exato de vezes
                for (int r = 0; r < repeticoes; r++) {
                    RemoteViews frameView = new RemoteViews(context.getPackageName(), R.layout.widget_frame);
                    frameView.setImageViewResource(R.id.frame_image, resId);
                    views.addView(flipperId, frameView);
                }
            }
        }
    }

    private static void scheduleNextUpdate(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, ClockWidgetProvider.class);
        intent.setAction(ACTION_UPDATE_CLOCK);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long now = System.currentTimeMillis();
        long nextMinute = now + (60000 - (now % 60000));

        if (alarmManager != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextMinute, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextMinute, pendingIntent);
                }
            } catch (SecurityException e) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextMinute, pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, nextMinute, pendingIntent);
                }
            }
        }
    }
}