package com.example.lab; // Your actual package name

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar; // Import RatingBar
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText; // Import TextInputEditText
import com.google.firebase.auth.FirebaseAuth; // Import FirebaseAuth
import com.google.firebase.auth.FirebaseUser; // Import FirebaseUser
import com.google.firebase.firestore.DocumentReference; // Import DocumentReference
import com.google.firebase.firestore.DocumentSnapshot; // Import DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue; // Import FieldValue for timestamp

import com.example.lab.models.Ebook; // Import Ebook model class

import java.util.HashMap;
import java.util.Map;

// Activity to display book details and allow user rating/review
public class BookDetailsActivity extends AppCompatActivity {

    // UI Elements
    private ImageView imageViewBookCoverDetails;
    private TextView textViewBookTitleDetails;
    private TextView textViewBookAuthorDetails;
    private TextView textViewBookCategoryDetails;
    private TextView textViewBookTotalPagesDetails;
    private RatingBar ratingBarBook; // RatingBar for user rating
    private TextInputEditText editTextReview; // TextInputEditText for user review
    private Button buttonSubmitReview; // Button to submit review

    private ProgressDialog progressDialog;

    // Firebase Instances
    private FirebaseFirestore db;
    private FirebaseAuth mAuth; // Firebase Authentication instance
    private DocumentReference ebookDocumentRef; // Reference to the specific ebook document

    private String ebookId; // To store the ID of the book being displayed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details); // Link to your book details layout

        // Get the ebook ID passed from the previous activity (home_screen)
        ebookId = getIntent().getStringExtra("ebookId");
        if (ebookId == null) {
            Toast.makeText(this, "Book ID not provided.", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if no ID is provided
            return;
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Auth

        // Get a reference to the specific ebook document in Firestore
        ebookDocumentRef = db.collection("books").document(ebookId);

        // Initialize UI Elements
        imageViewBookCoverDetails = findViewById(R.id.imageViewBookCoverDetails);
        textViewBookTitleDetails = findViewById(R.id.textViewBookTitleDetails);
        textViewBookAuthorDetails = findViewById(R.id.textViewBookAuthorDetails);
        textViewBookCategoryDetails = findViewById(R.id.textViewBookCategoryDetails);
        textViewBookTotalPagesDetails = findViewById(R.id.textViewBookTotalPagesDetails);
        ratingBarBook = findViewById(R.id.ratingBarBook); // Initialize RatingBar
        editTextReview = findViewById(R.id.editTextReview); // Initialize TextInputEditText
        buttonSubmitReview = findViewById(R.id.buttonSubmitReview); // Initialize Submit Button

        // Initialize Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading Book Details");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        // Fetch and display the book details
        fetchBookDetails();

        // Set OnClickListener for the Submit Review Button
        buttonSubmitReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitReview();
            }
        });
    }

    // Method to fetch the specific book details from Firestore
    private void fetchBookDetails() {
        progressDialog.show(); // Show progress dialog while fetching

        ebookDocumentRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        progressDialog.dismiss(); // Dismiss progress dialog

                        if (documentSnapshot.exists()) {
                            // Convert the document to an Ebook object
                            Ebook ebook = documentSnapshot.toObject(Ebook.class);
                            if (ebook != null) {
                                // Populate the UI elements with book data
                                textViewBookTitleDetails.setText(ebook.getName());
                                textViewBookAuthorDetails.setText("by " + ebook.getAuthor()); // Example formatting
                                textViewBookCategoryDetails.setText("Category: " + ebook.getCategory());
                                textViewBookTotalPagesDetails.setText("Pages: " + ebook.getTotalPages());

                                // Load the static cover image from resources
                                String imageResourceName = ebook.getImageResourceName();
                                if (imageResourceName != null && !imageResourceName.isEmpty()) {
                                    int resourceId = getResources().getIdentifier(
                                            imageResourceName, "drawable", getPackageName()); // Or "mipmap"

                                    if (resourceId != 0) {
                                        Drawable coverDrawable = ContextCompat.getDrawable(BookDetailsActivity.this, resourceId);
                                        if (coverDrawable != null) {
                                            imageViewBookCoverDetails.setImageDrawable(coverDrawable);
                                        } else {
                                            imageViewBookCoverDetails.setImageResource(android.R.drawable.ic_menu_gallery); // Fallback
                                        }
                                    } else {
                                        imageViewBookCoverDetails.setImageResource(android.R.drawable.ic_menu_gallery); // Fallback
                                    }
                                } else {
                                    imageViewBookCoverDetails.setImageResource(android.R.drawable.ic_menu_gallery); // Fallback
                                }

                                // TODO: Fetch and display existing reviews for this book (more complex, can add later)

                            } else {
                                Toast.makeText(BookDetailsActivity.this, "Failed to load book data.", Toast.LENGTH_SHORT).show();
                                finish(); // Close activity if data loading fails
                            }
                        } else {
                            Toast.makeText(BookDetailsActivity.this, "Book not found.", Toast.LENGTH_SHORT).show();
                            finish(); // Close activity if book document doesn't exist
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(BookDetailsActivity.this, "Error fetching book details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish(); // Close activity on error
                    }
                });
    }

    // Method to handle submitting the user's rating and review
    private void submitReview() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User is not logged in, show a message and return
            Toast.makeText(this, "Please log in to submit a review.", Toast.LENGTH_SHORT).show();
            // TODO: Optionally, redirect the user to the login screen
            return;
        }

        float rating = ratingBarBook.getRating(); // Get the rating from the RatingBar
        String reviewText = editTextReview.getText().toString().trim(); // Get the review text

        // Basic validation for rating and review
        if (rating == 0) {
            Toast.makeText(this, "Please provide a rating.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Optional: Add validation for reviewText if it's required
        // if (reviewText.isEmpty()) {
        //     Toast.makeText(this, "Please write a review.", Toast.LENGTH_SHORT).show();
        //     return;
        // }


        progressDialog.setTitle("Submitting Review");
        progressDialog.setMessage("Please wait...");
        progressDialog.show(); // Show progress dialog

        // Create a Map to store the review data
        Map<String, Object> review = new HashMap<>();
        review.put("userId", currentUser.getUid()); // Store the ID of the user who submitted the review
        review.put("bookId", ebookId); // Store the ID of the book being reviewed
        review.put("rating", rating); // Store the rating
        review.put("reviewText", reviewText); // Store the review text
        review.put("timestamp", FieldValue.serverTimestamp()); // Add a server timestamp

        // Save the review to Firestore
        // We'll store reviews in a subcollection named 'reviews' under the specific book document
        ebookDocumentRef.collection("reviews")
                .add(review) // Add a new document with a generated ID to the 'reviews' subcollection
                .addOnSuccessListener(documentReference -> {
                    progressDialog.dismiss();
                    Toast.makeText(BookDetailsActivity.this, "Review submitted successfully!", Toast.LENGTH_SHORT).show();
                    // Clear the rating and review fields after successful submission
                    ratingBarBook.setRating(0);
                    editTextReview.setText("");
                    // TODO: Optionally, refresh the displayed reviews section if you add one
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(BookDetailsActivity.this, "Error submitting review: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Optional: Override onResume if you need to refresh data when returning to this activity
    // @Override
    // protected void onResume() {
    //     super.onResume();
    //     // Example: If you allow editing reviews, you might refetch the user's review here
    // }
}
