package com.example.user.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.user.inventory.data.InventoryContract;

public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int EXISTING_ITEM_LOADER = 0;

    private Uri mCurrentItemUri;

    /**
     * EditText fields to enter product and supplier details
     */
    private EditText mProductNameEditText;
    private EditText mProductPriceEditText;
    private EditText mProductQuantityEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierPhoneEditText;

    private boolean mItemHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

//        Finds relevant view and assigns to xml views

        mProductNameEditText = findViewById(R.id.edit_product_name);
        mProductPriceEditText = findViewById(R.id.edit_product_price);
        mProductQuantityEditText = findViewById(R.id.edit_product_quantity);
        mSupplierNameEditText = findViewById(R.id.edit_supplier_name);
        mSupplierPhoneEditText = findViewById(R.id.edit_supplier_phone);

        mProductNameEditText.setOnTouchListener(mTouchListener);
        mProductPriceEditText.setOnTouchListener(mTouchListener);
        mProductQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();
        View incrementQtyView = findViewById(R.id.increment_qty_view);
        View decrementQtyView = findViewById(R.id.decrement_qty_view);

        if (mCurrentItemUri == null) {
            setTitle (R.string.add_item);
            invalidateOptionsMenu();
            incrementQtyView.setVisibility(View.INVISIBLE);
            decrementQtyView.setVisibility(View.INVISIBLE);
         } else {
            setTitle(R.string.edit_item);
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER,null,this);
            incrementQtyView.setVisibility(View.VISIBLE);
            decrementQtyView.setVisibility(View.VISIBLE);
        }

        Button increment = findViewById(R.id.increment_qty_view);
        increment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String productQuantityString = mProductQuantityEditText.getText().toString();
                int productQuantity = Integer.parseInt(productQuantityString);
                productQuantity = productQuantity + 1;
                productQuantityString = String.valueOf(productQuantity);
                mProductQuantityEditText.setText(productQuantityString);
            }
        });



        Button decrement = findViewById(R.id.decrement_qty_view);
        decrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String productQuantityString = mProductQuantityEditText.getText().toString();
                int productQuantity = Integer.parseInt(productQuantityString);
                productQuantity = productQuantity - 1;
                if (productQuantity <0) {
                    Toast.makeText(EditorActivity.this, R.string.negative_quantity, Toast.LENGTH_LONG).show();
                    productQuantity = 0;
                }
                    productQuantityString = String.valueOf(productQuantity);
                    mProductQuantityEditText.setText(productQuantityString);

            }

        });
    }


    private void saveProduct() {
        String productNameString = mProductNameEditText.getText().toString().trim();
        String productPriceString = mProductPriceEditText.getText().toString().trim();
        String productQtyString = mProductQuantityEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneString1 = mSupplierPhoneEditText.getText().toString().trim();
        String supplierPhoneString = supplierPhoneString1.replaceAll("[^\\d.]", "");

        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(productNameString) &&
                TextUtils.isEmpty(productPriceString) &&
                TextUtils.isEmpty(productQtyString) &&
                TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierPhoneString)) {

            return;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME, productNameString);
        double productPriceDouble = 0.00;
        if (!TextUtils.isEmpty(productPriceString)){
            productPriceDouble = Double.valueOf(productPriceString);
        }
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE, productPriceDouble);
        int productQtyInt = 0;
        if (!TextUtils.isEmpty(productQtyString)){
            productQtyInt = Integer.parseInt(productQtyString);
        }
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY, productQtyInt);
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_PHONE, supplierPhoneString);

        if (mCurrentItemUri == null) {
            Uri newUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, contentValues);
            if (newUri == null) {
                Toast.makeText(this, R.string.save_error, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.product_saved, Toast.LENGTH_LONG).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentItemUri, contentValues, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, R.string.save_error, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.product_saved, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
            MenuItem menuItem1 = menu.findItem(R.id.action_order);
            menuItem1.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String phone = "tel:" + mSupplierPhoneEditText.getText().toString().trim();
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_order:
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(phone));
                startActivity(intent);
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        finish();
                    }
                };
        showUnsavedChangedDialog(discardButtonClickListener);
    }

    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {
        String [] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryContract.InventoryEntry.COLUMN_SUPPLIER_PHONE};
        return new CursorLoader(this,
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int productNameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
            int productPriceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
            int productQtyColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_PHONE);

            String productName = cursor.getString(productNameColumnIndex);
            double productPrice = cursor.getDouble(productPriceColumnIndex);
            int productQuantity = cursor.getInt(productQtyColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);


            String[] digits = supplierPhone.split("");
            String displayPhoneArea = digits[1] + digits[2] + digits[3];
            String displayPhonePrefix = digits[4] + digits[5] + digits[6];
            String displayPhoneSuffix = digits[7] + digits [8] + digits [9] + digits[10];
            String displayPhone = "(" + displayPhoneArea  + ") "
                    + displayPhonePrefix + " - "
                    + displayPhoneSuffix ;

            mProductNameEditText.setText(productName);
            mProductPriceEditText.setText(Double.toString(productPrice));
            mProductQuantityEditText.setText(Integer.toString(productQuantity));
            mSupplierNameEditText.setText(supplierName);
            mSupplierPhoneEditText.setText(displayPhone);
        }
    }




    @Override
    public void onLoaderReset(Loader loader) {
        mProductNameEditText.setText("");
        mProductPriceEditText.setText("");
        mProductQuantityEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierPhoneEditText.setText("");
    }

    private void showUnsavedChangedDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes);
        builder.setPositiveButton(R.string.discard,discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_delete);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        }

        private void deleteItem() {
        if (mCurrentItemUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this,R.string.delete_error, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.product_deleted, Toast.LENGTH_LONG).show();
            }
        }
        finish();
    }
}
