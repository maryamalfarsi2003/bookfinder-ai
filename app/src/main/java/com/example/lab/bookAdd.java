package com.example.lab; // Your actual package name

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Book Add Activity
public class bookAdd extends AppCompatActivity {

    // UI Elements (editTextPrice is still declared)
    private TextInputEditText editTextEbookName, editTextAuthorName, editTextTotalPages, editTextPrice;
    private Spinner spinnerCategory;
    private LinearLayout linearLayoutCoverImages;
    private Button buttonSaveEbook;
    private ProgressDialog progressDialog;

    // Firebase Instances
    private FirebaseFirestore db;

    // Variables to hold category data and selected image resource name
    private List<String> categoryNames;
    private ArrayAdapter<String> categoryAdapter;
    private String selectedImageResourceName = null;
    private ImageView selectedImageView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_add); // Link to your XML layout

        // Initialize UI Elements (Initialize editTextPrice)
        editTextEbookName = findViewById(R.id.editTextEbookName);
        editTextAuthorName = findViewById(R.id.editTextAuthorName);
        editTextTotalPages = findViewById(R.id.editTextTotalPages);


        spinnerCategory = findViewById(R.id.spinnerCategory);
        linearLayoutCoverImages = findViewById(R.id.linearLayoutCoverImages);
        buttonSaveEbook = findViewById(R.id.buttonSaveEbook);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Saving Ebook");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        // Initialize category data list and adapter
        categoryNames = new ArrayList<>();
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryNames);
        spinnerCategory.setAdapter(categoryAdapter);

        // Fetch categories from Firebase
        fetchCategoriesFromFirebase();

        // Populate the horizontal scroll view with static images from resources
        populateCoverImages();

        // Set OnClickListener for Save Ebook Button
        buttonSaveEbook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEbookToFirebase();
            }
        });
    }

    // Method to fetch categories from Firebase Firestore
    private void fetchCategoriesFromFirebase() {
        db.collection("books_categories")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        categoryNames.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String categoryName = document.getString("name");
                            if (categoryName != null) {
                                categoryNames.add(categoryName);
                            }
                        }
                        categoryAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(bookAdd.this, "Error fetching categories: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to populate the horizontal scroll view with static images from resources
    private void populateCoverImages() {
        // ** IMPORTANT: Replace with the actual resource names of your static cover images **
        List<String> coverImageResourceNames = new ArrayList<>();
        coverImageResourceNames.add("babel"); // Replace with actual image resource names
        coverImageResourceNames.add("caraval");
        coverImageResourceNames.add("catching_fire");
        coverImageResourceNames.add("crooked_kingdom");
        coverImageResourceNames.add("cruel_prince");
        coverImageResourceNames.add("dorian_gray");
        coverImageResourceNames.add("the_book_thief");
        coverImageResourceNames.add("the_hunger_games");
        coverImageResourceNames.add("harry_potter");
        coverImageResourceNames.add("mockingjay");
        coverImageResourceNames.add("poppy_war");
        coverImageResourceNames.add("shadowandbone");
        coverImageResourceNames.add("six_of_crows");
        coverImageResourceNames.add("dragon_republic");
        coverImageResourceNames.add("george_orwell");
        coverImageResourceNames.add("letters_to_a_young_poet");
        coverImageResourceNames.add("the_little_prince_classic");
        coverImageResourceNames.add("vengeful");
        coverImageResourceNames.add("vicious");
        coverImageResourceNames.add("white_nights_classic");
        coverImageResourceNames.add("wuthering_heights");
        // Add all your static image resource names here

        linearLayoutCoverImages.removeAllViews();

        for (String resourceName : coverImageResourceNames) {
            int resourceId = getResources().getIdentifier(
                    resourceName, "drawable", getPackageName());

            if (resourceId != 0) {
                ImageView imageView = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        getResources().getDimensionPixelSize(R.dimen.cover_image_width),
                        getResources().getDimensionPixelSize(R.dimen.cover_image_height)
                );
                params.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.cover_image_margin));
                imageView.setLayoutParams(params);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
                imageView.setPadding(4, 4, 4, 4);

                Drawable coverDrawable = ContextCompat.getDrawable(this, resourceId);
                if (coverDrawable != null) {
                    imageView.setImageDrawable(coverDrawable);
                } else {
                    imageView.setImageResource(android.R.drawable.ic_menu_gallery);
                }

                imageView.setTag(resourceName);

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectImage((ImageView) v, (String) v.getTag());
                    }
                });

                linearLayoutCoverImages.addView(imageView);
            } else {
                Toast.makeText(this, "Warning: Image resource not found: " + resourceName, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to handle image selection
    private void selectImage(ImageView imageView, String resourceName) {
        if (selectedImageView != null) {
            selectedImageView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            selectedImageView.setPadding(4, 4, 4, 4);
        }

        selectedImageView = imageView;
        selectedImageResourceName = resourceName;
        selectedImageView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
        selectedImageView.setPadding(8, 8, 8, 8);

        Toast.makeText(this, "Selected: " + resourceName, Toast.LENGTH_SHORT).show();
    }

    // Method to save ebook data to Firebase Firestore
    private void saveEbookToFirebase() {
        String ebookName = editTextEbookName.getText().toString().trim();
        String authorName = editTextAuthorName.getText().toString().trim();
        String totalPagesStr = editTextTotalPages.getText().toString().trim();
        // String priceStr = editTextPrice.getText().toString().trim(); // Get text from price field (if it exists)

        String selectedCategory = spinnerCategory.getSelectedItem().toString();

        // Basic Validation (Adjusted validation)
        if (ebookName.isEmpty() || authorName.isEmpty() || totalPagesStr.isEmpty() || selectedCategory.isEmpty() || selectedImageResourceName == null) {
            Toast.makeText(this, "Please fill all fields, choose a category, and select a cover image", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalPages = Integer.parseInt(totalPagesStr);
        int price = 0; // Hardcoded price to 0

        progressDialog.show();

        // Save ebook data to Firestore
        saveEbookDataToFirestore(ebookName, authorName, totalPages, price, selectedCategory, selectedImageResourceName);
    }

    // Method to save ebook data to Cloud Firestore
    private void saveEbookDataToFirestore(String name, String author, int pages, double price, String category, String imageResourceName) {
        Map<String, Object> ebook = new HashMap<>();
        ebook.put("name", name);
        ebook.put("author", author);
        ebook.put("totalPages", pages);
        ebook.put("price", price); // Still save price as 0 in Firestore
        ebook.put("category", category);
        ebook.put("imageResourceName", imageResourceName);

        db.collection("books")
                .add(ebook)
                .addOnSuccessListener(documentReference -> {
                    progressDialog.dismiss();
                    Toast.makeText(bookAdd.this, "Ebook saved successfully!", Toast.LENGTH_SHORT).show();
                    clearForm();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(bookAdd.this, "Error saving ebook: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Method to clear the input form and reset selections
    private void clearForm() {
        editTextEbookName.setText("");
        editTextAuthorName.setText("");
        editTextTotalPages.setText("");
        editTextPrice.setText(""); // Clear the price field if it exists
        if (!categoryNames.isEmpty()) {
            spinnerCategory.setSelection(0);
        }
        if (selectedImageView != null) {
            selectedImageView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            selectedImageView.setPadding(4, 4, 4, 4);
            selectedImageView = null;
            selectedImageResourceName = null;
        }
    }
}
