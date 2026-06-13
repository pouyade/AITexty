package com.dpouya.aitexty.helper;

import android.content.Context;
import android.graphics.Typeface;

public final class FontAwesome {
    public enum Style {
        SOLID,
        REGULAR,
        BRANDS
    }

    public enum Icon {
        BARS("\uf0c9"),
        ARROW_LEFT("\uf060"),
        ARROW_RIGHT("\uf061"),
        SEARCH("\uf002"),
        CLOSE("\uf00d"),
        CHECK("\uf00c"),
        HOME("\uf015"),
        COMMENT("\uf075"),
        COMMENT_DOTS("\uf4ad"),
        USER_GEAR("\uf4fe"),
        COG("\uf013"),
        MOON("\uf186"),
        SUN("\uf185"),
        PAPER_PLANE("\uf1d8"),
        LOCK("\uf023"),
        SHIELD("\uf132"),
        BAN("\uf05e"),
        EYE_SLASH("\uf070"),
        ROBOT("\uf544"),
        QRCODE("\uf029"),
        ENVELOPE("\uf0e0"),
        PLUS("\uf067"),
        TRASH("\uf1f8"),
        STAR("\uf005"),
        SPAM("\uf714"),
        VOLUME_UP("\uf028"),
        MICROPHONE("\uf130");

        private final String unicode;

        Icon(String unicode) {
            this.unicode = unicode;
        }

        public String unicode() {
            return unicode;
        }
    }

    private static Typeface solid;
    private static Typeface regular;
    private static Typeface brands;

    private FontAwesome() {
    }

    public static void init(Context context) {
        if (solid == null) {
            solid = Typeface.createFromAsset(context.getAssets(), "fonts/fa-solid-900.ttf");
        }
        if (regular == null) {
            regular = Typeface.createFromAsset(context.getAssets(), "fonts/fa-regular-400.ttf");
        }
        if (brands == null) {
            brands = Typeface.createFromAsset(context.getAssets(), "fonts/fa-brands-400.ttf");
        }
    }

    public static Typeface getTypeface(Style style) {
        switch (style) {
            case REGULAR:
                return regular != null ? regular : Typeface.DEFAULT;
            case BRANDS:
                return brands != null ? brands : Typeface.DEFAULT;
            case SOLID:
            default:
                return solid != null ? solid : Typeface.DEFAULT;
        }
    }

    public static Typeface getTypeface(Icon icon) {
        return getTypeface(Style.SOLID);
    }
}
