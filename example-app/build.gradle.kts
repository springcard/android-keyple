import java.util.Properties
///////////////////////////////////////////////////////////////////////////////
//  GRADLE CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
plugins {
    id("com.android.application")
    id("kotlin-android")
    kotlin("android.extensions")
    id("com.diffplug.spotless")
}

///////////////////////////////////////////////////////////////////////////////
//  APP CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
val kotlinVersion: String by project
val archivesBaseName: String by project
android {
    compileSdkVersion(31)
    buildToolsVersion("30.0.3")

    signingConfigs {
        create("default") {
            val properties = Properties().apply {
                load(File("signing.properties").reader())
            }
            storeFile = File(properties.getProperty("storeFilePath"))
            storePassword = properties.getProperty("storePassword")
            keyPassword = properties.getProperty("keyPassword")
            keyAlias = properties.getProperty("keyAlias")
        }
    }

    defaultConfig {
        applicationId("com.springcard.keyple.plugin.android.pcsc.example")
        minSdkVersion(26)
        targetSdkVersion(31)
        versionName(project.version.toString())
        versionCode(3)
        testInstrumentationRunner("android.support.test.runner.AndroidJUnitRunner")
        multiDexEnabled = true
    }

    buildTypes {
        getByName("debug") {
            minifyEnabled(false)
            isTestCoverageEnabled = true
            isDebuggable = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("release") {
            minifyEnabled(true)
            isTestCoverageEnabled = false
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // signingConfig = signingConfigs.getByName("default")
        }
    }

    val javaSourceLevel: String by project
    val javaTargetLevel: String by project
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(javaSourceLevel)
        targetCompatibility = JavaVersion.toVersion(javaTargetLevel)
    }

    packagingOptions {
        exclude("META-INF/NOTICE.md")
    }

    lintOptions {
        isAbortOnError = false
    }

    kotlinOptions {
        jvmTarget = javaTargetLevel
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
    }
}

dependencies {

    //Keyple Common
    implementation(project(path = ":springcard-pcsclike-plugin"))

    implementation("org.calypsonet.terminal:calypsonet-terminal-reader-java-api:1.0.+") { isChanging = true }
    implementation("org.calypsonet.terminal:calypsonet-terminal-calypso-java-api:1.2.+") { isChanging = true }
    implementation("org.eclipse.keyple:keyple-common-java-api:2.0.+") { isChanging = true }
    implementation("org.eclipse.keyple:keyple-service-java-lib:2.0.1")
    implementation("org.eclipse.keyple:keyple-service-resource-java-lib:2.0.1")
    implementation("org.eclipse.keyple:keyple-card-calypso-java-lib:2.1.1-SNAPSHOT") { isChanging = true }
    implementation("org.eclipse.keyple:keyple-util-java-lib:2.1.0-SNAPSHOT") { isChanging = true }

    /*
    Android components
    */
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.activity:activity-ktx:1.2.1")
    implementation("androidx.fragment:fragment-ktx:1.3.1")

    /*
    Log
    */
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("com.jakewharton.timber:timber:4.7.1")
    implementation("at.favre.lib:slf4j-timber:1.0.1") //SLF4J binding for Timber
    /*
    Kotlin
    */
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    /*
    Coroutines
    */
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.3")

    implementation("androidx.multidex:multidex:2.0.1")

    testImplementation("junit:junit:4.12")
    androidTestImplementation("com.android.support.test:runner:1.0.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}