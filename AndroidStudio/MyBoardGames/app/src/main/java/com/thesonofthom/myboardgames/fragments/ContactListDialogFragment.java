package com.thesonofthom.myboardgames.fragments;

import com.thesonofthom.myboardgames.R;
import com.thesonofthom.myboardgames.activities.MainActivity;
import com.thesonofthom.myboardgames.tools.ContactLoader;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SearchView;

/**
 * Base class for all fragments that display a list of contacts
 * 
 * @author Kevin Thomson
 *
 */
public abstract class ContactListDialogFragment extends DialogFragment
{
	private String TAG = "ContactListDialogFragment";

	protected ContactLoader loader;
	
	protected ContactListDialogListener listener;
	
	protected ListView contactListView;
	protected SearchView contactSearchView;
    // Stores the previously selected search item so that on a configuration change the same item
    // can be reselected again
    private int mPreviouslySelectedSearchItem = 0;
	
	protected Dialog dialog;
	

	
	public ContactListDialogFragment(String tag)
	{
		TAG = tag;
	}
	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		final MainActivity activity = (MainActivity)getActivity();
		loader = new ContactLoader(activity)
		{
			
			@Override
			public Loader<Cursor> onCreateLoader(int id, Bundle args)
			{
				return getCursorLoader();
			}
			
			@Override
			public int getQueryId()
			{
				return ContactListDialogFragment.this.getQueryId();
			}
		};

		listener = (ContactListDialogListener)getTargetFragment();
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
		.setTitle("Loan game to...");
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.contact_list, null);
		
		contactListView = (ListView)view.findViewById(R.id.contact_list_view);
		contactListView.setAdapter(loader.getContactAdapter());
		contactListView.setFastScrollEnabled(true);
		contactListView.setFastScrollAlwaysVisible(true);
		contactListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause image loader to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                	activity.getContactThumbnailImageLoader().setPauseWork(true);
                } else {
                	activity.getContactThumbnailImageLoader().setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {}
        });
		
		contactListView.setOnItemClickListener(new OnItemClickListener() {
        	
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) 
			{
				Cursor cursor = loader.getContactAdapter().getCursor();
				cursor.moveToPosition(position);
				listener.contactChosen(cursor);
				dialog.dismiss();
			}
		});		
		
		contactSearchView = (SearchView)view.findViewById(R.id.contact_list_search);
		
		builder.setView(view);
		createDialog(builder);
		if (mPreviouslySelectedSearchItem == 0) 
		{
			Log.i(TAG, "Inititializing loader...");
			loader.restartLoader();
		}
		
		dialog = builder.create();
		
		return dialog;
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
	
	public abstract int getQueryId();
	
	public abstract void createDialog(AlertDialog.Builder builder);
	
    @Override
    public void onPause() {
        super.onPause();

        // In the case onPause() is called during a fling the image loader is
        // un-paused to let any remaining background work complete.
        final MainActivity activity = (MainActivity)getActivity();
        activity.getContactThumbnailImageLoader().setPauseWork(false);
    }

	
	public abstract CursorLoader getCursorLoader();

//	@Override
//	public void onLoadFinished(Loader<Cursor> loader, Cursor data)
//	{
//		Log.i(TAG, "onLoadFinished(): data=" + data);
//        if (loader.getId() == getQueryId()) {
//            adapter.swapCursor(data);
//        }
//	}
//
//	@Override
//	public void onLoaderReset(Loader<Cursor> loader)
//	{
//		Log.i(TAG, "onLoaderReset(): loader=" + loader);
//        if (loader.getId() == getQueryId()) {
//            // When the loader is being reset, clear the cursor from the adapter. This allows the
//            // cursor resources to be freed.
//            adapter.swapCursor(null);
//        }
//		
//	}
	
	public interface ContactListDialogListener
	{
		public void contactChosen(Cursor c);
	}
}
