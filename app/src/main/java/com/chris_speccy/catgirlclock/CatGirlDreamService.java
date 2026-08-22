package com.chris_speccy.catgirlclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.service.dreams.DreamService;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CatGirlDreamService extends DreamService {

    // Atualiza o relógio automaticamente a cada virada de minuto do sistema
    private final BroadcastReceiver timeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTime();
        }
    };

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setInteractive(false);
        setFullscreen(true);

        // Carrega o layout 4x2 original (com a data)
        setContentView(R.layout.widget_clock);

        // Aplica a máscara noturna via código
        View root = findViewById(R.id.widget_root);
        if (root != null) {
            root.setBackgroundColor(Color.BLACK); // Fundo totalmente preto
            root.setAlpha(0.45f); // Opacidade de 45% (ajuste conforme preferir)
        }

        updateTime();
        registerReceiver(timeReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();

        // Força o início das animações assim que o modo de carga ativa na tela
        startAllFlippers();
    }

    private void startAllFlippers() {
        int[] flippers = {
                R.id.digit1_flipper,
                R.id.digit2_flipper,
                R.id.digit3_flipper,
                R.id.digit4_flipper
        };

        for (int id : flippers) {
            ViewFlipper flipper = findViewById(id);
            if (flipper != null && !flipper.isFlipping()) {
                flipper.startFlipping();
            }
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unregisterReceiver(timeReceiver); // Previne vazamento de memória ao sair do modo
    }

    private void updateTime() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);

        setupFlipperNormal(R.id.digit1_flipper, hour / 10);
        setupFlipperNormal(R.id.digit2_flipper, hour % 10);
        setupFlipperNormal(R.id.digit3_flipper, min / 10);
        setupFlipperNormal(R.id.digit4_flipper, min % 10);

        TextView dateText = findViewById(R.id.date_text);
        if (dateText != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd 'de' MMMM", new Locale("pt", "BR"));
            dateText.setText(sdf.format(c.getTime()));
        }
    }

    private void setupFlipperNormal(int flipperId, int digit) {
        ViewFlipper flipper = findViewById(flipperId);
        if (flipper == null) return;

        flipper.removeAllViews();
        flipper.setFlipInterval(100);

        int[] delays;
        // Cole aqui o seu bloco "switch (digit)" com os tempos de 100ms a 2000ms
        switch (digit) {
            case 0: delays = new int[]{500, 200, 100, 100, 200, 500, 200, 100, 100, 200}; break;
            default: delays = new int[]{100}; break; // Adicione os outros casos
        }

        for (int i = 0; i < delays.length; i++) {
            String frameName = String.format(Locale.US, "digit_%d_%02d", digit, i + 1);
            int resId = getResources().getIdentifier(frameName, "drawable", getPackageName());

            if (resId != 0) {
                int repeticoes = delays[i] / 100;
                for (int r = 0; r < repeticoes; r++) {
                    // Instancia imagens normais em vez de RemoteViews
                    ImageView frameView = new ImageView(this);
                    frameView.setImageResource(resId);

                    // Garante que a imagem respeite os limites do Flipper
                    frameView.setLayoutParams(new ViewFlipper.LayoutParams(
                            ViewFlipper.LayoutParams.MATCH_PARENT,
                            ViewFlipper.LayoutParams.MATCH_PARENT
                    ));

                    flipper.addView(frameView);
                }
            }
        }
        flipper.startFlipping();
    }
}
