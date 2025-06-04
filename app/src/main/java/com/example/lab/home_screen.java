package com.example.lab; // Your actual package name

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent; // Import Intent for navigation
import android.graphics.Typeface; // Import Typeface
import android.graphics.drawable.Drawable; // Import for Drawable
import android.os.Bundle;
import android.view.View;
import android.widget.Button; // Import Button
import android.widget.ImageView;
import android.widget.LinearLayout; // Import LinearLayout
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query; // Import Query
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.example.lab.models.Ebook; // Import the Ebook model class (Adjust import based on your package)

import java.util.ArrayList;
import java.util.List;

// Activity for the normal user home screen
public class home_screen extends AppCompatActivity { // Your specified class name

    // UI Elements
    private LinearLayout linearLayoutCategoriesContainer; // Main container for categories
    private Button buttonReviewedBooks; // Declare the Reviewed Books button
    private Button buttonAIHelper; // Declare the AI Helper button

    // Firebase Instance
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Keep existing EdgeToEdge and WindowInsets logic if needed for your layout
        // EdgeToEdge.enable(this); // Uncomment if you are using EdgeToEdge
        setContentView(R.layout.activity_home_screen); // Link to your user home layout

        // Keep existing WindowInsets logic if needed
        // ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
        //     Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        //     v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        //     return insets;
        // });


        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI Elements
        linearLayoutCategoriesContainer = findViewById(R.id.linearLayoutCategoriesContainer); // Get reference to the main container from XML
        buttonReviewedBooks = findViewById(R.id.buttonReviewedBooks); // Initialize the Reviewed Books button
        buttonAIHelper = findViewById(R.id.buttonAIHelper); // Initialize the AI Helper button

