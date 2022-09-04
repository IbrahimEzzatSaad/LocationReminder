package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

/** Here we're testing RemindersDao interface
 *  Testing different ways of insertion
 *  The functions we will be testing on is:
 *    1-getReminders
 *    2-getReminderById
 *    3-saveReminder
 *    4-deleteAllReminders
 *   5-deleteReminderById    **/

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

//    TODO: Add testing implementation to the RemindersDao.kt

    private lateinit var database: RemindersDatabase

    private val reminder1 = ReminderDTO("Reminder1", "Description1", "Location1", 1.0, 1.0,"1")
    private val reminder2 = ReminderDTO("Reminder2", "Description2", "location2", 2.0, 2.0, "2")
    private val reminder3 = ReminderDTO("Reminder3", "Description3", "location3", 3.0, 3.0, "3")


    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()


    /**This method insert the Reminders we created above and trying to retrieve them again
     * as the expected number of reminders is 3**/
    @Test
    fun insertRemindersAndGetAll() = runBlockingTest {
        // GIVEN - Insert a task.
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)


        // WHEN - We call getReminders() we expected to get all reminders we have.
        val loaded = database.reminderDao().getReminders()

        // THEN - The loaded data has the correct number of reminders which is 3 reminders we just inserted
        assertThat(loaded.size, `is`(3))

    }



    /**insertReminderAndGetById is a function defined for us to test if we could retrieve only one reminder by certain values
     * Here we're inserting one reminder which is reminder1 defined above
     * and then we trying to retrieve it by its Id**/
    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        // GIVEN - Insert a task.
        database.reminderDao().saveReminder(reminder1)


        // WHEN - Get the task by id from the database.
        val loaded = database.reminderDao().getReminderById(reminder1.id)

        // THEN - The loaded data contains the expected values.
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.title, `is`(reminder1.title))
        assertThat(loaded.description, `is`(reminder1.description))
        assertThat(loaded.location, `is`(reminder1.location))
        assertThat(loaded.latitude, `is`(reminder1.latitude))
        assertThat(loaded.longitude, `is`(reminder1.longitude))
        assertThat(loaded.id, `is`(reminder1.id))
    }


    /**In this function we try to test if we could delete all the reminders after inserting.**/
    @Test
    fun insertRemindersAndDeleteAll()= runBlockingTest{
        // GIVEN - Insert a task.
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        // Deleting all the reminders we have
        database.reminderDao().deleteAllReminders()

        // WHEN - We try to get the reminders we expecting the size to be zero
        val loaded = database.reminderDao().getReminders()

        // THEN - The loaded data size 'is' 0.
        assertThat(loaded.size, `is`(0))
    }


    /**This function is meant to test deletion by Id**/
    @Test
    fun insertRemindersAndDeleteReminderById()= runBlockingTest{
        // GIVEN - Insert all reminders
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        // WHEN - Delete reminder by Id
        database.reminderDao().deleteReminderById(reminder1.id)

        // THEN - There should be only 2 reminders in the DB since we deleted one
        val loaded = database.reminderDao().getReminders()
        assertThat(loaded.size, `is`(2))
        // The reminder 0 in the DB should be the reminder 2 not 1 since we deleted it previously.
        assertThat(loaded[0].id, `is` (reminder2.id))
    }



    /**This function is meant to returns an error, Null Value.
     *  1-We inserted 3 reminders
     *  2-We removed the first reminder
     *  3-Now the reminder at position 0 is reminder 2 not 1
     *  4-We try to retrieve the deleted reminder by Id which we expect to receive null cause we deleted it**/
    @Test
    fun returnsError()= runBlockingTest{
        // GIVEN - Insert all reminders
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        // WHEN - Delete reminder by Id & Try to retrieve the same reminder we deleted by Id
        database.reminderDao().deleteReminderById(reminder1.id)
        val loaded = database.reminderDao().getReminderById(reminder1.id)

        // THEN - The value we should receive should be null Value
        assertThat(loaded, nullValue())
    }


}