package com.thesonofthom.myboardgames.asynctask;

import com.thesonofthom.myboardgames.activities.MainActivity;

import android.util.Log;
import android.widget.Toast;

public abstract class AsyncTaskExceptionHandler<Params, Progress> extends AsyncTaskFixed<Params, Progress, TaskResult>
{
	protected MainActivity context;
	public AsyncTaskExceptionHandler(MainActivity context, String tag)
	{
		super(tag);
		this.context = context;
	}
	
	@Override
	public final TaskResult performDoInBackground(Params... params)
	{
		try
		{
			return doMainTask(params);
		}
		catch(Exception e)
		{
			Log.e(TAG, "ERROR: " + Log.getStackTraceString(e));
			return new TaskResult(e);//false;
		}
	}
	
	@Override
	protected void performOnPostExecute(TaskResult result)
	{
		Log.i(TAG, "Result: " + result);
		if(result == null )
		{
			return;
		}
		if(result.getResult())
		{
			doPostExecute(result);
		}
		else
		{
			Toast.makeText(context, "ERROR: " + result.getMessage(), Toast.LENGTH_LONG).show();
		}
		
	}
	
	@Override
	public void performOnCancelled(TaskResult result)
	{
		Toast.makeText(context, "Cancelled...", Toast.LENGTH_SHORT).show();
	}
	
	public abstract void doPostExecute(TaskResult result);
	
	public abstract TaskResult doMainTask(Params... params) throws Exception;
}
