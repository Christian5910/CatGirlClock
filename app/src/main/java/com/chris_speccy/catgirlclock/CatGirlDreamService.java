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

    private ImageView imgH1, imgH2, imgM1, imgM2;
    private TextView textDate, textColon;
    private final Handler handler = new Handler(Looper.getMainLooper());

    // Mapeamento individual de quadros para cada número (0 a 9)
    private final int[] FRAME_COUNTS = {10, 9, 9, 12, 10, 8, 9, 9, 6, 8};
    private final int[] frameIndices = new int[4];

    private boolean colonVisible = true;

    private final Runnable animationRunnable = new Runnable() {
        @Override
        public void run() {
            atualizarRelogioEAnimacao();
            handler.postDelayed(this, 220); // Velocidade da animação em milissegundos
        }
    };

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        setInteractive(false);
        setFullscreen(true);
        setScreenBright(false); // Ativa o brilho baixo noturno para carregamento
        setContentView(R.layout.widget_clock);

        imgH1 = findViewById(R.id.digit_h1);
        imgH2 = findViewById(R.id.digit_h2);
        imgM1 = findViewById(R.id.digit_m1);
        imgM2 = findViewById(R.id.digit_m2);
        textDate = findViewById(R.id.text_date);
        textColon = findViewById(R.id.text_colon);
    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();
        handler.post(animationRunnable);
    }

    @Override
    public void onDreamingStopped() {
        super.onDreamingStopped();
        handler.removeCallbacks(animationRunnable);
    }

    private void atualizarRelogioEAnimacao() {
        Calendar cal = Calendar.getInstance();
        int hora = cal.get(Calendar.HOUR_OF_DAY);
        int minuto = cal.get(Calendar.MINUTE);

        int[] digits = { hora / 10, hora % 10, minuto / 10, minuto % 10 };
        ImageView[] views = { imgH1, imgH2, imgM1, imgM2 };

        // Animação dinâmica independente para cada dígito
        for (int i = 0; i < 4; i++) {
            int digito = digits[i];
            int totalFrames = FRAME_COUNTS[digito];

            frameIndices[i] = (frameIndices[i] + 1) % totalFrames;

            if (views[i] != null) {
                views[i].setImageResource(getFrameResource(digito, frameIndices[i] + 1));
            }
        }

        // Pisca os dois pontos do relógio
        colonVisible = !colonVisible;
        if (textColon != null) {
            textColon.setAlpha(colonVisible ? 1.0f : 0.25f);
        }

        // Exibe a data (ex: dom., 23 de agosto)
        if (textDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, d 'de' MMMM", new Locale("pt", "BR"));
            textDate.setText(sdf.format(new Date()));
        }
    }

    private int getFrameResource(int digito, int frame) {
        String name = String.format(Locale.US, "digit_%d_%02d", digito, frame);
        return getResources().getIdentifier(name, "drawable", getPackageName());
    }
}