/* Copyright 2017 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.esri.arcgisruntime.sample.portaluserinfo

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.sample.portaluserinfo.databinding.ActivityMainBinding
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

  private val activityMainBinding by lazy {
    ActivityMainBinding.inflate(layoutInflater)
  }

  private val userImage: ImageView by lazy {
    activityMainBinding.userImage
  }

  // objects that implement Loadable must be class fields to prevent being garbage collected before loading
  private lateinit var portal: Portal

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(activityMainBinding.root)

    // Set the DefaultAuthenticationChallengeHandler to allow authentication with the portal.
    val handler = DefaultAuthenticationChallengeHandler(this)
    AuthenticationManager.setAuthenticationChallengeHandler(handler)
    // Set loginRequired to true always prompt for credential,
    // When set to false to only login if required by the portal
    portal = Portal("https://www.arcgis.com", true)

    portal.addDoneLoadingListener {
      when (portal.loadStatus) {
        LoadStatus.LOADED -> {
          val portalInformation = portal.portalInfo
          val portalInfoName = portalInformation.portalName
          portalName.text = portalInfoName
          // this portal does not require authentication, if null send toast message
          if (portal.user != null) {
            // Get the authenticated portal user
            val user = portal.user
            // get the users full name
            val fullname = user.fullName
            userName.text = fullname
            // get the users email
            val userEmail = user.email
            email.text = userEmail
            // get the created date
            val startDate = user.created
            val simpleDateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.US)
            val formatDate = simpleDateFormat.format(startDate.time)
            createDate.text = formatDate
            // check if user profile thumbnail exists
            if (user.thumbnailFileName != null) {
              // fetch the thumbnail
              val thumbnailFuture = user.fetchThumbnailAsync()
              thumbnailFuture.addDoneListener {
                val itemThumbnailData = thumbnailFuture.get()
                if (itemThumbnailData != null && itemThumbnailData.isNotEmpty()) {
                  // create Bitmap to use as required
                  val itemThumbnail =
                    BitmapFactory.decodeByteArray(itemThumbnailData, 0, itemThumbnailData.size)
                  // set the Bitmap to the ImageView
                  userImage.setImageBitmap(itemThumbnail)
                }
              }
            } else {
              Toast.makeText(this, "No thumbnail associated with $fullname", Toast.LENGTH_LONG)
                .show()
            }
          } else {
            Toast.makeText(
              this,
              "User did not authenticate against $portalInfoName",
              Toast.LENGTH_LONG
            ).show()
          }
        }
        LoadStatus.FAILED_TO_LOAD -> {
          Toast.makeText(this, "Portal failed to load", Toast.LENGTH_LONG).show()
        }
      }
    }
    portal.loadAsync()
  }
}
