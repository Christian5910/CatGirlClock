package com.chris_speccy.catgirlclock;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final ImageView[] previewViews = new ImageView[4];
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final PreviewAnimator[] animators = new PreviewAnimator[4];

    private final int[][] GIF_DELAYS = {
            {500, 200, 100, 100, 200, 500, 200, 100, 100, 200}, // 0
            {500, 200, 100, 200, 100, 200, 100, 200, 100},      // 1
            {500, 200, 100, 200, 100, 100, 100, 100, 200},      // 2
            {500, 100, 200, 500, 100, 200, 500, 100, 200, 500, 100, 200}, // 3
            {100, 100, 100, 100, 100, 100, 100, 100, 100, 100}, // 4
            {500, 200, 200, 100, 500, 200, 500, 100},           // 5
            {100, 100, 200, 100, 100, 100, 100, 200, 100},      // 6
            {1000, 200, 200, 1000, 200, 1000, 200, 1000, 200},  // 7
            {2000, 100, 200, 200, 100, 500},                    // 8
            {2000, 100, 1000, 100, 2000, 200, 1000, 100}        // 9
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Mapeia os quadros de prévia
        previewViews[0] = findViewById(R.id.preview_h1);
        previewViews[1] = findViewById(R.id.preview_h2);
        previewViews[2] = findViewById(R.id.preview_m1);
        previewViews[3] = findViewById(R.id.preview_m2);

        // E-mail Clicável (Mailto)
        TextView tvEmail = findViewById(R.id.tv_email_contact);
        if (tvEmail != null) {
            tvEmail.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:Christianoliveira5910@gmail.com"));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Nenhum aplicativo de e-mail encontrado.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Configura botão do Daydream
        Button btnScreensaver = findViewById(R.id.btn_screensaver);
        if (btnScreensaver != null) {
            btnScreensaver.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Settings.ACTION_DREAM_SETTINGS);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Não foi possível abrir as configurações de Proteção de Tela.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Configuração dos botões de adição de Widget
        configurarBotaoWidget(findViewById(R.id.btn_add_4x2), ClockWidgetProvider.class);
        configurarBotaoWidget(findViewById(R.id.btn_add_4x1), Widget4x1.class);
        configurarBotaoWidget(findViewById(R.id.btn_add_2x1), Widget2x1.class);
    }

    private void configurarBotaoWidget(Button btn, Class<?> widgetClass) {
        if (btn == null) return;

        btn.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AppWidgetManager appWidgetManager = getSystemService(AppWidgetManager.class);
                ComponentName myProvider = new ComponentName(this, widgetClass);

                if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported()) {
                    appWidgetManager.requestPinAppWidget(myProvider, null, null);
                    return;
                }
            }
            Toast.makeText(this, "Adicione o widget pressionando e segurando a tela inicial do seu celular.", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (int i = 0; i < 4; i++) {
            animators[i] = new PreviewAnimator(i);
            handler.post(animators[i]);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        for (int i = 0; i < 4; i++) {
            if (animators[i] != null) {
                handler.removeCallbacks(animators[i]);
            }
        }
    }

    private class PreviewAnimator implements Runnable {
        int positionIndex;
        int currentDigit = -1;
        int frameIndex = 0;

        PreviewAnimator(int index) {
            this.positionIndex = index;
        }

        @Override
        public void run() {
            Calendar cal = Calendar.getInstance();
            int hora = cal.get(Calendar.HOUR_OF_DAY);
            int minuto = cal.get(Calendar.MINUTE);
            int[] currentDigits = { hora / 10, hora % 10, minuto / 10, minuto % 10 };

            int realDigit = currentDigits[positionIndex];

            if (currentDigit != realDigit) {
                currentDigit = realDigit;
                frameIndex = 0;
            }

            if (previewViews[positionIndex] != null) {
                String name = String.format(Locale.US, "digit_%d_%02d", currentDigit, frameIndex + 1);
                int resId = getResources().getIdentifier(name, "drawable", getPackageName());
                if (resId != 0) {
                    previewViews[positionIndex].setImageResource(resId);
                }
            }

            int currentDelay = GIF_DELAYS[currentDigit][frameIndex];
            frameIndex = (frameIndex + 1) % GIF_DELAYS[currentDigit].length;

            handler.postDelayed(this, currentDelay);
        }
    }
}