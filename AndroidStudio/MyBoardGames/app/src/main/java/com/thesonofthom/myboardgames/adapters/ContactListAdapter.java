package com.thesonofthom.myboardgames.adapters;


import com.thesonofthom.myboardgames.R;
import com.thesonofthom.myboardgames.activities.MainActivity;
import com.thesonofthom.myboardgames.tools.ContactLoader;
import com.thesonofthom.myboardgames.tools.ContactsQuery;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.QuickContactBadge;
import android.widget.SectionIndexer;
import android.widget.TextView;

/**
 * Adapter for viewing a list of Contacts from the Contacts SQL database
 * 
 * @author Kevin Thomson
 *
 */
public class ContactListAdapter extends CursorAdapter  implements SectionIndexer, GameAdapter
{
	private static String TAG = "ContactListAdapter";
	
	 protected LayoutInflater mInflater; // Stores the layout inflater
	 protected AlphabetIndexer mAlphabetIndexer; // Stores the AlphabetIndexer instance
	 protected MainActivity activity;
	 protected ContactLoader loader;
	 
	 
	public ContactListAdapter(MainActivity activity, ContactLoader loader)
	{
		super(activity, null, 0);
		this.activity = activity;
		mInflater = LayoutInflater.from(activity);
		this.loader = loader;
		final String alphabet = activity.getString(R.string.alphabet);
		 mAlphabetIndexer = new AlphabetIndexer(null, ContactsQuery.SORT_KEY, alphabet);
	}
	
    private class ViewHolder {
        TextView contactName;
        QuickContactBadge thumbnail;
    }

	@Override
	public void bindView(View view, Context context, Cursor cursor)
	{
		//Log.i(TAG, "bindView: cursor="+cursor);
		
		 final ViewHolder holder = (ViewHolder) view.getTag();
		int photoUriColumnIndex = ContactsQuery.PHOTO_THUMBNAIL_DATA;//cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI);
        final String photoUri = cursor.getString(photoUriColumnIndex);
        //Log.i(TAG, "Photo URI: index="+photoUriColumnIndex +", " + photoUri);
        
        int displayNameColumnIndex = ContactsQuery.DISPLAY_NAME;//cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);
        final String displayName = cursor.getString(displayNameColumnIndex);
       //Log.i(TAG, "Display Name: index="+displayNameColumnIndex +", " + displayName);
        holder.contactName.setText(displayName);
        final Uri contactUri = ContactsQuery.getContactUri(cursor);
        holder.thumbnail.assignContactUri(contactUri);
        activity.getContactThumbnailImageLoader().loadImage(photoUri, holder.thumbnail);
	}
	
	
	public void setLoader(ContactLoader loader)
	{
		this.loader = loader;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) 
	{
		//Log.i(TAG, "newView: cursor=" + cursor);
		final View mainView = mInflater.inflate(R.layout.contact_list_item, viewGroup, false);
		final ViewHolder holder = new ViewHolder();
		holder.thumbnail = (QuickContactBadge) mainView
				.findViewById(R.id.contact_thumbnail);
		holder.contactName = (TextView) mainView.findViewById(R.id.contact_name);
		mainView.setTag(holder);
		return mainView;
	}
	
	
    @Override
    public Cursor swapCursor(Cursor newCursor) {
        // Update the AlphabetIndexer with new cursor as well
        mAlphabetIndexer.setCursor(newCursor);
        return super.swapCursor(newCursor);
    }

    @Override
    public int getCount() {
    	int count = 0;
        if (getCursor() != null) 
        {
            count = super.getCount();
        }
        
       // Log.i(TAG, "getCount: cursor="+getCursor() + " count="+count);
        return count;
    }

    @Override
    public Object[] getSections() {
    	//Log.i(TAG, "getSections()");
        return mAlphabetIndexer.getSections();
    }

    /**
     * Defines the SectionIndexer.getPositionForSection() interface.
     */
    @Override
    public int getPositionForSection(int i) {
    	//Log.i(TAG, "getPositionForSection(): " + i);
        if (getCursor() == null) {
            return 0;
        }
        return mAlphabetIndexer.getPositionForSection(i);
    }

    /**
     * Defines the SectionIndexer.getSectionForPosition() interface.
     */
    @Override
    public int getSectionForPosition(int i) {
    	//Log.i(TAG, "getSectionForPosition(): " + i);
        if (getCursor() == null) {
            return 0;
        }
        return mAlphabetIndexer.getSectionForPosition(i);
    }


	@Override
	public void refresh()
	{
		Log.i(TAG, "Game requested a refresh...");
		loader.restartLoader(true);
	}
}
