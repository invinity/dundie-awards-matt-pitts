package com.ninjaone.dundie_awards;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class AwardsCacheTest {

    private AwardsCache awardsCache;

    @BeforeEach
    public void setUp() {
        awardsCache = new AwardsCache();
    }

    @Test
    public void setTotalAwards_should_set_the_correct_value() {
        awardsCache.setTotalAwards(10);
        assertThat(awardsCache.getTotalAwards(), is(10L));
    }

    @Test
    public void getTotalAwards_should_return_the_correct_value() {
        assertThat(awardsCache.getTotalAwards(), is(0L));
        awardsCache.setTotalAwards(5);
        assertThat(awardsCache.getTotalAwards(), is(5L));
    }

    @Test
    public void addOneAward_should_add_one_to_the_total() {
        awardsCache.setTotalAwards(5);
        long newTotal = awardsCache.addOneAward();
        assertThat(newTotal, is(6L));
        assertThat(awardsCache.getTotalAwards(), is(6L));
    }

    @Test
    public void incrementTotalAwards_should_increment_total_awards() {
        awardsCache.setTotalAwards(5);
        long newTotal = awardsCache.incrementTotalAwards(3);
        assertThat(newTotal, is(8L));
        assertThat(awardsCache.getTotalAwards(), is(8L));
    }
}
