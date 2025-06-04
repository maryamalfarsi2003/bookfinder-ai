package com.example.lab; // Your actual package name

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log; // Import Log class for logging
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.example.lab.adapters.ReviewedBooksAdapter;
import com.example.lab.models.Ebook;
import com.example.lab.models.Review;
import com.example.lab.ReviewedBooksActivity.ReviewedBookItem; // Import the custom item class

import java.util.ArrayList;
import java.util.List;

// Activity to display books reviewed by the current user
public class ReviewedBooksActivity extends AppCompatActivity {

    private static final String TAG = "ReviewedBooksActivity"; // Tag for Logcat

    // UI Elements
    private RecyclerView recyclerViewReviewedBooks;
    private TextView textViewNoReviewedBooks;
    private ReviewedBooksAdapter reviewedBooksAdapter;
    private List<ReviewedBookItem> reviewedBookItemList;

    private ProgressDialog progressDialog;

    // Firebase Instances
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviewed_books);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI Elements
        recyclerViewReviewedBooks = findViewById(R.id.recyclerViewReviewedBooks);
        textViewNoReviewedBooks = findViewById(R.id.textViewNoReviewedBooks);

        // Initialize list and adapter
        reviewedBookItemList = new ArrayList<>();
        reviewedBooksAdapter = new ReviewedBooksAdapter(this, reviewedBookItemList);

        // Set up RecyclerView
        recyclerViewReviewedBooks.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReviewedBooks.setAdapter(reviewedBooksAdapter);

        // Initialize Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading Reviewed Books");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        // Fetch and display reviewed books
        fetchReviewedBooks();
    }

    // Method to fetch reviews for the current user and then fetch the corresponding book details
    private void fetchReviewedBooks() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to view reviewed books.", Toast.LENGTH_SHORT).show();
            recyclerViewReviewedBooks.setVisibility(View.GONE);
            textViewNoReviewedBooks.setVisibility(View.VISIBLE);
            textViewNoReviewedBooks.setText("Please log in to view reviewed books.");
            return;
        }

        progressDialog.show();

        String currentUserId = currentUser.getUid();

        // 1. Fetch reviews submitted by the current user
        db.collectionGroup("reviews")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Review> userReviews = new ArrayList<>();
                            for (QueryDocumentSnapshot reviewDocument : task.getResult()) {
                                Review review = reviewDocument.toObject(Review.class);
                                userReviews.add(review);
                            }

                            if (userReviews.isEmpty()) {
                                progressDialog.dismiss();
                                recyclerViewReviewedBooks.setVisibility(View.GONE);
                                textViewNoReviewedBooks.setVisibility(View.VISIBLE);
                                textViewNoReviewedBooks.setText("You haven't reviewed any books yet.");
                            } else {
                                fetchBookDetailsForReviews(userReviews);
                            }

                        } else {
                            progressDialog.dismiss();
                            // --- DEBUGGING ADDITION ---
                            Log.e(TAG, "Error fetching reviews: " + task.getException(), task.getException()); // Log the full exception
                            Toast.makeText(ReviewedBooksActivity.this, "Error fetching reviews: " + task.getException().getMessage(), Toast.LENGTH_LONG).show(); // Show detailed error message
                            // --- END DEBUGGING ADDITION ---
                            recyclerViewReviewedBooks.setVisibility(View.GONE);
                            textViewNoReviewedBooks.setVisibility(View.VISIBLE);
                            textViewNoReviewedBooks.setText("Error loading reviewed books.");
                        }
                    }
                });
    }

    // Method to fetch book details for the reviews found
    private void fetchBookDetailsForReviews(List<Review> userReviews) {
        reviewedBookItemList.clear();

        final int totalReviews = userReviews.size();
        final int[] fetchedCount = {0};

        for (Review review : userReviews) {
            String bookId = review.getBookId();
            if (bookId != null && !bookId.isEmpty()) {
                db.collection("books").document(bookId).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot bookDocument) {
                                fetchedCount[0]++;
                                if (bookDocument.exists()) {
                                    Ebook ebook = bookDocument.toObject(Ebook.class);
                                    if (ebook != null) {
                                        ebook.setId(bookDocument.getId());
                                        reviewedBookItemList.add(new ReviewedBookItem(ebook, review));
                                    }
                                } else {
                                    // Log a warning if a book document for a review is not found
                                    Log.w(TAG, "Book document not found for review with bookId: " + bookId);
                                }
                                if (fetchedCount[0] == totalReviews) {
                                    progressDialog.dismiss();
                                    updateReviewedBooksList();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                fetchedCount[0]++;
                                // --- DEBUGGING ADDITION ---
                                Log.e(TAG, "Error fetching book details for bookId: " + bookId + ": " + e, e); // Log the full exception
                                // We won't show a Toast for every single book fetch error,
                                // as it could be spammy if many books fail.
                                // Toast.makeText(ReviewedBooksActivity.this, "Error fetching book details for a reviewed book.", Toast.LENGTH_SHORT).show();
                                // --- END DEBUGGING ADDITION ---
                                if (fetchedCount[0] == totalReviews) {
                                    progressDialog.dismiss();
                                    updateReviewedBooksList();
                                }
                            }
                        });
            } else {
                fetchedCount[0]++;
                Log.w(TAG, "Review document with missing or empty bookId found."); // Log warning for invalid review data
                if (fetchedCount[0] == totalReviews) {
                    progressDialog.dismiss();
                    updateReviewedBooksList();
                }
            }
        }
    }

    // Method to update the RecyclerView adapter
    private void updateReviewedBooksList() {
        if (reviewedBookItemList.isEmpty()) {
            recyclerViewReviewedBooks.setVisibility(View.GONE);
            textViewNoReviewedBooks.setVisibility(View.VISIBLE);
            textViewNoReviewedBooks.setText("You haven't reviewed any books yet.");
        } else {
            recyclerViewReviewedBooks.setVisibility(View.VISIBLE);
            textViewNoReviewedBooks.setVisibility(View.GONE);
            reviewedBooksAdapter.setReviewedBookItemList(reviewedBookItemList);
        }
    }

    // Custom class to hold an Ebook object and its corresponding Review object
    public static class ReviewedBookItem {
        private Ebook ebook;
        private Review review;

        public ReviewedBookItem(Ebook ebook, Review review) {
            this.ebook = ebook;
            this.review = review;
        }

        public Ebook getEbook() {
            return ebook;
        }

        public Review getReview() {
            return review;
        }
    }
}
