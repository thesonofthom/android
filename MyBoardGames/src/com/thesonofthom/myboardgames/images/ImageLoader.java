package com.thesonofthom.myboardgames.images;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.thesonofthom.myboardgames.R;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

public class ImageLoader 
{
	private static final String TAG = "ImageLoader";
    
    MemoryCache memoryCache=new MemoryCache();
    FileCache fileCache;
    private Map<ImageView, String> imageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    ExecutorService executorService; 
    
    public ImageLoader(File permanentImageDirectory, File cacheDirectory)
    {
        fileCache=new FileCache(permanentImageDirectory, cacheDirectory);
        executorService=Executors.newFixedThreadPool(5);
    }
    
    public FileCache getFileCache()
    {
    	return fileCache;
    }
    
    final int stub_id = R.drawable.no_image;
    public void DisplayImage(String url, ImageView imageView)
    {
    	//Log.i(TAG, "Displaying image: " + url);
        imageViews.put(imageView, url);
        Bitmap bitmap=memoryCache.get(url);
        if(bitmap!=null)
        {
        	//Log.i(TAG, "Bitmap already in memory!");
        	imageView.setImageBitmap(bitmap);
        }
            
        else
        {
        	//Log.i(TAG, "Queuing photo...");
            queuePhoto(url, imageView);
            imageView.setImageResource(stub_id);
        }
    }
        
    private void queuePhoto(String url, ImageView imageView)
    {
        PhotoToLoad p=new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(p));
    }
    
    
    
    private Bitmap getBitmap(String url) 
    {
    	Log.i(TAG, "Caching url " + url);
    	File f = fileCache.cacheFile(url);
    	if(f != null)
    	{
    		return decodeFile(f);
    	}
    	return null;
    }

    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f)
    {
    	//Log.i(TAG, "Decoding file " + f);
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);
            
            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE=200;
            int width_tmp=o.outWidth, height_tmp=o.outHeight;
            int scale=1;
            while(true){
                if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                    break;
                width_tmp/=2;
                height_tmp/=2;
                scale*=2;
            }
            
            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
            //Log.i(TAG, "Bitmap decoded! BMP: " + b);
            return b;
        } 
        catch (Exception e) 
        {
        	Log.e(TAG, Log.getStackTraceString(e));
        }
        return null;
    }
    

    //Task for the queue
    private class PhotoToLoad
    {
        public String url;
        public ImageView imageView;
        public PhotoToLoad(String u, ImageView i){
            url=u; 
            imageView=i;
        }
    }
    
    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;
        PhotosLoader(PhotoToLoad photoToLoad){
            this.photoToLoad=photoToLoad;
        }
        
        @Override
        public void run() {
            if(imageViewReused(photoToLoad))
            {
            	return;
            }
                
            Bitmap bmp=getBitmap(photoToLoad.url);
            //Log.i(TAG, "BMP: " + bmp);
            memoryCache.put(photoToLoad.url, bmp);
            if(imageViewReused(photoToLoad))
            {
            	return;
            }
            //Log.i(TAG, "Displaying bitmap...");
            BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad);
            Activity a=(Activity)photoToLoad.imageView.getContext();
            a.runOnUiThread(bd);
        }
    }
    
    boolean imageViewReused(PhotoToLoad photoToLoad){
        String tag=imageViews.get(photoToLoad.imageView);
        if(tag==null || !tag.equals(photoToLoad.url))
        {
        	//Log.i(TAG, "image view reused?");
        	return true;
        }
            
        return false;
    }
    
    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;
        public BitmapDisplayer(Bitmap b, PhotoToLoad p){bitmap=b;photoToLoad=p;}
        public void run()
        {
            if(imageViewReused(photoToLoad))
            {
            	return;
            }
                
            if(bitmap!=null)
            {
            	//Log.i(TAG, "Setting image view to bitmap " + bitmap);
                photoToLoad.imageView.setImageBitmap(bitmap);
            }
            else
            {
            	//Log.i(TAG, "BMP is NULL. Setting to stub_id");
            	photoToLoad.imageView.setImageResource(stub_id);
            }
               
        }
    }

    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }

}
