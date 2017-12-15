package com.pgssoft.testwarez.test.utils

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.support.test.runner.lifecycle.ActivityLifecycleMonitor
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import android.support.test.runner.lifecycle.Stage

import java.util.ArrayList
import java.util.EnumSet

/**
 * Created by mnicpon on 08/11/2017.
 */

class ActivityFinisher private constructor() : Runnable {
    private val activityLifecycleMonitor: ActivityLifecycleMonitor

    init {
        this.activityLifecycleMonitor = ActivityLifecycleMonitorRegistry.getInstance()
    }

    override fun run() {
        val activities = ArrayList<Activity>()

        for (stage in EnumSet.range(Stage.CREATED, Stage.STOPPED)) {
            activities.addAll(activityLifecycleMonitor.getActivitiesInStage(stage))
        }

        for (activity in activities) {
            if (!activity.isFinishing) {
                activity.finish()
            }
        }
    }

    companion object {

        fun finishOpenActivities() {
            Handler(Looper.getMainLooper()).post(ActivityFinisher())
        }
    }
}
