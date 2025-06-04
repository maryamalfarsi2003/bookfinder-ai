package com.example.lab; // Your package name

// Necessary imports
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull; // Make sure this is imported for OnCompleteListener Task
import androidx.appcompat.app.AppCompatActivity;

// Firebase imports
import com.google.android.gms.tasks.OnCompleteListener; // Make sure this is imported
import com.google.android.gms.tasks.Task;             // Make sure this is imported
import com.google.firebase.auth.AuthResult;           // Make sure this is imported
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// Class name follows Java convention (e.g., LoginActivity) but using your name 'login'
public class login extends AppCompatActivity {

    // Declare UI elements and Firebase Auth
    TextView textView2;
    EditText login_email;
    EditText login_password;
    Button login_button;
    Button to_register_button; // Button to navigate to registration
    FirebaseAuth Auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        Auth = FirebaseAuth.getInstance();

        // Enable edge-to-edge display (optional)
        EdgeToEdge.enable(this);

        // Set the layout file for this activity
        setContentView(R.layout.activity_login);

        // Find UI elements by their IDs from the XML layout
        textView2 = findViewById(R.id.textView2);
        login_email = findViewById(R.id.login_email);
        login_password = findViewById(R.id.login_password);
        login_button = findViewById(R.id.login_button);
        to_register_button = findViewById(R.id.to_register_button); // Find the register button

        // Set OnClickListener for the LOGIN button
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call the method to handle login logic when clicked
                loginUserAccount();
            }
        });

        // Set OnClickListener for the TO REGISTER button
        to_register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the Register activity
                // *** IMPORTANT: Make sure 'register.class' is the correct name of your registration Java class ***
                Intent registerIntent = new Intent(login.this, register.class);
                // Start the Register activity
                startActivity(registerIntent);
            }
        });

    } // End of onCreate method

    // Method to handle user login logic
    public void loginUserAccount() {
        // Use final for the email variable so it can be accessed in the inner class/lambda
        final String email;
        String password;

        // Get text from EditText fields
        email = login_email.getText().toString().trim(); // Added trim() for safety
        password = login_password.getText().toString().trim(); // Added trim() for safety

        // Validate if fields are empty
        if (TextUtils.isEmpty(email)) {
            login_email.setError("Email cannot be empty"); // Set error on the field
            return; // Stop execution if validation fails
        }
        if (TextUtils.isEmpty(password)) {
            login_password.setError("Password cannot be empty"); // Set error on the field
            return; // Stop execution if validation fails
        }

        // Show progress indicator (optional, recommended)
        // progressBar.setVisibility(View.VISIBLE);

        // Attempt to sign in with Firebase Auth
        Auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() { // Explicit type added
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Hide progress indicator (optional)
                        // progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            // Login successful
                            FirebaseUser user = Auth.getCurrentUser(); // Good practice to check user if needed

                            // --- START OF NEW ADMIN CHECK LOGIC ---
                            Intent destinationIntent; // Declare the Intent variable

                            // Check if the logged-in email matches the admin email
                            // Use .equals() for string comparison!
                            if ("admin1@gmail.com".equals(email)) {
                                // If it's the admin, set the destination to adminhome_screen
                                Toast.makeText(getApplicationContext(), "Admin Login Successful!", Toast.LENGTH_SHORT).show();
                                // *** Ensure adminhome_screen.class exists and is declared in AndroidManifest.xml ***
                                destinationIntent = new Intent(login.this, adminhome_screen.class);
                            } else {
                                // Otherwise, set the destination to the regular home_screen
                                Toast.makeText(getApplicationContext(), "Login Successful!", Toast.LENGTH_SHORT).show();
                                // *** Ensure home_screen.class exists and is declared in AndroidManifest.xml ***
                                destinationIntent = new Intent(login.this, home_screen.class);
                            }

                            // Start the determined activity
                            startActivity(destinationIntent);
                            finish(); // Close the login activity so user can't go back to it with back button
                            // --- END OF NEW ADMIN CHECK LOGIC ---

                        } else {
                            // Login failed
                            Toast.makeText(getApplicationContext(), "Login Failed! " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }); // End of addOnCompleteListener

    } // End of loginUserAccount method

} // End of login class
