package com.example.user.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class InventoryProvider extends ContentProvider{

    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();
    public static final int INVENTORY = 100;
    public static final int INVENTORY_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    InventoryDbHelper mDbHelper = new InventoryDbHelper(getContext());

    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY,INVENTORY);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", INVENTORY_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }
   @Override
   public Cursor query (Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case INVENTORY_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME,projection,selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI" + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
   }

   @Override
    public Uri insert (Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return insertItem (uri,contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
   }
   private Uri insertItem(Uri uri, ContentValues contentValues) {
        String productName = contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        if (productName == null) {
            throw new IllegalArgumentException("Product requires a name");
        }
        double productPrice = contentValues.getAsDouble(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
        if (productPrice <= 0) {
            throw new IllegalArgumentException("Product requires a valid price");
        }
        Integer productQuanitity = contentValues.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        if (productQuanitity != null && productQuanitity <0) {
            throw new IllegalArgumentException("Product requires a valid quantity");
        }
        String supplierName = contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME);
        if (supplierName == null) {
            throw new IllegalArgumentException("Product requires a valid supplier name");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, contentValues);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return ContentUris.withAppendedId(uri, id);
   }

   @Override
    public int update (Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updateProduct(uri,contentValues,selection,selectionArgs);
            case INVENTORY_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri,contentValues,selection,selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
   }
   private int updateProduct (Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
       if (contentValues.size() == 0) {
           return 0;
       }
       String productName = contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
       if (productName == null) {
           throw new IllegalArgumentException("Product requires a name");
       }
       double productPrice = contentValues.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
       if (productPrice <= 0) {
           throw new IllegalArgumentException("Product requires a valid price");
       }
       Integer productQuantity = contentValues.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);
       if (productQuantity != null && productQuantity <0) {
           throw new IllegalArgumentException("Product requires a valid quantity");
       }
       String supplierName = contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME);
       if (supplierName == null) {
           throw new IllegalArgumentException("Product requires a valid supplier name");
       }
       SQLiteDatabase database = mDbHelper.getWritableDatabase();

       int rowsUpdated = database.update(InventoryContract.InventoryEntry.TABLE_NAME, contentValues, selection,selectionArgs);
       if (rowsUpdated != 0) {
           getContext().getContentResolver().notifyChange(uri, null);
       }
       return database.update(InventoryContract.InventoryEntry.TABLE_NAME, contentValues, selection, selectionArgs);
   }
   public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME,selection,selectionArgs);
                break;
            case INVENTORY_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME,selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
   }
   @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryContract.InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryContract.InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match type " + match);
        }
   }
}
