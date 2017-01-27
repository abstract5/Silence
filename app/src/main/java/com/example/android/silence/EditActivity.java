package com.example.android.silence;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.silence.data.SilentContract.SilentEntry;

public class EditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private EditText mEditName;
    private EditText mEditAddress;
    private TextView mEditRadius;
    private Uri mCurrentLocaleUri;
    private boolean mLocaleHasChanged;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mLocaleHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Intent intent = getIntent();
        mCurrentLocaleUri = intent.getData();

        if(mCurrentLocaleUri == null){
            setTitle("Add Location");
            invalidateOptionsMenu();
        }else{
            setTitle("Edit Location");
            getLoaderManager().initLoader(0, null, this);
        }

        mEditName = (EditText) findViewById(R.id.edit_name);
        mEditAddress = (EditText) findViewById(R.id.edit_address);
        mEditRadius = (TextView) findViewById(R.id.edit_radius);

        mEditName.setOnTouchListener(mTouchListener);
        mEditAddress.setOnTouchListener(mTouchListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);

        if(mCurrentLocaleUri == null){
            MenuItem menuItem = menu.findItem(R.id.action_delete_item);
            menuItem.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_save:
                return true;
            case R.id.action_delete_item:
                return true;
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                SilentEntry._ID,
                SilentEntry.COLUMN_LOCALE_NAME,
                SilentEntry.COLUMN_LOCALE_ADDRESS,
                SilentEntry.COLUMN_LOCALE_RADIUS,
                SilentEntry.COLUMN_LOCALE_LONGITUDE,
                SilentEntry.COLUMN_LOCALE_LATITUDE
        };

        return new CursorLoader(this,
                mCurrentLocaleUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.moveToFirst()){
            int nameIndex = data.getColumnIndex(SilentEntry.COLUMN_LOCALE_NAME);
            int addressIndex = data.getColumnIndex(SilentEntry.COLUMN_LOCALE_ADDRESS);
            int radiusIndex = data.getColumnIndex(SilentEntry.COLUMN_LOCALE_RADIUS);

            String name = data.getString(nameIndex);
            String address = data.getString(addressIndex);
            int radius = data.getInt(radiusIndex);

            mEditName.setText(name);
            mEditAddress.setText(address);
            mEditRadius.setText(Integer.toString(radius));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mEditName.setText("");
        mEditAddress.setText("");
        mEditRadius.setText("0");
    }
}
