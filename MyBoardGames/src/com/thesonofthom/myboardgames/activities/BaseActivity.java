package com.thesonofthom.myboardgames.activities;

import com.thesonofthom.myboardgames.GamePool;
import com.thesonofthom.myboardgames.GamePool.GameGroup;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public abstract class BaseActivity extends Activity
{
	private String TAG = "BaseActivity";
	protected Menu menu;
		
	public BaseActivity(String TAG)
	{
		this.TAG = TAG;
	}
	
	public static enum Navigation
	{
		AllGames("All Games", GameGroup.AllGames),
		MyGames("My Games", GameGroup.MyGames),
		GamesOnLoan("Games on Loan", GameGroup.GamesOnLoan),
		MyFriends("My Friends", null),
		GameSearch("Search for Games", null);

		private String name;
		private GameGroup group;
		Navigation(String name, GameGroup group)
		{
			this.name = name;
			this.group = group;
		}
		
		public GameGroup getGroup()
		{
			return group;
		}
		
		public String getTag()
		{
			return name();
		}
		
		public int getCount()
		{
			switch(this)
			{
			case AllGames:
			case MyGames:
			case GamesOnLoan:
				return GamePool.getInstance().getCache(group).getSize();
			case MyFriends:
				return GamePool.getInstance().getListOfFriendLookupKeys().size();
			default:
				return -1;
			}
		}
		
		public String getTitle()
		{
			return name;
		}
		
		@Override
		public String toString()
		{
			String s = name;
			
			int count = getCount();
			if(count > -1)
			{
				s += " ("+count+")";
			}
			
			return s;
		}
		
		public static Navigation lookupByTag(String tag)
		{
			if(tag == null)
			{
				return null;
			}
			for(Navigation value : values())
			{
				if(value.getTag().equals(tag))
				{
					return value;
				}
			}
			return null;
		}
	}
	

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		Log.i(TAG, "onBackPressed()");
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		Log.i(TAG, "onRestoreInstanceState: " + savedInstanceState);
		super.onRestoreInstanceState(savedInstanceState);
		
		restoreInstanceState(savedInstanceState);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.i(TAG, "onCreate(): " + savedInstanceState);
		super.onCreate(savedInstanceState);
	}

	
	@Override
	protected void onRestart()
	{
		Log.i(TAG, "onRestart()...");
		super.onRestart();
	}
	
	@Override
	protected void onStart()
	{
		Log.i(TAG, "onStart()...");
		super.onStart();
	}
	
	@Override
	protected void onResume()
	{
		Log.i(TAG, "onRestart()...");
		super.onResume();
	}
	
	@Override
	protected void onPause()
	{
		Log.i(TAG, "onPause()...");
		super.onPause();
	}
	
	@Override
	protected void onStop()
	{
		Log.i(TAG, "onStop()...");
		super.onStop();
	}
	
	@Override
	protected void onDestroy()
	{
		Log.i(TAG, "onDestroy()...");
		super.onDestroy();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		Log.i(TAG, "onSaveInstanceState()...");
		super.onSaveInstanceState(outState);
	}
	
	protected void restoreInstanceState(Bundle savedInstanceState)
	{

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		Log.i(TAG, "onCreateOptionsMenu()...");
		this.menu = menu;
//		MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main, menu);
		return true;
	}
	
}
