package com.chris_speccy.catgirlclock;

import android.os.Handler;
import android.os.Looper;
import android.service.dreams.DreamService;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CatGirlDreamService extends DreamService {

    private ImageView[] views = new ImageView[4];
    private TextView textDate;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final DigitAnimator[] animators = new DigitAnimator[4];

    // Seus Delays exatos extraídos do script Python!
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
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setInteractive(false);
        setFullscreen(true);
        setScreenBright(false);
        setContentView(R.layout.dream_clock);

        views[0] = findViewById(R.id.digit_h1);
        views[1] = findViewById(R.id.digit_h2);
        views[2] = findViewById(R.id.digit_m1);
        views[3] = findViewById(R.id.digit_m2);
        textDate = findViewById(R.id.text_date);
    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();

        for (int i = 0; i < 4; i++) {
            animators[i] = new DigitAnimator(i);
            handler.post(animators[i]); // Inicia as animações independentes
        }
        handler.post(relogioRunnable);
    }

    @Override
    public void onDreamingStopped() {
        super.onDreamingStopped();
        for (int i = 0; i < 4; i++) {
            handler.removeCallbacks(animators[i]);
        }
        handler.removeCallbacks(relogioRunnable);
    }

    private final Runnable relogioRunnable = new Runnable() {
        @Override
        public void run() {
            if (textDate != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, d 'de' MMMM", new Locale("pt", "BR"));
                textDate.setText(sdf.format(new Date()).toLowerCase());
            }
            handler.postDelayed(this, 1000);
        }
    };

    // Classe responsável por animar CADA NÚMERO com sua velocidade específica
    private class DigitAnimator implements Runnable {
        int positionIndex;
        int currentDigit = -1;
        int frameIndex = 0;

        DigitAnimator(int index) {
            this.positionIndex = index;
        }

        @Override
        public void run() {
            Calendar cal = Calendar.getInstance();
            int hora = cal.get(Calendar.HOUR_OF_DAY);
            int minuto = cal.get(Calendar.MINUTE);
            int[] currentDigits = { hora / 10, hora % 10, minuto / 10, minuto % 10 };

            int realDigit = currentDigits[positionIndex];

            // Se o tempo passou e o número mudou (ex: de 0 para 1), reseta a animação do zero
            if (currentDigit != realDigit) {
                currentDigit = realDigit;
                frameIndex = 0;
            }

            if (views[positionIndex] != null) {
                String name = String.format(Locale.US, "digit_%d_%02d", currentDigit, frameIndex + 1);
                int resId = getResources().getIdentifier(name, "drawable", getPackageName());
                views[positionIndex].setImageResource(resId);
            }

            // Puxa da nossa matriz exatamente quanto tempo ESTE FRAME deve demorar
            int currentDelay = GIF_DELAYS[currentDigit][frameIndex];

            // Avança para o próximo frame
            frameIndex = (frameIndex + 1) % GIF_DELAYS[currentDigit].length;

            // Agenda a troca da imagem usando o tempo dinâmico
            handler.postDelayed(this, currentDelay);
        }
    }
}