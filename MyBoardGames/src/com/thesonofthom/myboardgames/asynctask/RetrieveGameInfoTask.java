package com.thesonofthom.myboardgames.asynctask;

import java.io.File;

import android.util.Log;
import com.thesonofthom.myboardgames.Game;
import com.thesonofthom.myboardgames.Game.Property;
import com.thesonofthom.myboardgames.activities.MainActivity;
import com.thesonofthom.myboardgames.adapters.GameInfoDataAdapter;
import com.thesonofthom.myboardgames.adapters.GameInfoDataAdapter.Section;
import com.thesonofthom.myboardgames.bgg.BGGXMLParser;
import com.thesonofthom.myboardgames.fragments.GameInfoFragment;

public class RetrieveGameInfoTask extends DialogAsyncTask<Void>
{
	private static final String TAG = "RetrieveGameInfoTask";
	private Game game;
	private GameInfoFragment fragment;
	private GameInfoDataAdapter adapter;
	private BGGXMLParser parser;
	
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
	}

	@Override
	public TaskResult doMainTask(Void... params) throws Exception
	{
		boolean result = true;
		
		int limit = Integer.MAX_VALUE;//settings.getSearchResultLimit();

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
		return result ? TaskResult.TRUE : TaskResult.FALSE;
	}
	
	@Override
	public void doPostExecute(TaskResult result)
	{
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
	



	@Override
	public String getDialogText()
	{
		return "Loading additional information for " + game.get(Property.name) + "...";
	}

}
