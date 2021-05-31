import com.buildsrc.kts.Dependencies
import com.buildsrc.kts.Publish

plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
}

apply("../common.gradle")

val versionTimestamp = Publish.Version.getVersionTimestamp()

android {
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            isMinifyEnabled = false
//            consumerProguardFiles("consumer-rules.pro") lib单独打包 此属性是无效的
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    afterEvaluate {
        buildTypes.forEach {
            it.buildConfigField("Integer", "versionCode", Publish.Version.versionCode.toString())
            it.buildConfigField("String", "versionName", "\"${Publish.Version.versionName}\"")
            it.buildConfigField("String", "versionTimeStamp", "\"$versionTimestamp\"")
        }
    }

}

dependencies {
    implementation(Dependencies.Kotlin.kotlin_stdlib)
    implementation(Dependencies.JetPack.core_ktx)
    implementation(Dependencies.Retrofit.retorifit)
    implementation(Dependencies.Coroutines.coroutines_android)
    implementation(Dependencies.Retrofit.gson)
    implementation(Dependencies.JetPack.lifecycle_liveData_ktx)
    implementation(Dependencies.JetPack.lifecycle_viewModel_ktx)
}

val sourceCodeTask: Jar = tasks.register("sourceCode", Jar::class.java) {
    from(android.sourceSets.getByName("main").java.srcDirs)
    classifier = "sources"
}.get()


tasks.register("createGitTagAndPush", Exec::class.java) {
    commandLine("git", "push", "origin", versionTimestamp)
}.get().dependsOn(tasks.register("createGitTag", Exec::class.java) {
    commandLine("git", "tag", versionTimestamp, "-m", "autoCreateWithMavenPublish")
})

publishing {
    val versionName = Publish.Version.versionName
    val groupId = Publish.Maven.groupId
    val artifactId = Publish.Maven.artifactId

    publications {
        create<MavenPublication>("Net") {
            setGroupId(groupId)
            setArtifactId(artifactId)
            version = versionName
            artifact(sourceCodeTask)
            afterEvaluate {//在脚本读取完成后绑定
                val bundleReleaseAarTask: Task = tasks.getByName("bundleReleaseAar")
                bundleReleaseAarTask.finalizedBy("createGitTagAndPush")
                artifact(bundleReleaseAarTask)
            }
//            artifact("$buildDir/outputs/aar/loading-release.aar")//直接指定文件
            " allDependencies=${configurations.implementation.get().allDependencies.size}".log("dep============")
            pom.withXml {
                val dependenciesNode = asNode().appendNode("dependencies")
                configurations.implementation.get().allDependencies.forEach {
                    if (it.version != "unspecified" && it.name != "unspecified") {
                        val depNode = dependenciesNode.appendNode("dependency")
                        depNode.appendNode("groupId", it.group)
                        depNode.appendNode("artifactId", it.name)
                        depNode.appendNode("version", it.version)
                    }
                }
            }

        }
        repositories {
            maven {
                setUrl(Publish.Maven.getCodingRepoUrl(project))
                credentials {
                    username = Publish.Maven.getCodingMavenUsername(project)
                    password = Publish.Maven.getCodingMavenPassword(project)
                }
            }
        }
    }

}


fun String.log(tag: String) {
    println("$tag:$this")
}