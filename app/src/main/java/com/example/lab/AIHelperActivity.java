package com.example.lab; // Your actual package name

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // Import for getting color
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Typeface; // Import Typeface for text style
import android.os.Bundle;
import android.os.Handler; // Import Handler for delayed actions
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.material.textfield.TextInputEditText;

// Removed imports for Google AI Generative AI SDK and Guava Futures
// import com.google.ai.client.generativeai.GenerativeModel;
// import com.google.ai.client.generativeai.java.GenerativeModelFutures;
// import com.google.ai.client.generativeai.GenerateContentResponse;
// import com.google.ai.client.generativeai.Content;
// import com.google.common.util.concurrent.ListenableFuture;
// import com.google.common.util.concurrent.Futures;

import com.example.lab.adapters.ChatMessageAdapter;
import com.example.lab.models.ChatMessage;
import com.example.lab.models.Ebook;

import java.util.ArrayList;
import java.util.HashSet; // Import HashSet to get unique genres
import java.util.List;
import java.util.Locale;
import java.util.Set; // Import Set

// Activity for the AI Helper chat interface (Interactive Rule-Based)
public class AIHelperActivity extends AppCompatActivity {

    private static final String TAG = "AIHelperActivity"; // Tag for Logcat

    // UI Elements
    private RecyclerView recyclerViewChat;
    private TextInputEditText editTextMessage;
    private Button buttonSend;
    private LinearLayout linearLayoutSuggestedQuestions; // LinearLayout to hold suggested question TextViews

    // Chat Adapter and Message List
    private ChatMessageAdapter chatMessageAdapter;
    private List<ChatMessage> messageList;

    // Firebase Instance
    private FirebaseFirestore db;

    // List to hold all books fetched from Firestore
    private List<Ebook> allBooksList;

    // List of predefined suggested questions
    private final List<String> suggestedQuestions = new ArrayList<>();

    // --- Conversation State Variables ---
    // Enum or constants to define the current state of the conversation
    private enum ConversationState {
        IDLE, // Waiting for a general query or suggested question selection
        WAITING_FOR_GENRE, // User selected "Recommend a book", waiting for genre
        WAITING_FOR_BOOK_TITLE // User selected "Tell me about [Book Title]", waiting for title
    }

