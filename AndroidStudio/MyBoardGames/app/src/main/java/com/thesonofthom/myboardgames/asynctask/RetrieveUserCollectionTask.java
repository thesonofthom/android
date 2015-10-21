package com.thesonofthom.myboardgames.asynctask;

import android.widget.ListView;
import android.widget.Toast;

import com.thesonofthom.myboardgames.Game;
import com.thesonofthom.myboardgames.GameCache;
import com.thesonofthom.myboardgames.Game.Property;
import com.thesonofthom.myboardgames.bgg.BGGXMLParser.XMLType;
import com.thesonofthom.myboardgames.fragments.LocalGamesFragment;
import com.thesonofthom.myboardgames.tools.Filter;

/**
 * Task to retrieve the list of games that belong to the specified user
 * @author Kevin Thomson
 *
 */
public class RetrieveUserCollectionTask extends RetrieveResultsTask
{
	private static final String TAG = "RetrieveUserCollectionTask";
	private String user;
	private LocalGamesFragment myGamesFragment;
	
	public RetrieveUserCollectionTask(LocalGamesFragment fragment, ListView view,
			GameCache cache, Filter filter, String user)
	{
		super(fragment, view, cache, filter, XMLType.USER_COLLECTION, TAG);
		this.user = user;
		myGamesFragment = fragment;
	}
	
	@Override
	public String getDialogText()
	{
		return "Retrieving user collection for " + user +"...";
	}

	@Override
	public void updateTextFieldQuery()
	{
		// do nothing, text field is invisible
	}
	
	@Override
	public TaskResult doMainTask(Void... params) throws Exception
	{
		TaskResult result = super.doMainTask();
		
		if(result.getResult() && cache != null)
		{
			for(int i = 0; i < cache.getSize(); i++)
			{
				Game game = cache.getList().get(i);
				updateProgress("Retrieving information for %s (%d of %d)...", game.get(Property.name), i+1, cache.getSize());
				game.setOwned(true);
				RetrieveGameInfoTask task = new RetrieveGameInfoTask(context, game);
				task.setCalledFromUiThread(false);
				task.doMainTask();
				SaveGamesTask saveGameTask = new SaveGamesTask(context);
				saveGameTask.doMainTask(game);
				removeLastProgressUpdate();
			}
		}
		return result;
	}
	
	@Override
	public void sortResults()
	{
		//no need to sort
	}
	
	@Override
	public void doPostExecute(TaskResult result)
	{
		super.doPostExecute(result);
		if(myGamesFragment != null)
		{
			myGamesFragment.setCorrectView();
			myGamesFragment.getActivity().invalidateOptionsMenu();
		}
		
		Toast.makeText(context, "Done loading user collection for " + user + "!", Toast.LENGTH_SHORT).show();
	}

	@Override
	public String getUrl()
	{
		return internet.getUserCollectionUrl(user);
	}

	@Override
	public void doPreExecute()
	{
		//nothing to do
	}

	public int getExpandedResultLimit()
	{
		return Integer.MAX_VALUE; //get everything
	}
	
	public boolean doExpandedInfoQuery()
	{
		return true; // always get full info
	}

}
