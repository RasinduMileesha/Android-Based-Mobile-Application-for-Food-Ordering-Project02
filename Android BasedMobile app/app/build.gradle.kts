plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.andro"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.andro"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("net.sourceforge.jtds:jtds:1.3.1")
    implementation("com.itextpdf:itext7-core:7.1.15")
    implementation ("com.github.barteksc:android-pdf-viewer:2.8.2")
    implementation ("com.itextpdf:itext7-core:7.2.0")
    implementation ("net.sourceforge.jtds:jtds:1.3.1")
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation ("de.codecrafters.tableview:tableview:2.8.0")










}