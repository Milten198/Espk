package com.pgssoft.testwarez.test.pages;

import com.pgssoft.testwarez.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by lfrydrych on 15.12.2017.
 */

public class AgendaPage {

    public void tapOnSearchIcon() {
        onView(withId(R.id.search_bar)).perform(click());
    }

    public void checkSearchInputOpens() {
        onView(withId(R.id.search_src_text)).check(matches(isDisplayed()));
    }
}
