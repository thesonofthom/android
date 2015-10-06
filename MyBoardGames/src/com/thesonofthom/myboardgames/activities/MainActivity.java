package com.thesonofthom.myboardgames.activities;

import java.util.ArrayList;
import java.util.List;

import com.thesonofthom.myboardgames.GamePool;
import com.thesonofthom.myboardgames.R;
import com.thesonofthom.myboardgames.activities.BaseActivity.Navigation;
import com.thesonofthom.myboardgames.adapters.GameAdapter;
import com.thesonofthom.myboardgames.asynctask.LoadGamesTask;
import com.thesonofthom.myboardgames.fragments.AllGamesFragment;
import com.thesonofthom.myboardgames.fragments.GameSearchFragment;
import com.thesonofthom.myboardgames.fragments.GamesOnLoanFragment;
import com.thesonofthom.myboardgames.fragments.MyFriendsFragment;
import com.thesonofthom.myboardgames.fragments.MyGamesFragment;
import com.thesonofthom.myboardgames.fragments.SettingsFragment;
import com.thesonofthom.myboardgames.images.ContactThumbnailImageLoader;
import com.thesonofthom.myboardgames.images.ImageLoader;
import com.thesonofthom.myboardgames.tools.FileTools;
import com.thesonofthom.myboardgames.tools.FragmentTools;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends BaseActivity
{
	private static final String TAG = "MainActivity";
	
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    //private ArrayAdapter<Navigation> mDrawerAdapter;
    private NavigationDrawerAdapter mDrawerAdapter;
    
    
    private GameSearchFragment searchFragment;
    private AllGamesFragment allGamesFragment;
    private MyGamesFragment myGamesFragment;
    private GamesOnLoanFragment gamesOnLoanFragment;
    private MyFriendsFragment myFriendsFragment;
    
    private ImageLoader imageLoader;
    private ContactThumbnailImageLoader contactThumbnailImageLoader;
    
	public MainActivity()
	{
		super(TAG);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getActionBar().show();
		
		imageLoader = new ImageLoader(getExternalFilesDir(FileTools.PERMANENT_IMAGE_DIRECTORY), getExternalFilesDir(FileTools.IMAGE_CACHE_DIRECTORY));
		contactThumbnailImageLoader = new ContactThumbnailImageLoader(this);
		
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        
        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
        searchFragment = new GameSearchFragment();
        allGamesFragment = new AllGamesFragment();
        myGamesFragment = new MyGamesFragment();
        gamesOnLoanFragment = new GamesOnLoanFragment();
        myFriendsFragment = new MyFriendsFragment();
        
     // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) 
        {
            public void onDrawerClosed(View view) 
            {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
            
            public void onDrawerOpened(View drawerView) 
            {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

		ArrayList<Navigation> itemList = new ArrayList<Navigation>();
		Navigation[] navigationValues = Navigation.values();
		for(int i = 0; i < navigationValues.length; i++)
		{
			Navigation option = navigationValues[i];
			itemList.add(option);
		}
		//mDrawerAdapter = new ArrayAdapter<Navigation>(this, R.layout.drawer_list_item, android.R.id.text1, itemList); //TO DO: convert this to a GameAdapter
		mDrawerAdapter = new NavigationDrawerAdapter(this, R.layout.drawer_list_item, itemList);
        mDrawerList.setAdapter(mDrawerAdapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		
        if (savedInstanceState == null) 
        {
        	transitionToFragment(Navigation.AllGames);
        }
        else
        {
        	onRestoreInstanceState(savedInstanceState);
        }
	}
	
	public NavigationDrawerAdapter getNavigationDrawerAdapter()
	{
		return mDrawerAdapter;
	}
	
	public class NavigationDrawerAdapter extends ArrayAdapter<Navigation> implements GameAdapter
	{

		public NavigationDrawerAdapter(Context context, int resource, List<Navigation> objects)
		{
			super(context, resource, android.R.id.text1, objects);
		}

		@Override
		public void refresh()
		{
			notifyDataSetChanged();
		}
		
	}
	
	public ImageLoader getImageLoader()
	{
		return imageLoader;
	}
	
	public ContactThumbnailImageLoader getContactThumbnailImageLoader()
	{
		return contactThumbnailImageLoader;
	}

	public ActionBarDrawerToggle getToggle()
	{
		return mDrawerToggle;
	}
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener 
	{
	    @Override
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	    {
	    	transitionToFragment(position);
	    }

	}
	
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		GamePool.getInstance().saveState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
		GamePool.getInstance().restoreState(savedInstanceState);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// TODO Auto-generated method stub
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
    	if(mDrawerToggle.isDrawerIndicatorEnabled())
    	{
    	       if (mDrawerToggle.onOptionsItemSelected(item)) 
    	       {
    	           return true;
    	       }
    	}
    	switch (item.getItemId())
    	{
    	case R.id.action_settings:
    		FragmentTools.transitionToFragment(this, new SettingsFragment(), null);
    		//transitionToFragment(Navigation.Settings, true);
    		return true;
//    	        case android.R.id.home:
//    	            onBackPressed();
//    	            return true;
    		default:
    			return super.onOptionsItemSelected(item);
    		}
   }
    
    @Override
    public void setTitle(CharSequence title) 
    {
        getActionBar().setTitle(title);
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    
    public void transitionToFragment(int position) 
    {
    	Navigation choice = Navigation.values()[position];
    	transitionToFragment(choice);
    }
    
    
	public void transitionToFragment(Navigation choice) 
	{
	    // Create a new fragment and specify the planet to show based on position
		
		Fragment fragment = null;
		switch (choice)
		{
			case AllGames:
				fragment = allGamesFragment;
				break;
			case MyGames:
				fragment = myGamesFragment;
				break;
			case GamesOnLoan:
				fragment = gamesOnLoanFragment;
				break;
			case GameSearch:
				fragment = searchFragment;
				break;
			case MyFriends:
				fragment = myFriendsFragment;
				break;
			default:
				fragment = null;
		}
	    //Fragment fragment = new ();
		if(fragment != null)
		{

		    // Insert the fragment by replacing any existing fragment
			Log.i(TAG, "Transitioning to fragment: " + choice);
		    FragmentManager fragmentManager = getFragmentManager();
		    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		    fragmentTransaction.replace(R.id.content_frame, fragment, choice.getTag());
//		    if(pushToBackStack)
//		    {
//	    	fragmentTransaction.addToBackStack(null);
//		    }
		    while(fragmentManager.popBackStackImmediate())
		    {
		    	//keep popping until there is nothing left on the back stack
		    }
		    fragmentTransaction.commit();
		}
	    // Highlight the selected item, update the title, and close the drawer
	    mDrawerList.setItemChecked(choice.ordinal(), true);
	    //setTitle(choice.toString());
	    mDrawerLayout.closeDrawer(mDrawerList);
	}
	
	public boolean setCorrectNavigationListSelection(Fragment fragment)
	{
		Log.i(TAG, "setCorrectNavigationListSelection");
		Navigation correctChoice = null;
		if(fragment != null)
		{
			Navigation choice = Navigation.lookupByTag(fragment.getTag());
			if(choice != null)
			{
				correctChoice = choice;
			}
		}
		else //figure out what we're switching to
		{
			for (Navigation choice : Navigation.values())
			{
				Fragment f = getFragmentManager().findFragmentByTag(choice.getTag());
				if (f != null && f.isVisible())
				{
					correctChoice = choice;
					break;

				}
			}
		}
		if(correctChoice != null)
		{
			mDrawerList.setItemChecked(correctChoice.ordinal(), true);
			return true;
		}

		Log.i(TAG, "No choices are visible");
		for (Navigation choice : Navigation.values())
		{
			mDrawerList.setItemChecked(choice.ordinal(), false);
		}
		return false;
	}
	
	public void refreshNavigationDrawer()
	{
		mDrawerAdapter.notifyDataSetChanged();
	}
	
    @Override
    public void onBackPressed()
    {
    	super.onBackPressed();
    	setCorrectNavigationListSelection(null);
    }
    
    
    @Override
    protected void onStart()
    {
    	// TODO Auto-generated method stub
    	super.onStart();
    	if(!GamePool.getInstance().verifyState(this))
    	{
    		Log.w(TAG, "Game pool memory is invalid! Reloading games!");
    		launchSplashScreen();
    	}

    	//otherwise, memory is ok
    }
    
    private void launchSplashScreen()
    {
 		Intent i = new Intent(this, SplashScreenActivity.class);
		startActivity(i);
		// close this activity
		finish();
    }

}