        // Set OnClickListener for the Reviewed Books button
        buttonReviewedBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This is the code that handles the Reviewed Books button click
                Intent intent = new Intent(home_screen.this, ReviewedBooksActivity.class); // Start ReviewedBooksActivity
                startActivity(intent);
            }
        });

        // Set OnClickListener for the AI Helper button
        buttonAIHelper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This is the code that handles the AI Helper button click
                Intent intent = new Intent(home_screen.this, AIHelperActivity.class); // Start AIHelperActivity
                startActivity(intent);
            }
        });

        // Fetch and display books by category
        fetchCategoriesAndBooks();
    }

    // Method to fetch categories and then books for each category
    private void fetchCategoriesAndBooks() {
        // Fetch all categories first from the 'books_categories' collection
        db.collection("books_categories")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Clear the container before adding new views (important on resume/refresh)
                        linearLayoutCategoriesContainer.removeAllViews();

                        if (queryDocumentSnapshots.isEmpty()) {
                            // Handle case where there are no categories in the database
                            TextView noCategoriesText = new TextView(home_screen.this);
                            noCategoriesText.setText("No categories found.");
                            noCategoriesText.setTextSize(18);
                            linearLayoutCategoriesContainer.addView(noCategoriesText);
                            return;
                        }

                        // Iterate through each category document fetched from Firestore
                        for (QueryDocumentSnapshot categoryDocument : queryDocumentSnapshots) {
                            String categoryName = categoryDocument.getString("name"); // Assuming 'name' is the field storing the category name

                            // Check if the category name is valid
                            if (categoryName != null && !categoryName.isEmpty()) {
                                // Create a section (Genre Title + Horizontal ScrollView) for this category
                                createCategorySection(categoryName);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle errors during category fetching
                        Toast.makeText(home_screen.this, "Error fetching categories: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to create UI elements for a single category section (Genre Title and Horizontal ScrollView)
    private void createCategorySection(String categoryName) {
        // 1. Create TextView for the Genre Title
        TextView genreTitleTextView = new TextView(this);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        // Add margins for spacing between category sections
        titleParams.setMargins(0, getResources().getDimensionPixelSize(R.dimen.horizontal_item_margin), 0, getResources().getDimensionPixelSize(R.dimen.horizontal_item_margin) / 2);
        genreTitleTextView.setLayoutParams(titleParams);
        genreTitleTextView.setText(categoryName); // Set the category name as the title
        genreTitleTextView.setTextSize(18); // Set text size
        // Corrected: Use setTypeface to set the text style to bold
        genreTitleTextView.setTypeface(null, Typeface.BOLD);


        // 2. Create HorizontalScrollView to contain the list of books for this category
        android.widget.HorizontalScrollView horizontalScrollView = new android.widget.HorizontalScrollView(this);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        horizontalScrollView.setLayoutParams(scrollParams);
        horizontalScrollView.setHorizontalScrollBarEnabled(false); // Optional: Hide the horizontal scroll bar for a cleaner look

        // 3. Create inner LinearLayout to hold book ImageViews within the HorizontalScrollView
        LinearLayout horizontalLinearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams innerLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, // Wrap content horizontally
                LinearLayout.LayoutParams.WRAP_CONTENT // Wrap content vertically
        );
        horizontalLinearLayout.setLayoutParams(innerLayoutParams);
        horizontalLinearLayout.setOrientation(LinearLayout.HORIZONTAL); // Arrange items horizontally
        horizontalLinearLayout.setGravity(android.view.Gravity.CENTER_VERTICAL); // Align items vertically in the center within the LinearLayout
        // Add padding to the inner LinearLayout for spacing at the start and end of the horizontal list
        horizontalLinearLayout.setPadding(
                getResources().getDimensionPixelSize(R.dimen.horizontal_item_margin), // Left padding
                0, // Top padding
                getResources().getDimensionPixelSize(R.dimen.horizontal_item_margin), // Right padding
                0  // Bottom padding
        );

        horizontalScrollView.addView(horizontalLinearLayout);

        // Add the Genre Title TextView and the HorizontalScrollView to the main vertical container
        linearLayoutCategoriesContainer.addView(genreTitleTextView);
        linearLayoutCategoriesContainer.addView(horizontalScrollView);

        // 4. Fetch books specifically for this category and populate the horizontal LinearLayout
        fetchBooksForCategory(categoryName, horizontalLinearLayout);
    }

    // Method to fetch books for a specific category and populate the given horizontal LinearLayout
    private void fetchBooksForCategory(String categoryName, LinearLayout horizontalLinearLayout) {
        // Query the 'books' collection, filtering by the 'category' field
        db.collection("books")
                .whereEqualTo("category", categoryName) // Filter books where the 'category' field equals the current categoryName
                .get() // Get the documents that match the query
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Check if any books were found for this category
                        if (queryDocumentSnapshots.isEmpty()) {
                            // Optional: Add a message if no books are found for this category
                            // TextView noBooksText = new TextView(home_screen.this);
                            // noBooksText.setText("No books in this category.");
                            // noBooksText.setTextSize(14);
                            // horizontalLinearLayout.addView(noBooksText);
                            return; // Exit the method if no books are found
                        }

                        // Iterate through each book document in the query results
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Convert the Firestore document to an Ebook object using the Ebook model class
                            Ebook ebook = document.toObject(Ebook.class);
                            ebook.setId(document.getId()); // Set the Firestore document ID (useful for potential book details screen)

                            // Create an ImageView for the book cover for this book
                            ImageView imageViewCover = new ImageView(home_screen.this);
                            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                                    getResources().getDimensionPixelSize(R.dimen.horizontal_cover_width), // Get width from dimens.xml
                                    getResources().getDimensionPixelSize(R.dimen.horizontal_cover_height) // Get height from dimens.xml
                            );
                            // Add margin to the right of the image for spacing between books
                            imageParams.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.horizontal_item_margin));
                            imageViewCover.setLayoutParams(imageParams); // Set the layout parameters
                            imageViewCover.setScaleType(ImageView.ScaleType.CENTER_CROP); // Scale the image to fill the ImageView
                            imageViewCover.setBackgroundColor(ContextCompat.getColor(home_screen.this, android.R.color.darker_gray)); // Placeholder background color
                            imageViewCover.setContentDescription(ebook.getName() + " Cover"); // Set content description for accessibility

                            // Load the static cover image from resources using the resource name stored in Firestore
                            String imageResourceName = ebook.getImageResourceName();
                            if (imageResourceName != null && !imageResourceName.isEmpty()) {
                                // Get the resource ID (e.g., R.drawable.my_cover) from the resource name string
                                int resourceId = getResources().getIdentifier(
                                        imageResourceName, "drawable", getPackageName()); // Use "drawable" or "mipmap" based on where you put the images

                                if (resourceId != 0) {
                                    // Resource ID found, get the Drawable object
                                    Drawable coverDrawable = ContextCompat.getDrawable(home_screen.this, resourceId);
                                    if (coverDrawable != null) {
                                        // Set the image drawable in the ImageView
                                        imageViewCover.setImageDrawable(coverDrawable);
                                    } else {
                                        // Fallback icon if drawable is somehow null
                                        imageViewCover.setImageResource(android.R.drawable.ic_menu_gallery);
                                    }
                                } else {
                                    // Fallback icon if the resource name is in Firestore but not found in resources
                                    imageViewCover.setImageResource(android.R.drawable.ic_menu_gallery);
                                    // Optionally, show a Toast or log a warning
                                    // Toast.makeText(home_screen.this, "Image resource not found for: " + ebook.getName(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Fallback icon if no image resource name is stored for this book
                                imageViewCover.setImageResource(android.R.drawable.ic_menu_gallery);
                            }

                            // Optional: Set click listener for the book cover image if you want to navigate to a book details screen
                            imageViewCover.setOnClickListener(v -> {
                                // Handle book click (e.g., navigate to book details screen)
                                // Toast.makeText(home_screen.this, "Clicked: " + ebook.getName(), Toast.LENGTH_SHORT).show();
                                // Example to navigate to a details activity:
                                Intent intent = new Intent(home_screen.this, BookDetailsActivity.class);
                                intent.putExtra("ebookId", ebook.getId()); // Pass the Firestore document ID
                                startActivity(intent);
                            });

                            // Add the created ImageView to the horizontal LinearLayout for this category
                            horizontalLinearLayout.addView(imageViewCover);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle errors during fetching books for a specific category
                        Toast.makeText(home_screen.this, "Error fetching books for category " + categoryName + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Consider refreshing the list when returning to this activity if needed
    // @Override
    // protected void onResume() {
    //     super.onResume();
    //     // Example: Refetch books if you expect data to change while this activity is paused
    //     // fetchCategoriesAndBooks();
    // }
}
