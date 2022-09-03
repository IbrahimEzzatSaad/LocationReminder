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


    @Test
    fun invalidateShowNoData_showNoData_isTrue()= runTest{

        fakeDataSource.deleteAllReminders()

        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().size, `is` (0))
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is` (true))

    }

    @Test
    fun loadReminders_loadsThreeReminders()= mainCoroutineRule.runBlockingTest {

        fakeDataSource.deleteAllReminders()

        fakeDataSource.saveReminder(reminder1)
        fakeDataSource.saveReminder(reminder2)
        fakeDataSource.saveReminder(reminder3)

        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().size, `is` (3))
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is` (false))

    }

    @Test
    fun loadReminders_checkLoading()= mainCoroutineRule.runBlockingTest{
        // Pause dispatcher so we can verify initial values
        mainCoroutineRule.pauseDispatcher()

        fakeDataSource.deleteAllReminders()
        fakeDataSource.saveReminder(reminder1)

        //when
        remindersListViewModel.loadReminders()

        // Then loading indicator is shown
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()

        // Then loading indicator is hidden
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))

    }

    @Test
    fun loadReminders_shouldReturnError()= mainCoroutineRule.runBlockingTest{

        //when
        fakeDataSource.setShouldReturnError(true)
        remindersListViewModel.loadReminders()

        //then
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`("Reminders not found"))

    }

}