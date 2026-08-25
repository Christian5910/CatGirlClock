package com.chris_speccy.catgirlclock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ClockWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_UPDATE_CLOCK = "com.chris_speccy.catgirlclock.UPDATE_CLOCK";

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
                || Intent.ACTION_TIME_TICK.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                || Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_USER_PRESENT.equals(action)) {

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            atualizarWidgetsDaClasse(context, appWidgetManager, ClockWidgetProvider.class);
            atualizarWidgetsDaClasse(context, appWidgetManager, Widget4x1.class);
            atualizarWidgetsDaClasse(context, appWidgetManager, Widget2x1.class);
        }
    }

    private void atualizarWidgetsDaClasse(Context context, AppWidgetManager manager, Class<?> clazz) {
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, clazz));
        for (int id : ids) {
            updateAppWidget(context, manager, id);
        }
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetId);
        int layoutId = (info != null) ? info.initialLayout : R.layout.widget_clock;

        RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);

        // Tolerância de +800ms para compensar despertares antecipados do sistema
        long nowWithBuffer = System.currentTimeMillis() + 800L;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(nowWithBuffer);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        setupFlipper(context, views, R.id.digit1_flipper, hour / 10);
        setupFlipper(context, views, R.id.digit2_flipper, hour % 10);
        setupFlipper(context, views, R.id.digit3_flipper, minute / 10);
        setupFlipper(context, views, R.id.digit4_flipper, minute % 10);

        if (layoutId == R.layout.widget_clock) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, d 'de' MMMM", new Locale("pt", "BR"));
            views.setTextViewText(R.id.date_text, sdf.format(calendar.getTime()).toLowerCase());
        }

        // Clique para atualizar
        Intent clickIntent = new Intent(context, ClockWidgetProvider.class);
        clickIntent.setAction(ACTION_UPDATE_CLOCK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, appWidgetId, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);

        // Agenda exatamente o próximo minuto cravado (+100ms)
        scheduleNextUpdate(context);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static void setupFlipper(Context context, RemoteViews views, int flipperId, int digit) {
        views.removeAllViews(flipperId);

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

        for (int i = 0; i < delays.length; i++) {
            String frameName = String.format(Locale.US, "digit_%d_%02d", digit, i + 1);
            int resId = context.getResources().getIdentifier(frameName, "drawable", context.getPackageName());

            if (resId != 0) {
                int repeticoes = delays[i] / 100;
                for (int r = 0; r < repeticoes; r++) {
                    RemoteViews frameView = new RemoteViews(context.getPackageName(), R.layout.widget_frame);
                    frameView.setImageViewResource(R.id.frame_image, resId);
                    views.addView(flipperId, frameView);
                }
            }
        }
    }

    public static void scheduleNextUpdate(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ClockWidgetProvider.class);
        intent.setAction(ACTION_UPDATE_CLOCK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long now = System.currentTimeMillis();
        // Calcula a virada exata do próximo minuto + 100ms
        long nextMinute = ((now / 60000L) + 1L) * 60000L + 100L;

        if (alarmManager != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextMinute, pendingIntent);
                    } else {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextMinute, pendingIntent);
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextMinute, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextMinute, pendingIntent);
                }
            } catch (SecurityException e) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, nextMinute, pendingIntent);
            }
        }
    }
}