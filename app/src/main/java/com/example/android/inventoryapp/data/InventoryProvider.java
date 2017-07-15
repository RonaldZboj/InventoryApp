package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;


/**
 * Created by RonaldZboj on 2017-07-13.
 */

public class InventoryProvider extends ContentProvider {

    // Tag for the log messages
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    // URI matcher code for inventory table
    private static final int ITEM = 100;

    // URI matcher code for single item
    private static final int ITEM_ID = 101;

    // UriMatcher object to match a content URI to a corresponding code.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // The content URI of the form "content://com.example.android.inventoryapp/inventory"
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, ITEM);
        // The content URI of the form "content://com.example.android.inventoryapp/inventory/#"
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", ITEM_ID);
    }

    private InventoryDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // Cursor hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM:
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ITEM_ID:
                // For the ITEM_ID code, extract out the ID from the URI.
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM:
                return InsertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    // Insert a item into the database with the given content values.
    private Uri InsertItem(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Item requires a name");
        }

        // Check if price is valid
        double price = values.getAsDouble(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE);
        if (!InventoryContract.InventoryEntry.isPriceValid(price)) {
            throw new IllegalArgumentException("Price can not be negative");
        }

        // Check if quantity is valid
        Integer quantity = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY);
        if (!InventoryContract.InventoryEntry.isQuantityValid(quantity)) {
            throw new IllegalArgumentException("Quantity can not be negative");
        }

        // Check if phone is valid
        Integer phone = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_ITEM_PHONE);
        if (phone == null) {
            throw new IllegalArgumentException("Item requires a phone");
        }


        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new item
        long id = database.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        // Notify all listeners that the data has changed for URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case ITEM_ID:
                // For the ITEM_ID code, extract out the ID from the URI,
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // check that the name value is not null.
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME)) {
            String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Item requires a name");
            }
        }

        // Check if price is valid
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE)) {
            double price = values.getAsDouble(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE);
            if (!InventoryContract.InventoryEntry.isPriceValid(price)) {
                throw new IllegalArgumentException("Price can not be negative");
            }
        }

        // Check if quantity is valid
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer quantity = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY);
            if (!InventoryContract.InventoryEntry.isQuantityValid(quantity)) {
                throw new IllegalArgumentException("Quantity can not be negative");
            }
        }

        // Check if phone is valid
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_ITEM_PHONE)) {
            Integer phone = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_ITEM_PHONE);
            if (phone == null) {
                throw new IllegalArgumentException("Item requires a phone");
            }
        }

        // If there are no values to update, return 0;
        if (values.size() == 0) {
            return 0;
        }
        // Otherwise update database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update
        int rowsUpdated = database.update(InventoryContract.InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that URI change
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEM_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Delete is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that URI change
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM:
                return InventoryContract.InventoryEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return InventoryContract.InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }


}
