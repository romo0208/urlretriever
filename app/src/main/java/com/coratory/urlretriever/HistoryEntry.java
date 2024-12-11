package com.coratory.urlretriever;

import java.util.Date;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

public class HistoryEntry {

    private static final String JSON_ID = "id";
    private static final String JSON_NAME = "name";
    private static final String JSON_DATE = "date";
    private UUID mId;
    private String mName;
    private Date mDate;

    public HistoryEntry(String name, Date date) {
        this.mId = UUID.randomUUID();
        setName(name);
        setDate(date);
    }

    public UUID getId() {
        return mId;
    }

    public HistoryEntry(JSONObject json) throws JSONException {
        mId = UUID.fromString(json.getString(JSON_ID));
        mName = json.getString(JSON_NAME);
        mDate = new Date(json.getLong(JSON_DATE));
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_ID, mId.toString());
        json.put(JSON_NAME, mName.toString());
        json.put(JSON_DATE, mDate.getTime());
        return json;
    }

    @Override
    public String toString() {
        return mName;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

}
