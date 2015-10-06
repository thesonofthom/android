package com.thesonofthom.myboardgames.tools;

import java.util.HashMap;

import com.thesonofthom.myboardgames.activities.MainActivity;
import com.thesonofthom.myboardgames.adapters.ContactListAdapter;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.database.Cursor;
import android.util.Log;

public abstract class ContactLoader  implements LoaderCallbacks<Cursor>
{

	private static final String TAG= "ContactLoader";
	
	protected  ContactListAdapter contactAdapter;
	
	private static int contactLoaderInstance = 0;
	
	private static HashMap<Integer, ContactListAdapter> registeredAdapters = new HashMap<Integer, ContactListAdapter>();

	
	private MainActivity a;
	private int contactLoaderNum;
	public ContactLoader(MainActivity a)
	{
		this.a = a;
		
		if(getQueryId() < GAME_INFO_QUERY_ID_OFFSET)
		{
			contactAdapter = new ContactListAdapter(a, this);
		}
		else
		{
			contactAdapter = registeredAdapters.get(getQueryId());
			if(contactAdapter == null)
			{
				contactAdapter = new ContactListAdapter(a, this);
				registeredAdapters.put(getQueryId(), contactAdapter);
			}
		}
		
		contactLoaderNum = contactLoaderInstance;
		contactLoaderInstance++;
		contactAdapter.setLoader(this);
	}
	
	public ContactListAdapter getContactAdapter()
	{
		return contactAdapter;
	}
	

	public static final int INVALID_QUERY_ID = -1;
	public static final int GAME_INFO_QUERY_ID_OFFSET = 100;

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data)
	{
		Log.i(TAG, "onLoadFinished()...");
		if(loader.getId() == getQueryId())
		{
			contactAdapter.swapCursor(data);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		Log.i(TAG, "onLoaderReset()...");
		if(loader.getId() == getQueryId())
		{
			contactAdapter.swapCursor(null);
		}
		
	}
	
	public abstract int getQueryId();
	
	
	public void restartLoader()
	{
		restartLoader(false);
	}
	
	private boolean singleContactMode;
	public void setSingleContactMode()
	{
		singleContactMode = true;
	}
	
	private boolean destroyLoaderOnRestart = true;
	public void destroyLoaderOnRestart(boolean destroy)
	{
		destroyLoaderOnRestart = destroy;
	}
	
    public void restartLoader(boolean forceRestart)
    {
    	forceRestart = forceRestart && !singleContactMode;
    	Log.i(TAG, "#" + contactLoaderNum + ": restartLoader(), id: "+ getQueryId() + ", forceRestart="+forceRestart+"...");
    	if(getQueryId() != INVALID_QUERY_ID)
    	{
    		
    		boolean doRestart = true;
    		if(!forceRestart)
			{
				try
				{
					int count = contactAdapter.getCount();
					Log.i(TAG, "Count: " + count);
					if (count > 0)
					{
						Log.i(TAG, "No need to restart loader.");
						doRestart = false;
					}
				}
				catch (Exception e)
				{
					Log.w(TAG, "Failed to get count: " + e.getMessage());
				}
			}
    		if(doRestart)
    		{
    			Log.i(TAG, "restarting loader");
    			if(destroyLoaderOnRestart)
    			{
    				destroyLoader();
    			}
    			
    			a.getLoaderManager().restartLoader(getQueryId(), null, this);
    		}
    	}
    }
    
    public void destroyLoader()
    {
    	Log.i(TAG, "destroyLoader...");
    	if(getQueryId() != INVALID_QUERY_ID)
    	{
    		a.getLoaderManager().destroyLoader(getQueryId());
    	}
    }



}
