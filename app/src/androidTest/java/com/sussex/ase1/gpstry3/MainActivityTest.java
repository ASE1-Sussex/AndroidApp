package com.sussex.ase1.gpstry3;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import junit.framework.AssertionFailedError;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.anyOf;
import static org.junit.Assert.assertTrue;



/**
 * Created by User on 09/11/2016.
 */

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = getTargetContext();

        assertEquals("com.sussex.ase1.gpstry3", appContext.getPackageName());
    }

    @Test
    public void testPermissionGrantedACCESS_FINE_LOCATION() throws Exception {
        Log.e("testPermissionFINEELOC", "");
        MainActivity aaa = mActivityRule.getActivity();


        Context testContext = aaa;
        //Context testContext = getActivity().getContext();
        //Context testContext = InstrumentationRegistry.getTargetContext();

        PackageManager pm = testContext.getPackageManager();
        int permission = ContextCompat.checkSelfPermission(aaa, android.Manifest.permission.ACCESS_FINE_LOCATION);

        int expected = PackageManager.PERMISSION_GRANTED;
        if (expected == permission) {
            Log.e("" + Integer.toString(permission) + " == " + Integer.toString(expected) + " :", " test success");
        } else {
            Log.e("" + Integer.toString(permission) + " != " + Integer.toString(expected) + " :", " test failed");
        }
        assertEquals(expected, permission);
    }

    @Test
    public void testPermissionGrantedACCESS_COARSE_LOCATION() throws Exception {
        Log.e("testPermissionCOARSELOC", "");
        MainActivity aaa = mActivityRule.getActivity();


        Context testContext = aaa;
        //Context testContext = getActivity().getContext();
        //Context testContext = InstrumentationRegistry.getTargetContext();

        PackageManager pm = testContext.getPackageManager();
        int permission = ContextCompat.checkSelfPermission(aaa, android.Manifest.permission.ACCESS_COARSE_LOCATION);

        int expected = PackageManager.PERMISSION_GRANTED;
        if (expected == permission) {
            Log.e("" + Integer.toString(permission) + " == " + Integer.toString(expected) + " :", " test success");
        } else {
            Log.e("" + Integer.toString(permission) + " != " + Integer.toString(expected) + " :", " test failed");
        }
        assertEquals(expected, permission);

    }

    @Test
    public void testPermissionGrantedINTERNET() throws Exception {
        Log.e("testPermissionINTERNET", "");
        MainActivity aaa = mActivityRule.getActivity();


        Context testContext = aaa;
        //Context testContext = getActivity().getContext();
        //Context testContext = InstrumentationRegistry.getTargetContext();

        PackageManager pm = testContext.getPackageManager();
        int permission = ContextCompat.checkSelfPermission(aaa, Manifest.permission.INTERNET);

        int expected = PackageManager.PERMISSION_GRANTED;

        if (expected == permission) {
            Log.e("" + Integer.toString(permission) + " == " + Integer.toString(expected) + " :", " test success");
        } else {
            Log.e("" + Integer.toString(permission) + " != " + Integer.toString(expected) + " :", " test failed");
        }
        assertEquals(expected, permission);


    }

//    @Test
//    public void testPostcodeInputValid(){
//        Log.e("testPostcodeInputValid", "");
//        onView(withId(R.id.postcode))
//                .perform(typeText("RM2 5UX"));  //valid postcode
//
//        //change Button to "Find Postcode"
//        onView(withText("FIND POSTCODE")).perform(click()); //press button to update settings
//
////        boolean testPassed = false;
//
////        try{
////            onView(withText("FIND POSTCODE")).check(matches(isDisplayed()));
////            //View is in hierarchy
////
////        }catch (AssertionFailedError e){
////            //if button is not in hierarchy - then postcode valid and test passed
////            testPassed = true;
////        }
//        //intended(hasComponent(new ComponentName(getTargetContext(), WebViewActivity.class)));
//
//
//
//        assertEquals(onView(
//                anyOf(withId(R.id.content_main), withId(R.id.content_main))
//        ).check(matches(isDisplayed())),false);
//
////        Log.e("testPassed = "+testPassed,"");
////
////        assertEquals(true, testPassed);
//
//    }


    @Test
    public void validPostcode() throws Exception {
        String[] goodPostcodes = {"BN7 2AZ", "TN22 1QW", "BN3 3WD"};
        String[] badPostcodes = {"QN1 3AZ", "BN275 1A", "ZZ12 3AX"};

        for (int i = 0; i < goodPostcodes.length; i++) {

            String[] pArray = goodPostcodes[i].toUpperCase().trim().split(" ");
            String areaDistrict = pArray[0];

            String sectorUnit = "";
            if (pArray.length == 2)
                sectorUnit = pArray[1];

            String aMatch = "";
            String sMatch = "";

            switch (areaDistrict.length()) {
                case 1:
                    aMatch = "[A-Z&&[^QVX]]";
                    break;
                case 2:
                    aMatch = "([A-Z&&[^QVX]][0-9])|([A-Z&&[^QVX]][A-Z&&[^IJZ]])";
                    break;
                case 3:
                    aMatch = "(([A-Z&&[^QVX]][0-9][0-9])|([A-Z&&[^QVX]][A-Z&&[^IJZ]][0-9])|([A-Z&&[^QVX]][0-9][A-HJKPSTUW]))";
                    break;
                case 4:
                    aMatch = "(([A-Z&&[^QVX]][A-Z&&[^IJZ]][0-9][0-9])|([A-Z&&[^QVX]][A-Z&&[^IJZ]][0-9][ABEHMNPRVWXY]))";
                    break;
                default:
                    ;
            }

            switch (sectorUnit.length()) {
                case 1:
                    sMatch = "[0-9]";
                    break;
                case 2:
                    sMatch = "([0-9][A-Z&&[^CIKMOV]])";
                    break;
                case 3:
                    sMatch = "([0-9][A-Z&&[^CIKMOV]]{2})";
                    break;
                default:
                    ;
            }

            MainActivity aaa = mActivityRule.getActivity();

            String logString = "";

            if (aaa.validPostcode(goodPostcodes[i]) == (areaDistrict.matches(aMatch) == sectorUnit.matches(sMatch))) {
                logString = goodPostcodes[i] + " true";
            } else {
                logString = goodPostcodes[i] + " false";
            }
            Log.e("TASE1_VALID_POSTCODE", logString);
            assertTrue(aaa.validPostcode(goodPostcodes[i]) == (areaDistrict.matches(aMatch) == sectorUnit.matches(sMatch)));

        }
    }
}