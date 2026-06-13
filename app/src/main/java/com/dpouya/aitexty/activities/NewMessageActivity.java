package com.dpouya.aitexty.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dpouya.aitexty.components.BaseActivity;
import com.dpouya.aitexty.data.ContactEntry;
import com.dpouya.aitexty.helper.AndroidUtilities;
import com.dpouya.aitexty.helper.ContactHelper;
import com.dpouya.aitexty.helper.LayoutHelper;
import com.dpouya.aitexty.helper.LocaleController;
import com.dpouya.aitexty.helper.PermissionHelper;
import com.dpouya.aitexty.helper.Theme;
import com.dpouya.aitexty.ui.ActionBar.ActionBar;
import com.dpouya.aitexty.ui.adapter.ContactAdapter;

import java.util.ArrayList;
import java.util.List;

public class NewMessageActivity extends BaseActivity {
    private ContactAdapter adapter;
    private LinearLayout emptyPanel;
    private TextView emptyView;
    private Button permissionBtn;
    private List<ContactEntry> allContacts = new ArrayList<>();
    private String searchQuery = "";
    private boolean permissionRequested;

    @Override
    protected View initViews(Context context) {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Theme.getColor(Theme.BACKGROUND_COLOR));

        EditText searchField = new EditText(context);
        searchField.setHint(LocaleController.getString("search_contacts"));
        searchField.setSingleLine(true);
        searchField.setPadding(AndroidUtilities.dp12, AndroidUtilities.dp10, AndroidUtilities.dp12, AndroidUtilities.dp10);
        searchField.setBackgroundColor(Theme.getColor(Theme.CARD_BACKGROUND_COLOR));
        searchField.setTextColor(Theme.getColor(Theme.TEXT_COLOR));
        searchField.setHintTextColor(Theme.getColor(Theme.SUB_TEXT_COLOR));
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString();
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        root.addView(searchField, LayoutHelper.createLinear(
                LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        FrameLayout listContainer = new FrameLayout(context);

        RecyclerView listView = new RecyclerView(context);
        listView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new ContactAdapter(context, contact -> {
            Intent intent = new Intent(NewMessageActivity.this, ChatActivity.class);
            intent.putExtra("address", contact.phoneNumber);
            startActivity(intent);
            finish();
        });
        listView.setAdapter(adapter);
        listContainer.addView(listView, LayoutHelper.createFrame(
                LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        LinearLayout emptyPanel = new LinearLayout(context);
        this.emptyPanel = emptyPanel;
        emptyPanel.setOrientation(LinearLayout.VERTICAL);
        emptyPanel.setGravity(Gravity.CENTER);
        emptyPanel.setPadding(AndroidUtilities.dp20, AndroidUtilities.dp20, AndroidUtilities.dp20, AndroidUtilities.dp20);

        emptyView = new TextView(context);
        emptyView.setText(LocaleController.getString("no_contacts"));
        emptyView.setTextColor(Theme.getColor(Theme.SUB_TEXT_COLOR));
        emptyView.setGravity(Gravity.CENTER);
        emptyPanel.addView(emptyView, LayoutHelper.createLinear(
                LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        permissionBtn = new Button(context);
        permissionBtn.setText(LocaleController.getString("grant_permissions"));
        permissionBtn.setVisibility(View.GONE);
        permissionBtn.setOnClickListener(v -> requestContactsAccess());
        emptyPanel.addView(permissionBtn, LayoutHelper.createLinear(
                LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, 16, 0, 0));

        listContainer.addView(emptyPanel, LayoutHelper.createFrame(
                LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        root.addView(listContainer, LayoutHelper.createLinear(0, 0, 1f));

        return root;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContacts();
    }

    @Override
    protected void initActionbar(ActionBar bar) {
        bar.showBackButton(true);
        bar.setOnIconClick(v -> finish());
        bar.setTitle(LocaleController.getString("new_message"));
    }

    private void requestContactsAccess() {
        if (PermissionHelper.hasReadContacts(this)) {
            loadContacts();
            return;
        }
        if (!PermissionHelper.requestReadContacts(this)) {
            openAppSettings();
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }

    private void loadContacts() {
        if (!PermissionHelper.hasReadContacts(this)) {
            if (!permissionRequested) {
                permissionRequested = true;
                PermissionHelper.requestReadContacts(this);
            }
            showPermissionRequired();
            return;
        }
        permissionRequested = false;
        permissionBtn.setVisibility(View.GONE);
        allContacts = ContactHelper.loadContacts(this);
        applyFilter();
    }

    private void applyFilter() {
        List<ContactEntry> filtered = ContactHelper.filterContacts(allContacts, searchQuery);
        adapter.setItems(filtered);
        if (filtered.isEmpty()) {
            showEmpty(allContacts.isEmpty()
                    ? LocaleController.getString("no_contacts")
                    : LocaleController.getString("no_contact_results"));
        } else {
            emptyPanel.setVisibility(View.GONE);
        }
    }

    private void showPermissionRequired() {
        emptyView.setText(LocaleController.getString("contacts_permission_required"));
        emptyPanel.setVisibility(View.VISIBLE);
        permissionBtn.setVisibility(View.VISIBLE);
        adapter.setItems(new ArrayList<>());
    }

    private void showEmpty(String message) {
        emptyView.setText(message);
        emptyPanel.setVisibility(View.VISIBLE);
        permissionBtn.setVisibility(View.GONE);
        adapter.setItems(new ArrayList<>());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionHelper.REQUEST_READ_CONTACTS
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadContacts();
        } else if (requestCode == PermissionHelper.REQUEST_READ_CONTACTS) {
            showPermissionRequired();
        }
    }
}
