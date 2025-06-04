package com.example.lab; // Your actual package name

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.lab.models.Ebook; // Import Ebook model

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Activity to update or delete an existing ebook
public class UpdateDeleteEbookActivity extends AppCompatActivity {

    // UI Elements (Removed editTextPrice declaration)
    private TextInputEditText editTextEbookName, editTextAuthorName, editTextTotalPages;
    private Spinner spinnerCategory;
    private LinearLayout linearLayoutCoverImages;
    private Button buttonUpdateEbook, buttonDeleteEbook;
    private ProgressDialog progressDialog;

    // Firebase Instances
    private FirebaseFirestore db;
    private DocumentReference ebookDocumentRef;

    // Variables to hold category data and selected image resource name
    private List<String> categoryNames;
    private ArrayAdapter<String> categoryAdapter;
    private String selectedImageResourceName = null;
    private ImageView selectedImageView = null;

    private String ebookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_delete_ebook); // Link to the XML layout

        // Get the ebook ID passed from the previous activity
        ebookId = getIntent().getStringExtra("ebookId");
        if (ebookId == null) {
            Toast.makeText(this, "Ebook ID not provided.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        ebookDocumentRef = db.collection("books").document(ebookId);

        // Initialize UI Elements (Removed findViewById for editTextPrice)
        editTextEbookName = findViewById(R.id.editTextEbookName);
        editTextAuthorName = findViewById(R.id.editTextAuthorName);
        editTextTotalPages = findViewById(R.id.editTextTotalPages);

        spinnerCategory = findViewById(R.id.spinnerCategory);
        linearLayoutCoverImages = findViewById(R.id.linearLayoutCoverImages);
        buttonUpdateEbook = findViewById(R.id.buttonUpdateEbook);
        buttonDeleteEbook = findViewById(R.id.buttonDeleteEbook);

        // Initialize Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Processing Ebook");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        // Initialize category data list and adapter
        categoryNames = new ArrayList<>();
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryNames);
        spinnerCategory.setAdapter(categoryAdapter);

        // Fetch categories and ebook data
        fetchCategoriesAndEbookData();

        // Populate the horizontal scroll view with static images
        populateCoverImages();

        // Set OnClickListener for Update Button
        buttonUpdateEbook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEbookInFirebase();
            }
        });

        // Set OnClickListener for Delete Button
        buttonDeleteEbook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete();
            }
        });
    }

    // Method to fetch categories and then the specific ebook data
    private void fetchCategoriesAndEbookData() {
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

                        // After fetching categories, fetch the specific ebook data
                        fetchEbookData();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UpdateDeleteEbookActivity.this, "Error fetching categories: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
    }

    // Method to fetch the specific ebook data from Firestore
    private void fetchEbookData() {
        progressDialog.show();

        ebookDocumentRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        progressDialog.dismiss();

                        if (documentSnapshot.exists()) {
                            Ebook ebook = documentSnapshot.toObject(Ebook.class);
                            if (ebook != null) {
                                // Populate the form fields with existing data
                                editTextEbookName.setText(ebook.getName());
                                editTextAuthorName.setText(ebook.getAuthor());
                                editTextTotalPages.setText(String.valueOf(ebook.getTotalPages()));
                                // Removed setting text for editTextPrice

                                // Select the correct category in the spinner
                                int categoryPosition = categoryNames.indexOf(ebook.getCategory());
                                if (categoryPosition != -1) {
                                    spinnerCategory.setSelection(categoryPosition);
                                }

                                // Select the correct image in the horizontal scroll view
                                selectedImageResourceName = ebook.getImageResourceName();
                                highlightSelectedImage();

                            } else {
                                Toast.makeText(UpdateDeleteEbookActivity.this, "Failed to load ebook data.", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(UpdateDeleteEbookActivity.this, "Ebook not found.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(UpdateDeleteEbookActivity.this, "Error fetching ebook data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }


    // Method to populate the horizontal scroll view with static images
    private void populateCoverImages() {
        // ** IMPORTANT: Replace with the actual resource names of your static cover images **
        List<String> coverImageResourceNames = new ArrayList<>();
        coverImageResourceNames.add("babel"); // Replace with your actual image resource names
        coverImageResourceNames.add("carnaval");
        coverImageResourceNames.add("catching_fire");
        coverImageResourceNames.add("crooked_kingdom");
        coverImageResourceNames.add("cruel_prince");
        coverImageResourceNames.add("dorian_gray");
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

    // Method to highlight the image that is currently selected for the ebook
    private void highlightSelectedImage() {
        if (selectedImageResourceName != null && linearLayoutCoverImages != null) {
            for (int i = 0; i < linearLayoutCoverImages.getChildCount(); i++) {
                View view = linearLayoutCoverImages.getChildAt(i);
                if (view instanceof ImageView) {
                    ImageView imageView = (ImageView) view;
                    String tag = (String) imageView.getTag();
                    if (tag != null && tag.equals(selectedImageResourceName)) {
                        selectedImageView = imageView;
                        selectedImageView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
                        selectedImageView.setPadding(8, 8, 8, 8);
                    } else {
                        imageView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
                        imageView.setPadding(4, 4, 4, 4);
                    }
                }
            }
        }
    }


    // Method to update ebook data in Firebase
    private void updateEbookInFirebase() {
        // Get updated data from input fields
        String ebookName = editTextEbookName.getText().toString().trim();
        String authorName = editTextAuthorName.getText().toString().trim();
        String totalPagesStr = editTextTotalPages.getText().toString().trim();
        // Removed getting priceStr
        String selectedCategory = spinnerCategory.getSelectedItem().toString();

        // Basic Validation (Removed priceStr check)
        if (ebookName.isEmpty() || authorName.isEmpty() || totalPagesStr.isEmpty() || selectedCategory.isEmpty() || selectedImageResourceName == null) {
            Toast.makeText(this, "Please fill all fields, choose a category, and select a cover image", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalPages = Integer.parseInt(totalPagesStr);
        // Removed price variable as it's not in the data model

        progressDialog.setTitle("Updating Ebook");
        progressDialog.show();

        // Create a Map with updated ebook data
        Map<String, Object> updatedEbook = new HashMap<>();
        updatedEbook.put("name", ebookName);
        updatedEbook.put("author", authorName);
        updatedEbook.put("totalPages", totalPages);
        // Removed updating price field
        updatedEbook.put("category", selectedCategory);
        updatedEbook.put("imageResourceName", selectedImageResourceName);

        // Update the document in Firestore
        ebookDocumentRef.update(updatedEbook)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(UpdateDeleteEbookActivity.this, "Ebook updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(UpdateDeleteEbookActivity.this, "Error updating ebook: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Method to show confirmation dialog before deleting
    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this ebook?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteEbookFromFirebase();
                    }
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Method to delete ebook from Firebase
    private void deleteEbookFromFirebase() {
        progressDialog.setTitle("Deleting Ebook");
        progressDialog.show();

        ebookDocumentRef.delete()
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(UpdateDeleteEbookActivity.this, "Ebook deleted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(UpdateDeleteEbookActivity.this, "Error deleting ebook: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
