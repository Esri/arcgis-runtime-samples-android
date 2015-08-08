/* Copyright 2015 Esri
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

package com.arcgis.android.samples.maps.basemaps;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Tests to verify that the behavior of {@link MainActivity} is correct.
 * <p>
 * Note that in order to scroll the list you shouldn't use {@link ViewActions#scrollTo()} as
 * {@link android.support.test.espresso.Espresso#onData(org.hamcrest.Matcher)} handles scrolling.</p>
 *
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    /**
     * A JUnit {@link org.junit.Rule @Rule} to launch your activity under test. This is a replacement
     * for {@link android.test.ActivityInstrumentationTestCase2}.
     * <p>
     * Rules are interceptors which are executed for each test method and will run before
     * any of your setup code in the {@link org.junit.Before @Before} method.
     * <p>
     * {@link android.support.test.rule.ActivityTestRule} will create and launch of the activity for you and also expose
     * the activity under test. To get a reference to the activity you can use
     * the {@link android.support.test.rule.ActivityTestRule#getActivity()} method.
     */
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testStreetsBasemap() {
        // Open the overflow menu
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());

        // Click the item.
        onView(withText("Streets")) // ViewMatcher
                .perform(ViewActions.click()); // click() is a ViewAction

        // Verify that we have really clicked on the selection and map is enabled
        onView(withId(R.id.map)).check(ViewAssertions.matches(isEnabled()));
    }

    @Test
    public void testGrayBasemap() {
        // Open the overflow menu
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());

        // Click the item.
        onView(withText("Gray")) // ViewMatcher
                .perform(ViewActions.click()); // click() is a ViewAction

        // Verify that we have really clicked on the selection and map is enabled
        onView(withId(R.id.map)).check(ViewAssertions.matches(isEnabled()));
    }

    @Test
    public void testOceansBasemap() {
        // Open the overflow menu
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());

        // Click the item.
        onView(withText("Oceans")) // ViewMatcher
                .perform(ViewActions.click()); // click() is a ViewAction

        // Verify that we have really clicked on the selection and map is enabled
        onView(withId(R.id.map)).check(ViewAssertions.matches(isEnabled()));
    }

}
