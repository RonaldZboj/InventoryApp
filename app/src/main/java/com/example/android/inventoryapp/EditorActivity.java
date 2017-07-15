package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;

import java.io.IOException;

import static com.example.android.inventoryapp.R.id.phone;
import static com.example.android.inventoryapp.data.InventoryProvider.LOG_TAG;

/**
 * Created by RonaldZboj on 2017-07-13.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_ITEM_LOADER = 0;

    int quantity;

    private static final int IMAGE_CODE = 0;

    // Content URI for existing item, it is null when it's new item
    private Uri mCurrentItemUri;

    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mPhoneEditText;
    private ImageView mImageImageView;
    private Button mMinusButton;
    private Button mPlusButton;
    private Button mDeleteButton;
    private Button mOrderButton;
    private Button mSaveButton;
    private Button mAddImageButton;
    private Uri mImageUri;

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

        // Find all views
        mNameEditText = (EditText) findViewById(R.id.name);
        mPriceEditText = (EditText) findViewById(R.id.price);
        mQuantityEditText = (EditText) findViewById(R.id.quantity);
        mMinusButton = (Button) findViewById(R.id.minus);
        mPlusButton = (Button) findViewById(R.id.plus);
        mDeleteButton = (Button) findViewById(R.id.delete);
        mOrderButton = (Button) findViewById(R.id.order);
        mSaveButton = (Button) findViewById(R.id.save_product);
        mImageImageView = (ImageView) findViewById(R.id.image);
        mAddImageButton = (Button) findViewById(R.id.add_image_button);
        mPhoneEditText = (EditText) findViewById(phone);


        // Examine the Intent, so we must to know if this is a new item or we must edit an existing one
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        // If the intent does't have a item content URI, we creating a new item
        if (mCurrentItemUri == null) {
            // New item, set the app bar to "Add item"
            setTitle(getString(R.string.editor_title_new_item));
            mMinusButton.setVisibility(View.INVISIBLE);
            mPlusButton.setVisibility(View.INVISIBLE);
            mOrderButton.setVisibility(View.INVISIBLE);
            mDeleteButton.setVisibility(View.INVISIBLE);

        } else {
            // Existing item, set app bar to "Edit Item"
            setTitle(getString(R.string.editor_title_edit_item));
            // Initialize loader and display current values
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveItem();
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });

        mMinusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                manageStock(mCurrentItemUri, (quantity - 1));

            }
        });

        mPlusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                manageStock(mCurrentItemUri, (quantity + 1));
            }
        });


        mAddImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addImage();
            }
        });

        // Setup OnTouchListeners on all EditText, when we know user change them we can prompt
        // him that he leave without saving
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mMinusButton.setOnTouchListener(mTouchListener);
        mPlusButton.setOnTouchListener(mTouchListener);
        mPhoneEditText.setOnTouchListener(mTouchListener);

    }

    // Get user input and save into database
    private void saveItem() {
        // Read from input field and use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String phoneString = mPhoneEditText.getText().toString().trim();
        String imagePath;

        if (TextUtils.isEmpty(nameString)) {
            mNameEditText.requestFocus();
            mNameEditText.setError("Empty name");
            return;
        }

        if (TextUtils.isEmpty(priceString)) {
            mPriceEditText.requestFocus();
            mPriceEditText.setError("Empty Price");
            return;
        }

        if (TextUtils.isEmpty(phoneString)) {
            mPhoneEditText.requestFocus();
            mPhoneEditText.setError("Empty Phone");
            return;
        }

        if (mImageUri == null) {
            mImageImageView.requestFocus();
            Toast.makeText(EditorActivity.this, "Image required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a ContentValues object column names - keys item attributes - values.
        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME, nameString);
        values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE, priceString);

        // If quantity not provided use 0 default
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }

        values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY, quantity);
        values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_PHONE, phoneString);

        imagePath = mImageUri.toString();

        values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_IMAGE, imagePath);

        //mCurrentItemUri is null - new Item, not null - existing item
        if (mCurrentItemUri == null) {
            // This is a new Item, so insert a new item into the provider
            Uri newUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);

            // Show a toast message on result
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING item, update the item with content URI: mCurrentItemUri
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            // Show a toast message on result
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that contains all columns from the inventory table
        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_ITEM_NAME,
                InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryContract.InventoryEntry.COLUMN_ITEM_IMAGE,
                InventoryContract.InventoryEntry.COLUMN_ITEM_PHONE,
                InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY};


        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentItemUri,         // Query the content URI for the current item
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Return early if the cursor is null or there is less than 1 row
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Moving to the first row of the cursor and reading data from it
        if (cursor.moveToFirst()) {
            // Find the columns of item attributes
            int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY);
            int phoneColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_PHONE);
            int imageColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_IMAGE);

            // Extract values from cursor for the given index
            String name = cursor.getString(nameColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            quantity = cursor.getInt(quantityColumnIndex);
            final String phoneNumber = cursor.getString(phoneColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            //Update the views
            mNameEditText.setText(name);
            mPriceEditText.setText(String.valueOf(price));
            mQuantityEditText.setText(String.valueOf(quantity));
            mPhoneEditText.setText(String.valueOf(phoneNumber));

            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(image));
            } catch (IOException e) {
                e.printStackTrace();
            }
            mImageImageView.setImageBitmap(bitmap);
            // Need for known that image is set, so that the application does not request a re-add image
            mImageUri = Uri.parse(image);

            mOrderButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    orderMore(phoneNumber);
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is broken, clear out all data
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mPhoneEditText.setText("");
    }

    // Delete item from database
    private void deleteItem() {
        // Only  delete if this is an existing item
        if (mCurrentItemUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);
            // Show a toast message on result
            if (rowsDeleted == 0) {
                // If no rows were affected, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();

    }

    // Helper method for plus and minus button
    private int manageStock(Uri uri, int quantity) {
        if (quantity < 0) {
            return 0;
        }
        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY, quantity);
        int rowUpdated = getContentResolver().update(uri, values, null, null);
        return rowUpdated;
    }

    private void addImage() {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_CODE && (resultCode == RESULT_OK)) {
            try {
                mImageUri = data.getData();
                Log.i(LOG_TAG, "Uri: " + mImageUri.toString());
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
                mImageImageView.setImageBitmap(bitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // If user clicked "Keep editing"  dismiss the dialog and continue editing
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        // Show dialog that we have unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, dismiss dialog continue editing
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void orderMore(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

}
