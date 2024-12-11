package com.coratory.urlretriever;

import java.lang.reflect.Method;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class URLRetrieverActivity extends AppCompatActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_urlretriever);
		FragmentManager manager = getSupportFragmentManager();
		Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);

		if (fragment == null) {
			fragment = new URLRetrieverFragment();
			manager.beginTransaction().add(R.id.fragmentContainer, fragment)
					.commit();
		}
	}

	@Override
	protected boolean onPrepareOptionsPanel(View view, Menu menu) {
	        if (menu != null) {
	            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
	                try {
	                    Method m = menu.getClass().getDeclaredMethod(
	                            "setOptionalIconsVisible", Boolean.TYPE);
	                    m.setAccessible(true);
	                    m.invoke(menu, true);
	                } catch (Exception e) {
	                    Log.e(getClass().getSimpleName(), "onMenuOpened...unable to set icons for overflow menu", e);
	                }
	            }
	        }
	    return super.onPrepareOptionsPanel(view, menu);
	}

}
