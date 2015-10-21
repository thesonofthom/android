package com.thesonofthom.myboardgames.tools;

import java.io.File;

import android.os.Environment;

/**
 * Miscellaneous tools related to Files
 * @author Kevin Thomson
 *
 */
public class FileTools
{
	
	public static final String GAME_STORAGE_DIRECTORY = "Games";
	public static final String IMAGE_CACHE_DIRECTORY = "Cache";
	public static final String PERMANENT_IMAGE_DIRECTORY = "Images";
	
	public static boolean isExternalStorageReadable() 
	{
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}
	
	public static boolean isExternalStorageWritable() 
	{
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}
	
	public static long getFolderSize(File folder)
	{
		long size = 0;
		if(!folder.isDirectory())
		{
			return size;
		}
		for(File file : folder.listFiles())
		{
			if(file.isFile())
			{
				size += file.length();
			}
		}
		return size;
	}

}
