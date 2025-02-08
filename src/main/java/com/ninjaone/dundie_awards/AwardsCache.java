package com.ninjaone.dundie_awards;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

/**
 * Spring {@link Component} to hold the total awards count
 */
@Component
public class AwardsCache {
    private final AtomicLong totalAwards = new AtomicLong(0);

    /**
     * Set the total awards count
     * @param totalAwards
     */
    public void setTotalAwards(int totalAwards) {
        this.totalAwards.set(totalAwards);
    }

    /**
     * Get the total awards count
     * @return
     */
    public long getTotalAwards(){
        return totalAwards.get();
    }

    /**
     * Add one award to the total awards
     * @return the new total awards count
     */
    public long addOneAward(){
        return incrementTotalAwards(1);
    }

    /**
     * Increment the total awards by the given count
     * @param count
     * @return the new total awards count
     */
    public long incrementTotalAwards(long count) {
        return this.totalAwards.addAndGet(count);
    }
}
