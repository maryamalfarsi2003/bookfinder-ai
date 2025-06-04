plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.lab"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.lab"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    // ... existing dependencies

    // Firebase BOM (Platform) - Use the latest BOM version
    implementation(platform("com.google.firebase:firebase-bom:32.8.1")) // Corrected quotes and syntax

    implementation("com.google.ai.client.generativeai:generativeai:0.9.0") // Or the latest available version you found
    implementation("com.google.guava:guava:32.1.3-android") // Or the latest compatible Android version
    // Firebase SDKs
    implementation("com.google.firebase:firebase-firestore") // Corrected quotes and syntax
    implementation("com.google.firebase:firebase-storage") // Corrected quotes and syntax
    implementation("com.google.firebase:firebase-auth") // Assuming you need auth based on your original code

    // Glide library for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0") // Added parentheses
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0") // Added parentheses

    // RecyclerView for displaying lists
    implementation("androidx.recyclerview:recyclerview:1.3.2") // Use the latest version




    // CardView for styling list items (optional but recommended for better look)
    implementation("androidx.cardview:cardview:1.0.0") // Use the latest version

    // ... other dependencies (appcompat, material, activity, constraintlayout, junit, etc.)


    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}


