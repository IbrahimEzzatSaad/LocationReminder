package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import getOrAwaitValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.hamcrest.CoreMatchers.nullValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin



/**Here we're testing SaveReminderViewModel with fakeDataSource**/
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    // Use a fake data source to be injected into the viewmodel
    private lateinit var fakeDataSource: FakeDataSource

    private val reminder1 = ReminderDataItem("Reminder1", "Description1", "Location1", 1.0, 1.0,"1")
    private val reminder2_noTitle = ReminderDataItem("", "Description2", "location2", 2.0, 2.0, "2")
    private val reminder3_noLocation = ReminderDataItem("Reminder3", "Description3", "", 3.0, 3.0, "3")

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
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    /**In this function we clear all reminders Live Data and test if they all null*/
    @Test
    fun onClear_clearsReminderLiveData(){

        //GIVEN - Data to the variables
        saveReminderViewModel.reminderTitle.value = reminder1.title
        saveReminderViewModel.reminderDescription.value = reminder1.description
        saveReminderViewModel.reminderSelectedLocationStr.value = reminder1.location
        saveReminderViewModel.latitude.value = reminder1.latitude
        saveReminderViewModel.longitude.value = reminder1.longitude
        saveReminderViewModel.reminderId.value = reminder1.id

        //WHEN - We call on clear
        saveReminderViewModel.onClear()

        //THEN - We expect we have them all null
        assertThat(saveReminderViewModel.reminderTitle.getOrAwaitValue(), `is` (nullValue()))
        assertThat(saveReminderViewModel.reminderDescription.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.latitude.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.longitude.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderId.getOrAwaitValue(), `is`(nullValue()))

    }

    /**In this function we testing set the Live Data of reminder to be edited*/
    @Test
    fun editReminder_setsLiveDataOfReminderToBeEdited(){

        //GIVEN & WHEN - We call Edit reminder and passing reminder1
        saveReminderViewModel.editReminder(reminder1)


        //THEN - We expect that our saveReminderViewModel is holding the data of reminder1.
        assertThat(saveReminderViewModel.reminderTitle.getOrAwaitValue(), `is` (reminder1.title))
        assertThat(saveReminderViewModel.reminderDescription.getOrAwaitValue(), `is`(reminder1.description))
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(), `is`(reminder1.location))
        assertThat(saveReminderViewModel.latitude.getOrAwaitValue(), `is`(reminder1.latitude))
        assertThat(saveReminderViewModel.longitude.getOrAwaitValue(), `is`(reminder1.longitude))
        assertThat(saveReminderViewModel.reminderId.getOrAwaitValue(), `is`(reminder1.id))
    }


    /**In this function we testing add Reminder to Data Source via our ViewModel.saveReminder function **/
    @Test
    fun saveReminder_addsReminderToDataSource() = mainCoroutineRule.runBlockingTest{

        //GIVEN - We call save reminder passing reminder1
        saveReminderViewModel.saveReminder(reminder1)

        //WHEN - Call get reminder that has id 1
        val checkReminder = fakeDataSource.getReminder("1") as Result.Success

        //THEN - We expect to get reminder1
        assertThat(checkReminder.data.title, `is` (reminder1.title))
        assertThat(checkReminder.data.description, `is` (reminder1.description))
        assertThat(checkReminder.data.location, `is` (reminder1.location))
        assertThat(checkReminder.data.latitude, `is` (reminder1.latitude))
        assertThat(checkReminder.data.longitude, `is` (reminder1.longitude))
        assertThat(checkReminder.data.id, `is` (reminder1.id))

    }

    /**In this function we test check Loading **/
    @Test
    fun saveReminder_checkLoading()= mainCoroutineRule.runBlockingTest{
        // Pause dispatcher so we can verify initial values
        mainCoroutineRule.pauseDispatcher()

        //GIVEN - reminder1 to be saved
        saveReminderViewModel.saveReminder(reminder1)

        // THEN -  loading indicator is shown
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()

        // THEN -  loading indicator is hidden
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))

    }

    /**In this function we test validateData by passing null title and we expect
     * showSnackBarInt to indicate to err_enter_title
     * and validate return false */
    @Test
    fun validateData_missingTitle_showSnackbarAndReturnFalse(){

        //GIVEN - Calling validateEnteredData and passing no title
        val validate = saveReminderViewModel.validateEnteredData(reminder2_noTitle)

        //THEN - We expect a Snackbar to be shown displaying err_enter_title string and validate return false
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is` (R.string.err_enter_title))
        assertThat(validate, `is` (false))
    }

    /**In this function we test validateData by passing null location and we expect
     * showSnackBarInt to indicate to err_select_location
     * and validate return false*/
    @Test
    fun validateData_missingLocation_showSnackbarAndReturnFalse(){

        //GIVEN - Calling validateEnteredData and passing no location
        val validate = saveReminderViewModel.validateEnteredData(reminder3_noLocation)

        //THEN - We expect a Snackbar to be shown displaying err_select_location string and validate return false
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is` (R.string.err_select_location))
        assertThat(validate, `is` (false))
    }




}