package com.example.android.inventoryapp.data;

import android.provider.BaseColumns;

public class InventoryContract {

    private InventoryContract(){}

    public static final class ProductEntry implements BaseColumns{

        public static final String TABLE_NAME = "product";

        public static final String _ID = BaseColumns._ID;

        public static final String COLUMN_PRODUCT_NAME = "product_name";

        public static final String COLUMN_PRICE = "price";

        public static final String COLUMN_QTY = "quantity";

        public static final String COLUMN_SUPPLIER_NAME = "supplier_name";

        public static final String COLUMN_SUPPLIER_PHONE_NO = "supplier_phone_no";

    }
}
