package com.example.lab.adapters; // Your actual package name

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab.R; // Make sure this points to your project's R file
import com.example.lab.models.Ebook; // Import the Ebook model class

import java.util.List;

// Adapter for the RecyclerView to display Ebook items
// Takes a list of Ebook objects and creates/binds views for each item using list_item_ebook.xml
public class EbookAdapter extends RecyclerView.Adapter<EbookAdapter.EbookViewHolder> {

    private Context context;
    private List<Ebook> ebookList;
    // Optional: Add an interface for item click handling if you want to make items clickable
    private OnItemClickListener listener; // Declare the listener

    // Interface for item click handling
    public interface OnItemClickListener {
        void onItemClick(Ebook ebook); // Method to be called when an item is clicked
    }

    // Method to set the click listener from the Activity
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Constructor for the Adapter
    public EbookAdapter(Context context, List<Ebook> ebookList) {
        this.context = context;
        this.ebookList = ebookList;
    }

    // Create new views (invoked by the layout manager)
    // This method inflates the list_item_ebook.xml layout for each item
    @NonNull
    @Override
    public EbookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the custom layout for each list item
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_ebook, parent, false);
        return new EbookViewHolder(itemView); // No need to pass listener here anymore
    }

    // Replace the contents of a view (invoked by the layout manager)
    // This method binds the data from an Ebook object to the views in the item layout
    @Override
    public void onBindViewHolder(@NonNull EbookViewHolder holder, int position) {
        // Get the ebook object at the current position in the list
        Ebook currentEbook = ebookList.get(position);

        // Bind the ebook data to the views in the item layout
        holder.textViewTitle.setText(currentEbook.getName());
        holder.textViewAuthor.setText("by " + currentEbook.getAuthor()); // Example formatting
        // Removed binding for textViewPrice
        holder.textViewCategory.setText("Category: " + currentEbook.getCategory()); // Bind Category
        holder.textViewTotalPages.setText("Pages: " + currentEbook.getTotalPages()); // Bind Total Pages


        // Load the static cover image from resources using the resource name stored in Firestore
        String imageResourceName = currentEbook.getImageResourceName();
        if (imageResourceName != null && !imageResourceName.isEmpty()) {
            // Get the resource ID from the resource name (e.g., "my_cover" -> R.drawable.my_cover)
            int resourceId = context.getResources().getIdentifier(
                    imageResourceName, "drawable", context.getPackageName()); // Use "drawable" or "mipmap" based on where you put the images

            if (resourceId != 0) {
                // Resource ID found, get the Drawable
                Drawable coverDrawable = ContextCompat.getDrawable(context, resourceId);
                if (coverDrawable != null) {
                    // Set the image in the ImageView
                    holder.imageViewCover.setImageDrawable(coverDrawable);
                } else {
                    // Handle case where drawable is null (shouldn't happen if resourceId is valid)
                    holder.imageViewCover.setImageResource(android.R.drawable.ic_menu_gallery); // Show a fallback icon
                }
            } else {
                // Resource name is in Firestore but the corresponding image file is not found in resources
                holder.imageViewCover.setImageResource(android.R.drawable.ic_menu_gallery); // Show a fallback icon
                // Optionally, show a Toast or log a warning to the admin
                // Toast.makeText(context, "Image resource not found for: " + currentEbook.getName(), Toast.LENGTH_SHORT).show();
            }
        } else {
            // No image resource name stored for this book in Firestore
            holder.imageViewCover.setImageResource(android.R.drawable.ic_menu_gallery); // Show a fallback icon
        }

        // Set click listener for the entire item view in onBindViewHolder
        holder.itemView.setOnClickListener(v -> {
            // Check if the listener is set and the position is valid
            if (listener != null && position != RecyclerView.NO_POSITION) {
                // Call the onItemClick method of the listener, passing the current Ebook object
                listener.onItemClick(ebookList.get(position)); // Get the Ebook object using the position
            }
        });
    }

    // Return the total number of items in the data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return ebookList.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class EbookViewHolder extends RecyclerView.ViewHolder {
        // Declare the views in your list item layout (list_item_ebook.xml)
        public ImageView imageViewCover;
        public TextView textViewTitle;
        public TextView textViewAuthor;
        // Removed textViewPrice declaration
        public TextView textViewCategory;
        public TextView textViewTotalPages;


        // Constructor to initialize the views
        public EbookViewHolder(View itemView) { // No need to receive listener here
            super(itemView);
            // Initialize the views by finding them in the item layout
            imageViewCover = itemView.findViewById(R.id.imageViewCover);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewAuthor = itemView.findViewById(R.id.textViewAuthor);
            // Removed textViewPrice initialization
            textViewCategory = itemView.findViewById(R.id.textViewCategory);
            textViewTotalPages = itemView.findViewById(R.id.textViewTotalPages);

            // Click listener is now set in onBindViewHolder
        }
    }

    // Method to update the list of ebooks in the adapter and refresh the RecyclerView
    public void setEbookList(List<Ebook> ebookList) {
        this.ebookList = ebookList;
        notifyDataSetChanged(); // Notify the RecyclerView that the data has changed, triggering a re-render
    }
}
