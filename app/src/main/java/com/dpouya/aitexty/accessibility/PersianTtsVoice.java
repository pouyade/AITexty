package com.dpouya.aitexty.accessibility;

import com.dpouya.aitexty.helper.LocaleController;

public enum PersianTtsVoice {
    GYRO("gyro", "persian_voice_gyro", "vits-piper-fa_IR-gyro-medium", "fa_IR-gyro-medium.onnx", null,
            62_000_000L, 0L, 63_122_437L,
            "79646f0a1942d8be05562f4103da6470bcf69337e65bd8a1ec95afe1c6a89e6b"),
    AMIR("amir", "persian_voice_amir", "vits-piper-fa_IR-amir-medium", "fa_IR-amir-medium.onnx",
            "vits-piper-fa_IR-amir-medium.tar.bz2", 62_000_000L, 50_000_000L, 63_152_970L,
            "ec6cf5d89067fcf72c410206d42b83ebaf95ee8219ecd169c85c6868a2ab977c"),
    GANJI("ganji", "persian_voice_ganji", "vits-piper-fa_IR-ganji-medium", "fa_IR-ganji-medium.onnx",
            "vits-piper-fa_IR-ganji-medium.tar.bz2", 62_000_000L, 50_000_000L, 63_145_182L,
            "28e66a8dfe4fb5662598d41b855c098ef844a81286956956b77eb121b3d63116"),
    GANJI_ADABI("ganji_adabi", "persian_voice_ganji_adabi", "vits-piper-fa_IR-ganji_adabi-medium",
            "fa_IR-ganji_adabi-medium.onnx", "vits-piper-fa_IR-ganji_adabi-medium.tar.bz2",
            62_000_000L, 50_000_000L, 63_145_182L,
            "39eea519cc6fd580541edb86e33c7d049495c4fe2db0c61b86bf4e2e2f3bd39e"),
    REZA_IBRAHIM("reza_ibrahim", "persian_voice_reza_ibrahim", "vits-piper-fa_IR-reza_ibrahim-medium",
            "fa_IR-reza_ibrahim-medium.onnx", "vits-piper-fa_IR-reza_ibrahim-medium.tar.bz2",
            62_000_000L, 50_000_000L, 63_153_775L,
            "c3e12eae4c435d39bee2dc0b870cbace6eb0e7b7c5d34c34521f9a12b1cdade4");

    public final String id;
    public final String labelKey;
    public final String modelDirName;
    public final String modelFileName;
    public final String downloadArchive;
    public final long minOnnxBytes;
    public final long minArchiveBytes;
    public final long expectedOnnxBytes;
    public final String onnxSha256;

    PersianTtsVoice(String id, String labelKey, String modelDirName, String modelFileName,
                      String downloadArchive, long minOnnxBytes, long minArchiveBytes,
                      long expectedOnnxBytes, String onnxSha256) {
        this.id = id;
        this.labelKey = labelKey;
        this.modelDirName = modelDirName;
        this.modelFileName = modelFileName;
        this.downloadArchive = downloadArchive;
        this.minOnnxBytes = minOnnxBytes;
        this.minArchiveBytes = minArchiveBytes;
        this.expectedOnnxBytes = expectedOnnxBytes;
        this.onnxSha256 = onnxSha256;
    }

    public String getSettingValue() {
        return "persian:" + id;
    }

    public String getLabel() {
        return LocaleController.getString(labelKey);
    }

    public static PersianTtsVoice fromSettingValue(String value) {
        if (value == null || value.isEmpty()) {
            return GYRO;
        }
        if ("builtin_persian".equals(value)) {
            return GYRO;
        }
        if (value.startsWith("persian:")) {
            String id = value.substring("persian:".length());
            for (PersianTtsVoice voice : values()) {
                if (voice.id.equals(id)) {
                    return voice;
                }
            }
        }
        return GYRO;
    }

    public static PersianTtsVoice fromId(String id) {
        if (id == null || id.isEmpty()) {
            return GYRO;
        }
        for (PersianTtsVoice voice : values()) {
            if (voice.id.equals(id)) {
                return voice;
            }
        }
        return GYRO;
    }
}
