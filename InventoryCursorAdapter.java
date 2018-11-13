package com.example.user.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.inventory.data.InventoryContract;
import com.example.user.inventory.data.InventoryDbHelper;

import static java.lang.String.format;
import static java.lang.String.valueOf;

public class InventoryCursorAdapter extends CursorAdapter{

    public  InventoryCursorAdapter (Context context, Cursor c) {
        super(context,c,0);}



    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false );
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        final TextView productNameTextView = view.findViewById(R.id.product_name_list);
        TextView productPriceTextView = view.findViewById(R.id.price_list);
        final TextView productQtyTextView = view.findViewById(R.id.qty_list);

        final int idColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry._ID);
        int productNameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        final int productPriceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
        int productQtyColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        int supplierNameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME);
        int supplierPhoneColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_PHONE);

        final String id = cursor.getString(idColumnIndex);
        final String productName = cursor.getString(productNameColumnIndex);
        final String productPrice = cursor.getString(productPriceColumnIndex);
        final String productQuantity1 = cursor.getString(productQtyColumnIndex);
        final String supplierName = cursor.getString(supplierNameColumnIndex);
        final String supplierPhone = cursor.getString(supplierPhoneColumnIndex);

        productNameTextView.setText(productName);
        productPriceTextView.setText(productPrice);
        productQtyTextView.setText(productQuantity1);

        Button saleButton = view.findViewById(R.id.sale_button);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int qtyIndexInt = Integer.parseInt(productQuantity1);
                InventoryDbHelper mDbHelper = new InventoryDbHelper(context);
                SQLiteDatabase database = mDbHelper.getReadableDatabase();
                qtyIndexInt = qtyIndexInt -1;
                if (qtyIndexInt < 0) {
                    Toast toast = Toast.makeText(view.getContext(),R.string.negative_quantity,Toast.LENGTH_LONG);
                    toast.show();
                }
                productQtyTextView.setText(valueOf(qtyIndexInt));

                ContentValues contentValues = new ContentValues();
                contentValues.getAsString(productName); //App crashes here with Error: Product requires a name
                contentValues.getAsString(productPrice);
                contentValues.getAsString(productQtyTextView.getText().toString());
                contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME);
                contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_PHONE);

                Uri newUri = context.getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, contentValues);
                Uri currentItemUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, idColumnIndex);
                context.getContentResolver().notifyChange(newUri, null);
                String selection = InventoryContract.InventoryEntry._ID + "=?";
                String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(currentItemUri))};
                database.update(InventoryContract.InventoryEntry.TABLE_NAME, contentValues, selection, selectionArgs);


            }
        });


    }

}

