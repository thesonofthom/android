package com.thesonofthom.myboardgames.images;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.thesonofthom.myboardgames.R;
import com.thesonofthom.myboardgames.activities.MainActivity;

public class ContactThumbnailImageLoader extends ContactImageLoader
{
	
	public ContactThumbnailImageLoader(MainActivity context)
	{
		super(context);
		setImageSize(getListPreferredItemHeight());
		setLoadingImage(R.drawable.ic_contact_picture_holo_light);
	}
	
    private int getListPreferredItemHeight() {
        final TypedValue typedValue = new TypedValue();

        // Resolve list item preferred height theme attribute into typedValue
        activity.getTheme().resolveAttribute(
                android.R.attr.listPreferredItemHeight, typedValue, true);

        // Create a new DisplayMetrics object
        final DisplayMetrics metrics = new android.util.DisplayMetrics();

        // Populate the DisplayMetrics
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // Return theme value based on DisplayMetrics
        return (int) typedValue.getDimension(metrics);
    }
	
	@Override
	protected Bitmap processBitmap(String photoData)
	{
	        // Instantiates an AssetFileDescriptor. Given a content Uri pointing to an image file, the
	        // ContentResolver can return an AssetFileDescriptor for the file.
	        AssetFileDescriptor afd = null;

	        // This "try" block catches an Exception if the file descriptor returned from the Contacts
	        // Provider doesn't point to an existing file.
	        try {
	            Uri thumbUri = Uri.parse(photoData);

	            // Retrieves a file descriptor from the Contacts Provider. To learn more about this
	            // feature, read the reference documentation for
	            // ContentResolver#openAssetFileDescriptor.
	            afd = activity.getContentResolver().openAssetFileDescriptor(thumbUri, "r");

	            // Gets a FileDescriptor from the AssetFileDescriptor. A BitmapFactory object can
	            // decode the contents of a file pointed to by a FileDescriptor into a Bitmap.
	            FileDescriptor fileDescriptor = afd.getFileDescriptor();

	            if (fileDescriptor != null) {
	                // Decodes a Bitmap from the image pointed to by the FileDescriptor, and scales it
	                // to the specified width and height
	                return decodeSampledBitmapFromDescriptor(
	                        fileDescriptor, getImageSize(), getImageSize());
	            }
	        } catch (FileNotFoundException e) {
	            // If the file pointed to by the thumbnail URI doesn't exist, or the file can't be
	            // opened in "read" mode, ContentResolver.openAssetFileDescriptor throws a
	            // FileNotFoundException.
	                Log.d(TAG, "Contact photo thumbnail not found for contact " + photoData
	                        + ": " + e.toString());
	        } finally {
	            // If an AssetFileDescriptor was returned, try to close it
	            if (afd != null) {
	                try {
	                    afd.close();
	                } catch (IOException e) {
	                    // Closing a file descriptor might cause an IOException if the file is
	                    // already closed. Nothing extra is needed to handle this.
	                }
	            }
	        }
	        // If the decoding failed, returns null
	        return null;
	    }
}