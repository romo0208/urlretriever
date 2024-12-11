package com.coratory.urlretriever;

import java.util.UUID;

import android.content.Context;
import android.util.Log;

public class HistoryStorage {

	public static final int CAPACITY = 20;
	private static final String FILENAME = "entities.json";
	private JSONSerializer mSerializer;
	private Context mAppContext;
	private static RestrictedCapacityDeque<HistoryEntry> mDeque = new RestrictedCapacityDeque<HistoryEntry>(
			CAPACITY);

	private HistoryStorage(Context appContext) {

		mAppContext = appContext;
		mSerializer = new JSONSerializer(mAppContext, FILENAME);

		try {
			mDeque = mSerializer.loadEntries();
		} catch (Exception e) {
			mDeque = new RestrictedCapacityDeque<HistoryEntry>(1);
			Log.e(URLRetriever.TAG, "Error loading entries: ", e);
		}

	}

	public static HistoryStorage getInstance(Context appContext) {
		return new HistoryStorage(appContext);
	}

	public HistoryEntry getEntry(UUID id) {
		for (HistoryEntry e : mDeque) {
			if (e.getId().equals(id))
				return e;
		}
		return null;
	}

	public RestrictedCapacityDeque<HistoryEntry> getHistory() {
		return mDeque;
	}

	public void addHistoryEntry(HistoryEntry entity) {
		mDeque.addFirst(entity);
		saveEntries();
	}

	public boolean saveEntries() {
		try {
			mSerializer.saveEntries(mDeque);
			Log.d(URLRetriever.TAG, "Entries saved to file");
			return true;
		} catch (Exception e) {
			Log.e(URLRetriever.TAG, "Error saving entries: " + e);
			return false;
		}
	}

}
