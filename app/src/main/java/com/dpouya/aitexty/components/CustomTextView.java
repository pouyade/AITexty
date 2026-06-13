package com.dpouya.aitexty.components;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import com.dpouya.aitexty.helper.Theme;

public class CustomTextView extends AppCompatTextView {
    public CustomTextView(@NonNull Context context) {
        super(context);
        init(Theme.TypeFaceKey.DEFAULT_TYPEFACE);
    }
    public CustomTextView(@NonNull Context context,Theme.TypeFaceKey typeFaceKey) {
        super(context);
        init(typeFaceKey);
    }
    void init(Theme.TypeFaceKey typeFaceKey){
        setTypeface(Theme.getTypeface(typeFaceKey));
    }
}
