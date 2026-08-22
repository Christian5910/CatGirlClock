package com.chris_speccy.catgirlclock;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnScreensaver = findViewById(R.id.btn_screensaver);
        Button btnAddWidget = findViewById(R.id.btn_add_widget);
        SwitchMaterial switchHideIcon = findViewById(R.id.switch_hide_icon);

        btnScreensaver.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_DREAM_SETTINGS);
            startActivity(intent);
        });

        btnAddWidget.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AppWidgetManager appWidgetManager = getSystemService(AppWidgetManager.class);
                ComponentName myProvider = new ComponentName(this, ClockWidgetProvider.class);

                if (appWidgetManager.isRequestPinAppWidgetSupported()) {
                    appWidgetManager.requestPinAppWidget(myProvider, null, null);
                } else {
                    Toast.makeText(this, "Adicione mantendo o dedo pressionado na tela inicial.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        switchHideIcon.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                new AlertDialog.Builder(this)
                        .setTitle("Ocultar Ícone?")
                        .setMessage("O app sumirá da sua gaveta de aplicativos. Você poderá reabri-lo depois indo em Configurações > Aplicativos > CatGirl Clock > Abrir.")
                        .setPositiveButton("Sim, ocultar", (dialog, which) -> setAppIconVisible(false))
                        .setNegativeButton("Cancelar", (dialog, which) -> switchHideIcon.setChecked(false))
                        .setOnCancelListener(dialog -> switchHideIcon.setChecked(false))
                        .show();
            } else {
                setAppIconVisible(true);
            }
        });
    }

    private void setAppIconVisible(boolean visible) {
        PackageManager pm = getPackageManager();
        ComponentName componentName = new ComponentName(this, MainActivity.class);
        int newState = visible
                ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

        pm.setComponentEnabledSetting(componentName, newState, PackageManager.DONT_KILL_APP);

        if (!visible) {
            Toast.makeText(this, "Ícone ocultado com sucesso!", Toast.LENGTH_LONG).show();
        }
    }
}