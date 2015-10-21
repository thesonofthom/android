package com.thesonofthom.myboardgames.fragments;

import java.util.ArrayList;

import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.thesonofthom.myboardgames.GamePool;
import com.thesonofthom.myboardgames.R;
import com.thesonofthom.myboardgames.activities.BaseActivity.Navigation;
import com.thesonofthom.myboardgames.tools.ContactLoader;
import com.thesonofthom.myboardgames.tools.ContactsQuery;
import com.thesonofthom.myboardgames.tools.FragmentTools;

/**
 * Fragment to show a list of the user's "Friends" (i.e. people that at least one game is currently on loan to)
 * @author Kevin Thomson
 *
 */
public class MyFriendsFragment extends BaseFragment
{
	private static final String TAG = "MyFriendsFragment";
	public MyFriendsFragment()
	{
		super(TAG, Navigation.MyFriends);
	}
	
	private View mainView;
	private ListView contactListView;
	private ContactLoader contactLoader;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);
		if(mainView == null)
		{
			mainView = inflater.inflate(R.layout.contact_list, container, false);
			contactListView = (ListView)mainView.findViewById(R.id.contact_list_view);

			contactLoader = new ContactLoader(a)
			{
				
				@Override
				public Loader<Cursor> onCreateLoader(int id, Bundle args)
				{
					return ContactsQuery.getLoanedContactsLoader(a);
				}
				
				@Override
				public int getQueryId()
				{
					return ContactsQuery.MY_FRIENDS_QUERY;
				}
				
				@Override
				public void onLoadFinished(Loader<Cursor> loader, Cursor data)
				{
					super.onLoadFinished(loader, data);
					updateTitle();
				}
			};
			
			contactListView.setOnItemClickListener(new OnItemClickListener()
			{

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id)
				{
					Cursor cursor = contactLoader.getContactAdapter().getCursor();
					cursor.moveToPosition(position);
					FriendGameListFragment f = FriendGameListFragment.newInstance(cursor);
					FragmentTools.transitionToFragment(a, f, null);
				}
			});
			
			contactListView.setAdapter(contactLoader.getContactAdapter());
		}
		contactLoader.restartLoader(true);
		
		return mainView;
	}
	
	
	
	private int getNumberOfFriends()
	{
		ArrayList<String> uniqueIds = GamePool.getInstance().getListOfFriendLookupKeys();
		return uniqueIds.size();
	}
	
	@Override
	public String getTitle()
	{
		String title = super.getTitle();
		title += " (" + getNumberOfFriends() + ")";
		return title;
	}

}
