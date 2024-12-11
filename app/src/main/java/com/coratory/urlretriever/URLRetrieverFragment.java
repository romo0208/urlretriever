package com.coratory.urlretriever;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Date;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class URLRetrieverFragment extends Fragment {

    /* GUI Controls */
    private ImageButton mProceedButton;
    private ImageButton mStopButton;
    private EditText mUrlField;
    private TextView mWebContent;
    private FrameLayout mFrameLayout;

    /*Shows whether DDoS Mode is activated*/
    private boolean mIsDdosMode = false;
    /*Shows whether periodic task is activated*/
    private boolean mIsRunning = false;
    /*Shows whether last retrieval was successful*/
    private boolean mIsSuccess = false;

    /*Delay between AsyncTask executions*/
    private static final int DELAY = 1000;
    /*An empty string*/
    private static final String EMPTY_STRING = "";
    /*Default file extension (used for saving files)*/
    private static final String EXTENSION = ".txt";
    /*Default filename*/
    private static final String DEFAULT_FILENAME = "output.txt";
    /*Protocol prefixes*/
    private static final String PREFIX_HTTP = "http://";
    private static final String PREFIX_HTTPS = "https://";

    /*String keys for saving application's state*/
    private static final String KEY_CONTENT = "CONTENT";
    private static final String KEY_VALUE = "VALUE";
    private static final String KEY_IS_DDOS_MODE = "IS_DDOS_MODE";
    private static final String KEY_IS_RUNNING = "IS_RUNNING";
    private static final String KEY_IS_SUCCESS = "IS_SUCCESS";

    /*A sting field to store last error occurred*/
    private String mError = null;
    /*A sting field to store last value retrieved*/
    private String mValue;

    /*URLRetriever instance*/
    private URLRetriever mRetriever = new URLRetriever();

    /*A handler for periodic (DDoS) Mode*/
    private Handler mHandler = new Handler();
    /*A runnable for periodic (DDoS) Mode*/
    private Runnable mHandlerTask = new Runnable() {
        @Override
        public void run() {
            retrieveURL();
            mHandler.postDelayed(mHandlerTask, DELAY);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View v = inflater
                .inflate(R.layout.fragment_urlretriever, parent, false);

        mFrameLayout = (FrameLayout) v.findViewById(R.id.progressContainer);

        ActionBar actionBar = ((AppCompatActivity) getActivity())
                .getSupportActionBar();

        actionBar.setCustomView(R.layout.actionbar_view);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_SHOW_HOME);

        mUrlField = (EditText) actionBar.getCustomView().findViewById(
                R.id.urlfield);
        mProceedButton = (ImageButton) actionBar.getCustomView().findViewById(
                R.id.proceed_button);
        mStopButton = (ImageButton) actionBar.getCustomView().findViewById(
                R.id.stop_button);

        mWebContent = (TextView) v.findViewById(R.id.webcontent);
        registerForContextMenu(mWebContent);

        mProceedButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (mIsDdosMode) {

                    startRepeatingTask();
                } else {
                    retrieveURL();
                }
                mValue = mUrlField.getText().toString();
                if (mValue != null && !mValue.equals(EMPTY_STRING)) {
                    HistoryStorage.getInstance(
                            getActivity().getApplicationContext())
                            .addHistoryEntry(
                                    new HistoryEntry(mValue, new Date()));
                }

            }
        });

        mStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopRepeatingTask();
            }
        });

        if (savedInstanceState != null) {
            mWebContent.setText(savedInstanceState.getString(KEY_CONTENT));
            mValue = savedInstanceState.getString(KEY_VALUE);
            mIsDdosMode = savedInstanceState.getBoolean(KEY_IS_DDOS_MODE);
            mIsSuccess = savedInstanceState.getBoolean(KEY_IS_SUCCESS);

            if (savedInstanceState.getBoolean(KEY_IS_RUNNING)) {

                startRepeatingTask();
            }
        }

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        menu.findItem(R.id.ddos_mode).setChecked(mIsDdosMode);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {

            case R.id.ddos_mode:
                mIsDdosMode = !mIsDdosMode;
                item.setChecked(mIsDdosMode);
                return true;
            case R.id.save:
                String mFileName = getFileName();
                String path = mRetriever.saveFile(mFileName, mWebContent.getText()
                        .toString());
                showProgressNotification(
                        this.getActivity().getApplicationContext(), 1, mFileName,
                        getResources().getString(R.string.file_is_saved), path);

                return true;

            case R.id.exit:
                getActivity().finish();
                System.exit(0);

            case R.id.about:
                this.showInfoDialog();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(R.string.context_menu_title);
        RestrictedCapacityDeque<HistoryEntry> historyInfo = HistoryStorage
                .getInstance(getActivity().getApplicationContext())
                .getHistory();

        int id = 0;
        for (HistoryEntry entry : historyInfo) {
            menu.add(0, id, id, entry.getName() + " "
                    + DateFormat.getDateTimeInstance().format(entry.getDate()));
            id++;
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        mUrlField.setText(HistoryStorage
                .getInstance(getActivity().getApplicationContext())
                .getHistory().toArray()[item.getItemId()].toString());
        return super.onContextItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CONTENT, mWebContent.getText().toString());
        outState.putString(KEY_VALUE, mValue);
        outState.putBoolean(KEY_IS_DDOS_MODE, mIsDdosMode);
        outState.putBoolean(KEY_IS_RUNNING, mIsRunning);
        outState.putBoolean(KEY_IS_SUCCESS, mIsSuccess);
    }

    @TargetApi(11)
    /**
     * A main method for retrieving of an URL
     */
    private void retrieveURL() {

        AsyncTask<Void, Void, String> mAsyncTask = new AsyncTask<Void, Void, String>() {

            protected void onPreExecute() {
                mFrameLayout.setVisibility(View.VISIBLE);
                if (!mIsDdosMode) {
                    mProceedButton.setEnabled(false);
                    mProceedButton
                            .setImageResource(R.drawable.button_proceed_grey);
                }
                mWebContent.setText(EMPTY_STRING);

                mIsSuccess = true;
                mError = null;
            }

            protected String doInBackground(Void... params) {
                String result = null;

                try {

                    if (mValue.startsWith(PREFIX_HTTP)
                            || mValue.startsWith(PREFIX_HTTPS)) {
                        result = mRetriever.getWebContent(mValue);

                    } else {
                        result = mRetriever.getWebContent(PREFIX_HTTP
                                + mValue);
                    }

                } catch (UnknownHostException uhe) {
                    mError = getString(R.string.err_unknown_host_exception);
                    mIsSuccess = false;
                } catch (MalformedURLException mue) {
                    mError = getString(R.string.err_malformed_url_exception);
                    mIsSuccess = false;
                } catch (IOException ioe) {
                    mError = getString(R.string.err_io_exception);
                    mIsSuccess = false;
                } catch (Exception e) {
                    mError = getString(R.string.err_exception);
                    mIsSuccess = false;
                }

                return result;
            }

            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                if (mError != null) {
                    Toast.makeText(getActivity(), mError.toString(),
                            Toast.LENGTH_LONG).show();
                } else {
                    mWebContent.setText(result);

                }
                mFrameLayout.setVisibility(View.INVISIBLE);
                if (!mIsDdosMode) {
                    mProceedButton.setEnabled(true);
                    mProceedButton.setImageResource(R.drawable.button_proceed);
                }

            }

        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mAsyncTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

        } else {
            mAsyncTask.execute();
        }

    }

    /**
     * Starts repeating (DDos Mode) task
     */

    private void startRepeatingTask() {
        mProceedButton.setEnabled(false);
        mProceedButton.setImageResource(R.drawable.button_proceed_grey);
        mStopButton.setEnabled(true);
        mStopButton.setImageResource(R.drawable.button_stop);
        mIsRunning = true;
        mHandlerTask.run();
    }

    /**
     * Stops repeating (DDos Mode) task
     */

    private void stopRepeatingTask() {
        mProceedButton.setEnabled(true);
        mProceedButton.setImageResource(R.drawable.button_proceed);
        mStopButton.setEnabled(false);
        mStopButton.setImageResource(R.drawable.button_stop_grey);
        mIsRunning = false;
        mHandler.removeCallbacks(mHandlerTask);
    }

    /**
     * Shows downloading progress notification. It's used for saving of the retrieved content to a file
     *
     * @param context        - application context
     * @param notificationId - notification ID
     * @param title          - A title of the notification
     * @param message        - Message of the notification
     * @param path           - Path to save a file
     */

    private void showProgressNotification(Context context, int notificationId,
                                          String title, String message, String path) {

        NotificationManager manager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                context);

        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        File file = new File(path);
        intent.setDataAndType(Uri.fromFile(file), "text/*");
        PendingIntent pIntent = PendingIntent.getActivity(getActivity(), 0,
                intent, 0);

        mBuilder.setContentIntent(pIntent);

        Notification mNotification = mBuilder.setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setTicker(title).setProgress(0, 0, false).build();

        mNotification.flags |= Notification.FLAG_AUTO_CANCEL;

        manager.notify(notificationId, mNotification);

    }

    /**
     * Returns a filename for a file to be saved
     *
     * @return Filename for a file to be saved
     */
    private String getFileName() {

        if (mIsSuccess) {
            return mValue + EXTENSION;
        } else {
            return DEFAULT_FILENAME;
        }
    }

    /**
     * Shows "About" dialog
     */

    private void showInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.about);
        builder.setMessage(R.string.about_description);

        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // positive button logic
                    }
                });

        String negativeText = getString(android.R.string.cancel);
        builder.setNegativeButton(negativeText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // negative button logic
                    }
                });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }

}
