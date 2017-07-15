package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;


/**
 * Created by RonaldZboj on 2017-07-13.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "inventory.db";
    public static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_INVENTORY_TABLE =
            "CREATE TABLE " + InventoryEntry.TABLE_NAME + " (" +
                    InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    InventoryEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL," +
                    InventoryEntry.COLUMN_ITEM_PRICE + " REAL NOT NULL," +
                    InventoryEntry.COLUMN_ITEM_QUANTITY + " INTEGER," +
                    InventoryEntry.COLUMN_ITEM_PHONE + " INTEGER NOT NULL," +
                    InventoryEntry.COLUMN_ITEM_IMAGE + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + InventoryEntry.TABLE_NAME;


    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
