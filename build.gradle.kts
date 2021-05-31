// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {

    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath(group = "com.android.tools.build", name = "gradle", version = "4.1.0")
        classpath(
            group = "org.jetbrains.kotlin",
            name = "kotlin-gradle-plugin",
            version = com.buildsrc.kts.Dependencies.Kotlin.version
        )
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle-backup-backup files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven { setUrl("https://jitpack.io") }
    }
}

tasks.register("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}
