package com.tq.wechatnotifier.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tq.wechatnotifier.R;
import com.tq.wechatnotifier.model.ContactInfo;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    public interface OnSelectionChangedListener {
        void onSelectionChanged(ContactInfo contact, boolean selected);
    }

    private final List<ContactInfo> contacts;
    private final OnSelectionChangedListener listener;

    public ContactAdapter(List<ContactInfo> contacts, OnSelectionChangedListener listener) {
        this.contacts = contacts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContactInfo contact = contacts.get(position);
        holder.name.setText(contact.getName());
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(contact.isSelected());
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            contact.setSelected(isChecked);
            if (listener != null) {
                listener.onSelectionChanged(contact, isChecked);
            }
        });
        holder.itemView.setOnClickListener(v -> {
            holder.checkBox.toggle();
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView name;

        ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox_contact);
            name = itemView.findViewById(R.id.text_contact_name);
        }
    }
}
