package com.thesonofthom.myboardgames.asynctask;

import com.thesonofthom.myboardgames.GameCache;
import com.thesonofthom.myboardgames.adapters.GameListAdapter;
import com.thesonofthom.myboardgames.bgg.BGGXMLParser;
import com.thesonofthom.myboardgames.bgg.BGGXMLParser.XMLType;
import com.thesonofthom.myboardgames.fragments.BaseFragment;
import com.thesonofthom.myboardgames.tools.Filter;
import com.thesonofthom.myboardgames.tools.InternetTools;

import android.util.Log;
import android.widget.ListView;

/**
 * base abstract Task to retrieve a list of games
 * @author Kevin Thomson
 *
 */
public abstract class RetrieveResultsTask extends DialogAsyncTask<Void>
{
	private XMLType type;
	protected ListView view;
	protected GameListAdapter adapter;
	protected GameCache cache;
	private BGGXMLParser parser;
	private BaseFragment fragment;
	private Filter filter;
	
	public RetrieveResultsTask(BaseFragment fragment, ListView view,
			GameCache cache, Filter filter, XMLType type, String TAG)
	{
		super(fragment.getMainActivity(), TAG);
		this.fragment = fragment;
		this.type = type;
		this.view = view;
		this.cache = cache;
		this.filter = filter;
		parser = new BGGXMLParser(context, cache);
		internet = new InternetTools(context, this);
		
	}
	
	public GameListAdapter getAdapter()
	{
		return adapter;
	}
	
	public GameCache getCache()
	{
		return cache;
	}
	
	public abstract void updateTextFieldQuery();
	
	public abstract String getUrl();
	
	public abstract void sortResults();

	@Override
	public TaskResult doMainTask(Void... params) throws Exception
	{
		Boolean result = true;
		String url = getUrl();
		String xml = internet.getXmlFromInternet(url);

		if(isDone())
		{
			return TaskResult.FALSE;
		}
		updateProgress("Parsing results...");
		boolean success = parser.parseGameList(xml, type);
		
		if(success)
		{
			sortResults();
		}
		
		if(isDone())
		{
			return TaskResult.FALSE;
		}
		if(doExpandedInfoQuery())
		{
			if (success)
			{
				updateProgress("Retrieving detailed game info...");
				String list = internet.getObjectIdList(cache, getExpandedResultLimit());
				if(!list.isEmpty())
				{
					result = internet.retrieveExpandedGameInfo(list, parser);
					if(result)
					{
						cache.updatePosition(settings.getSearchResultLimit());
					}
				}
			}
		}
		else
		{
			cache.useEntireList(true);
		}
		return TaskResult.TRUE;
	}
	
	public int getExpandedResultLimit()
	{
		return settings.getSearchResultLimit();
	}
	
	public boolean doExpandedInfoQuery()
	{
		return settings.detailedSearchResultsEnabled();
	}
	
	@Override
	public void doPostExecute(TaskResult result)
	{
		updateTextFieldQuery();
		Log.i(TAG, "Set adapter...");
		if(adapter == null)
		{
			Log.i(TAG, "Creating new adapter...");
			adapter = new GameListAdapter(context, cache, filter);
			view.setAdapter(adapter);
		}
		else
		{
			adapter.notifyDataSetChanged();
		}
		fragment.updateTitle();
		Log.i(TAG, "Done");
	}
	
	
}
