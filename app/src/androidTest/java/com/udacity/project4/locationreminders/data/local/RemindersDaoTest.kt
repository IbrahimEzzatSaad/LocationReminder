package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

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

    @Test
    fun insertRemindersAndGetAll() = runBlockingTest {
        // GIVEN - Insert a task.
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)


        // WHEN - Get the task by id from the database.
        val loaded = database.reminderDao().getReminders()

        // THEN - The loaded data has the correct number of reminders
        assertThat(loaded.size, `is`(3))

    }


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

    @Test
    fun insertRemindersAndDeleteAll()= runBlockingTest{
        // GIVEN - Insert a task.
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        database.reminderDao().deleteAllReminders()

        // WHEN - Get the task by id from the database.
        val loaded = database.reminderDao().getReminders()

        assertThat(loaded.size, `is`(0))

    }

    @Test
    fun insertRemindersAndDeleteReminderById()= runBlockingTest{
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        database.reminderDao().deleteReminderById(reminder1.id)

        val loaded = database.reminderDao().getReminders()
        assertThat(loaded.size, `is`(2))
        assertThat(loaded[0].id, `is` (reminder2.id))

    }

    @Test
    fun returnsError()= runBlockingTest{
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        database.reminderDao().deleteReminderById(reminder1.id)

        val loaded = database.reminderDao().getReminders()
        assertThat(loaded.size, `is`(2))
        assertThat(loaded[0].id, `is` (reminder2.id))

    }


}