    private ConversationState currentState = ConversationState.IDLE;
    // --- End Conversation State Variables ---


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_helper); // Link to your AI Helper layout

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI Elements
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        linearLayoutSuggestedQuestions = findViewById(R.id.linearLayoutSuggestedQuestions); // Initialize LinearLayout

        // Initialize message list and adapter
        messageList = new ArrayList<>();
        chatMessageAdapter = new ChatMessageAdapter(this, messageList);

        // Set up RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setAdapter(chatMessageAdapter);

        // Initialize the list to hold all books
        allBooksList = new ArrayList<>();

        // Fetch all books from Firestore when the activity starts
        fetchAllBooks();

        // Populate the suggested questions list
        populateSuggestedQuestions();

        // Add an initial welcome message from the AI
        addAIMessage("Hello! I'm your Ebook Helper. Ask me about the books available in the library.");


        // Set OnClickListener for the Send Button
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }


    // Method to populate the predefined suggested questions list
    private void populateSuggestedQuestions() {
        // Add your desired suggested questions here
        suggestedQuestions.clear(); // Clear previous list if any
        suggestedQuestions.add("List all books");
        suggestedQuestions.add("Recommend a book");
        suggestedQuestions.add("Tell me about a book"); // Changed to be a prompt
        suggestedQuestions.add("What genres do you have?");
        // Add more questions as needed

        // Display the suggested questions in the LinearLayout
        displaySuggestedQuestions();
    }

    // Method to dynamically create and display TextViews for suggested questions
    private void displaySuggestedQuestions() {
        linearLayoutSuggestedQuestions.removeAllViews(); // Clear any existing views

        for (String question : suggestedQuestions) {
            TextView questionTextView = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            // Use the dimension resource for margin
            params.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.suggested_question_margin));
            questionTextView.setLayoutParams(params);
            // Apply the background drawable
            questionTextView.setBackgroundResource(R.drawable.suggested_question_background);
            // Use dimension resources for padding
            questionTextView.setPadding(
                    getResources().getDimensionPixelSize(R.dimen.suggested_question_padding_horizontal),
                    getResources().getDimensionPixelSize(R.dimen.suggested_question_padding_vertical),
                    getResources().getDimensionPixelSize(R.dimen.suggested_question_padding_horizontal),
                    getResources().getDimensionPixelSize(R.dimen.suggested_question_padding_vertical)
            );
            questionTextView.setText(question);
            // Use ContextCompat to get color
            questionTextView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            questionTextView.setClickable(true);
            questionTextView.setFocusable(true);

            // Set click listener for each suggested question
            questionTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String selectedQuestion = ((TextView) v).getText().toString();
                    // Simulate sending this question as a user message
                    addMessage(new ChatMessage(selectedQuestion, true));
                    // Process this selected question with the rule-based system
                    processMessageWithRules(selectedQuestion);
                    // Scroll to the bottom
                    recyclerViewChat.scrollToPosition(messageList.size() - 1);
                }
            });

            linearLayoutSuggestedQuestions.addView(questionTextView);
        }
    }


    // Method to fetch all books from Firestore
    private void fetchAllBooks() {
        db.collection("books")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        allBooksList.clear(); // Clear previous data
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Ebook ebook = document.toObject(Ebook.class);
                            allBooksList.add(ebook);
                        }
                        Log.d(TAG, "Successfully fetched " + allBooksList.size() + " books.");
                        buttonSend.setEnabled(true); // Enable send button once books are loaded
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AIHelperActivity.this, "Error loading book data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error fetching books", e);
                        buttonSend.setEnabled(false);
                        addAIMessage("Sorry, I couldn't load the book data right now. Please try again later.");
                    }
                });
    }

    // Method to handle sending a message (from the text input)
    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();

        if (messageText.isEmpty()) {
            return;
        }

        // Add user message to the chat display
        addMessage(new ChatMessage(messageText, true));

        // Clear the input field
        editTextMessage.setText("");

        // Process the user message with the rule-based system
        processMessageWithRules(messageText);
    }

    // Method to add a message to the chat display
    private void addMessage(ChatMessage message) {
        messageList.add(message);
        chatMessageAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerViewChat.scrollToPosition(messageList.size() - 1);
    }

    // Method to add an AI message to the chat display
    private void addAIMessage(String text) {
        addMessage(new ChatMessage(text, false));
    }


    // --- Rule-Based AI Logic with State Management ---
    private void processMessageWithRules(String userQuery) {
        String lowerQuery = userQuery.toLowerCase(Locale.getDefault()).trim();

        // Show a "thinking" message while processing
        addAIMessage("Thinking...");
        // Scroll to the bottom immediately after adding "Thinking..."
        recyclerViewChat.scrollToPosition(messageList.size() - 1);

        // Use a Handler for a short delay before processing and responding
        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // Remove the "Thinking..." message after the delay
                        removeThinkingMessage();

                        String aiResponse = "";

                        // Check the current state to determine how to process the query
                        if (currentState == ConversationState.WAITING_FOR_GENRE) {
                            // We were waiting for a genre, process the user's input as a genre
                            aiResponse = generateRecommendationResponse(userQuery); // userQuery is the genre
                            currentState = ConversationState.IDLE; // Reset state after processing
                        } else if (currentState == ConversationState.WAITING_FOR_BOOK_TITLE) {
                            // We were waiting for a book title, process the user's input as a title
                            aiResponse = generateBookDetailsResponse(userQuery); // userQuery is the book title
                            currentState = ConversationState.IDLE; // Reset state after processing
                        } else {
                            // In IDLE state, process the general query or suggested question selection
                            if (allBooksList.isEmpty()) {
                                aiResponse = "I don't have any book data loaded right now. Please try again later.";
                            } else if (lowerQuery.contains("list all books") || lowerQuery.contains("what books do you have")) {
                                aiResponse = generateListAllBooksResponse();
                            } else if (lowerQuery.contains("recommend a book")) {
                                // User selected "Recommend a book", ask for the genre
                                aiResponse = "Sure, what genre are you interested in?";
                                currentState = ConversationState.WAITING_FOR_GENRE; // Set state to wait for genre
                            } else if (lowerQuery.contains("tell me about a book")) {
                                // User selected "Tell me about a book", ask for the title
                                aiResponse = "Okay, what is the title of the book you want to know about?";
                                currentState = ConversationState.WAITING_FOR_BOOK_TITLE; // Set state to wait for book title
                            } else if (lowerQuery.contains("what genres do you have") || lowerQuery.contains("list genres")) {
                                aiResponse = generateGenreListResponse();
                            } else if (lowerQuery.contains("hello") || lowerQuery.contains("hi")) {
                                aiResponse = "Hello! How can I help you find a book today?";
                            }
                            // --- Added check for unhandled queries in IDLE state ---
                            else {
                                // If in IDLE state and query doesn't match any specific rule
                                aiResponse = "I can only answer questions about the books in the library. Please select one of the suggested questions or ask about listing books, recommendations by genre, or details about a specific book.";
                            }
                            // --- End added check ---
                        }

                        // Add the generated AI response to the chat
                        addAIMessage(aiResponse);
                        // Scroll to the bottom
                        recyclerViewChat.scrollToPosition(messageList.size() - 1);
                    }
                }, 500); // 500 milliseconds delay (adjust as needed)
    }

    // Helper method to generate a recommendation response based on a genre
    private String generateRecommendationResponse(String genreQuery) {
        if (allBooksList.isEmpty()) {
            return "I don't have any book data loaded to make recommendations right now.";
        }

        String lowerGenreQuery = genreQuery.toLowerCase(Locale.getDefault()).trim();
        List<Ebook> recommendedBooks = new ArrayList<>();

        // Find books that match the requested genre
        for (Ebook book : allBooksList) {
            if (book.getCategory() != null && book.getCategory().toLowerCase(Locale.getDefault()).contains(lowerGenreQuery)) {
                recommendedBooks.add(book);
            }
        }

        if (recommendedBooks.isEmpty()) {
            return "Sorry, I couldn't find any books in the '" + genreQuery + "' genre.";
        } else {
            StringBuilder recommendation = new StringBuilder("Here are some books in the '" + genreQuery + "' genre:\n");
            for (int i = 0; i < recommendedBooks.size(); i++) {
                recommendation.append("- ").append(recommendedBooks.get(i).getName()).append(" by ").append(recommendedBooks.get(i).getAuthor());
                if (i < recommendedBooks.size() - 1) {
                    recommendation.append("\n");
                }
            }
            return recommendation.toString();
        }
    }

    // Helper method to generate a book details response based on a book title
    private String generateBookDetailsResponse(String bookTitleQuery) {
        if (allBooksList.isEmpty()) {
            return "I don't have any book data loaded to provide details right now.";
        }

        String lowerBookTitleQuery = bookTitleQuery.toLowerCase(Locale.getDefault()).trim();
        Ebook foundBook = null;

        // Search for a book whose name contains the query string (case-insensitive)
        for (Ebook book : allBooksList) {
            if (book.getName().toLowerCase(Locale.getDefault()).contains(lowerBookTitleQuery)) {
                foundBook = book;
                break; // Found a match, exit the loop
            }
        }

        if (foundBook != null) {
            // Found a book, generate details response
            return "Details for " + foundBook.getName() + ":\n" +
                    "Author: " + foundBook.getAuthor() + "\n" +
                    "Category: " + foundBook.getCategory() + "\n" +
                    "Pages: " + foundBook.getTotalPages() + ".";
            // Add other details like price if needed
        } else {
            // No book found matching the query
            return "Sorry, I couldn't find a book with the title '" + bookTitleQuery + "' in the library.";
        }
    }

    // Helper method to generate a list of all books response
    private String generateListAllBooksResponse() {
        if (allBooksList.isEmpty()) {
            return "I don't have any book data loaded right now.";
        }

        StringBuilder bookListResponse = new StringBuilder("Here are the books I have information on:\n");
        for (Ebook book : allBooksList) {
            bookListResponse.append("- ").append(book.getName()).append(" by ").append(book.getAuthor()).append("\n");
        }
        return bookListResponse.toString();
    }

    // Helper method to generate a list of available genres
    private String generateGenreListResponse() {
        if (allBooksList.isEmpty()) {
            return "I don't have any book data loaded to list genres right now.";
        }

        Set<String> uniqueGenres = new HashSet<>(); // Use a Set to automatically handle duplicates
        for (Ebook book : allBooksList) {
            String category = book.getCategory();
            if (category != null && !category.isEmpty()) {
                uniqueGenres.add(category);
            }
        }

        if (uniqueGenres.isEmpty()) {
            return "I don't have any specific genre information available.";
        }

        StringBuilder genreListResponse = new StringBuilder("Available genres:\n");
        for (String genre : uniqueGenres) {
            genreListResponse.append("- ").append(genre).append("\n");
        }
        return genreListResponse.toString();
    }


    // Helper method to remove the "Thinking..." message
    private void removeThinkingMessage() {
        if (!messageList.isEmpty() && messageList.get(messageList.size() - 1).getText().equals("Thinking...")) {
            messageList.remove(messageList.size() - 1);
            chatMessageAdapter.notifyItemRemoved(messageList.size());
        }
    }
}
