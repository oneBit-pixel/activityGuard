package com.kotlin


import org.gradle.api.Plugin
import org.gradle.api.Project
import util.compareVersions
import util.getAGPVersion

/**
 * Created by DengLongFei
 * 2024/12/27
 */
class ActivityGuardPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val agpVersion = getAGPVersion(project)
        println("activityGuard:agpVersion $agpVersion")
        when {
            //大于等于 8.6.0
            compareVersions(agpVersion, "8.7.0") != -1 -> {
                ActivityGuardPlugin87().apply(project)
            }
            //大于等于 8.6.0
            compareVersions(agpVersion, "8.6.0") != -1 -> {
                ActivityGuardPlugin86().apply(project)
            }
            //大于等于 7.4.0
            compareVersions(agpVersion, "7.4.0") != -1 -> {
                ActivityGuardPlugin74().apply(project)
            }
            else -> {
                ActivityGuardPlugin74().apply(project)
            }
        }

    }
}