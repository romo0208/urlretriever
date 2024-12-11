package com.coratory.urlretriever;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import android.os.Environment;
import android.util.Log;

public class URLRetriever {

	public static final String TAG = "com.coratory.urlretriever.URLRetriever";
	private StringBuffer mWebContent;
	private BufferedReader mBufferedReader;

	public String saveFile(String fileName, String body) {

		String path = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
				+ "/" + fileName;

		File file = new File(path);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(body.getBytes());
			fos.close();
		} catch (IOException ioe) {
			Log.d(TAG, ioe.toString());
		}

		return path;

	}

	public final String getWebContent(final String address)
			throws UnknownHostException, IOException, MalformedURLException {
		mWebContent = new StringBuffer();
		URL url = new URL(address);
		mBufferedReader = new BufferedReader(new InputStreamReader(
				url.openStream()));
		String line = null;
		while ((line = mBufferedReader.readLine()) != null)
			mWebContent.append(line);

		mBufferedReader.close();

		return mWebContent.toString();
	}

}
