package com.thesonofthom.myboardgames.asynctask;

public class TaskResult
{
	private boolean result;
	private String message;
	public TaskResult(boolean result, String message)
	{
		this.result = result;
		this.message = message;
	}
	
	public TaskResult(boolean result)
	{
		this(result, null);
	}
	
	public TaskResult(Exception e)
	{
		this(false, e.getMessage());
	}
	
	public boolean getResult()
	{
		return result;
	}
	
	public String getMessage()
	{
		return message;
	}
	
	@Override
	public String toString()
	{
		String resultMessage = "TaskResult: " + result;
		if(message != null)
		{
			resultMessage += " (" + message + ")";
		}
		return resultMessage;
	}
	
	public static final TaskResult FALSE = new TaskResult(false);
	public static final TaskResult TRUE =  new TaskResult(true);
}
