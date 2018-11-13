package com.example.user.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.user.inventory.data.InventoryContract;

public class MainActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<Cursor>{
    //Activity ID for logs
    String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int INVENTORY_LOADER = 0;

    InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView inventoryListView = findViewById(R.id.text_view_inventory);
        View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);

        mCursorAdapter = new InventoryCursorAdapter(this, null);
        inventoryListView.setAdapter(mCursorAdapter);

        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                Uri currentItemUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);

                intent.setData(currentItemUri);

                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    private void insertItem() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME, "Candles");
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE, "14.99");
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY, "50");
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME, "Yankee Candle");
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_PHONE, "2126062332");

        Uri newUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, contentValues);
        if (newUri == null) {
            Toast.makeText(this, R.string.save_error, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.product_saved,Toast.LENGTH_LONG).show();
        }
    }

    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(InventoryContract.InventoryEntry.CONTENT_URI, null, null);
        String rowsString = getString(R.string.rows_deleted);
        Log.v(LOG_TAG, rowsDeleted + rowsString);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Listens for which menu item is selected.
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertItem();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllItems();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String [] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryContract.InventoryEntry.COLUMN_SUPPLIER_PHONE};

        return new CursorLoader(this,
                InventoryContract.InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
