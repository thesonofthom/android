package com.thesonofthom.myboardgames.fragments;

import com.thesonofthom.myboardgames.R;
import com.thesonofthom.myboardgames.R.xml;
import com.thesonofthom.myboardgames.Settings;
import com.thesonofthom.myboardgames.activities.BaseActivity.Navigation;
import com.thesonofthom.myboardgames.activities.MainActivity;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener
{
	private static String TAG = "SettingsFragment";
	private MainActivity a;
	private Settings settings;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate()...");
		a = (MainActivity)getActivity();
		a.getActionBar().setHomeButtonEnabled(true);
		addPreferencesFromResource(R.xml.settings);
		settings = new Settings(a);
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		View view =  super.onCreateView(inflater, container, savedInstanceState);
		a.getActionBar().setHomeButtonEnabled(true);
		a.getToggle().setDrawerIndicatorEnabled(false);
		return view;
	}
	
	@Override
	public void onStart()
	{
		Log.i(TAG, "onStart()...");
		super.onStart(); 
		if(getActivity() != null)
		{
			getActivity().setTitle("Settings");
		}
		SharedPreferences pref = getPreferenceScreen().getSharedPreferences();
		pref.registerOnSharedPreferenceChangeListener(this);
		onSharedPreferenceChanged(pref, getString(R.string.setting_key_bgg_account));
		onSharedPreferenceChanged(pref, getString(R.string.setting_key_limit_search_results));
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
	        case android.R.id.home:
	            a.onBackPressed();
	            return true;
			default:
				return super.onOptionsItemSelected(item);
		}
		
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key)
	{
		Preference pref = findPreference(key);
		if(key.equals(getString(R.string.setting_key_bgg_account)))
		{
			String newValue = settings.getBGGAccount();
			String newSummary = getString(R.string.setting_summary_bgg_account);
			if(newValue != null && !newValue.isEmpty())
			{
				newSummary += ": " + newValue;
			}
			pref.setSummary(newSummary);
		}
		else if(key.equals(getString(R.string.setting_key_limit_search_results)))
		{
			int newValue = settings.getSearchResultLimit();
			String newSummary = getString(R.string.setting_summary_number_of_search_results);
			String newValueString = Integer.toString(newValue);
			if (newValue == -1 || newValue == Integer.MAX_VALUE)
			{
				newValueString = "No Limit";
			}
			newSummary += "\nCurrent Limit: " + newValueString;
			pref.setSummary(newSummary);
		}
	}
}
