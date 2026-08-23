package com.chris_speccy.catgirlclock;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnScreensaver = findViewById(R.id.btn_screensaver);
        Button btnAddWidget = findViewById(R.id.btn_add_widget);

        // Direciona o usuário para as Configurações de Proteção de Tela / Daydream
        if (btnScreensaver != null) {
            btnScreensaver.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Settings.ACTION_DREAM_SETTINGS);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Acesse Configurações > Tela > Proteção de tela.", Toast.LENGTH_LONG).show();
                }
            });
        }

        // Adiciona o Widget automaticamente na Tela Inicial (Android 8.0+)
        if (btnAddWidget != null) {
            btnAddWidget.setOnClickListener(v -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    AppWidgetManager appWidgetManager = getSystemService(AppWidgetManager.class);
                    ComponentName myProvider = new ComponentName(this, ClockWidgetProvider.class);

                    if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported()) {
                        appWidgetManager.requestPinAppWidget(myProvider, null, null);
                    } else {
                        Toast.makeText(this, "Toque e segure na Tela Inicial para adicionar o widget.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Toque e segure na Tela Inicial para adicionar o widget.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}