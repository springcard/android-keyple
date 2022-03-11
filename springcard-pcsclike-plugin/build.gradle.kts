///////////////////////////////////////////////////////////////////////////////
//  GRADLE CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
plugins {
    id("com.android.library")
    id("kotlin-android")
    kotlin("android.extensions")
    id("org.jetbrains.dokka")
    jacoco
    id("com.diffplug.spotless")
}

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:1.6.10")
    }
}

///////////////////////////////////////////////////////////////////////////////
//  APP CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
val kotlinVersion: String by project
val archivesBaseName: String by project
android {
    compileSdkVersion(31)
    buildToolsVersion("30.0.2")

    buildFeatures {
        viewBinding = true
    }
    defaultConfig {
        minSdkVersion(26)
        targetSdkVersion(31)
        versionName(project.version.toString())

        testInstrumentationRunner("android.support.test.runner.AndroidJUnitRunner")
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            minifyEnabled(false)
            isTestCoverageEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    val javaSourceLevel: String by project
    val javaTargetLevel: String by project
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(javaSourceLevel)
        targetCompatibility = JavaVersion.toVersion(javaTargetLevel)
    }

    testOptions {
        unitTests.apply {
            isReturnDefaultValues = true // mock Log Android object
            isIncludeAndroidResources = true
        }
    }

    lintOptions {
        isAbortOnError = false
    }

    // generate output aar with a qualified name : with version number
    libraryVariants.all {
        outputs.forEach { output ->
            if (output is com.android.build.gradle.internal.api.BaseVariantOutputImpl) {
                output.outputFileName =
                    "${archivesBaseName}-${project.version}.${output.outputFile.extension}"
            }
        }
    }

    kotlinOptions {
        jvmTarget = javaTargetLevel
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("debug").java.srcDirs("src/debug/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
        getByName("androidTest").java.srcDirs("src/androidTest/kotlin")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    //keyple
    implementation("org.eclipse.keyple:keyple-common-java-api:2.0.0")
    implementation("org.eclipse.keyple:keyple-plugin-java-api:2.0.0")
    implementation("org.eclipse.keyple:keyple-util-java-lib:2.0.0")

    //android
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.activity:activity-ktx:1.4.0")

    //Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.1")

    //logging
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("com.jakewharton.timber:timber:4.7.1") //Android
    implementation("at.favre.lib:slf4j-timber:1.0.1") //SLF4J binding for Timber

    /** Test **/
    testImplementation("androidx.test:core-ktx:1.4.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.10.0")
    testImplementation("org.robolectric:robolectric:4.4")

    androidTestImplementation("com.android.support.test:runner:1.0.2")
    androidTestImplementation("com.android.support.test.espresso:espresso-core:3.0.2")
}

///////////////////////////////////////////////////////////////////////////////
//  TASKS CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
tasks {
    dokkaHtml.configure {
        moduleName.set("Keyple® plugin SpringCard PCSC-like Android")
        dokkaSourceSets {
            named("main") {
                noAndroidSdkLink.set(false)
                includeNonPublic.set(false)
                includes.from(files(
                    "src/main/kotlin/com/springcard/keyple/plugin/android/pcsclike/package-info.md",
                    "src/main/kotlin/com/springcard/keyple/plugin/android/pcsclike/spi/package-info.md"))
                // Suppress a package
                perPackageOption {
                    matchingRegex.set(""".*springcard.pcsclike.*""") // will match all low level pcsclike packages and sub-packages
                    suppress.set(true)
                }
            }
        }
        pluginConfiguration<org.jetbrains.dokka.base.DokkaBase, org.jetbrains.dokka.base.DokkaBaseConfiguration> {
            customStyleSheets = listOf(rootProject.file("springcard-pcsclike-plugin/src/main/kdoc/logo-styles.css"))
            footerMessage = "Copyright @ 2022 <a href='https://www.springcard.com'>SpringCard</a>."
        }
    }
}
apply(plugin = "org.eclipse.keyple") // To do last

