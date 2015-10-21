package com.thesonofthom.myboardgames.fragments;

import java.io.File;

import com.thesonofthom.myboardgames.Game;
import com.thesonofthom.myboardgames.GamePool;
import com.thesonofthom.myboardgames.GamePool.GameGroup;
import com.thesonofthom.myboardgames.R;
import com.thesonofthom.myboardgames.Game.Property;
import com.thesonofthom.myboardgames.Settings;
import com.thesonofthom.myboardgames.asynctask.RemoveGamesTask;
import com.thesonofthom.myboardgames.asynctask.RetrieveGameInfoTask;
import com.thesonofthom.myboardgames.asynctask.SaveGamesTask;
import com.thesonofthom.myboardgames.fragments.ContactListDialogFragment.ContactListDialogListener;
import com.thesonofthom.myboardgames.tools.ContactLoader;
import com.thesonofthom.myboardgames.tools.ContactsQuery;
import com.thesonofthom.myboardgames.tools.FragmentTools;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView.OnChildClickListener;

/**
 * Fragment to display the full detailed info for a particular game
 * @author Kevin Thomson
 *
 */
public class GameInfoFragment extends BaseFragment implements ContactListDialogListener//, LoaderCallbacks<Cursor>
{

	private static final String TAG = "GameInfoFragment";
	
	public GameInfoFragment()
	{
		super(TAG, null);
	}
	
	private Game game;


	public TextView gameName;

	public ImageView gameImage;
	
	public TextView ownershipView;
	
	public ListView gameLoanView;
	
	
	public ExpandableListView mainDataView;
	private ContactLoader contactLoader;

	
	private RetrieveGameInfoTask gameInfoTask;
	
	private static Game currentGame;
	
	private View mRefreshIndeterminateProgressView = null;
	
	private MenuItem addRemoveGameItem;
	private MenuItem loanGameItem;
	
	public static void setGame(Game game)
	{
		currentGame = game;
	}
	
