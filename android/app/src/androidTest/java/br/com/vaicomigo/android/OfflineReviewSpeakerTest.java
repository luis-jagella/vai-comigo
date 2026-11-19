package br.com.vaicomigo.android;

import static org.junit.Assert.*;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OfflineReviewSpeakerTest {
    private Voice voice(String name, String country, boolean network) {
        return new Voice(name, new Locale("pt", country), Voice.QUALITY_NORMAL,
                Voice.LATENCY_NORMAL, network, Collections.emptySet());
    }

    @Test public void prefersInstalledOfflineBrazilianVoice() {
        Voice portuguese = voice("local-pt", "PT", false);
        Voice brazilian = voice("local-br", "BR", false);
        Voice online = voice("network-br", "BR", true);
        assertSame(brazilian, OfflineReviewSpeaker.offlineVoice(new HashSet<>(
                Arrays.asList(portuguese, online, brazilian))));
    }

    @Test public void missingAndNetworkVoicesAreNeverSelected() {
        Voice missing = new Voice("not-installed", new Locale("pt", "BR"), Voice.QUALITY_NORMAL,
                Voice.LATENCY_NORMAL, false, Collections.singleton(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED));
        assertNull(OfflineReviewSpeaker.offlineVoice(new HashSet<>(Arrays.asList(
                missing, voice("network", "BR", true)))));
        assertNull(OfflineReviewSpeaker.offlineVoice(null));
    }

    @Test public void mayUseOfflinePortugueseButNeverAnUnrelatedLanguage() {
        Voice portuguese = voice("local-pt", "PT", false);
        Voice english = new Voice("local-en", Locale.US, Voice.QUALITY_NORMAL,
                Voice.LATENCY_NORMAL, false, Collections.emptySet());
        assertNull(OfflineReviewSpeaker.offlineVoice(Collections.singleton(english)));
        assertSame(portuguese, OfflineReviewSpeaker.offlineVoice(new HashSet<>(Arrays.asList(english, portuguese))));
    }
}
