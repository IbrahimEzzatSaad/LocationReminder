package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.service.autofill.Validators.not
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.udacity.project4.R
import com.udacity.project4.locationreminders.FakeDataSource
import com.udacity.project4.locationreminders.ReminderDescriptionViewModel
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.hamcrest.core.IsNot.not
import org.mockito.AdditionalMatchers.not
import org.mockito.ArgumentMatchers.matches


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest: KoinTest {

    private val dataSource: ReminderDataSource by inject()

    private val reminder1 = ReminderDTO("Reminder1", "Description1", "Location1", 1.0, 1.0,"1")
    private val reminder2 = ReminderDTO("Reminder2", "Description2", "location2", 2.0, 2.0, "2")
    private val reminder3 = ReminderDTO("Reminder3", "Description3", "location3", 3.0, 3.0, "3")

    @Before
    fun initRepository() {
        stopKoin()
        /**
         * use Koin Library as a service locator
         */
        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                RemindersListViewModel(
                    get(),
                    get()
                )
            }
            single {
                FakeDataSource() as ReminderDataSource
            }
        }

        startKoin {
            androidContext(getApplicationContext())
            modules(listOf(myModule))
        }
    }

    @After
    fun cleanupDb() = runBlockingTest {
        dataSource.deleteAllReminders()
    }

//    @get:Rule
//    val activityRule = ActivityTestRule(RemindersActivity::class.java)


    @Test
    fun reminderList_DisplayedInUi() = runBlockingTest{
        // GIVEN - Add active (incomplete) task to the DB
        dataSource.saveReminder(reminder1)
        dataSource.saveReminder(reminder2)
        dataSource.saveReminder(reminder3)

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //then
        onView(withText(reminder1.title)).check(matches(isDisplayed()))
        onView(withText(reminder2.description)).check(matches(isDisplayed()))
        onView(withText(reminder3.title)).check(matches(isDisplayed()))
        onView(withId(R.id.noDataTextView)).check(matches(not(isDisplayed())))

    }

    @Test
    fun reminderList_noReminders() = runBlockingTest{
        // GIVEN - Add active (incomplete) task to the DB
        dataSource.deleteAllReminders()

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        //then
        onView(withText(R.string.no_data)).check(matches(isDisplayed()))
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        onView(withText(reminder1.title)).check(doesNotExist())

    }

    @Test
    fun clickFab_navigateToReminderFragment() = runBlockingTest {

        // GIVEN - On the home screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - Click on the "+" button
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - Verify that we navigate to the save reminder fragment
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
            )
    }


}