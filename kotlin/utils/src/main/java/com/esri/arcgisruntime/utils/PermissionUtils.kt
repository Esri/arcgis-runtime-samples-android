package com.esri.arcgisruntime.utils

import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import kotlin.reflect.KFunction0

class PermissionUtils {


  companion object {

    /**
     * Request permissions at runtime.
     */
    fun requestPermission(activity: Activity, method: KFunction0<Unit>, reqPermission: Array<String>,
                          requestCode: Int) {
      reqPermission.forEach {
        when {
          ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED -> method()
          else -> // request permission
            ActivityCompat.requestPermissions(activity, reqPermission, requestCode)

        }
      }
    }

    fun onRequestPermissionResult(activity: Activity, method: KFunction0<Unit>, permissions: Array<String>,
                                  grantResults: IntArray) {
      permissions.forEach {
        when {
          grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED -> method()
          else -> Toast.makeText(activity, activity.localClassName + " could not be run without " + it + " permission",
              Toast.LENGTH_LONG).show()
        }
      }
    }
  }
}