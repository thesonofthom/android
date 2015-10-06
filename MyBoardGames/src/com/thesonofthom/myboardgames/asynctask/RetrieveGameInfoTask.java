package com.thesonofthom.myboardgames.asynctask;

import java.io.File;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.thesonofthom.myboardgames.Game;
import com.thesonofthom.myboardgames.Game.Property;
import com.thesonofthom.myboardgames.GameCache;
import com.thesonofthom.myboardgames.activities.MainActivity;
import com.thesonofthom.myboardgames.adapters.GameInfoDataAdapter;
import com.thesonofthom.myboardgames.adapters.GameInfoDataAdapter.Section;
import com.thesonofthom.myboardgames.bgg.BGGXMLParser;
import com.thesonofthom.myboardgames.fragments.GameInfoFragment;
import com.thesonofthom.myboardgames.fragments.LocalGamesFragment;
import com.thesonofthom.myboardgames.images.ImageLoader;
import com.thesonofthom.myboardgames.tools.InternetTools;

public class RetrieveGameInfoTask extends DialogAsyncTask<Void>
{
	private static final String TAG = "RetrieveGameInfoTask";
	private Game game;
	private GameInfoFragment fragment;
	private GameInfoDataAdapter adapter;
	private BGGXMLParser parser;
	private boolean fetchAll;
	
	public RetrieveGameInfoTask(GameInfoFragment fragment, Game game)
	{
		this(fragment.getMainActivity(), game);
		this.fragment = fragment;
	}
	
	public RetrieveGameInfoTask(MainActivity context, Game game)
	{
		super(context, TAG);
		this.game = game;
		parser = new BGGXMLParser(context, null);
		hideDialogInitially();
		
	}
	

	@Override
	public void doPreExecute()
	{
		//Log.i(TAG, "CACHE: " + cache);
//		if(!needToFetchMoreData())
//		{
//			disableDialog(); //nothing to do, so disable the popup
//		}
	}

	@Override
	public TaskResult doMainTask(Void... params) throws Exception
	{
		boolean result = true;
		
		int limit = Integer.MAX_VALUE;//settings.getSearchResultLimit();

		//if(needToFetchMoreData())
		{
			if(game.getStatus() < Game.STATUS_FULL)
			{
				//haven't yet fetched the game yet at all.
				Log.i(TAG, "Game isn't fully loaded yet!");
				//first check if the game already exists locally
				LoadGamesTask loadGamesTask = new LoadGamesTask(context, parser);
				File file = loadGamesTask.getFileFromGame(game);
				if(file.exists())
				{
					Log.i(TAG, "Fetching from local storage...");
					result = loadGamesTask.loadGame(file);
				}
				else
				{
					String id = Integer.toString(game.getObjectId());
					result = internet.retrieveExpandedGameInfo(id, parser);
				}
			}
			
			if(DialogAsyncTask.isDone())
			{
				return TaskResult.FALSE;
			}
			
			if(result)
			{
				Log.i(TAG, game.toString());
				//next, get the list of baseGame
				Log.i(TAG, "base game cache: " + game.getBaseGameCache().toString());
				Log.i(TAG, "base game cache position: " + game.getBaseGameCache().getPosition());
				
				//only want to show the first set of elements
				game.getBaseGameCache().setPosition(0);
				String objectIdList = internet.getObjectIdList(game.getBaseGameCache(), limit);
				if(!objectIdList.isEmpty())
				{
					updateProgress("Retrieving base games...");
					result = internet.retrieveExpandedGameInfo(objectIdList, parser);
				}
				if(result)
				{
					game.getBaseGameCache().setPosition(limit);
				}
			}
			
			if(result)
			{
				Log.i(TAG, "expansion cache: " + game.getExpansionCache().toString());
				Log.i(TAG, "expansion cache position: " + game.getExpansionCache().getPosition());
				game.getExpansionCache().setPosition(0);
				String objectIdList = internet.getObjectIdList(game.getExpansionCache(), limit);
				if(!objectIdList.isEmpty())
				{
					updateProgress("Retrieving expansions...");
					result = internet.retrieveExpandedGameInfo(objectIdList, parser);
				}
				if(result)
				{
					game.getExpansionCache().setPosition(limit); //force initial list to only show the first elements
				}
			}
		}
		return result ? TaskResult.TRUE : TaskResult.FALSE;
	}
	
	@Override
	public void doPostExecute(TaskResult result)
	{
		//Log.i(TAG, "CACHE: " + cache);
		context.getImageLoader().DisplayImage(game.get(Property.image), fragment.gameImage);
		
		if(adapter == null)
		{
			adapter = new GameInfoDataAdapter(fragment, this, game);
			fragment.mainDataView.setAdapter(adapter);
		}
		else
		{
			Log.i(TAG, "notifyDataSetChanged");
			adapter.notifyDataSetChanged();
		}
		
		fragment.updateAddRemoveIcon();
    	fragment.updateOwnershipStatus();
    	game.updateAllAdapters();
		
		for(int i=0; i < adapter.getGroupCount(); i++)
		{
			Section group = (Section)adapter.getGroup(i);
			if(group.startExpanded)
			{
				fragment.mainDataView.expandGroup(i);
			}
		}
	}
	
	public GameInfoDataAdapter getAdapter()
	{
		return adapter;
	}
	
//	public boolean needToFetchMoreData()
//	{
//		//obviously need to fetch the game's data
//		if(game.getStatus() < Game.STATUS_FULL)
//		{
//			Log.i(TAG, "needToFetchMoreData(): Game  " + game.getObjectId() + "Status is not yet STATUS_FULL. Instead: " + game.getStatus());
//			return true;
//		}
//		//also need to fetch the base game list if the data we're going to show hasn't been loaded yet
//
//		for(Game base : game.getBaseGameCache().getList())
//		{
//			if(base.getStatus() < Game.STATUS_FULL)
//			{
//				Log.i(TAG, "needToFetchMoreData(): Game  " + game.getObjectId() + " hasn't retrieved every necessary element in the base game cache.");
//				Log.i(TAG, "position: "+ game.getBaseGameCache().getPosition());
//				Log.i(TAG, "needed: " + settings.getSearchResultLimit());
//				return true;
//			}
//		}
//
//		//also need to fetch the initial expansion list
//		for(Game expansion : game.getExpansionCache().getList())
//		{
//			if(expansion.getStatus() < Game.STATUS_FULL)
//			{
//				Log.i(TAG, "needToFetchMoreData(): Game  " + game.getObjectId() + " hasn't retrieved every necessary element in the expansion cache.");
//				Log.i(TAG, "position: "+ game.getExpansionCache().getPosition());
//				Log.i(TAG, "needed: " + settings.getSearchResultLimit());
//				return true;
//			}
//		}
//		Log.i(TAG, "All Data needed for game is already retrieved");
//		return false; //we have all of the information we need
//	}
	


	@Override
	public String getDialogText()
	{
		return "Loading additional information for " + game.get(Property.name) + "...";
	}

}
