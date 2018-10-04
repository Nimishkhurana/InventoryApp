package com.example.android.inventoryapp;

/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

import static android.support.constraint.Constraints.TAG;


public class ProductCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        ImageView productImage = view.findViewById(R.id.product_image);
        TextView nameTextView = view.findViewById(R.id.product_name);
        TextView categoryTextView = view.findViewById(R.id.product_category_text_view);
        TextView priceTextView = view.findViewById(R.id.product_price);
        TextView quantityTextView = view.findViewById(R.id.product_current_quantity);
        ImageButton sellProductImageButton = view.findViewById(R.id.product_sell_button);


        // Find the columns of pet attributes that we're interested in
        final int productIdColumnIndex = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
        int qtyColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QTY);
        int categoryColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_CATEGORY);
         int photoColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE_URI);

        // Extract out the value from the Cursor for the given column index
        String productName = cursor.getString(nameColumnIndex);
        int categoryCode = cursor.getInt(categoryColumnIndex);
        final int currentQty = cursor.getInt(qtyColumnIndex);
        float price = cursor.getFloat(priceColumnIndex);

        String imageUriString = cursor.getString(photoColumnIndex);
        if (imageUriString != null) {
            Uri productImageUri = Uri.parse(imageUriString);
            productImage.setImageURI(productImageUri);

        } else {
            productImage.setImageResource(R.drawable.no_image);
        }

        // Update the TextViews with the attributes for the current product
        nameTextView.setText(productName);
        categoryTextView.setText(getCategory(context,categoryCode));
        priceTextView.setText(Float.toString(price));
        quantityTextView.setText(Integer.toString(currentQty));

        sellProductImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri productUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, productIdColumnIndex);
                sellProduct(context, productUri, currentQty);
            }
        });
    }

    void sellProduct(Context context, Uri productUri, int currentQuantityInStock){
        int newQuantityValue = (currentQuantityInStock >= 1) ? currentQuantityInStock - 1 : 0;

        if (currentQuantityInStock == 0) {
            Toast.makeText(context.getApplicationContext(), R.string.toast_out_of_stock_msg, Toast.LENGTH_SHORT).show();
        }

        // Update table by using new value of quantity
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductEntry.COLUMN_QTY, newQuantityValue);
        int numRowsUpdated = context.getContentResolver().update(productUri, contentValues, null, null);
        if (numRowsUpdated > 0) {
            // Show error message in Logs with info about pass update.
            Log.i(TAG, context.getString(R.string.buy_msg_confirm));
        } else {
            Toast.makeText(context.getApplicationContext(), R.string.no_product_in_stock, Toast.LENGTH_SHORT).show();
            // Show error message in Logs with info about fail update.
            Log.e(TAG, context.getString(R.string.error_msg_stock_update));
        }
    }

    private String getCategory(Context context,int categoryCode){
        switch (categoryCode){
            case ProductEntry.CATEGORY_UNKNOWN:
                return context.getString(R.string.category_unknown);
            case ProductEntry.CATEGORY_GARMENTS:
                return context.getString(R.string.category_garments);
            case ProductEntry.CATEGORY_COSMETICS:
                return context.getString(R.string.category_cosmetics);
            case ProductEntry.CATEGORY_BAGS:
                return context.getString(R.string.category_bags);
            case ProductEntry.CATEGORY_ELECTRONICS:
                return context.getString(R.string.category_electronics);
            case ProductEntry.CATEGORY_OTHER:
                return context.getString(R.string.category_other);
            default:
                return context.getString(R.string.category_unknown);
        }
    }


}
