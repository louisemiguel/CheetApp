package com.cheatsheet.cheet;

import java.util.HashSet;
import java.util.Set;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.cheatsheet.cheet.ToggleImageButton.OnCheckedChangeListener;

/**
 * Displays a tag and its definition.
 */
public class TagActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.word);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Uri uri = getIntent().getData();
        @SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(uri, null, null, null, null);

        if (cursor == null) {
            finish();
        } else {
            cursor.moveToFirst();
            
			final SharedPreferences pref = getApplicationContext().getSharedPreferences("cheatsheet_pref", Context.MODE_PRIVATE); 
			final Set<String> bkmrks = pref.getStringSet("bookmarked", new HashSet<String>());
			final Set<String> vstd = pref.getStringSet("visited", new HashSet<String>());
			final SharedPreferences.Editor edit = pref.edit();
			edit.clear();
			
            int tIndex = cursor.getColumnIndexOrThrow(CheatSheetDatabase.KEY_TAG);  
            int dfIndex = cursor.getColumnIndexOrThrow(CheatSheetDatabase.KEY_DEFINITION);
            int dsIndex = cursor.getColumnIndexOrThrow(CheatSheetDatabase.KEY_DESCRIPTION);
            final String tagTxt = cursor.getString(tIndex);
 
            TextView tag = (TextView) findViewById(R.id.tag);
            TextView definition = (TextView) findViewById(R.id.definition);
            WebView description = (WebView) findViewById(R.id.description);
            ToggleImageButton bookmark = (ToggleImageButton) findViewById(R.id.btn_bkmrk);
            
            if(bkmrks.contains(tagTxt))
            	bookmark.setChecked(true);
            			
            String codeCSS = "<style> .example_code {width:auto;background-color:#ffffff;padding:4px;padding-left:7px;border-left:4px solid #8AC007;font-size:14px;font-family:Consolas,'courier new';border-radius:4px;}.highCOM {color:green;}.highELE {color:brown;}.highATT {color:crimson;}.highVAL {color:mediumblue;}.highLT, .highGT {color:blue;}  </style>";
           
            bookmark.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(ToggleImageButton buttonView, boolean isChecked) {
					if(isChecked){
						bkmrks.add(tagTxt);
						edit.putStringSet("bookmarked", bkmrks);
						Toast.makeText(getApplicationContext(), "Bookmarked!", Toast.LENGTH_SHORT).show();
					}
					else{
						bkmrks.remove(tagTxt);
						edit.remove("bookmarked");
						edit.putStringSet("bookmarked", bkmrks);
						Toast.makeText(getApplicationContext(), "Removed from bookmarks!", Toast.LENGTH_SHORT).show();
					}
		            edit.apply();
		            edit.commit();
				}
			});
            
            tag.setText(tagTxt);
            definition.setText(cursor.getString(dfIndex));
            description.loadData(codeCSS+cursor.getString(dsIndex), "text/html", null);
            vstd.add(tagTxt);
			edit.putStringSet("visited", vstd); 
            edit.apply();
            edit.commit();
        }
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
            case android.R.id.home:
                Intent intent = new Intent(this, SearchableCheatSheet.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.clr_bkmrks:
            	return clearPref(0);
            case R.id.clr_hstry:
            	return clearPref(1);
            case R.id.exit:
            	finish(); 
            	return true;
            default:
                return false;
        }
    }
    
    public boolean clearPref(int ch){
		SharedPreferences pref = getApplicationContext().getSharedPreferences("cheatsheet_pref", Context.MODE_PRIVATE); 
		SharedPreferences.Editor edit = pref.edit();
		edit.clear();
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
}
