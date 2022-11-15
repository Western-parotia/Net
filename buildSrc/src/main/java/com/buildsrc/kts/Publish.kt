package com.buildsrc.kts

import com.buildsrc.kts.Repositories.mavenPassword
import org.gradle.api.artifacts.dsl.RepositoryHandler
import java.io.File


/**
 *-
 *-
 *create by zhusw on 5/6/21 16:43
 */


object Publish {

    private const val VERSION = "1.0.6"
    const val SNAPSHOT = true
    private const val ARTIFACT_ID = "net"

    object Version {
        val versionName = if (SNAPSHOT) "$VERSION-SNAPSHOT" else VERSION
        const val versionCode = 1
        const val artifactId = ARTIFACT_ID

        private fun getTimestamp(): String {
            return java.text.SimpleDateFormat(
                "yyyy-MM-dd-hh-mm-ss",
                java.util.Locale.CHINA
            ).format(java.util.Date())
        }

        fun getVersionTimestamp(): String {
            return "$versionName-${getTimestamp()}"
        }
    }

    object Maven {
        private val repositoryUserName: String
        private val repositoryPassword: String

        init {
            val lp = PropertiesUtils.localProperties
            val name = lp.getProperty("repositoryUserName")
            val password = lp.getProperty("repositoryPassword")
            if (name.isNullOrEmpty() || password.isNullOrEmpty()) {
                throw IllegalArgumentException("请在local.properties添加私有仓库的用户名（repositoryUserName）和密码（repositoryPassword）")
            }
            repositoryUserName = name
            repositoryPassword = password

            autoChangeMDVersion()
        }

        /**
         * 自动修改md里的版本号，匹配像：xxx:manager:1.0.1-SNAPSHOT"
         * 每次修改版本需要sync一下
         */
        private fun autoChangeMDVersion() {
            // window 默认运行目录是项目磁盘根目录
            // mac m1 是 /Users/jzxs/.gradle/daemon/6.5/../README.md
            var md = File(GlobalConfig.rootDirFile, "README.md")
            if (md.exists()) {
                val text = md.readText()
                ":$ARTIFACT_ID:.+\"".toRegex()
                    .find(text)
                    ?.let {
                        val currentVersion = ":$ARTIFACT_ID:${Version.versionName}\""
                        if (it.value == currentVersion) {
                            return@let
                        }
                        md.writeText(
                            text.substring(0, it.range.first)
                                    + currentVersion
                                    + text.substring(it.range.last + 1, text.length)
                        )
                    }
            }
        }

        /**
         * 获取模块3级包名，如：com.foundation.widget
         * 用于group
         */
        fun getThreePackage(projectDir: File): String {
            val st = getFourPackage(projectDir)
            return st.substring(0, st.lastIndexOf("."))
        }

        /**
         * 获取模块4级包名，如：com.foundation.widget.shape
         * 用于kotlin module做唯一标识
         */
        fun getFourPackage(projectDir: File): String {
            try {
                val javaFile = File(projectDir, "src/main/java")
                if (javaFile.exists()) {
                    val child = javaFile.listFiles()[0].listFiles()[0].listFiles()[0].listFiles()[0]
                    //先删掉前段路径，然后转为.
                    return child.absolutePath.substring(javaFile.absolutePath.length + 1)
                        .replace("/", ".")
                        .replace("\\", ".")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            throw  IllegalArgumentException("没有找到第四级包名")
        }

        /**
         * 使用本地账号密码（用于推送）
         */
        fun aliyunReleaseRepositories(rh: RepositoryHandler) {
            rh.mavenPassword(
                Repositories.aliyunReleaseAndArtifacts,
                repositoryUserName,
                repositoryPassword
            )
        }

        fun aliyunSnapshotRepositories(rh: RepositoryHandler) {
            rh.mavenPassword(
                Repositories.aliyunSnapshotAndArtifacts,
                repositoryUserName,
                repositoryPassword
            )
        }

        fun codingRepositories(rh: RepositoryHandler) {
            rh.mavenPassword(
                Repositories.codingMjMaven,
                repositoryUserName,
                repositoryPassword
            )
        }
    }

}
