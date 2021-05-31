package com.buildsrc.kts

import org.gradle.api.Project
import java.io.File


/**

 *-
 *-
 *create by zhusw on 5/6/21 16:43
 */

private const val VERSION = "1.0"
private const val SNAPSHOT = true

object Publish {
    object Version {
        var versionName = VERSION
            private set
            get() = when (SNAPSHOT) {
                true -> "$field-SNAPSHOT"
                false -> field
            }
        const val versionCode = 1

        private fun getTimestamp(): String {
            return java.text.SimpleDateFormat(
                "yyyy-MM-dd-hh-mm-ss",
                java.util.Locale.CHINA
            ).format(java.util.Date(System.currentTimeMillis()))
        }

        fun getVersionTimestamp(): String {
            return "$versionName-${getTimestamp()}"
        }
    }

    object Maven {
        const val groupId = "com.foundation.service"
        const val artifactId = "net"
        private const val fileName = "local.properties"
        fun getCodingRepoUrl(project: Project): String {
            val pFile = File("${project.rootDir}/$fileName")
            val url = getProperties(pFile, "codingArtifactsRepoUrl")
            "url $url".log("Maven===")
            return url
        }

        fun getCodingMavenUsername(project: Project): String {
            val pFile = File("${project.rootDir}/$fileName")
            val userName = getProperties(pFile, "codingArtifactsGradleUsername")
            "userName $userName".log("Maven===")
            return userName
        }

        fun getCodingMavenPassword(project: Project): String {
            val pFile = File("${project.rootDir}/$fileName")
            val pw = getProperties(pFile, "codingArtifactsGradlePassword")
            "pw $pw".log("Maven===")
            return pw
        }

    }

}
