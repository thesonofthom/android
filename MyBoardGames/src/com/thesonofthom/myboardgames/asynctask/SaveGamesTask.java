package com.thesonofthom.myboardgames.asynctask;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.thesonofthom.myboardgames.Game;
import com.thesonofthom.myboardgames.GamePool;
import com.thesonofthom.myboardgames.Game.Property;
import com.thesonofthom.myboardgames.activities.MainActivity;
import com.thesonofthom.myboardgames.bgg.BGGXMLWriter;
import com.thesonofthom.myboardgames.fragments.GameInfoFragment;
import com.thesonofthom.myboardgames.images.FileCache;


public class SaveGamesTask extends AsyncTaskExceptionHandler<Game, Void>//AsyncTaskFixed<Game, Void, String>
{
	private static final String TAG = "SaveGameActivity";

	private GameInfoFragment fragment;
	private BGGXMLWriter writer;
	private boolean forceSaveGame;
	private boolean forceSaveExpansions;
	
	private FileCache fileCache;
	
	private boolean showToast;
	
	public void showToast(boolean show)
	{
		showToast = show;
	}
	
	public SaveGamesTask(MainActivity context, boolean forceSaveGame, boolean forceSaveExpansions)
	{
		super(context, TAG);
		writer = new BGGXMLWriter(context);
		this.forceSaveGame = forceSaveGame;
		this.forceSaveExpansions = forceSaveExpansions;
		showToast = false;
		fileCache = context.getImageLoader().getFileCache();
	}
	
	public SaveGamesTask(MainActivity context)
	{
		this(context, false, false);
	}
	
	public SaveGamesTask(GameInfoFragment fragment, boolean forceSaveGame, boolean forceSaveExpansions)
	{
		this(fragment.getMainActivity(), forceSaveGame, forceSaveExpansions);
		this.fragment = fragment;
		showToast = true;
	}
	
	public SaveGamesTask(GameInfoFragment fragment)
	{
		this(fragment, false, false);
	}
	
	@Override
	public void peformOnPreExecute()
	{
		if(fragment != null)
		{
			fragment.setRefreshActionItemState(true);
		}
	}

	//only save game if the full information for the game has been retrieved
	private boolean needToSaveGameToXML(Game game)
	{
		
		if(game.getStatus() < Game.STATUS_FULL)
		{
			return false; //never save a game if it hasn't been fully loaded
		}
		if(!writer.getGameFile(game).exists())
		{
			Log.i(TAG, "Game " + game + " has not yet been saved.");
			return true;
		}
		if(game.isDirty())
		{
			Log.i(TAG, "Game " + game + " is dirty and needs to be saved again.");
			return true;
		}
		return false;
	}
	
	@Override
	public TaskResult doMainTask(Game... params) throws Exception
	{
		String result = null;
		for (Game game : params)
		{
			writeGame(game, forceSaveGame);

			// also save expansions so the game loads faster when clicking on it
			for (Game baseGame : game.getBaseGameCache().getList())
			{
				writeGame(baseGame, forceSaveExpansions);
			}
			for (Game expansion : game.getExpansionCache().getList())
			{
				writeGame(expansion, forceSaveExpansions);
			}
		}

		if (params.length == 1)
		{
			result = params[0].get(Property.name) + " added to library!";
		}
		else
		{
			result = "All games successfully added to library!";
		}

		return new TaskResult(true, result);
	}
	
	private void writeGame(Game game, boolean forceSave) throws ParserConfigurationException, TransformerException, IOException
	{
		if(forceSave || needToSaveGameToXML(game))
		{
			writer.writeToXml(game);
		}
		context.getImageLoader().getFileCache()
				.saveImage(game.get(Property.thumbnail));
		if (game.isOwned()) // only save the image for the expansion if the expansion is actually owned
		{
			context.getImageLoader().getFileCache().saveImage(game.get(Property.image));
		}
		GamePool.getInstance().addLocalGame(game);
	}
	
	@Override
	public void doPostExecute(TaskResult result)
	{
		if (fragment != null)
		{
			fragment.setRefreshActionItemState(false);
		}
		if (showToast)
		{
			Toast.makeText(context, result.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

}
