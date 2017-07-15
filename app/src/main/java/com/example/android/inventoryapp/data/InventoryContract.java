package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API contract for the Inventory app
 */

public final class InventoryContract {

    // Empty constructor because this is contract class
    private InventoryContract() {
    }

    // Name for entire content provider
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    // Use CONTENT_AUTHORITY to create rhe base for URI's
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible path "content://com.example.android.inventoryapp/inventory/"
    public static final String PATH_INVENTORY = "inventory";

    // Inner class for defines constant values for items in database table
    public static final class InventoryEntry implements BaseColumns {

        // Content URI for access item data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        // Database name
        public static final String TABLE_NAME = "inventory";

        // Unique ID number, type: INTEGER
        public final static String _ID = BaseColumns._ID;

        // Name of item, type: TEXT
        public final static String COLUMN_ITEM_NAME = "name";

        // Price of item, type: INTEGER
        public final static String COLUMN_ITEM_PRICE = "price";

        // Quantity of item, type: INTEGER
        public final static String COLUMN_ITEM_QUANTITY = "quantity";

        // Phone to supplier, type: INTEGER
        public final static String COLUMN_ITEM_PHONE = "phone";

        // Image of item, type: TEXT
        public final static String COLUMN_ITEM_IMAGE = "image";

        // MIME type of CONTENT_URI for a list of items
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        // MIME type of CONTENT_URI for single item
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        // Return if quantity is valid (not negative)
        public static boolean isQuantityValid(int quantity) {
            if (quantity >= 0) {
                return true;
            }
            return false;
        }

        // Return if price is valid (not negative)
        public static boolean isPriceValid(double price) {
            if (price >= 0) {
                return true;
            }
            return false;
        }

    }

}
