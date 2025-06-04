package com.example.lab; // Your actual package name

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.example.lab.adapters.EbookAdapter;
import com.example.lab.models.Ebook;

import java.util.ArrayList;
import java.util.List;

// Your Admin Home Activity class
public class adminhome_screen extends AppCompatActivity implements EbookAdapter.OnItemClickListener { // Implement the listener interface

    // UI Elements
    Button addButton;
    private RecyclerView recyclerViewEbooks;
    private EbookAdapter ebookAdapter;
    private List<Ebook> ebookList;

    // Firebase Instance
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminhome_screen);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI Elements
        addButton = findViewById(R.id.buttonAddEbook);

        // Initialize RecyclerView and list
        recyclerViewEbooks = findViewById(R.id.recyclerViewEbooks);
        ebookList = new ArrayList<>();
        ebookAdapter = new EbookAdapter(this, ebookList);

        // Set up RecyclerView with a LayoutManager and the Adapter
        recyclerViewEbooks.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEbooks.setAdapter(ebookAdapter);

        // Set the item click listener for the adapter
        ebookAdapter.setOnItemClickListener(this); // 'this' refers to this activity implementing the interface

        // Set OnClickListener for the Add Ebook Button
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent AddIntent = new Intent(adminhome_screen.this, bookAdd.class);
                startActivity(AddIntent);
            }
        });

        // Fetch books from Firestore when the activity is created
        fetchBooksFromFirestore();
    }

    // Method to fetch books from Firebase Firestore
    private void fetchBooksFromFirestore() {
        db.collection("books")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ebookList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Ebook ebook = document.toObject(Ebook.class);
                                ebook.setId(document.getId()); // Set the Firestore document ID
                                ebookList.add(ebook);
                            }
                            ebookAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(adminhome_screen.this, "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Implement the onItemClick method from the EbookAdapter.OnItemClickListener interface
    @Override
    public void onItemClick(Ebook ebook) {
        // Handle item click - Start the Update/Delete Ebook Activity
        Toast.makeText(this, "Clicked: " + ebook.getName(), Toast.LENGTH_SHORT).show(); // Optional: Show a toast

        Intent intent = new Intent(adminhome_screen.this, UpdateDeleteEbookActivity.class); // Replace UpdateDeleteEbookActivity.class with the actual name
        intent.putExtra("ebookId", ebook.getId()); // Pass the Firestore document ID to the next activity
        startActivity(intent);
    }

    // Refresh the list when returning to this activity
    @Override
    protected void onResume() {
        super.onResume();
        fetchBooksFromFirestore();
    }
}
