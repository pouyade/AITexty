package com.dpouya.aitexty.accessibility;

import android.content.Context;

import com.k2fsa.sherpa.onnx.OfflineTtsConfig;
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig;
import com.k2fsa.sherpa.onnx.OfflineTtsVitsModelConfig;

import java.io.File;

final class PersianTtsConfigBuilder {
    private PersianTtsConfigBuilder() {
    }

    static OfflineTtsConfig build(Context context, PersianTtsVoice voice) {
        File modelDir = PersianTtsModelStore.getModelDir(context, voice);
        File espeakDir = PersianTtsModelStore.resolveEspeakDir(context, voice);
        OfflineTtsVitsModelConfig vits = new OfflineTtsVitsModelConfig(
                new File(modelDir, voice.modelFileName).getAbsolutePath(),
                modelDir.getAbsolutePath() + "/",
                new File(modelDir, "tokens.txt").getAbsolutePath(),
                espeakDir.getAbsolutePath(),
                "",
                0.667f,
                0.8f,
                1.0f
        );
        OfflineTtsModelConfig modelConfig = new OfflineTtsModelConfig(vits, 1, false, "cpu");
        return new OfflineTtsConfig(modelConfig, "", "", 3);
    }
}
