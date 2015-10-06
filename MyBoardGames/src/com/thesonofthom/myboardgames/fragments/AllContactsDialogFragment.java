package com.thesonofthom.myboardgames.fragments;

import com.thesonofthom.myboardgames.tools.ContactsQuery;
import android.app.Fragment;
import android.app.SearchManager;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.CursorLoader;
import android.text.TextUtils;
import android.view.View;
import android.widget.SearchView;

public class AllContactsDialogFragment extends ContactListDialogFragment
{
	private static final String TAG = "AllContactsDialogFragment";
	
	private String mSearchTerm;
	private boolean mSearchQueryChanged;
	
	public AllContactsDialogFragment()
	{
		super(TAG);
	}

	public static AllContactsDialogFragment newInstance(ContactListDialogListener listener)
	{
		AllContactsDialogFragment fragment = new AllContactsDialogFragment();
		fragment.setTargetFragment((Fragment)listener, 0);
		return fragment;
	}
	

	@Override
	public void createDialog(Builder builder)
	{
		//set up search view
		contactSearchView.setVisibility(View.VISIBLE);
		contactSearchView.setIconified(false);
        final SearchManager searchManager =
                (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        contactSearchView.setSearchableInfo(
                searchManager.getSearchableInfo(getActivity().getComponentName()));
        loader.destroyLoaderOnRestart(false);
        contactSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			
			@Override
			public boolean onQueryTextSubmit(String query)
			{
				// TODO Auto-generated method stub
				return true;
			}
			
			@Override
			public boolean onQueryTextChange(String newText)
			{
				// TODO Auto-generated method stub
				String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
                if (mSearchTerm == null && newFilter == null) {
                    return true;
                }
                // Don't do anything if the new filter is the same as the current filter
                if (mSearchTerm != null && mSearchTerm.equals(newFilter)) {
                    return true;
                }
                
             // Updates current filter to new filter
                mSearchTerm = newFilter;
                
                mSearchQueryChanged = true;
                loader.restartLoader(true);
                return true;
			}
		});
        
	}

	@Override
	public CursorLoader getCursorLoader()
	{
		// TODO Auto-generated method stub
		if (mSearchTerm == null) 
		{
			return ContactsQuery.getAllContactsLoader(getActivity());
		}
		else
		{
			return ContactsQuery.getContactSearchQueryLoader(getActivity(), mSearchTerm);
		}
		
	}

	@Override
	public int getQueryId()
	{
		return ContactsQuery.ALL_CONTACTS_QUERY;
	}

}
