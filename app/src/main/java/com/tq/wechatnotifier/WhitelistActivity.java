package com.tq.wechatnotifier;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.tq.wechatnotifier.adapter.ContactAdapter;
import com.tq.wechatnotifier.model.ContactInfo;
import com.tq.wechatnotifier.util.PrefsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WhitelistActivity extends AppCompatActivity {

    private PrefsManager prefsManager;
    private ContactAdapter adapter;
    private List<ContactInfo> contactList;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whitelist);

        prefsManager = new PrefsManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recycler_contacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        emptyView = findViewById(R.id.text_empty);

        contactList = new ArrayList<>();
        adapter = new ContactAdapter(contactList, (contact, selected) -> {
            if (selected) {
                prefsManager.addToWhitelist(contact.getName());
            } else {
                prefsManager.removeFromWhitelist(contact.getName());
            }
        });
        recyclerView.setAdapter(adapter);

        loadContacts();
    }

    private void loadContacts() {
        contactList.clear();

        Set<String> allSeen = prefsManager.getAllSeenContacts();
        Set<String> whitelist = prefsManager.getWhitelist();

        for (String name : allSeen) {
            contactList.add(new ContactInfo(name, 0, whitelist.contains(name)));
        }

        adapter.notifyDataSetChanged();

        emptyView.setVisibility(contactList.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
