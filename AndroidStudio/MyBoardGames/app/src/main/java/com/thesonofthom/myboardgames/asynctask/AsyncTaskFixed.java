package com.thesonofthom.myboardgames.asynctask;

import android.os.AsyncTask;
import android.util.Log;


/**
 * Adds a couple of extra features to the standard AsyncTask
 * @author Kevin Thomson
 *
 * @param <Params>
 * @param <Progress>
 * @param <Result>
 */
public abstract class AsyncTaskFixed<Params, Progress, Result> extends AsyncTask<Params, Progress, Result>
{
	protected String TAG = "AsyncTaskFixed";
	
	private static volatile boolean done = false;
	
	private boolean calledFromUiThread = false;
	
	public AsyncTaskFixed(String tag)
	{
		TAG = tag;
		done = false;
		calledFromUiThread = false;
	}
	

	/**
	 * Set up whether this task is being called from the main UI thread or not.
	 * @param enable - false if being called from another task.
	 */
	public void setCalledFromUiThread(boolean enable)
	{
		calledFromUiThread = enable;
	}
	
	public abstract void peformOnPreExecute();
	
	@Override
	/**
	 * make final to ensure calling classes don't forget to call super()
	 * Instead, force calling classes to implement performOnPreExecute
	 */
	protected final void onPreExecute()
	{
		Log.i(TAG, "preExecute...");
		//automatically called from the ui thread
		calledFromUiThread = true;
		peformOnPreExecute();
		super.onPreExecute();
	}
	
	public abstract Result performDoInBackground(Params... params);

	@Override
	protected final Result doInBackground(Params... params)
	{
//		if(done)
//		{
//			//for some reason it gets executed twice. prevent this from happening
//			Log.w(TAG, "Why is this getting executed twice?");
//		}
		Log.i(TAG, "doInBackground...");
		Result result = performDoInBackground(params);
		done = true;
		return result;
	}
	
	protected abstract void performOnPostExecute(Result result);
	
	@Override
	protected final void onPostExecute(Result result)
	{
		Log.i(TAG, "postExecute...");
		//if(done)
		{
			performOnPostExecute(result);
		}
		super.onPostExecute(result);
	}
	
	public static boolean isDone()
	{
		return done;
	}
	
	public void updateProgress(Progress... values)
	{
		if(calledFromUiThread)
		{
			publishProgress(values);
		}
	}
	
	@Override
	protected void onProgressUpdate(Progress... values)
	{
		Log.i(TAG, "onProgressUpdate()...");
		super.onProgressUpdate(values);
	}

	
	public static void cancel()
	{
		done = true;
	}
	
	public abstract void performOnCancelled(Result result);
	
	@Override
	protected final void onCancelled(Result result)
	{
		Log.i(TAG, "Cancelled...");
		done = true;
		super.onCancelled(result);
		performOnCancelled(result);
	}

}
