package com.thesonofthom.myboardgames.fragments;

import com.thesonofthom.myboardgames.tools.ContactsQuery;

import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;


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
		// TODO Auto-generated method stub
		return ContactsQuery.MY_FRIENDS_QUERY_DIALOG;
	}

}
