package com.thesonofthom.myboardgames.images;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;

import com.thesonofthom.myboardgames.GamePool;
import com.thesonofthom.myboardgames.tools.FileTools;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class FileCache 
{
	private final String TAG = "FileCache";
    
    private File cacheDir;
    private File permanentDir;
    
    private long currentCacheSize;
    
    private static final int CACHE_SIZE_LIMIT = 50 * 1024 * 1024; //50MB TODO: make this user configurable
    
    public FileCache(File permanentDirectory, File cacheDirectory)
    {
        //Find the dir to save cached images
    	this.permanentDir = permanentDirectory;
    	this.cacheDir = cacheDirectory;
        //Log.i(TAG, cacheDir.toString());
        if(!cacheDir.exists())
        {
        	 cacheDir.mkdirs();
        }
        
        currentCacheSize = FileTools.getFolderSize(cacheDir);
        
    }
    
	public File cacheFile(String url)
	{
		if(url == null)
		{
			return null;
		}
		File f = getFile(url);
		if (f.exists()) 
		{
			//Log.i(TAG, "File" + f + "exists");
			return f; 
		}
		// from web
		try
		{
			//Log.i(TAG, "Getting image from " + url);
			URL imageUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) imageUrl
					.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			InputStream is = conn.getInputStream();
			OutputStream os = new FileOutputStream(f);
			copyStream(is, os);
			os.close();
			
			//check if cache is too large
			currentCacheSize += f.length();
			reduceCacheSizeIfNecessary();
			return f;
		}
		catch (Exception ex)
		{
			//Log.e(TAG, Log.getStackTraceString(ex));
			return null;
		}
	}
	
	
	
	public void saveImage(String url) throws IOException
	{
		if(url == null)
		{
			return;
		}
		File file = getFileFromPermanentStorage(getFileName(url));
		if(file.exists())
		{
			//Log.i(TAG, "Image " + url + "already saved to local storage.");
			return; //nothing to do
		}
		//Log.i(TAG, "Saving imaage...");
		//see if it is in cache, and if not, fetch it from the internet
		file = cacheFile(url);
		if(file != null)
		{
			moveFile(file, permanentDir);
		}	
	}
	
	public void deleteImageFromPermanentStorage(String url) throws IOException
	{
		if(url == null)
		{
			return;
		}
		//delete it from permanent storage, but put it back in the cache
		File file = getFileFromPermanentStorage(getFileName(url));
		if(file.exists())
		{
			//Log.i(TAG, "Deleting file: " + file);
			moveFile(file, cacheDir);
		}
	}
	
	private void moveFile(File file, File newDirectory) throws IOException
	{
		FileInputStream is = new FileInputStream(file);
		FileOutputStream os = new FileOutputStream(new File(newDirectory, file.getName()));
		copyStream(is, os);
		file.delete(); //deletes the old file
	}
    
    private static void copyStream(InputStream is, OutputStream os) throws IOException
    {
        final int buffer_size=1024;
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
              int count=is.read(bytes, 0, buffer_size);
              if(count<=0)
                  break;
              os.write(bytes, 0, count);
            }
    }
    
    
    public String getFileName(String url)
    {
    	return Uri.parse(url).getLastPathSegment();
    }
    
    private File getFileFromPermanentStorage(String fileName)
    {
    	//first, check permanent storage
        return new File(permanentDir, fileName);
    }
    
    public boolean isFileInPermanentStorage(String url)
    {
    	if(url == null)
    	{
    		return false;
    	}
    	File file = getFileFromPermanentStorage(getFileName(url));
    	return file.exists();
    }
    
    public File getFile(String url)
    {
    	if(url == null)
    	{
    		return null;
    	}
    	String fileName = getFileName(url);
    	File f = getFileFromPermanentStorage(fileName);
        if(f.exists())
        {
        	//Log.i(TAG, url + " exists on persistant storage. Returning file: " + f);
        	return f;
        }
        //otherwise, try to get the file from the cache directory
        f = new File(cacheDir, fileName);
       //Log.i(TAG, "Getting file in cache directory: " + f);
        return f;
    }
    


    private void reduceCacheSizeIfNecessary() throws IOException
    {
    	if(currentCacheSize >= CACHE_SIZE_LIMIT)
    	{
    		Log.i(TAG, String.format("Cache is too large: %d bytes exceeds limit of %d bytes", currentCacheSize, CACHE_SIZE_LIMIT));
    		//reduce by half
    		File[] files = cacheDir.listFiles();
    		Log.i(TAG, "Sorting cache by modified date...");
    		Arrays.sort(files, new Comparator<File>(){
    		    public int compare(File f1, File f2)
    		    {
    		        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
    		    } });
    		int index = 0;
    		Log.i(TAG, "Deleting oldest entries...");
    		while(currentCacheSize > (CACHE_SIZE_LIMIT / 2))
    		{
    			File file = files[index];
    			long length = file.length();
    			Log.i(TAG, String.format("Deleting file: %s (Modified: %d, size: %d)", file, file.lastModified(), length));
    			if(file.delete())
    			{
    				currentCacheSize -= length;
    				Log.i(TAG, "New cache size: " + currentCacheSize);
    			}
    			else
    			{
    				Log.e(TAG, "Couldn't delete file!");
    			}
    			index++;
    		}
    		
    	}
    }
    
    public void clear(){
        File[] files=cacheDir.listFiles();
        if(files==null)
            return;
        for(File f:files)
            f.delete();
    }

}