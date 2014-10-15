package com.cheatsheet.cheet;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.SearchView;
import android.widget.TextView;

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
            String codeCSS = "<style> .example_code {width:auto;background-color:#ffffff;padding:4px;padding-left:7px;border-left:4px solid #8AC007;font-size:14px;font-family:Consolas,'courier new';border-radius:4px;}.highCOM {color:green;}.highELE {color:brown;}.highATT {color:crimson;}.highVAL {color:mediumblue;}.highLT, .highGT {color:blue;}  </style>";
            String sample = "<div class='example_code notranslate htmlHigh'><span class='highLT'>&lt;</span><span class='highELE'>a</span> <span class='highATT'>href=</span><span class='highVAL'>'http://www.w3schools.com'</span><span class='highGT'>&gt;</span>Visit W3Schools.com!<span class='highLT'>&lt;</span><span class='highELE'>/a</span><span class='highGT'>&gt;</span></div>";
            TextView tag = (TextView) findViewById(R.id.tag);
            TextView definition = (TextView) findViewById(R.id.definition);
            WebView description = (WebView) findViewById(R.id.description);
//            ToggableImageButton bookmark = (ToggableImageButton) findViewById(R.id.btn_bkmrk);

            int tIndex = cursor.getColumnIndexOrThrow(CheatSheetDatabase.KEY_TAG);
            int dfIndex = cursor.getColumnIndexOrThrow(CheatSheetDatabase.KEY_DEFINITION);
            int dsIndex = cursor.getColumnIndexOrThrow(CheatSheetDatabase.KEY_DESCRIPTION);
//            int bIndex = cursor.getColumnIndexOrThrow(CheatSheetDatabase.KEY_BKMRKD);
//            isBookmarked = cursor.getInt(bIndex);
            
//            if(isBookmarked==0)
//            	bookmark.setPressed(false);
//            else
//            	bookmark.setPressed(true);
//            bookmark.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View bkmk) {
//                	bkmk.setPressed(!bkmk.isPressed());    
//                	bookmark.setChecked(!bookmark.isChecked());
//                }
//            });
            
            tag.setText(cursor.getString(tIndex));
            definition.setText(cursor.getString(dfIndex));
            description.loadData(codeCSS + sample +cursor.getString(dsIndex), "text/html", null);
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
            default:
                return false;
        }
    }
}
