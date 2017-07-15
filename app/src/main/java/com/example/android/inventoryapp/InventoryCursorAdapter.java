package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract;

import static com.example.android.inventoryapp.R.id.price;

/**
 * Created by RonaldZboj on 2017-07-13.
 */

// InventoryCursorAdapter is an adapter for a list and uses a cursor of items as its data source
public class InventoryCursorAdapter extends CursorAdapter {

    // Constructor
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        //Find views that we want to show and saleButton
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        Button saleButton = (Button) view.findViewById(R.id.sale);

        // Find the column of items
        int idColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY);

        // Read the items atr for the cursor for current item
        final int itemId = cursor.getInt(idColumnIndex);
        String itemName = cursor.getString(nameColumnIndex);
        double itemPrice = cursor.getDouble(priceColumnIndex);
        final int itemQuantity = cursor.getInt(quantityColumnIndex);


        // Update the TextViews
        nameTextView.setText(itemName);
        priceTextView.setText("$" + String.format("%.02f", itemPrice));
        quantityTextView.setText(String.valueOf(itemQuantity));

        // Listener for sale button, reduce quantity -1
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri itemUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, itemId);
                manageStock(context, itemUri, itemQuantity);
            }
        });

    }

    // Helper method for reduce quantity by 1
    private void manageStock(Context context, Uri itemUri, int currentQuantity) {
        // Reduce quantity by 1, set 0 when <1
        int newQuantity;
        if (currentQuantity >= 1) {
            newQuantity = currentQuantity - 1;
        } else {
            newQuantity = 0;
        }

        // Update table with new quantity
        ContentValues contentValues = new ContentValues();
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY, newQuantity);
        int numRowsUpdated = context.getContentResolver().update(itemUri, contentValues, null, null);

    }
}
