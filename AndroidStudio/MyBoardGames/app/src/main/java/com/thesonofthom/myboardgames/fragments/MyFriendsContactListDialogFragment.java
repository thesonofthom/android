package com.thesonofthom.myboardgames.fragments;

import com.thesonofthom.myboardgames.tools.ContactsQuery;

import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;

/**
 * Fragment to shows a subset of the users full contact list. The subset contains only contacts that currently have a game lent to them.
 * The user is more likely to lend a game to someone in this list, so display it first before displaying the full contact list
 * @author Kevin Thomson
 *
 */
public class MyFriendsContactListDialogFragment extends ContactListDialogFragment
{
	private static final String TAG = "MyFriendsContactListDialogFragment";
	public MyFriendsContactListDialogFragment()
	{
		super(TAG);
	}

	public static MyFriendsContactListDialogFragment newInstance(ContactListDialogListener listener)
	{
		MyFriendsContactListDialogFragment fragment = new MyFriendsContactListDialogFragment();
		fragment.setTargetFragment((Fragment)listener, 0);
		return fragment;
	}

	@Override
	public void createDialog(Builder builder)
	{
		builder.setPositiveButton("Browse all contacts", new OnClickListener()
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				AllContactsDialogFragment fragment = AllContactsDialogFragment.newInstance(listener);
				fragment.show(getFragmentManager(), null);
			}
		});
		contactSearchView.setVisibility(View.GONE);
		
	}

	@Override
	public CursorLoader getCursorLoader()
	{
		return ContactsQuery.getLoanedContactsLoader(getActivity());//
		
	}

	@Override
	public int getQueryId()
	{
		return ContactsQuery.MY_FRIENDS_QUERY_DIALOG;
	}

}
