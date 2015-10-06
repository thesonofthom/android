package com.thesonofthom.myboardgames.tools;

import java.util.ArrayList;

import com.thesonofthom.myboardgames.Game;
import com.thesonofthom.myboardgames.GamePool;
import com.thesonofthom.myboardgames.Game.Property;
import com.thesonofthom.myboardgames.GamePool.GameGroup;

import android.app.Activity;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.util.Log;


public class ContactsQuery
{
	private static final String TAG = "ContactsQuery";
	
    public static CursorLoader getAllContactsLoader(Activity a)
    {
        return new CursorLoader(a,
        		CONTENT_URI,
                PROJECTION,
                SELECTION,
                null,
                SORT_ORDER);
    }
    
    public static CursorLoader getContactSearchQueryLoader(Activity a, String search)
    {
    	Uri searchUri = Uri.withAppendedPath(FILTER_URI, Uri.encode(search));
    	return new CursorLoader(a,
    			searchUri,
    			PROJECTION,
    			SELECTION,
    			null,
    			SORT_ORDER);
    }
    
    
    public static CursorLoader getContactLoader(Activity a, String lookupKey)
    {
    	return new CursorLoader(a,
    			CONTENT_URI,
    			PROJECTION,
    			Contacts.LOOKUP_KEY+"=?",
    			new String[]{lookupKey},
    			SORT_ORDER);
    	
//    	return new CursorLoader(a,
//    			contactUri,
//    			PROJECTION,
//    			null,
//    			null,
//    			null);
    }
    
    public static CursorLoader getLoanedContactsLoader(Activity a)
    {
    	ArrayList<String> uniqueLookupKeys = GamePool.getInstance().getListOfFriendLookupKeys();
    	String selection = "";
    	for(int i = 0; i < uniqueLookupKeys.size(); i++)
    	{
    		if(i > 0)
    		{
    			selection += " OR ";
    		}
    		selection += Contacts.LOOKUP_KEY+"=?";
    	}
    	
//    	ArrayList<String> uniqueLookupKeys = new ArrayList<String>();
//    	
//    	for(Game game : GamePool.getInstance().getCache(GameGroup.GamesOnLoan).getList())
//    	{
//    		String lookupkey = game.get(Property.loanContact_lookup_key);
//    		if(!uniqueLookupKeys.contains(lookupkey))
//    		{
//    			uniqueLookupKeys.add(lookupkey);
//    			if(!selection.isEmpty())
//    			{
//    				selection += " OR ";
//    			}
//    			
//    			selection += Contacts.LOOKUP_KEY+"=?";
//    		}
//    	}
    	
    	Log.i(TAG, "Selection: " + selection);
    	
    	return new CursorLoader(a,
    			CONTENT_URI,
    			PROJECTION,
    			selection,
    			uniqueLookupKeys.toArray(new String[uniqueLookupKeys.size()]),
    			SORT_ORDER);
    	
    }
    

    public static Uri getContactUri(Cursor c)
    {
    	//return Contacts.getLookupUri(c.getLong(c.getColumnIndex(Contacts._ID)), c.getString(c.getColumnIndex(Contacts.LOOKUP_KEY)));
    	return Contacts.getLookupUri(c.getLong(ContactsQuery.ID), c.getString(LOOKUP_KEY));
    }
    
	
    // An identifier for the loader
    public static final int SINGLE_CONTACT_QUERY = 1;
    public static final int MY_FRIENDS_QUERY_DIALOG = 2;
    public static final int MY_FRIENDS_QUERY = 2;
    public static final int ALL_CONTACTS_QUERY = 3;
    

    // A content URI for the Contacts table
    public static final Uri CONTENT_URI = Contacts.CONTENT_URI;

    // The search/filter query Uri
    public static final Uri FILTER_URI = Contacts.CONTENT_FILTER_URI;

    // The selection clause for the CursorLoader query. The search criteria defined here
    // restrict results to contacts that have a display name and are linked to visible groups.
    // Notice that the search on the string provided by the user is implemented by appending
    // the search string to CONTENT_FILTER_URI.
    public static final String SELECTION = Contacts.DISPLAY_NAME_PRIMARY + "<>''" + " AND " + Contacts.IN_VISIBLE_GROUP + "=1";;

    // The desired sort order for the returned Cursor. In Android 3.0 and later, the primary
    // sort key allows for localization. In earlier versions. use the display name as the sort
    // key.
    public static final String SORT_ORDER =
            Contacts.SORT_KEY_PRIMARY;
    


    // The projection for the CursorLoader query. This is a list of columns that the Contacts
    // Provider should return in the Cursor.
    public static final String[] PROJECTION = {

            // The contact's row id
            Contacts._ID,

            // A pointer to the contact that is guaranteed to be more permanent than _ID. Given
            // a contact's current _ID value and LOOKUP_KEY, the Contacts Provider can generate
            // a "permanent" contact URI.
            Contacts.LOOKUP_KEY,

            // In platform version 3.0 and later, the Contacts table contains
            // DISPLAY_NAME_PRIMARY, which either contains the contact's displayable name or
            // some other useful identifier such as an email address. This column isn't
            // available in earlier versions of Android, so you must use Contacts.DISPLAY_NAME
            // instead.
           Contacts.DISPLAY_NAME_PRIMARY,

            // In Android 3.0 and later, the thumbnail image is pointed to by
            // PHOTO_THUMBNAIL_URI. In earlier versions, there is no direct pointer; instead,
            // you generate the pointer from the contact's ID value and constants defined in
            // android.provider.ContactsContract.Contacts.
            Contacts.PHOTO_THUMBNAIL_URI,

            // The sort order column for the returned Cursor, used by the AlphabetIndexer
            SORT_ORDER,
    };

    // The query column numbers which map to each value in the projection
    public static final int ID = 0;
    public static final int LOOKUP_KEY = 1;
    public static final int DISPLAY_NAME = 2;
    public static final int PHOTO_THUMBNAIL_DATA = 3;
    public static final int SORT_KEY = 4;
}
