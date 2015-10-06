package com.thesonofthom.myboardgames.asynctask;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.util.Log;
import com.thesonofthom.myboardgames.Settings;
import com.thesonofthom.myboardgames.activities.MainActivity;
import com.thesonofthom.myboardgames.tools.InternetTools;

public abstract class DialogAsyncTask<Params> extends AsyncTaskExceptionHandler<Params, String> //<Params, Progress, Result>
{
	private ProgressDialog dialog;
	
	protected Settings settings;
	
	protected InternetTools internet;

	protected ArrayList<String> dialogMessage;
	
	private boolean hideDialogInitially;
	private boolean hideDialogPermanently;
	
	public void hideDialogInitially()
	{
		hideDialogInitially = true;
	}
	
	public void hideDialogPermanently()
	{
		hideDialogPermanently = true;
	}
	
	public DialogAsyncTask(MainActivity context, String TAG)
	{
		super(context, TAG);
		settings = new Settings(context);;
		internet = new InternetTools(context, this);
		dialogMessage = new ArrayList<String>();
	}
	
	@Override
	public final void peformOnPreExecute()
	{
		doPreExecute();
		dialogMessage = new ArrayList<String>();
		dialogMessage.add(getDialogText());
		dialog = ProgressDialog.show(context, "Please Wait...",
				getDialogText(), true, true, new OnCancelListener()
				{
					@Override
					public void onCancel(DialogInterface dialog)
					{
						if (DialogAsyncTask.this.cancel(true))
						{
							DialogAsyncTask.cancel();
						}
					}
				});
		if(hideDialogInitially || hideDialogPermanently)
		{
			dialog.hide();
		}
	}
	

	@Override
	protected final void performOnPostExecute(TaskResult result)
	{
		Log.i(TAG, "postExecute...");
		dialog.dismiss();
		super.performOnPostExecute(result);
	}
	
	@Override
	protected final void onProgressUpdate(String... values)
	{
		super.onProgressUpdate(values);
		if(values.length > 0 && !hideDialogPermanently)
		{
			dialog.setMessage(values[0]);
			dialog.show();
		}
	}
	
	public void removeLastProgressUpdate()
	{
		Log.i(TAG, "Removing last progress update...");
		if(!dialogMessage.isEmpty())
		{
			dialogMessage.remove(dialogMessage.size() - 1);
		}
	}
	
	public void updateProgress(String format, Object... args)
	{
		updateProgress(String.format(format, args));
	}
	
	@Override
	public void updateProgress(String... values)
	{
		for(String value : values)
		{
			Log.i(TAG, "Update Progress: " + value);
			dialogMessage.add(value);
		}
		
		StringBuilder s = new StringBuilder();
		for(int i = 0; i < dialogMessage.size(); i++)
		{
			if(i > 0)
			{
				s.append("\n");
			}
			s.append(dialogMessage.get(i));
		}

		super.updateProgress(s.toString());
	}
	
	@Override
	public void performOnCancelled(TaskResult result)
	{
		//nothing to do
	}
	
	public abstract void doPreExecute();

	public abstract String getDialogText();

}
