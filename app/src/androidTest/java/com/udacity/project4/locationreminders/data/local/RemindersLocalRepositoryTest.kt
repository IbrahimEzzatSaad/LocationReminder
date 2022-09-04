package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.Assert.assertThat



/**This test class is meant to test our RemindersLocal Repository.
 * We have four functions we need to test which is:
 *      1-getReminders
 *      2-saveReminder
 *      3-getReminder by Id
 *      4-deleteAllReminders
 *      5-deleteReminder by Id**/

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {


    private lateinit var localRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    private val reminder1 = ReminderDTO("Reminder1", "Description1", "Location1", 1.0, 1.0,"1")
    private val reminder2 = ReminderDTO("Reminder2", "Description2", "location2", 2.0, 2.0, "2")
    private val reminder3 = ReminderDTO("Reminder3", "Description3", "location3", 3.0, 3.0, "3")

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // using an in-memory database for testing, since it doesn't survive killing the process
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localRepository =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    /**This function we save a reminder and then we retrieve it by Id**/
    @Test
    fun saveReminder_retrievesReminderById() = runBlocking {
        // GIVEN - A new reminder saved in the database.
        localRepository.saveReminder(reminder1)

        // WHEN  - reminder retrieved by ID.
        val result = localRepository.getReminder(reminder1.id)

        // THEN - Same reminder is returned.
        result as Result.Success
        assertThat(result.data.title, `is`(reminder1.title))
        assertThat(result.data.description, `is`(reminder1.description))
        assertThat(result.data.location, `is`(reminder1.location))
        assertThat(result.data.latitude, `is`(reminder1.latitude))
        assertThat(result.data.longitude, `is`(reminder1.longitude))
        assertThat(result.data.id, `is`(reminder1.id))
    }

    /**This function we save all reminders defined above and then we retrieve all again**/
    @Test
    fun saveReminders_retrievesAllReminders() = runBlocking {
        // GIVEN - A new reminders saved in the database.
        localRepository.saveReminder(reminder1)
        localRepository.saveReminder(reminder2)
        localRepository.saveReminder(reminder3)

        // WHEN  - We retrieve all reminders
        val result = localRepository.getReminders()

        // THEN - Correct number of reminders returned which is 3.
        result as Result.Success
        assertThat(result.data.size, `is`(3))
    }


    /**In this function we save all reminders and then we delete one by Id**/
    @Test
    fun saveReminders_deletesOneReminderById() = runBlocking {
        // GIVEN - A new reminders saved in the database.
        localRepository.saveReminder(reminder1)
        localRepository.saveReminder(reminder2)
        localRepository.saveReminder(reminder3)

        // WHEN  - Delete one reminder by Id and retrieve all reminders
        localRepository.deleteReminder(reminder1.id)
        val result = localRepository.getReminders()

        //THEN - We expect the size to be just 2 reminders left.
        result as Result.Success
        assertThat(result.data.size, `is`(2))
        assertThat(result.data[0].location, `is`(reminder2.location))
    }


    /**In this function we save all reminders and then we delete all reminders**/
    @Test
    fun saveReminders_deletesAllReminders() = runBlocking {
        // GIVEN - A new reminders saved in the database.
        localRepository.saveReminder(reminder1)
        localRepository.saveReminder(reminder2)
        localRepository.saveReminder(reminder3)

        // WHEN  - Delete all Reminders and try to retrieve all reminders
        localRepository.deleteAllReminders()
        val result = localRepository.getReminders()

        // THEN - We expect we retrieve 0 reminders cause we deleted all previously.
        result as Result.Success
        assertThat(result.data.size, `is`(0))

    }


    /**In this function we delete all reminders if we have any and try to retrieve reminder by id which does not exist.**/
    @Test
    fun getReminder_returnsError() = runBlocking {

        //GIVEN - Empty DB
        localRepository.deleteAllReminders()

        //WHEN - We try to retrieve reminder by id which does not exist
        val result = localRepository.getReminder(reminder1.id) as Result.Error

        //THEN - We get an Result.Error message
        assertThat(result.message, `is`("Reminder not found!"))
    }

}