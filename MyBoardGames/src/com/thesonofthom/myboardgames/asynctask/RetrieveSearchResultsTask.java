package com.thesonofthom.myboardgames.asynctask;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.widget.ListView;

import com.thesonofthom.myboardgames.Game;
import com.thesonofthom.myboardgames.Game.Property;
import com.thesonofthom.myboardgames.GameCache;
import com.thesonofthom.myboardgames.bgg.BGGXMLParser.XMLType;
import com.thesonofthom.myboardgames.fragments.GameSearchFragment;

public class RetrieveSearchResultsTask extends RetrieveResultsTask
{
	private static final String TAG = "RetrieveSearchResultsTask";
	private GameSearchFragment gameFragment;
	private String query;

	public RetrieveSearchResultsTask(GameSearchFragment fragment, ListView view, GameCache newCache,
			String query)
	{
		super(fragment, view, newCache, null, XMLType.SEARCH_RESULT, TAG);
		gameFragment = fragment;
		this.query = query;
	}
	
	@Override
	public void doPreExecute()
	{

	}
	
	@Override
	public void sortResults()
	{
		List<Game> gameList = cache.getList();
		if(gameList.size() <= 1)
		{
			return; //nothing to sort
		}
		ArrayList<Game> exactMatches = new ArrayList<Game>(gameList.size());
		for(int i = gameList.size() - 1; i >= 0; i--)
		{
			Game game = gameList.get(i);
			//if the name matches exactly, move it to the front of the list
			if(query.equalsIgnoreCase(game.get(Property.name)))
			{
				exactMatches.add(game);
			}
		}
		
		for(Game exactMatch : exactMatches)
		{
			gameList.remove(exactMatch);
			gameList.add(0, exactMatch);
		}
	}
	

	@Override
	public void doPostExecute(TaskResult result)
	{
		if (cache.getPosition() >= cache.getList().size())
		{
			gameFragment.moreResultsButton.setVisibility(View.GONE);
		}
		else
		{
			gameFragment.moreResultsButton.setVisibility(View.VISIBLE);
		}
		gameFragment.updateTask(this);
		super.doPostExecute(result);
	}
	
	@Override
	public String getDialogText()
	{
		return "Searching for \"" + query + "\"...";
	}
	
	public String getQuery()
	{
		return query;
	}

	@Override
	public void updateTextFieldQuery()
	{
		String text = String.format("Search Results: \"%s\" (%d of %d)", query, cache.getPosition(), cache.getSize());
		gameFragment.getSearchQuery().setText(text);
	}

	@Override
	public String getUrl()
	{
		return internet.getSearchUrl(query);
	}


}
