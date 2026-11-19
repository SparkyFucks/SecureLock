// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

// Redirect build outputs and intermediates to C drive to prevent D drive I/O hardware issues
allprojects {
    val buildRoot = file("C:/Android/builds/SecureLock")
    layout.buildDirectory.set(File(buildRoot, project.name))
}
