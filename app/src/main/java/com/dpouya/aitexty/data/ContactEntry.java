package com.dpouya.aitexty.data;

public class ContactEntry {
    public String displayName;
    public String phoneNumber;
    public long contactId;

    public ContactEntry(String displayName, String phoneNumber, long contactId) {
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
        this.contactId = contactId;
    }
}
