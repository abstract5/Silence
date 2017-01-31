package com.example.android.silence;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
                saveLocation();
                finish();
                return true;
            case R.id.action_delete_item:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if(!mLocaleHasChanged){
                    NavUtils.navigateUpFromSameTask(EditActivity.this);
                    return true;
                }

                showUnsavedChangesDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveLocation(){
        ContentValues values = new ContentValues();

        String name = mEditName.getText().toString().trim();
        String address = mEditAddress.getText().toString().trim();
        String radiusText = mEditRadius.getText().toString().trim();
        int radius = Integer.parseInt(radiusText);

        if(TextUtils.isEmpty(name) && TextUtils.isEmpty(address)){
            return;
        }

        values.put(SilentEntry.COLUMN_LOCALE_NAME, name);
        values.put(SilentEntry.COLUMN_LOCALE_ADDRESS, address);
        values.put(SilentEntry.COLUMN_LOCALE_RADIUS, radius);

        if(mCurrentLocaleUri == null){
            getContentResolver().insert(SilentEntry.CONTENT_URI, values);
        }else{
            int rowUpdated = getContentResolver().update(mCurrentLocaleUri, values, null, null);

            if(rowUpdated == 0){
                Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Location Saved", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void deleteLocale(){
        int rowDeleted = getContentResolver().delete(mCurrentLocaleUri, null, null);

        if(rowDeleted == 0){
            Toast.makeText(this, "Deletion failed", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Location Deleted", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    public void showDeleteConfirmationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this Location?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteLocale();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void showUnsavedChangesDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard changes and quit editing?");
        builder.setPositiveButton("Discard", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NavUtils.navigateUpFromSameTask(EditActivity.this);
            }
        });
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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

            if(address.equals(null) || address.equals("")){
                address = SilentEntry.getAddress(this, data);
            }
            if(address.equals(null) || address.equals("")){
                address = "No address available";
            }
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
