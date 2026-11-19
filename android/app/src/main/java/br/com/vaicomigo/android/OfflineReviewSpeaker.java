package br.com.vaicomigo.android;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import java.util.Comparator;
import java.util.Set;

/** Uses only an installed Portuguese voice advertised as not requiring network. */
final class OfflineReviewSpeaker {
    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextToSpeech engine;
    private String pendingText;
    private Runnable failure;
    private int generation;

    OfflineReviewSpeaker(Context context) {
        this.context = context.getApplicationContext();
    }

    void read(String text, Runnable unavailable) {
        stop();
        int request = generation;
        pendingText = text;
        failure = unavailable;
        try {
            engine = new TextToSpeech(context, status -> handler.post(() -> initialize(request, status)));
            handler.postDelayed(() -> {
                if (request == generation && pendingText != null) { fail(); }
            }, 10_000);
        } catch (RuntimeException exception) {
            fail();
        }
    }

    private void initialize(int request, int status) {
        if (request != generation || engine == null) { return; }
        try {
            Voice voice = status == TextToSpeech.SUCCESS ? offlineVoice(engine.getVoices()) : null;
            if (voice == null || engine.setVoice(voice) != TextToSpeech.SUCCESS
                    || engine.setSpeechRate(0.85f) != TextToSpeech.SUCCESS) {
                fail();
                return;
            }
            engine.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override public void onStart(String id) { }
                @Override public void onDone(String id) {
                    handler.post(() -> { if (request == generation) { stop(); } });
                }
                @Override public void onError(String id) {
                    handler.post(() -> { if (request == generation) { fail(); } });
                }
            });
            String text = pendingText;
            pendingText = null;
            if (engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "trip-review") != TextToSpeech.SUCCESS) { fail(); }
        } catch (RuntimeException exception) {
            fail();
        }
    }

    static Voice offlineVoice(Set<Voice> voices) {
        if (voices == null) { return null; }
        return voices.stream()
                .filter(voice -> !voice.isNetworkConnectionRequired()
                        && "pt".equals(voice.getLocale().getLanguage())
                        && (voice.getFeatures() == null || !voice.getFeatures().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)))
                .min(Comparator.comparingInt((Voice voice) -> "BR".equals(voice.getLocale().getCountry()) ? 0 : 1)
                        .thenComparing(Voice::getName)).orElse(null);
    }

    void stop() {
        generation++;
        pendingText = null;
        failure = null;
        handler.removeCallbacksAndMessages(null);
        TextToSpeech current = engine;
        engine = null;
        if (current != null) {
            try { current.stop(); } catch (RuntimeException ignored) { }
            try { current.shutdown(); } catch (RuntimeException ignored) { }
        }
    }

    private void fail() {
        Runnable report = failure;
        stop();
        if (report != null) { report.run(); }
    }
}
