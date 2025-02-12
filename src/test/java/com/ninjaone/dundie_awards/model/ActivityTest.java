
package com.ninjaone.dundie_awards.model;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



public class ActivityTest {

    private Activity activity;

    @BeforeEach
    public void setUp() {
        activity = new Activity();
    }

    @Test
    public void prePersist_should_set_occurredAt_when_null() {
        activity.prePersist();
        assertThat(activity.getOccurredAt(), notNullValue());
    }

    @Test
    public void prePersist_should_not_override_occurredAt_when_already_set() {
        LocalDateTime occurredAt = LocalDateTime.of(2023, 1, 1, 12, 0);
        activity.setOccurredAt(occurredAt);
        activity.prePersist();
        assertThat(activity.getOccurredAt(), is(occurredAt));
    }

    @Test
    public void prePersist_should_set_createdInThread_when_null() {
        activity.prePersist();
        assertThat(activity.getCreatedInThread(), is(Thread.currentThread().getName()));
    }

    @Test
    public void prePersist_should_not_override_createdInThread_when_already_set() {
        String threadName = "TestThread";
        activity.setCreatedInThread(threadName);
        activity.prePersist();
        assertThat(activity.getCreatedInThread(), is("TestThread"));
    }
}