	private View mainView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		settings = new Settings(a);
		game = currentGame;

	}
	
	@Override
	public String getTitle()
	{
		if(game != null)
		{
			return game.get(Property.name);	
		}
		return super.getTitle();
	}
	
	private int queryId = ContactLoader.INVALID_QUERY_ID;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);
		a.getActionBar().setHomeButtonEnabled(true);
		getToggle().setDrawerIndicatorEnabled(false);
		if(mainView == null)
		{
			mainView = inflater.inflate(R.layout.activity_game_info_data_list, container, false);
			mainDataView = (ExpandableListView)mainView.findViewById(R.id.game_info_data);
			View mTop    = inflater.inflate(R.layout.activity_game_info_top, null);
			
			View loanContactView = inflater.inflate(R.layout.activity_game_info_loan_contact, null);
			gameLoanView = (ListView)loanContactView.findViewById(R.id.game_loan_view);
			
			contactLoader = new ContactLoader(a)
			{
				
				@Override
				public Loader<Cursor> onCreateLoader(int id, Bundle args)
				{
					Log.i(TAG, "onCreateLoader(), id: " + id + "...");
					String lookupKey = game.get(Property.loanContact_lookup_key);
					if(lookupKey != null)
					{
						Log.i(TAG, "LookupKey: " + lookupKey);
						return ContactsQuery.getContactLoader(a, lookupKey);//uri);
					}
					return null;
				}
				
				@Override
				public int getQueryId()
				{
			    	if(queryId == INVALID_QUERY_ID && game != null)
			    	{
			    		queryId = game.getObjectId() + GAME_INFO_QUERY_ID_OFFSET;
			    	}
			    	return queryId;
				}
			};
			contactLoader.setSingleContactMode();
			gameLoanView.setAdapter( contactLoader.getContactAdapter());
			gameLoanView.setOnItemClickListener(new OnItemClickListener() {
	        	
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) 
				{
					Cursor cursor =  contactLoader.getContactAdapter().getCursor();
					cursor.moveToPosition(position);
					FriendGameListFragment f = FriendGameListFragment.newInstance(cursor);
					FragmentTools.transitionToFragment(a, f, null);
				}
			});		

			mainDataView.addHeaderView(mTop, null, false);
			mainDataView.addHeaderView(loanContactView, null, false);
			
			mainDataView.setOnChildClickListener(new OnChildClickListener() 
			{
				@Override
				public boolean onChildClick(ExpandableListView parent, View v,
						int groupPosition, int childPosition, long id) 
				{
					Log.i(TAG, "onChildClick: groupPosition: " + groupPosition + " childPosition: " + childPosition + " id: " + id);
					Game game = GamePool.getInstance().get((int)id);
					if(game != null)
					{
						setGame(game);
						GameInfoFragment newGameFragment = new GameInfoFragment();
						FragmentTools.transitionToFragment(a, newGameFragment, null);
						return true;
					}
					else
					{
						Log.e(TAG, "Game with id: " + id + " not found");
						return false;
					}
				}
			});
			
			gameName = (TextView)mTop.findViewById(R.id.game_info_name);
			
			ownershipView = (TextView)mTop.findViewById(R.id.game_info_ownership_status);

			gameImage = (ImageView)mTop.findViewById(R.id.game_info_image);
			gameImage.setOnClickListener(new OnClickListener()
			{
				
				@Override
				public void onClick(View v)
				{
					
					Log.i(TAG, "Image clicked!");
					File url = a.getImageLoader().getFileCache().getFile(game.get(Property.image));
					
					Log.i(TAG, "Image: " + url);
					if(url != null && url.exists())
					{
						Uri uri = Uri.fromFile(url);
						WebViewFragment fragment = WebViewFragment.newInstance(game.get(Property.name), uri.toString());
						FragmentTools.transitionToFragment(a, fragment, null);
					}
					
				}
			});
			
			a.getActionBar().setDisplayHomeAsUpEnabled(true);
			if(game != null)
			{
				game.addAdapter(a.getNavigationDrawerAdapter());
				gameName.setText(game.get(Property.name));
				game.addAdapter(contactLoader.getContactAdapter());
				if(savedInstanceState == null)
				{
					gameInfoTask = new RetrieveGameInfoTask(this, game);
					gameInfoTask.execute();
					if(game.isOwned())
					{
						SaveGamesTask saveGames = new SaveGamesTask(this);
						saveGames.showToast(false);
						saveGames.execute(game);
					}
				}
			}
			
		}
		
		updateAddRemoveIcon();
		updateOwnershipStatus();
		
		//contactLoader.restartLoader();
		
		if(savedInstanceState != null)
		{
			restoreInstanceState(savedInstanceState);
		}
		return mainView;
	}

	
	private void saveGame(boolean forceSave, boolean showToast)
	{
		SaveGamesTask task = new SaveGamesTask(this, forceSave, false);
		task.showToast(showToast);
		task.execute(game);
	}
	
	public void removeGame()
	{
		new AlertDialog.Builder(a)
	    .setMessage("Are you sure you want to remove this game from your library?")
	    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
	    {
	        public void onClick(DialogInterface dialog, int which) 
	        { 
	            // continue with delete
				RemoveGamesTask task = new RemoveGamesTask(GameInfoFragment.this);
				task.hideDialogPermanently();
				task.execute(game);
	        }
	     })
	    .setNegativeButton("No", new DialogInterface.OnClickListener() 
	    {
	        public void onClick(DialogInterface dialog, int which) 
	        { 
	            // do nothing
	        }
	     })
	     .show();
	}
	
	
	//refreshing - whether or not to show the spinning icon
    public void setRefreshActionItemState(boolean refreshing) 
    {
        // On Honeycomb, we can set the state of the refresh button by giving it a custom
        // action view.
        if (menu == null) 
        {
            return;
        }
        
        if (addRemoveGameItem != null) 
        {
        	Log.i(TAG, "setRefreshActionItemState: " +refreshing);
            if (refreshing) 
            {
                if (mRefreshIndeterminateProgressView == null) 
                {
                    LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    mRefreshIndeterminateProgressView = inflater.inflate(R.layout.actionbar_indeterminate_progress, null);
                }
                addRemoveGameItem.setActionView(mRefreshIndeterminateProgressView);
            } 
            else 
            {
            	
            	addRemoveGameItem.setActionView(null);
            	updateAddRemoveIcon();
            	updateOwnershipStatus();
            	game.updateAllAdapters();
            }
        }
    }
    
    
