package com.thesonofthom.myboardgames.asynctask;

import android.content.Context;
import android.util.Log;

import com.thesonofthom.myboardgames.GameCache;
import com.thesonofthom.myboardgames.activities.MainActivity;
import com.thesonofthom.myboardgames.bgg.BGGXMLParser;

public class RetrieveAdditionalResultsTask extends DialogAsyncTask<Void>
{
	/*
	 * retrieves more elements in the specified cache and then updates the view with that remaining data,
	 * restoring which element the user was looking at in the process
	 */
	private static final String TAG = "RetrieveAdditionalResultsTask";
	private DialogAsyncTask relatedTask;
	private GameCache cache;
	protected BGGXMLParser parser;

	public RetrieveAdditionalResultsTask(MainActivity context, GameCache cache, DialogAsyncTask relatedTask)
	{
		super(context, TAG);
		this.relatedTask = relatedTask;
		this.cache = cache;
		parser = new BGGXMLParser(context, cache);
	}
	
	@Override
	public void doPreExecute()
	{
		if(relatedTask != null) //this means we are continuing the work of another task
		{
			relatedTask.doPreExecute();
			//startListPosition = view.getFirstVisiblePosition();
		}
	}
	
	@Override
	public TaskResult doMainTask(Void... params) throws Exception
	{
		//get the next results in the list
		int limit = settings.getSearchResultLimit();
		String list = internet.getObjectIdList(cache, limit);
		boolean  result = true;
		if(!list.isEmpty())
		{
			result = internet.retrieveExpandedGameInfo(list, parser);
		}
		if(result)
		{
			cache.updatePosition(limit);
			Log.i(TAG, "New position: " + cache.getPosition());
		}
		return TaskResult.TRUE;
	}
	
	@Override
	public void doPostExecute(TaskResult result)
	{
		if(relatedTask != null)
		{
			relatedTask.doPostExecute(result);
		}
	}

	@Override
	public String getDialogText()
	{
		return "Retrieving more results...";
	}
}
