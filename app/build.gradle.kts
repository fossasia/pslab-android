plugins {
    id("com.android.application")
}

apply(plugin = "realm-android")

val KEYSTORE_FILE = rootProject.file("scripts/pslab.jks")
val GITHUB_BUILD = System.getenv("GITHUB_ACTIONS") == "true" && KEYSTORE_FILE.exists()

android {
    namespace = "io.pslab"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.pslab"
        minSdk = 24
        targetSdk = 34
        versionCode = System.getenv("VERSION_CODE")?.toInt() ?: 1
        versionName = System.getenv("VERSION_NAME") ?: "1.0.0"
        resConfigs("en","ru","ar","si","pl")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (GITHUB_BUILD) {
            register("release") {
                storeFile = KEYSTORE_FILE
                storePassword = System.getenv("STORE_PASS")
                keyAlias = System.getenv("ALIAS")
                keyPassword = System.getenv("KEY_PASS")
            }
        }
    }

    buildTypes {
        debug {
            versionNameSuffix = "Version: "
            resValue("string", "version", "${versionNameSuffix}${defaultConfig.versionName}")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "version", "${defaultConfig.versionName}")
            signingConfig = if (GITHUB_BUILD) signingConfigs.getByName("release") else null
        }
    }
    lint {
        abortOnError = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
    }
}


dependencies {

    // Android stock libraries
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.preference:preference:1.2.1")
    implementation("androidx.browser:browser:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Custom tools libraries
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.github.bmelnychuk:atv:1.2.9")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.devlight.navigationtabstrip:navigationtabstrip:1.0.4")
    implementation("com.afollestad.material-dialogs", "commons", "0.9.6.0")
    implementation("com.github.mik3y:usb-serial-for-android:3.7.3")
    implementation("com.github.medyo:android-about-page:1.3.1")
    implementation("com.github.tiagohm.MarkdownView:library:0.19.0")
    implementation("com.github.mirrajabi:search-dialog:1.2.4")
    implementation("com.sdsmdg.harjot:croller:1.0.7")
    implementation("com.github.BeppiMenozzi:Knob:1.9.0")
    implementation("com.github.warkiz.widget:indicatorseekbar:2.1.2")
    implementation("com.github.Vatican-Cameos:CarouselPicker:1.2")
    implementation("com.github.anastr:speedviewlib:1.6.1")
    implementation("com.github.GoodieBag:ProtractorView:v1.2")
    implementation("com.github.Triggertrap:SeekArc:v1.1")

    // Apache commons
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("org.apache.commons:commons-lang3:3.14.0")

    // Picasso
    implementation("com.squareup.picasso:picasso:2.71828")

    // OKHTTP
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    androidTestImplementation("tools.fastlane:screengrab:2.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.6.1")
    androidTestImplementation("androidx.test:rules:1.6.1")

    // ButterKnife
    val butterKnifeVersion = "10.2.3"
    annotationProcessor("com.jakewharton:butterknife-compiler:$butterKnifeVersion")
    implementation("com.jakewharton:butterknife:$butterKnifeVersion")

    // Map libraries
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    implementation("org.osmdroid:osmdroid-mapsforge:6.1.18")
    implementation("org.osmdroid:osmdroid-geopackage:6.1.18") {
        exclude("org.osmdroid.gpkg")
        exclude("ormlite-core")
        exclude("com.j256.ormlite")
    }

    // Realm
    implementation("io.realm:android-adapters:4.0.0")
}