//    public int getQueryId()
//    {
//    	if(queryId == INVALID_QUERY_ID && game != null)
//    	{
//    		queryId = game.getObjectId() + 100;
//    	}
//    	return queryId;
//    }
//    
    public void updateOwnershipStatus()
    {
    	if(ownershipView != null && game != null)
    	{
    		Log.i(TAG," updateOwnershipStatus()");
			if(game.isOwned())
			{
				
				ownershipView.setVisibility(View.VISIBLE);
				if(game.isOnLoan())
				{
					Log.i(TAG, "Game is owned and on loan...");
					ownershipView.setBackgroundResource(R.color.yellow);
					ownershipView.setText("You own this game but it is on loan!");
				    //contactLoader.restartLoader();
					gameLoanView.setVisibility(View.VISIBLE);
				}
				else
				{
					Log.i(TAG, "Game is owned...");
					ownershipView.setBackgroundResource(R.color.green);
					ownershipView.setText("You own this game!");
					gameLoanView.setVisibility(View.GONE);
					contactLoader.destroyLoader();
				}
			}
			else
			{
				Log.i(TAG, "Game is NOT owned...");
				ownershipView.setVisibility(View.GONE);
				gameLoanView.setVisibility(View.GONE);
				contactLoader.destroyLoader();
			}
    	}
    }
    


    public void updateAddRemoveIcon()
    {
    	Log.i(TAG, "updateAddRemoveIcon()");
    	if(addRemoveGameItem == null)
    	{
    		return;
    	}
    	if(game != null && game.isOwned())
    	{
    		Log.i(TAG, "Game " + game + " is owned. Setting icon to DISCARD");
    		addRemoveGameItem.setIcon(R.drawable.ic_action_discard);
    		addRemoveGameItem.setTitle(R.string.action_remove_game);
    	}
    	else
    	{
    		Log.i(TAG, "Game "+ game + " is not owned. Setting icon to ADD");
    		addRemoveGameItem.setIcon(R.drawable.ic_action_new);
    		addRemoveGameItem.setTitle(R.string.action_add_game);
    	}
    	//a.invalidateOptionsMenu();
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
    	super.onCreateOptionsMenu(menu, inflater);
    	inflater.inflate(R.menu.game, menu);
        addRemoveGameItem = menu.findItem(R.id.action_add_remove_game);
        loanGameItem = menu.findItem(R.id.action_loan_game);
        updateAddRemoveIcon();
    	
    }
    
    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
    	super.onPrepareOptionsMenu(menu);
    	
    	if(game != null)
    	{
    		boolean showLoanGameIcon = game.isOwned();
    		loanGameItem.setVisible(showLoanGameIcon);
    		addRemoveGameItem.setVisible(true);
    		if(showLoanGameIcon)
    		{
    			loanGameItem.setIcon(R.drawable.ic_action_person);
    		}
    	}
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.action_add_remove_game:
			{
				if(!game.isOwned())
				{
					game.setOwned(true);
					saveGame(false, true); //only save if you have to
					return true;
				}
				else
				{
					removeGame();
					return true;
				}
			}
			case R.id.action_loan_game:
			{
				loanGame();
				return true;
			}
			case R.id.action_expand_all:
				expandAll();
				return true;
			case R.id.action_collapse_all:
				collapseAll();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
		
	}
	
	public void loanGame()
	{
		if(!game.isOnLoan())
		{
			ContactListDialogFragment f;
			if(GamePool.getInstance().getCache(GameGroup.GamesOnLoan).getSize() > 0)
			{
				f = MyFriendsContactListDialogFragment.newInstance(this);
			}
			else
			{
				f = AllContactsDialogFragment.newInstance(this);
			}
			
			f.show(getFragmentManager(), null);
		}
		else
		{
			new AlertDialog.Builder(a)
		    .setMessage("Return this game to its rightful owner?")
		    .setPositiveButton("Yes", new DialogInterface.OnClickListener()
		    {
		        public void onClick(DialogInterface dialog, int which) 
		        { 
		        	game.returnLoanedGame();
		        	updateOwnershipStatus();
		        	game.updateAllAdapters();
		        	saveGame(true, false);
		        }
		     })
		    .setNegativeButton("No", new DialogInterface.OnClickListener() 
		    {
		        public void onClick(DialogInterface dialog, int which) 
		        { 
		            // do nothing
		        }
		     })
		     .show();
		}
	}
	
	@Override
	public void contactChosen(Cursor c)
	{
		Log.i(TAG, "Contact chosen!");
		Log.i(TAG, "Contact name: " + c.getString(ContactsQuery.DISPLAY_NAME));
		
		game.loanToContact(c.getLong(ContactsQuery.ID), c.getString(ContactsQuery.LOOKUP_KEY));
		updateOwnershipStatus();
		game.updateAllAdapters();
		saveGame(true, false);
	}

	
	public void expandAll()
	{
		for(int i=0; i < mainDataView.getExpandableListAdapter().getGroupCount(); i++)
		{
				mainDataView.expandGroup(i);
		}
	}
	
	public void collapseAll()
	{
		for(int i=0; i < mainDataView.getExpandableListAdapter().getGroupCount(); i++)
		{
				mainDataView.collapseGroup(i);
		}
	}
}
