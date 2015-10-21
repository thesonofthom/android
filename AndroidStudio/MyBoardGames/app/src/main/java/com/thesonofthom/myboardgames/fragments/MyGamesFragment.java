package com.thesonofthom.myboardgames.fragments;


import com.thesonofthom.myboardgames.activities.BaseActivity.Navigation;

/**
 * Fragment to display the list of games that the user currently owns and are not on loan to another person
 * @author Kevin Thomson
 *
 */
public class MyGamesFragment extends LocalGamesFragment
{
	private static final String TAG = "MyGamesFragment";
	public MyGamesFragment()
	{
		super(TAG, Navigation.MyGames);
	}
}
