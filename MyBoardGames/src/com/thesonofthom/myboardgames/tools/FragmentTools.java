package com.thesonofthom.myboardgames.tools;

import com.thesonofthom.myboardgames.R;
import com.thesonofthom.myboardgames.activities.MainActivity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

public class FragmentTools
{
	
	public static void transitionToFragment(MainActivity activity, Fragment fragment, String tag)
	{
	    FragmentManager fragmentManager = activity.getFragmentManager();
	    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
	    fragmentTransaction.replace(R.id.content_frame, fragment, tag);
	    fragmentTransaction.addToBackStack(null);
	    fragmentTransaction.commit();
	    activity.setCorrectNavigationListSelection(fragment);
	}
}
