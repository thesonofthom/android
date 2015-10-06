package com.thesonofthom.myboardgames.fragments;


import com.thesonofthom.myboardgames.activities.BaseActivity.Navigation;

public class MyGamesFragment extends LocalGamesFragment
{
	private static final String TAG = "MyGamesFragment";
	public MyGamesFragment()
	{
		super(TAG, Navigation.MyGames);
	}
}
