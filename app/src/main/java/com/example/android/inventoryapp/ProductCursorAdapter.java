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

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;


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
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        ImageView productImage = view.findViewById(R.id.product_image);
        TextView nameTextView = view.findViewById(R.id.product_name);
        TextView supplierTextView = view.findViewById(R.id.supplier_name);
        TextView priceTextView = view.findViewById(R.id.product_price);
        TextView quantityTextView = view.findViewById(R.id.product_current_quantity);


        // Find the columns of pet attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
        int qtyColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QTY);
        int supplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME);
        int supplierPhoneColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_PHONE_NO);
        int photoColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE_URI);

        // Extract out the value from the Cursor for the given column index
        String productName = cursor.getString(nameColumnIndex);
        String supplierName = cursor.getString(supplierNameColumnIndex);
        int qty = cursor.getInt(qtyColumnIndex);
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
        supplierTextView.setText(supplierName);
        priceTextView.setText(Float.toString(price));
        quantityTextView.setText(Integer.toString(qty));
    }
}
