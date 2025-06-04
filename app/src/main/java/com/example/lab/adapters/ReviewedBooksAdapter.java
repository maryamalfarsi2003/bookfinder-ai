package com.example.lab.adapters; // Your adapters package

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar; // Import RatingBar
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab.R; // Your R file
import com.example.lab.models.Ebook; // Import Ebook
import com.example.lab.models.Review; // Import Review
import com.example.lab.ReviewedBooksActivity.ReviewedBookItem; // Import the custom item class

import java.util.List;

// Adapter for displaying reviewed books
public class ReviewedBooksAdapter extends RecyclerView.Adapter<ReviewedBooksAdapter.ReviewedBookViewHolder> {

    private Context context;
    private List<ReviewedBookItem> reviewedBookItemList; // List of custom items

    // Constructor
    public ReviewedBooksAdapter(Context context, List<ReviewedBookItem> reviewedBookItemList) {
        this.context = context;
        this.reviewedBookItemList = reviewedBookItemList;
    }

    @NonNull
    @Override
    public ReviewedBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_reviewed_book, parent, false); // Link to the new item layout
        return new ReviewedBookViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewedBookViewHolder holder, int position) {
        ReviewedBookItem currentItem = reviewedBookItemList.get(position);
        Ebook ebook = currentItem.getEbook();
        Review review = currentItem.getReview();

        // Bind book details
        holder.textViewReviewedBookTitle.setText(ebook.getName());
        holder.textViewReviewedBookAuthor.setText("by " + ebook.getAuthor());

        // Bind review details
        holder.ratingBarReviewedBook.setRating(review.getRating());
        holder.textViewReviewedBookReviewSnippet.setText(review.getReviewText());

        // Load book cover image
        String imageResourceName = ebook.getImageResourceName();
        if (imageResourceName != null && !imageResourceName.isEmpty()) {
            int resourceId = context.getResources().getIdentifier(
                    imageResourceName, "drawable", context.getPackageName());

            if (resourceId != 0) {
                Drawable coverDrawable = ContextCompat.getDrawable(context, resourceId);
                if (coverDrawable != null) {
                    holder.imageViewReviewedBookCover.setImageDrawable(coverDrawable);
                } else {
                    holder.imageViewReviewedBookCover.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                holder.imageViewReviewedBookCover.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.imageViewReviewedBookCover.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Optional: Set click listener for the item if you want to navigate back to details
        // holder.itemView.setOnClickListener(v -> {
        //     // Handle click
        // });
    }

    @Override
    public int getItemCount() {
        return reviewedBookItemList.size();
    }

    public static class ReviewedBookViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageViewReviewedBookCover;
        public TextView textViewReviewedBookTitle;
        public TextView textViewReviewedBookAuthor;
        public RatingBar ratingBarReviewedBook;
        public TextView textViewReviewedBookReviewSnippet;

        public ReviewedBookViewHolder(View itemView) {
            super(itemView);
            imageViewReviewedBookCover = itemView.findViewById(R.id.imageViewReviewedBookCover);
            textViewReviewedBookTitle = itemView.findViewById(R.id.textViewReviewedBookTitle);
            textViewReviewedBookAuthor = itemView.findViewById(R.id.textViewReviewedBookAuthor);
            ratingBarReviewedBook = itemView.findViewById(R.id.ratingBarReviewedBook);
            textViewReviewedBookReviewSnippet = itemView.findViewById(R.id.textViewReviewedBookReviewSnippet);
        }
    }

    // Method to update the list and refresh the RecyclerView
    public void setReviewedBookItemList(List<ReviewedBookItem> reviewedBookItemList) {
        this.reviewedBookItemList = reviewedBookItemList;
        notifyDataSetChanged();
    }
}
    