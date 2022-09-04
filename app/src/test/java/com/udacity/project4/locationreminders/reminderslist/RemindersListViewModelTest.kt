package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.After


/**In this class we trying to test RemindersListViewModel.**/
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    // Subject under test
    private lateinit var remindersListViewModel: RemindersListViewModel

    // Use a fake data source to be injected into the viewmodel
    private lateinit var fakeDataSource: FakeDataSource

    private val reminder1 = ReminderDTO("Reminder1", "Description1", "Location1", 1.0, 1.0,"1")
    private val reminder2 = ReminderDTO("Reminder2", "Description2", "location2", 2.0, 2.0, "2")
    private val reminder3 = ReminderDTO("Reminder3", "Description3", "location3", 3.0, 3.0, "3")

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUpViewModel(){
        stopKoin()
        fakeDataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun clearDataSource() = runBlockingTest{
        fakeDataSource.deleteAllReminders()
    }



    /**In this function we try to test deleting all reminders and then we try to load the reminders from our View Model
     * We here testing two variables which is:
     * 1-showNoData
     * 2-reminderList**/
    @Test
    fun invalidateShowNoData_showNoData_isTrue()= runTest{

        //GIVEN - Empty DB
        fakeDataSource.deleteAllReminders()

        //WHEN - Try to load Reminders
        remindersListViewModel.loadReminders()

        //THEN - We expect that our reminder list Live data size is 0 and show no data is true
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().size, `is` (0))
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is` (true))

    }


    /**In this function we test to retrieve the 3 reminders we're inserting**/
    @Test
    fun loadReminders_loadsThreeReminders()= mainCoroutineRule.runBlockingTest {

        //GIVEN - Only 3 Reminders in the DB
        fakeDataSource.deleteAllReminders()
        fakeDataSource.saveReminder(reminder1)
        fakeDataSource.saveReminder(reminder2)
        fakeDataSource.saveReminder(reminder3)



        //WHEN - We try to load Reminders
        remindersListViewModel.loadReminders()

        //THEN - We expect to have only 3 reminders in remindersList and showNoData is false cause we have data
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().size, `is` (3))
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is` (false))

    }



    /**Here in this test we testing checkLoading*/
    @Test
    fun loadReminders_checkLoading()= mainCoroutineRule.runBlockingTest{
        // Pause dispatcher so we can verify initial values
        mainCoroutineRule.pauseDispatcher()

        //GIVEN - Only 1 Reminder
        fakeDataSource.deleteAllReminders()
        fakeDataSource.saveReminder(reminder1)

        //WHEN - We load Reminders
        remindersListViewModel.loadReminders()

        //THEN - loading indicator is shown and after we finishes we get the loading indicator hidden again.
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()

        // Then loading indicator is hidden
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))

    }


    /**Here in this test we testing showing an Error*/

    @Test
    fun loadReminders_shouldReturnError()= mainCoroutineRule.runBlockingTest{

        //GIVEN - Set should return error to "true"
        fakeDataSource.setShouldReturnError(true)

        //WHEN - We load Reminders
        remindersListViewModel.loadReminders()

        //THEN - We get showSnackBar in the view model giving us "Reminders not found"
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`("Reminders not found"))

    }

}