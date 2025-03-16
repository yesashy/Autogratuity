package com.autogratuity.data.model;

/**
 * Model class representing delivery statistics.
 * Used for UI display and analytics, not directly mapped to a Firestore collection.
 */
public class DeliveryStats {
    
    private int count;
    private int tipCount;
    private double totalTips;
    private double averageTip;
    private double highestTip;
    private int pendingCount;
    private double averageTimeMinutes;
    
    // Default constructor
    public DeliveryStats() {
        this.count = 0;
        this.tipCount = 0;
        this.totalTips = 0.0;
        this.averageTip = 0.0;
        this.highestTip = 0.0;
        this.pendingCount = 0;
        this.averageTimeMinutes = 0.0;
    }
    
    /**
     * Create a delivery stats object with initial values
     * 
     * @param count Total delivery count
     * @param tipCount Number of deliveries with tips
     * @param totalTips Sum of all tips
     */
    public DeliveryStats(int count, int tipCount, double totalTips) {
        this.count = count;
        this.tipCount = tipCount;
        this.totalTips = totalTips;
        this.averageTip = tipCount > 0 ? totalTips / tipCount : 0.0;
        this.highestTip = 0.0;
        this.pendingCount = 0;
        this.averageTimeMinutes = 0.0;
    }
    
    // Getters and setters
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public int getTipCount() {
        return tipCount;
    }
    
    public void setTipCount(int tipCount) {
        this.tipCount = tipCount;
        // Recalculate average
        if (tipCount > 0) {
            this.averageTip = totalTips / tipCount;
        } else {
            this.averageTip = 0.0;
        }
    }
    
    public double getTotalTips() {
        return totalTips;
    }
    
    public void setTotalTips(double totalTips) {
        this.totalTips = totalTips;
        // Recalculate average
        if (tipCount > 0) {
            this.averageTip = totalTips / tipCount;
        } else {
            this.averageTip = 0.0;
        }
    }
    
    public double getAverageTip() {
        return averageTip;
    }
    
    public void setAverageTip(double averageTip) {
        this.averageTip = averageTip;
    }
    
    public double getHighestTip() {
        return highestTip;
    }
    
    public void setHighestTip(double highestTip) {
        this.highestTip = highestTip;
    }
    
    public int getPendingCount() {
        return pendingCount;
    }
    
    public void setPendingCount(int pendingCount) {
        this.pendingCount = pendingCount;
    }
    
    public double getAverageTimeMinutes() {
        return averageTimeMinutes;
    }
    
    public void setAverageTimeMinutes(double averageTimeMinutes) {
        this.averageTimeMinutes = averageTimeMinutes;
    }
    
    /**
     * Get the tip rate as a percentage
     * 
     * @return Percentage of deliveries that included tips
     */
    public double getTipRate() {
        return count > 0 ? (double) tipCount / count * 100.0 : 0.0;
    }
    
    /**
     * Get the average tip for all deliveries (including those without tips)
     * 
     * @return Average tip per delivery
     */
    public double getAverageTipPerDelivery() {
        return count > 0 ? totalTips / count : 0.0;
    }
    
    /**
     * Get a formatted string representing the average tip
     * 
     * @return Formatted average tip amount (e.g., "$5.25")
     */
    public String getFormattedAverageTip() {
        return String.format("$%.2f", averageTip);
    }
    
    /**
     * Get a formatted string representing the total tips
     * 
     * @return Formatted total tip amount (e.g., "$125.75")
     */
    public String getFormattedTotalTips() {
        return String.format("$%.2f", totalTips);
    }
    
    /**
     * Add another set of stats to this one (for aggregation)
     * 
     * @param other The other stats to add
     * @return This object for chaining
     */
    public DeliveryStats add(DeliveryStats other) {
        if (other == null) {
            return this;
        }
        
        this.count += other.count;
        this.tipCount += other.tipCount;
        this.totalTips += other.totalTips;
        this.pendingCount += other.pendingCount;
        
        // Update averages
        if (this.tipCount > 0) {
            this.averageTip = this.totalTips / this.tipCount;
        }
        
        // Take the max of the highest tips
        if (other.highestTip > this.highestTip) {
            this.highestTip = other.highestTip;
        }
        
        // Weight the average time by delivery count
        if (this.count > 0) {
            double thisWeight = (this.count - other.count) / (double) this.count;
            double otherWeight = other.count / (double) this.count;
            this.averageTimeMinutes = (thisWeight * this.averageTimeMinutes) + 
                                     (otherWeight * other.averageTimeMinutes);
        }
        
        return this;
    }
}
