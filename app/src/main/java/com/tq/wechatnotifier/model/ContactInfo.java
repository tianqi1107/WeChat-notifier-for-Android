package com.tq.wechatnotifier.model;

public class ContactInfo {
    private String name;
    private long lastSeen;
    private boolean selected;

    public ContactInfo(String name, long lastSeen, boolean selected) {
        this.name = name;
        this.lastSeen = lastSeen;
        this.selected = selected;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getLastSeen() { return lastSeen; }
    public void setLastSeen(long lastSeen) { this.lastSeen = lastSeen; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
}
