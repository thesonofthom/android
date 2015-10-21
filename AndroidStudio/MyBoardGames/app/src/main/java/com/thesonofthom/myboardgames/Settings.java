package com.thesonofthom.myboardgames;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Wrapper class to retrieve different Settings
 * .
 * @author Kevin Thomson
 *
 */
public class Settings
{
	
	private Context context;
	public Settings(Context context)
	{
		this.context = context;
	}
	
	public String getBGGAccount()
	{
		String bggAccount = getString(R.string.setting_key_bgg_account, null);
		if(bggAccount != null && bggAccount.isEmpty())
		{
			return null;
		}
		return bggAccount;
	}
	
	
	public int getSearchResultLimit()
	{
		int limitNumberOfResults = Integer.MAX_VALUE;
		if(detailedSearchResultsEnabled())
		{
			int limit = getIntegerFromString(R.string.setting_key_limit_search_results, 10);
			if(limit != -1)
			{
				limitNumberOfResults = limit;
			}
		}
		return limitNumberOfResults;
	}
	
	public boolean detailedSearchResultsEnabled()
	{
		return true;
		//return getBoolean(R.string.setting_key_detailed_search_results, true);
	}
	
	
	public String getString(int key, String defaultValue)
	{
		String keyName = getKeyName( key);
		return getSettings().getString(keyName, defaultValue);
	}
	
	public int getIntegerFromString(int key, int defaultValue)
	{
		try
		{
			return Integer.parseInt(getString(key, Integer.toString(defaultValue)));
		}
		catch(NumberFormatException e)
		{
			
			return -1;
		}
	}
	
	public int getInt(int key, int defaultValue)
	{
		String keyName = getKeyName(key);
		return getSettings().getInt(keyName, defaultValue);
	}
	
	public boolean getBoolean( int key, boolean defaultValue)
	{
		String keyName = getKeyName(key);
		return getSettings().getBoolean(keyName, defaultValue);
	}
	
	private SharedPreferences getSettings()
	{
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public String getKeyName(int key)
	{
		return context.getResources().getString(key);
	}
}
