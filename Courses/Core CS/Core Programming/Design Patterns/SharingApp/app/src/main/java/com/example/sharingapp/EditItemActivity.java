package com.example.sharingapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Editing a pre-existing item consists of deleting the old item and adding a
 * new item with the old
 * item's id.
 * Note: invisible EditText is used to setError for status. For whatever reason
 * we cannot .setError to
 * the status Switch so instead an error is set to an "invisible" EditText.
 */
public class EditItemActivity extends AppCompatActivity implements Observer {

    private ItemList itemList = new ItemList();
    private ItemListController itemListController = new ItemListController(itemList);

    private Item item;
    private ItemController itemController;

    private Context context;

    private ContactList contactList = new ContactList();
    private ContactListController contactListController = new ContactListController(contactList);

    private Bitmap image;
    private int REQUEST_CODE = 1;
    private ImageView photo;

    private EditText title;
    private EditText maker;
    private EditText description;
    private EditText length;
    private EditText width;
    private EditText height;
    private Spinner borrower_spinner;
    private TextView borrower_tv;
    private Switch status;
    private EditText invisible;

    private String title_str;
    private String maker_str;
    private String description_str;
    private String length_str;
    private String width_str;
    private String height_str;

    private ArrayAdapter<String> adapter;
    private boolean on_create_update = false;
    private int pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        title = findViewById(R.id.title);
        maker = findViewById(R.id.maker);
        description = findViewById(R.id.description);
        length = findViewById(R.id.length);
        width = findViewById(R.id.width);
        height = findViewById(R.id.height);
        borrower_spinner = findViewById(R.id.borrower_spinner);
        borrower_tv = findViewById(R.id.borrower_tv);
        photo = findViewById(R.id.image_view);
        status = findViewById(R.id.available_switch);
        invisible = findViewById(R.id.invisible);

        invisible.setVisibility(View.GONE);

        Intent intent = getIntent(); // Get intent from ItemsFragment
        pos = intent.getIntExtra("position", 0);

        context = getApplicationContext();

        itemListController.addObserver(this);
        itemListController.loadItems(context);

        on_create_update = true;

        contactListController.addObserver(this);
        contactListController.loadContacts(context);

        on_create_update = false;
    }

    public void addPhoto(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    public void deletePhoto(View view) {
        image = null;
        photo.setImageResource(android.R.drawable.ic_menu_gallery);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int request_code, int result_code, Intent intent) {
        if (request_code == REQUEST_CODE && result_code == RESULT_OK) {
            Bundle extras = intent.getExtras();
            image = (Bitmap) extras.get("data");
            photo.setImageBitmap(image);
        }
    }

    public void deleteItem(View view) {

        // Delete item
        boolean success = itemListController.deleteItem(item, context);
        if (!success) {
            return;
        }

        // End EditItemActivity
        itemListController.removeObserver(this);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void saveItem(View view) {

        title_str = title.getText().toString();
        maker_str = maker.getText().toString();
        description_str = description.getText().toString();
        length_str = length.getText().toString();
        width_str = width.getText().toString();
        height_str = height.getText().toString();

        if (!validateInput()) {
            return;
        }

        Contact contact = null;
        if (!status.isChecked()) {
            String borrower_str = borrower_spinner.getSelectedItem().toString();
            contact = contactListController.getContactByUsername(borrower_str);
        }

        String id = itemController.getId(); // Reuse the item id
        Item updated_item = new Item(title_str, maker_str, description_str, image, id);
        ItemController updated_item_controller = new ItemController(updated_item);
        updated_item_controller.setDimensions(length_str, width_str, height_str);

        boolean checked = status.isChecked();
        if (!checked) {
            updated_item_controller.setStatus("Borrowed");
            updated_item_controller.setBorrower(contact);
        }

        // Edit item
        boolean success = itemListController.editItem(item, updated_item, context);
        if (!success) {
            return;
        }

        // End EditItemActivity
        itemListController.removeObserver(this);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public boolean validateInput() {

        if (title_str.equals("")) {
            title.setError("Empty field!");
            return false;
        }

        if (maker_str.equals("")) {
            maker.setError("Empty field!");
            return false;
        }

        if (description_str.equals("")) {
            description.setError("Empty field!");
            return false;
        }

        if (length_str.equals("")) {
            length.setError("Empty field!");
            return false;
        }

        if (width_str.equals("")) {
            width.setError("Empty field!");
            return false;
        }

        if (height_str.equals("")) {
            height.setError("Empty field!");
            return false;
        }

        return true;
    }

    /**
     * Checked == "Available"
     * Unchecked == "Borrowed"
     */
    public void toggleSwitch(View view) {
        if (status.isChecked()) {
            // Means was previously borrowed, switch was toggled to available
            borrower_spinner.setVisibility(View.GONE);
            borrower_tv.setVisibility(View.GONE);
            itemController.setBorrower(null);
            itemController.setStatus("Available");

        } else {
            // Means not borrowed
            if (contactList.getSize() == 0) {
                // No contacts, need to add contacts to be able to add a borrower
                invisible.setEnabled(false);
                invisible.setVisibility(View.VISIBLE);
                invisible.requestFocus();
                invisible.setError("No contacts available! Must add borrower to contacts.");
                status.setChecked(true); // Set switch to available

            } else {
                // Means was previously available
                borrower_spinner.setVisibility(View.VISIBLE);
                borrower_tv.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Only need to update the view from the onCreate method
     */
    public void update() {
        if (on_create_update) {
            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_dropdown_item,
                    contactListController.getAllUsernames());
            borrower_spinner.setAdapter(adapter);
            item = itemListController.getItem(pos);
            itemController = new ItemController(item);

            Contact contact = itemController.getBorrower();
            if (contact != null) {
                int contact_pos = contactListController.getIndex(contact);
                borrower_spinner.setSelection(contact_pos);
            }

            title.setText(itemController.getTitle());
            maker.setText(itemController.getMaker());
            description.setText(itemController.getDescription());
            length.setText(itemController.getLength());
            width.setText(itemController.getWidth());
            height.setText(itemController.getHeight());

            String status_str = itemController.getStatus();
            if (status_str.equals("Borrowed")) {
                status.setChecked(false);
            } else {
                borrower_tv.setVisibility(View.GONE);
                borrower_spinner.setVisibility(View.GONE);
            }

            image = itemController.getImage();
            if (image != null) {
                photo.setImageBitmap(image);
            } else {
                photo.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }
    }
}