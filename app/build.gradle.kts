plugins {
    id("com.android.application")
}

apply(plugin = "realm-android")

val keystoreExists = System.getenv("KEYSTORE_FILE") != null

android {
    namespace = "io.pslab"
    compileSdk = 33

    defaultConfig {
        applicationId = "io.pslab"
        minSdk = 21
        targetSdk = 31
        versionCode = 22
        versionName = "2.1.0"
    }

    signingConfigs {
        if (keystoreExists) {
            register("release") {
                storeFile = file(System.getenv("KEYSTORE_FILE"))
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
            signingConfig = if (keystoreExists) signingConfigs.getByName("release") else null
        }
    }
    lint {
        abortOnError = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    // Android stock libraries
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.preference:preference:1.2.0")
    implementation("androidx.browser:browser:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Custom tools libraries
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.github.bmelnychuk:atv:1.2.9")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.devlight.navigationtabstrip:navigationtabstrip:1.0.4")
    implementation("com.afollestad.material-dialogs:core:0.9.6.0")
    implementation("com.github.medyo:android-about-page:1.3.1")
    implementation("com.github.tiagohm.MarkdownView:library:0.19.0")
    implementation("com.github.mirrajabi:search-dialog:1.2.4")
    implementation("com.sdsmdg.harjot:croller:1.0.7")
    implementation("com.github.BeppiMenozzi:Knob:1.9.0")
    implementation("com.github.warkiz.widget:indicatorseekbar:2.1.2")
    implementation("com.github.Vatican-Cameos:CarouselPicker:1.2")
    implementation("com.github.anastr:speedviewlib:1.6.0")
    implementation("com.github.GoodieBag:ProtractorView:v1.2")
    implementation("com.github.Triggertrap:SeekArc:v1.1")

    // Apache commons
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("org.apache.commons:commons-lang3:3.12.0")

    // Picasso
    implementation("com.squareup.picasso:picasso:2.71828")

    // OKHTTP
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    // ButterKnife
    val butterKnifeVersion = "10.2.3"
    annotationProcessor("com.jakewharton:butterknife-compiler:$butterKnifeVersion")
    implementation("com.jakewharton:butterknife:$butterKnifeVersion")

    // Map libraries
    val osmDroidVersion = "6.1.11"
    implementation("org.osmdroid:osmdroid-android:$osmDroidVersion")
    implementation("org.osmdroid:osmdroid-mapsforge:$osmDroidVersion")
    implementation("org.osmdroid:osmdroid-geopackage:$osmDroidVersion")

    // Realm
    implementation("io.realm:android-adapters:4.0.0")
}
