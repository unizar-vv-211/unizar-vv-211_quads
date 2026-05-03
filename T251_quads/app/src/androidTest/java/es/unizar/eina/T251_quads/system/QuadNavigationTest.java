package es.unizar.eina.T251_quads.system;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import es.unizar.eina.T251_quads.MainActivity;
import es.unizar.eina.T251_quads.R;

@RunWith(AndroidJUnit4.class)
public class QuadNavigationTest {

    @Rule
    public ActivityScenarioRule<MainActivity> scenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testProfundidad2_NavegacionQuads() {
        // Arco a: N1 -> N2
        onView(withId(R.id.card_quads)).perform(click());
        onView(withId(R.id.recyclerview)).check(matches(isDisplayed()));

        // Arco c: N2 -> N3
        onView(withId(R.id.fab)).check(matches(isDisplayed())).perform(click());
        onView(withId(R.id.edit_matricula)).check(matches(isDisplayed()));

        // Arco d: N3 -> N2
        pressBack();
        onView(withId(R.id.recyclerview)).check(matches(isDisplayed()));

        // Arco b: N2 -> N1
        pressBack();
        onView(withId(R.id.card_quads)).check(matches(isDisplayed()));
    }
}