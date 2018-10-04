package com.example.android.inventoryapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>  {

    private static final int EXISTING_PRODUCT_LOADER = 0;
    public static final String LOG_TAG = EditorActivity.class.getSimpleName();

    Uri mCurrentProductUri;
    Uri mImageUri;

    private int mQty;
    private TextView mPhotoHintText;
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private Spinner mCategorySpinner;
    private TextView mQtyTextView;
    private EditText mSupplierEditText;
    private EditText mSupplierPhoneEditText;
    private ImageView mProductImageSelector;
    private Button mIncrementQtyButton;
    private Button mDecrementQtyButton;
    private Button mOrderMoreButton;

    private boolean mProductHasChanged = false;

    private int mCategory = ProductEntry.CATEGORY_UNKNOWN;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mProductHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.edit_product_name);
        mPriceEditText = findViewById(R.id.edit_product_price);
        mCategorySpinner = findViewById(R.id.spinner_category);
        mQtyTextView = findViewById(R.id.quantity_text_view);
        mSupplierEditText = findViewById(R.id.edit_supplier_name);
        mSupplierPhoneEditText = findViewById(R.id.edit_supplier_phone);
        mProductImageSelector = findViewById(R.id.edit_product_photo);
        mPhotoHintText = findViewById(R.id.add_or_edit_photo_hint);
        mIncrementQtyButton = findViewById(R.id.increment_qty_button);
        mDecrementQtyButton = findViewById(R.id.decrement_qty_button);
        mOrderMoreButton = findViewById(R.id.order_more_button);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        if(mCurrentProductUri == null){
            setTitle(R.string.editor_activity_title_new_product);
            mPhotoHintText.setText(R.string.add_photo_hint_text);
            mProductImageSelector.setImageResource(R.drawable.ic_empty_storehouse);
            invalidateOptionsMenu();
        }
        else{
            setTitle(R.string.editor_activity_title_edit_product);
            mPhotoHintText.setText(R.string.edit_photo_hint_text);
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mProductImageSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trySelector();
                mProductHasChanged = true;
            }
        });

        mIncrementQtyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incrementQtyButton(view);
            }
        });

        mDecrementQtyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decrementQtyutton(view);
            }
        });

        mOrderMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderMore();
            }
        });


        mIncrementQtyButton.setOnTouchListener(mTouchListener);
        mDecrementQtyButton.setOnTouchListener(mTouchListener);
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mCategorySpinner.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);

        setupSpinner();

    }

    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter gradeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_category_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        gradeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mCategorySpinner.setAdapter(gradeSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.category_unknown))) {
                        mCategory = ProductEntry.CATEGORY_UNKNOWN;
                    }else if (selection.equals(getString(R.string.category_garments))) {
                        mCategory = ProductEntry.CATEGORY_GARMENTS;
                    } else if (selection.equals(getString(R.string.category_cosmetics))) {
                        mCategory = ProductEntry.CATEGORY_COSMETICS;
                    }else if(selection.equals(getString(R.string.category_bags))) {
                        mCategory = ProductEntry.CATEGORY_BAGS;
                    }else if(selection.equals(getString(R.string.category_electronics))){
                        mCategory = ProductEntry.CATEGORY_ELECTRONICS;
                    } else{
                        mCategory = ProductEntry.CATEGORY_OTHER;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCategory = ProductEntry.CATEGORY_UNKNOWN;
            }
        });
    }


    public void trySelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }
        openSelector();
    }

    private void openSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType(getString(R.string.intent_type));
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openSelector();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mImageUri = data.getData();
                mProductImageSelector.setImageURI(null);
                mProductImageSelector.setImageURI(mImageUri);
                mProductImageSelector.invalidate();
            }
        }
    }

    private boolean saveProduct() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        boolean hasAllRequiredValues = true;

        String nameString = mNameEditText.getText().toString().trim();
        String price = mPriceEditText.getText().toString().trim();
        String qty = mQtyTextView.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String supplierPhoneString = mSupplierPhoneEditText.getText().toString().trim();

        if(mCurrentProductUri == null && TextUtils.isEmpty(nameString) || TextUtils.isEmpty(price) || TextUtils.isEmpty(qty) ||
                TextUtils.isEmpty(supplierString) || TextUtils.isEmpty(supplierPhoneString) ||
                mCategory == ProductEntry.CATEGORY_UNKNOWN || mImageUri == null) {

            Toast.makeText(this, "No null values are accepted",Toast.LENGTH_SHORT).show();
            hasAllRequiredValues = false;
            return hasAllRequiredValues;
        }

        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRICE, price );
        values.put(ProductEntry.COLUMN_QTY,qty);
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, supplierString);
        values.put(ProductEntry.COLUMN_SUPPLIER_PHONE_NO, supplierPhoneString);
        values.put(ProductEntry.COLUMN_PRODUCT_CATEGORY,mCategory);

        if(mCurrentProductUri == null){
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        else {
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        return hasAllRequiredValues;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to database
                boolean success = saveProduct();
                // Exit activity
                if(success == true) finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                if(!mProductHasChanged){
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(this,   // Parent activity context
                 mCurrentProductUri,   // Provider content URI to query
                null,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
            int qtyColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QTY);
            int supplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_PHONE_NO);
            int photoColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE_URI);
            int categoryColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_CATEGORY);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            mQty = cursor.getInt(qtyColumnIndex);
            float price = cursor.getFloat(priceColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);
            String imageUriString = cursor.getString(photoColumnIndex);
            int category = cursor.getInt(categoryColumnIndex);

            if(imageUriString!=null){
                Uri productImageUri = Uri.parse(imageUriString);
                mProductImageSelector.setImageURI(productImageUri);
                mProductImageSelector.invalidate();

            }
            else{
                mProductImageSelector.setImageResource(R.drawable.ic_empty_storehouse);
            }

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(String.valueOf(price));
            mQtyTextView.setText(String.valueOf(mQty));
            mSupplierEditText.setText(supplierName);
            mSupplierPhoneEditText.setText(supplierPhone);

            switch (category){
                case ProductEntry.CATEGORY_GARMENTS:
                    mCategorySpinner.setSelection(1);
                    break;
                case ProductEntry.CATEGORY_COSMETICS:
                    mCategorySpinner.setSelection(2);
                    break;
                case ProductEntry.CATEGORY_BAGS:
                    mCategorySpinner.setSelection(3);
                    break;
                case ProductEntry.CATEGORY_ELECTRONICS:
                    mCategorySpinner.setSelection(4);
                    break;
                case ProductEntry.CATEGORY_OTHER:
                    mCategorySpinner.setSelection(5);
                    break;
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCategorySpinner.setSelection(0);
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQtyTextView.setText("");
        mSupplierEditText.setText("");
        mSupplierPhoneEditText.setText("");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    public void incrementQtyButton(View view) {
        mQty++;
        displayQuantity();
    }

    public void decrementQtyutton(View view) {
        if (mQty == 0) {
            Toast.makeText(this, "Can't decrease quantity", Toast.LENGTH_SHORT).show();
        } else {
            mQty--;
            displayQuantity();
        }
    }

    public void displayQuantity() {
        mQtyTextView.setText(String.valueOf(mQty));
    }

    public void orderMore() {
        String phoneNumber = String.format("tel: %s", mSupplierPhoneEditText.getText().toString());
        Intent intent = new Intent(android.content.Intent.ACTION_DIAL);
        intent.setData(Uri.parse(phoneNumber));

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.e(LOG_TAG, "Can't resolve app for ACTION_DIAL Intent.");
        }

    }
}