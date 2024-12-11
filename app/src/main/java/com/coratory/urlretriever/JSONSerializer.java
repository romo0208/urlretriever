package com.coratory.urlretriever;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import android.content.Context;

public class JSONSerializer {

	private Context mContext;
	private String mFilename;

	public JSONSerializer(Context c, String f) {
		mContext = c;
		mFilename = f;
	}

	public RestrictedCapacityDeque<HistoryEntry> loadEntries()
			throws IOException, JSONException {
		RestrictedCapacityDeque<HistoryEntry> entries = new RestrictedCapacityDeque<HistoryEntry>(
				HistoryStorage.CAPACITY);
		BufferedReader reader = null;
		try {
			// open and read the file into a StringBuilder
			InputStream in = mContext.openFileInput(mFilename);
			reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder jsonString = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				// line breaks are omitted and irrelevant
				jsonString.append(line);
			}
			// parse the JSON using JSONTokener
			JSONArray array = (JSONArray) new JSONTokener(jsonString.toString())
					.nextValue();
			// build the array of entries from JSONObjects
			for (int i = 0; i < array.length(); i++) {
				entries.add(new HistoryEntry(array.getJSONObject(i)));
			}
		} catch (FileNotFoundException e) {
			// we will ignore this one, since it happens when we start fresh
		} finally {
			if (reader != null)
				reader.close();
		}
		return entries;
	}

	public void saveEntries(RestrictedCapacityDeque<HistoryEntry> entries)
			throws JSONException, IOException {
		// build an array in JSON
		JSONArray array = new JSONArray();
		for (HistoryEntry e : entries)
			array.put(e.toJSON());

		// write the file to disk
		Writer writer = null;
		try {
			OutputStream out = mContext.openFileOutput(mFilename,
					Context.MODE_PRIVATE);
			writer = new OutputStreamWriter(out);
			writer.write(array.toString());
		} finally {
			if (writer != null)
				writer.close();
		}
	}
}
