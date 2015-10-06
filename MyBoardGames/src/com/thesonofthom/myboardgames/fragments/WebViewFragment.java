package com.thesonofthom.myboardgames.fragments;


import com.thesonofthom.myboardgames.R;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class WebViewFragment extends BaseFragment
{
	private static final String TAG = "WebViewFragment";
	
	private View view;
	private WebView webView;
	private String url;
	private String title;

	public WebViewFragment()
	{
		super(TAG, null);
	}
	
	private static final String TITLE = "TITLE";
	public static String URL = "URL";
	public static WebViewFragment newInstance(String title, String url)
	{
		Bundle args = new Bundle();
		args.putString(TITLE, title);
		args.putString(URL, url);
		WebViewFragment f = new WebViewFragment();
		f.setArguments(args);
		return f;
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		title = getArguments().getString(TITLE);
		
	}
	

	
	@Override
	public String getTitle()
	{
		return title;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		a.getActionBar().setHomeButtonEnabled(true);
		getToggle().setDrawerIndicatorEnabled(false);
		if(view == null)
		{
			view = inflater.inflate(R.layout.activity_webview, container, false);
			webView = (WebView)view.findViewById(R.id.webView);
			WebSettings webSettings = webView.getSettings();
			webSettings.setLoadWithOverviewMode(true);
			webSettings.setUseWideViewPort(true);
			webSettings.setBuiltInZoomControls(true);
		}
		return view;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putString(URL, url);
		outState.putString(TITLE, title);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		if(savedInstanceState == null)
		{
			Bundle arguments = getArguments();
			if(arguments != null)
			{
				url = arguments.getString(URL);
				title = arguments.getString(TITLE);
			}
		}
		else
		{
			url = savedInstanceState.getString(URL);
			title = savedInstanceState.getString(TITLE);
		}
		Log.i(TAG, "URL: " + url);
		webView.loadUrl(url);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.action_settings).setVisible(false);
	}

}
