package com.example.lab.models; // Your models package

import com.google.firebase.firestore.ServerTimestamp; // Import ServerTimestamp

import java.util.Date; // Import Date

// Model class for a book review
public class Review {
    private String userId;
    private String bookId;
    private float rating;
    private String reviewText;
    private Date timestamp; // Use Date for timestamp

    // Required empty public constructor for Firestore
    public Review() {
    }

    // Constructor (optional)
    public Review(String userId, String bookId, float rating, String reviewText, Date timestamp) {
        this.userId = userId;
        this.bookId = bookId;
        this.rating = rating;
        this.reviewText = reviewText;
        this.timestamp = timestamp;
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getBookId() {
        return bookId;
    }

    public float getRating() {
        return rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    @ServerTimestamp // Annotation to automatically populate timestamp from server
    public Date getTimestamp() {
        return timestamp;
    }

    // Setters (required by Firestore)
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
    