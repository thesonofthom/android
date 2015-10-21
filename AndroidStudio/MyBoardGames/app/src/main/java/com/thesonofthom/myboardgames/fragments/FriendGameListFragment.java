package com.thesonofthom.myboardgames.fragments;

import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.thesonofthom.myboardgames.GameCache;
import com.thesonofthom.myboardgames.GamePool;
import com.thesonofthom.myboardgames.R;
import com.thesonofthom.myboardgames.tools.ContactLoader;
import com.thesonofthom.myboardgames.tools.ContactsQuery;
import com.thesonofthom.myboardgames.tools.Filter;


public class FriendGameListFragment extends LocalGamesFragment
{
	private static final String TAG = "FriendGameListFragment";
	

	private String contact_name;
	private String lookupKey;
	private ContactLoader contactLoader;
	public FriendGameListFragment()
	{
		super(TAG, null);
	}
	
	
	private static final String CONTACT_NAME = "CONTACT_NAME";
	private static final String LOOKUP_KEY = "LOOKUP_KEY";
	
	public static FriendGameListFragment newInstance(Cursor cursor)
	{
		Bundle args = new Bundle();
		args.putString(CONTACT_NAME, cursor.getString(ContactsQuery.DISPLAY_NAME));
		args.putString(LOOKUP_KEY, cursor.getString(ContactsQuery.LOOKUP_KEY));
		FriendGameListFragment f = new FriendGameListFragment();
		f.setArguments(args);
		return f;
	}

	private ListView contactInfoView;
	private View divider;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		boolean mainViewWasNull = (mainView == null);
		super.onCreateView(inflater, container, savedInstanceState);
		if(mainViewWasNull)
		{
			contactLoader = new ContactLoader(a)
			{
				@Override
				public Loader<Cursor> onCreateLoader(int id, Bundle args)
				{
					return ContactsQuery.getContactLoader(a, lookupKey);
				}
				
				@Override
				public int getQueryId()
				{
					return ContactsQuery.SINGLE_CONTACT_QUERY;
				}
			};
			
			contactInfoView = (ListView)mainView.findViewById(R.id.loan_game_contact_info);
			divider = mainView.findViewById(R.id.loan_game_contact_divider);
			contactInfoView.setAdapter(contactLoader.getContactAdapter());
			contactInfoView.setVisibility(View.VISIBLE);
			divider.setVisibility(View.VISIBLE);
		}
		contactLoader.restartLoader();
		a.getActionBar().setHomeButtonEnabled(true);
		getToggle().setDrawerIndicatorEnabled(false);
		return mainView;
	}
	
	@Override
	public void setupEmptyView()
	{
		emptyView_sadFace.setVisibility(View.GONE);
		emptyView_sadFace = null;
		
		emptyView_text1.setText("You currently have no games on loan to " + contact_name +".");
		emptyView_text2.setVisibility(View.GONE);
		emptyView_text2 = null;
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);
		filterItems.setEnabled(false).setVisible(false);
		reloadGamesIcon.setEnabled(false).setVisible(false);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		contact_name = getArguments().getString(CONTACT_NAME);
		lookupKey = getArguments().getString(LOOKUP_KEY);
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public String getTitle()
	{
		return "On Loan to " + contact_name;
	}
	
	@Override
	protected GameCache getCache()
	{
		return GamePool.getInstance().getLoanGameCache(lookupKey);
	}
	
	@Override
	protected Filter getFilter()
	{
		return new Filter(cache);
	}
}
