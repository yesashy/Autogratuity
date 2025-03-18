package com.autogratuity.data.model;

/**
 * Model class representing monetary amounts in the Autogratuity app.
 * This is a standalone version of the inner class in Delivery.
 */
public class Amounts {
    private double baseAmount;
    private double estimatedPay;
    private double finalPay;
    private double tipAmount;
    private Double tipPercentage;
    private double distanceMiles;
    private String currency;
    
    // Default constructor required for Firestore
    public Amounts() {
    }
    
    // Getters and setters
    
    public double getBaseAmount() {
        return baseAmount;
    }
    
    public void setBaseAmount(double baseAmount) {
        this.baseAmount = baseAmount;
    }
    
    public double getEstimatedPay() {
        return estimatedPay;
    }
    
    public void setEstimatedPay(double estimatedPay) {
        this.estimatedPay = estimatedPay;
    }
    
    public double getFinalPay() {
        return finalPay;
    }
    
    public void setFinalPay(double finalPay) {
        this.finalPay = finalPay;
    }
    
    public double getTipAmount() {
        return tipAmount;
    }
    
    public void setTipAmount(double tipAmount) {
        this.tipAmount = tipAmount;
    }
    
    public Double getTipPercentage() {
        return tipPercentage;
    }
    
    public void setTipPercentage(Double tipPercentage) {
        this.tipPercentage = tipPercentage;
    }
    
    public double getDistanceMiles() {
        return distanceMiles;
    }
    
    public void setDistanceMiles(double distanceMiles) {
        this.distanceMiles = distanceMiles;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
