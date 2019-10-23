package com.ksc.movingalarm

import android.app.IntentService
import android.content.Intent
import android.content.Context

// TODO: Rename actions, choose action names that describe tasks that this
// IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
private const val ACTION_FAIL = "com.ksc.movingalarm.action.FAIL"
private const val ACTION_SUCCESS = "com.ksc.movingalarm.action.SUCCESS"

// TODO: Rename parameters
private const val EXTRA_PARAM1 = "com.ksc.movingalarm.extra.PARAM1"
private const val EXTRA_PARAM2 = "com.ksc.movingalarm.extra.PARAM2"

class MyIntentService : IntentService("MyIntentService") {

    override fun onHandleIntent(intent: Intent) {
        val param1 = intent.getStringExtra(EXTRA_PARAM1)
        val param2 = intent.getStringExtra(EXTRA_PARAM2)
        when (intent.action) {
            ACTION_FAIL -> {
                handleActionFAIL(param1, param2)
            }
            ACTION_SUCCESS -> {
                handleActionSUCCESS(param1, param2)
            }
        }
    }

    private fun handleActionFAIL(param1: String, param2: String) {
        TODO("Handle action Foo")
    }

    private fun handleActionSUCCESS(param1: String, param2: String) {
        TODO("Handle action Baz")
    }
}
