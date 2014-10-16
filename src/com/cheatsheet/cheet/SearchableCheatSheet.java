package com.cheatsheet.cheet;

import java.util.HashSet;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The main activity for the dictionary.
 * Displays search results triggered by the search dialog and handles
 * actions from search suggestions.
 */
public class SearchableCheatSheet extends FragmentActivity implements TabListener {

    private TextView mTextView;
    private ListView mListView;
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mTextView = (TextView) findViewById(R.id.text);
        mListView = (ListView) findViewById(R.id.list);
        
        handleIntent(getIntent());
        
        // Set up the action bar to show tabs.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // for each of the sections in the app, add a tab to the action bar.
        actionBar.addTab(actionBar.newTab().setText("HTML").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText("CSS").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText("PHP").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText("BOOKMARKS").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText("HISTORY").setTabListener(this));
        
        mListView.setOnTouchListener(new OnSwipeTouchListener(this) {
			@Override
			public void onSwipeRight() {
				super.onSwipeRight();
				int prevTab = actionBar.getSelectedNavigationIndex()-1;
				if(prevTab==-1)
					prevTab = actionBar.getTabCount()-1;
				actionBar.setSelectedNavigationItem(prevTab);
			}
			@Override
			public void onSwipeLeft() {
				super.onSwipeLeft();
				int nextTab = actionBar.getSelectedNavigationIndex()+1;
				if(nextTab > actionBar.getTabCount()-1)
					nextTab = 0;
				actionBar.setSelectedNavigationItem(nextTab);
			}
		});
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // Because this activity has set launchMode="singleTop", the system calls this method
        // to deliver the intent if this activity is currently the foreground activity when
        // invoked again (when the user executes a search from this activity, we don't create
        // a new instance of this activity, so the system delivers the search intent here)
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // handles a click on a search suggestion; launches activity to show word
            Intent wordIntent = new Intent(this, TagActivity.class);
            wordIntent.setData(intent.getData());
            startActivity(wordIntent);
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // handles a search query
            String query = intent.getStringExtra(SearchManager.QUERY);
            showResults(query);
        }
    }

    /**
     * Searches the dictionary and displays results for the given query.
     * @param query The search query
     */
    private void showResults(String query) {
        @SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(CheatSheetProvider.CONTENT_URI, null, null,
                                new String[] {query}, null);
        
        if (cursor == null) {
            // There are no results
    		mTextView.setText(getString(R.string.no_results, new Object[] {query}));
        } 
        else 
        {	
            // Display the number of results
            int count = cursor.getCount();
            String countString = getResources().getQuantityString(R.plurals.search_results,
                                    count, new Object[] {count, query});
            mTextView.setText(countString);
        }	
            // Specify the columns we want to display in the result
            String[] from = new String[] { CheatSheetDatabase.KEY_TAG,
                                           CheatSheetDatabase.KEY_DEFINITION };

            // Specify the corresponding layout elements where we want the columns to go
            int[] to = new int[] { R.id.tag,
                                   R.id.definition };

            // Create a simple cursor adapter for the definitions and apply them to the ListView
            @SuppressWarnings("deprecation")
			SimpleCursorAdapter tags = new SimpleCursorAdapter(this,R.layout.result, cursor, from, to);
            mListView.setAdapter(tags);

            // Define the on-click listener for the list items
            mListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Build the Intent used to open WordActivity with a specific word Uri
                    Intent tagIntent = new Intent(getApplicationContext(), TagActivity.class);
                    Uri data = Uri.withAppendedPath(CheatSheetProvider.CONTENT_URI,
                                                    String.valueOf(id));
                    tagIntent.setData(data);
                    startActivity(tagIntent);
                }
            });
            
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                onSearchRequested();
                return true;
            case R.id.clr_bkmrks:
            	if(clearPref(0)){
	            	if((getActionBar().getSelectedTab().getText())=="BOOKMARKS")
	    				showResults("BOOKMARKS");
            	}
            	return true;
            case R.id.clr_hstry:
            	if(clearPref(1)){
	    			if((getActionBar().getSelectedTab().getText())=="HISTORY")
	    				showResults("HISTORY");
            	}
            	return true;
            case R.id.exit:
            	finish();
            	return true;
            default:
                return false;
        }
    }
    
    public boolean clearPref(int ch){
		SharedPreferences pref = getApplicationContext().getSharedPreferences("cheatsheet_pref", Context.MODE_MULTI_PROCESS); 
		SharedPreferences.Editor edit = pref.edit();
		if(ch==0){
			edit.putStringSet("bookmarked", new HashSet<String>());
			Toast.makeText(getApplicationContext(), "Bookmarks cleared!", Toast.LENGTH_SHORT).show();
		}
		else if(ch==1){
			edit.putStringSet("visited", new HashSet<String>());
			Toast.makeText(getApplicationContext(), "History cleared!", Toast.LENGTH_SHORT).show();
		}
		return edit.commit();	
    }

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
		
		if (tab.getText() == "HTML") {
			showResults("[HTML]");
		}
		else if (tab.getText() == "CSS") {
			showResults("[CSS]");
		}           
		else if (tab.getText() == "PHP") { 
			showResults("[PHP]");
		}
		else if (tab.getText() == "BOOKMARKS") {
			showResults("BOOKMARKS");
		}           
		else if (tab.getText() == "HISTORY") { 
			showResults("HISTORY");
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {

	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {

	}
	
	@Override
	  public void onRestoreInstanceState(Bundle savedInstanceState) {
	    // Restore the previously serialized current tab position.
	    if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
	      getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
	    }
	  }
	@Override
	  public void onSaveInstanceState(Bundle outState) {
	    // Serialize the current tab position.
	    outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
	        .getSelectedNavigationIndex());
	  }
	
}
