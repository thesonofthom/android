package com.thesonofthom.myboardgames.fragments;

import com.thesonofthom.myboardgames.R;
import com.thesonofthom.myboardgames.Settings;
import com.thesonofthom.myboardgames.activities.BaseActivity.Navigation;
import com.thesonofthom.myboardgames.activities.MainActivity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseFragment extends Fragment
{
	protected String TAG = "BaseFragment";
	protected Menu menu;
	protected MainActivity a;
	
	
	protected Navigation fragmentType;
	protected Settings settings;
		
	public BaseFragment(String TAG, Navigation fragmentType)
	{
		this.TAG = TAG;
		this.fragmentType = fragmentType;
	}
	
	public String getTitle()
	{
		if(fragmentType != null)
		{
			return fragmentType.getTitle();
		}
		return null;
	}
	
	public void updateTitle()
	{
		a.setTitle(getTitle());
		a.refreshNavigationDrawer();
	}
	
	public MainActivity getMainActivity()
	{
		return a;
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
	public void onCreate(Bundle savedInstanceState)
	{
		Log.i(TAG, "onCreate(): " + savedInstanceState);
		super.onCreate(savedInstanceState);
		a = (MainActivity)getActivity();
		setHasOptionsMenu(true);
		
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.i(TAG, "onCreateView(): " + savedInstanceState);
		super.onCreateView(inflater, container, savedInstanceState);
		a.getActionBar().setHomeButtonEnabled(true);
		getToggle().setDrawerIndicatorEnabled(true);
		return container;
	}
	
	public ActionBarDrawerToggle getToggle()
	{
		if(a instanceof MainActivity)
		{
			return ((MainActivity)a).getToggle();
		}
		return null;
	}
	

	@Override
	public void onStart()
	{
		Log.i(TAG, "onStart()...");
		super.onStart();
		if(a != null)
		{
			String title = getTitle();
			if(title != null)
			{
				a.setTitle(getTitle());
			}
		}
	}
	
	@Override
	public void onResume()
	{
		Log.i(TAG, "onResume()...");
		super.onResume();
		updateTitle();
	}
	
	@Override
	public void onPause()
	{
		Log.i(TAG, "onPause()...");
		super.onPause();
	}
	
	@Override
	public void onStop()
	{
		Log.i(TAG, "onStop()...");
		super.onStop();
	}
	
	@Override
	public void onDestroy()
	{
		Log.i(TAG, "onDestroy()...");
		super.onDestroy();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		Log.i(TAG, "onSaveInstanceState()...");
		super.onSaveInstanceState(outState);
	}
	
	protected void restoreInstanceState(Bundle savedInstanceState)
	{
		Log.i(TAG, "restoreInstanceState()...");
	}
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
	{
		Log.i(TAG, "onCreateOptionsMenu()...");
		this.menu = menu;
        if(menu.findItem(R.menu.main) == null)
        {
        	inflater.inflate(R.menu.main, menu);
        }
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu)
	{
		Log.i(TAG, "onPrepareOptionsMenu()...");
		super.onPrepareOptionsMenu(menu);
	}
}
