package com.dpouya.aitexty.activities;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dpouya.aitexty.components.BaseActivity;
import com.dpouya.aitexty.helper.AndroidUtilities;
import com.dpouya.aitexty.helper.LayoutHelper;
import com.dpouya.aitexty.helper.LocaleController;
import com.dpouya.aitexty.helper.Theme;
import com.dpouya.aitexty.security.CryptoManager;
import com.dpouya.aitexty.security.KeyExchangeManager;
import com.dpouya.aitexty.ui.ActionBar.ActionBar;
import com.dpouya.aitexty.ui.widgets.QrCodeView;

public class EncryptChatActivity extends BaseActivity {
    private long conversationId;
    private QrCodeView qrView;

    @Override
    protected View initViews(Context context) {
        conversationId = getIntent().getLongExtra("conversation_id", -1);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(AndroidUtilities.dp20, AndroidUtilities.dp20, AndroidUtilities.dp20, AndroidUtilities.dp20);
        layout.setBackgroundColor(Theme.getColor(Theme.BACKGROUND_COLOR));

        qrView = new QrCodeView(context);
        layout.addView(qrView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 280, 0, 0, 0, 16));

        Button enableLocal = new Button(context);
        enableLocal.setText(LocaleController.getString("encrypt_chat"));
        enableLocal.setOnClickListener(v -> {
            CryptoManager.getInstance(context).enableLocalEncryption(conversationId);
            Toast.makeText(context, LocaleController.getString("encrypt_chat"), Toast.LENGTH_SHORT).show();
        });
        layout.addView(enableLocal, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 8));

        Button showQr = new Button(context);
        showQr.setText(LocaleController.getString("show_qr"));
        showQr.setOnClickListener(v -> generateQr());
        layout.addView(showQr, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 8));

        EditText qrInput = new EditText(context);
        qrInput.setHint("QR JSON");
        layout.addView(qrInput, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 8));

        Button scanQr = new Button(context);
        scanQr.setText(LocaleController.getString("scan_qr"));
        scanQr.setOnClickListener(v -> {
            try {
                new KeyExchangeManager(context).scanAndExchange(conversationId, qrInput.getText().toString());
                Toast.makeText(context, "E2E enabled", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        layout.addView(scanQr, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        return layout;
    }

    @Override
    protected void initActionbar(ActionBar bar) {
        bar.showBackButton(true);
        bar.setOnIconClick(v -> finish());
        bar.setTitle(LocaleController.getString("encrypt_chat"));
    }

    private void generateQr() {
        try {
            String payload = new KeyExchangeManager(this).createQrPayload(conversationId);
            qrView.setPayload(payload, AndroidUtilities.dp(250));
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
