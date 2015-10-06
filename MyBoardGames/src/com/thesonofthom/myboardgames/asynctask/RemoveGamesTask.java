package com.thesonofthom.myboardgames.asynctask;

import java.io.File;
import java.io.IOException;

import android.util.Log;
import android.widget.Toast;

import com.thesonofthom.myboardgames.Game;
import com.thesonofthom.myboardgames.GamePool;
import com.thesonofthom.myboardgames.Game.Property;
import com.thesonofthom.myboardgames.fragments.GameInfoFragment;
import com.thesonofthom.myboardgames.fragments.LocalGamesFragment;
import com.thesonofthom.myboardgames.tools.FileTools;


public class RemoveGamesTask extends DialogAsyncTask<Game>
{
	private static final String TAG = "RemoveGamesTask";

	private GameInfoFragment gameInfoFragment;
	private LocalGamesFragment myGamesFragment;
	public RemoveGamesTask(GameInfoFragment fragment)
	{
		super(fragment.getMainActivity(), TAG);
		this.gameInfoFragment = fragment;
	}
	
	public RemoveGamesTask(LocalGamesFragment fragment)
	{
		super(fragment.getMainActivity(), TAG);
		myGamesFragment = fragment;
	}
	
	@Override
	public void doPreExecute()
	{
		if(gameInfoFragment != null)
		{
			gameInfoFragment.setRefreshActionItemState(true);
		}
	}

	@Override
	public TaskResult doMainTask(Game... params) throws Exception
	{
		String result = null;
			for(int i = 0; i < params.length; i++)
			{
				Game game = params[i];
				game.setOwned(false);
				updateProgress("Removing %s (%d of %d)...", game.get(Property.name), i+1, params.length);
				delete(game);
				for(Game baseGame : game.getBaseGameCache().getList())
				{
					//also delete the cache of linked games, unless the user actually own that game
					if(!baseGame.isOwned())
					{
						delete(baseGame);
					}
				}
				for(Game expansion : game.getExpansionCache().getList())
				{
					//also delete the cache of linked games, unless the user actually own that game
					if(!expansion.isOwned())
					{
						delete(expansion);
					}
				}
				removeLastProgressUpdate();
			}
			
			if(params.length == 1)
			{
				result = params[0].get(Property.name) + " removed from library";
			}
			else
			{
				result = "All games successfully removed from library";
			}
			
			return new TaskResult(true, result);
	}


	private void delete(Game game) throws IOException
	{
		
		File file = new File(context.getExternalFilesDir(FileTools.GAME_STORAGE_DIRECTORY), game.getFileName());
		if(file.exists())
		{
			Log.i(TAG, "Deleting: " + game + "(" + file + ")");
			boolean success = file.delete();
			if(success)
			{
				GamePool.getInstance().removeLocalGame(game);
			}
			else
			{
				Log.e(TAG, "Couldn't delete " + file);
			}
		}
		context.getImageLoader().getFileCache().deleteImageFromPermanentStorage(game.get(Property.thumbnail));
		context.getImageLoader().getFileCache().deleteImageFromPermanentStorage(game.get(Property.image));
		
	}
	
	

	@Override
	public void doPostExecute(TaskResult result)
	{
		if(result != null)
		{
			if(gameInfoFragment != null)
			{
				gameInfoFragment.setRefreshActionItemState(false);
			}
			
			if(myGamesFragment != null)
			{
				myGamesFragment.setCorrectView();
			}
		}
		Toast.makeText(context, result.getMessage(), Toast.LENGTH_SHORT).show();
		
	}



	@Override
	public String getDialogText()
	{
		return "Removing games from library...";
	}
}
