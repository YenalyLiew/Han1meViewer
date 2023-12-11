// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.1" apply false
    id("com.android.library") version "8.1.1" apply false
    kotlin("android") version "1.9.20" apply false

    id("com.github.ben-manes.versions") version "0.50.0"
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}