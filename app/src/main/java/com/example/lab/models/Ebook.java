package com.example.lab.models; // Recommended to put model classes in a 'models' package

// This class represents a single Ebook object
// It should match the fields you store in your Firestore 'books' collection
public class Ebook {
    private String id; // To store the Firestore document ID (useful for updates/deletes)
    private String name;
    private String author;
    private int totalPages;
    private String category;
    private String imageResourceName; // For static images stored in resources

    // Required empty public constructor for Firestore to automatically convert documents to objects
    public Ebook() {
    }

    // Constructor (optional, but good practice for creating Ebook objects in code)
    // Removed price from constructor
    public Ebook(String id, String name, String author, int totalPages, String category, String imageResourceName) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.totalPages = totalPages;
        this.category = category;
        this.imageResourceName = imageResourceName;
    }

    // Getters (required by Firestore to retrieve data from the object)
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public int getTotalPages() {
        return totalPages;
    }

    // Removed getPrice()
    // public double getPrice() {
    //     return price;
    // }

    public String getCategory() {
        return category;
    }

    public String getImageResourceName() {
        return imageResourceName;
    }

    // Setters (required by Firestore to set data when reading documents)
    // Firestore automatically sets the ID when reading, but a setter is good practice if you set it manually elsewhere.
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    // Removed setPrice()
    // public void setPrice(double price) {
    //     this.price = price;
    // }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setImageResourceName(String imageResourceName) {
        this.imageResourceName = imageResourceName;
    }
}
