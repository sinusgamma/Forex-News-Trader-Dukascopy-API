/*
 * The settings for the events - these are changing on instrument, side, ensemble member
 * 
 */
package com.madar.library.newsstraddle;

public class MyOrderScenario {
    
    private long scenarioId;
    private boolean triggeredBreakEven = false;
    private boolean triggeredTrail = false;
    
    private double weight;
    
    private double pointsAway;
    private double takeProfit;
    private double stopLoss;
    private double modifyGap;
    
    private long secondsAfterNewsOffset; // use in ensemble to close farther positions later
    
    private double breakevenTrigger;
    private double breakevenDistance;
    private double trailingStop;
    private double trailingAfter;
    private boolean trailImmediately;
    
    private double riskPercent; 
    private double amount;

    private double maxSpread;
    private double maxSlippage; 
    
    //------------------------------------------------------------------------

    public long getScenarioId() {
        return scenarioId;
    }
    public void setScenarioId(long scenarioId) {
        this.scenarioId = scenarioId;
    }  

    public boolean isTriggeredBreakEven() {
        return triggeredBreakEven;
    }

    public void setTriggeredBreakEven(boolean triggeredBreakEven) {
        this.triggeredBreakEven = triggeredBreakEven;
    }

    public boolean isTriggeredTrail() {
        return triggeredTrail;
    }

    public void setTriggeredTrail(boolean triggeredTrail) {
        this.triggeredTrail = triggeredTrail;
    } 
    
    public double getWeight() {
        return weight;
    }
    public void setWeight(double weight) {
        if(weight >= 0){
            this.weight = weight;
        } else {
            this.weight = 0.0;
        }
    }

    public double getPointsAway() {
        return pointsAway;
    }
    public void setPointsAway(double pointsAway) {        
        if(pointsAway >= 0){
            this.pointsAway = pointsAway;
        } else {
            this.pointsAway = 0.0;
        }        
    }

    public double getTakeProfit() {
        return takeProfit;
    }
    public void setTakeProfit(double takeProfit) {
        if(takeProfit >= 0){
            this.takeProfit = takeProfit;
        } else {
            this.takeProfit = 0.0;
        }
    }

    public double getStopLoss() {
        return stopLoss;
    }
    public void setStopLoss(double stopLoss) {
        if(stopLoss >= 0){
            this.stopLoss = stopLoss;
        } else {
            this.stopLoss = 0.0;
        }
    }

    public double getModifyGap() {
        return modifyGap;
    }
    public void setModifyGap(double modifyGap) {
        if(modifyGap >= 0){
            this.modifyGap = modifyGap;
        } else {
            this.modifyGap = 0.0;
        }
    }

    public long getSecondsAfterNewsOffset() {
        return secondsAfterNewsOffset;
    }
    public void setSecondsAfterNewsOffset(long secondsAfterNewsOffset) {
        if(secondsAfterNewsOffset >= 0){
            this.secondsAfterNewsOffset = secondsAfterNewsOffset;
        } else {
            this.secondsAfterNewsOffset = 0;
        }
    }

    public double getBreakevenTrigger() {
        return breakevenTrigger;
    }
    public void setBreakevenTrigger(double breakevenTrigger) {
        if(breakevenTrigger >= 0){
            this.breakevenTrigger = breakevenTrigger;
        } else {
            this.breakevenTrigger = 0.0;
        }
    }

    public double getBreakevenDistance() {
        return breakevenDistance;
    }
    public void setBreakevenDistance(double breakevenDistance) {
        if(breakevenDistance >= 0){
            this.breakevenDistance = breakevenDistance;
        } else {
            this.breakevenDistance = 0.0;
        }
    }

    public double getTrailingStop() {
        return trailingStop;
    }
    public void setTrailingStop(double trailingStop) {
        if(trailingStop >= 0){
            this.trailingStop = trailingStop;
        } else {
            this.trailingStop = 0.0;
        }
    }

    public double getTrailingAfter() {
        return trailingAfter;
    }
    public void setTrailingAfter(double trailingAfter) {
        if(trailingAfter >= 0){
            this.trailingAfter = trailingAfter;
        } else {
            this.trailingAfter = 0.0;
        }
    }

    public boolean isTrailImmediately() {
        return trailImmediately;
    }
    public void setTrailImmediately(boolean trailImmediately) {
        this.trailImmediately = trailImmediately;
    }

    public double getRiskPercent() {
        return riskPercent;
    }
    public void setRiskPercent(double riskPercent) {
        if(riskPercent >= 0){
            this.riskPercent = riskPercent;
        } else {
            this.riskPercent = 0.0;
        }
    }

    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        if(amount >= 0){
            this.amount = amount;
        } else {
            this.amount = 0.0;
        }
    }

    public double getMaxSpread() {
        return maxSpread;
    }
    public void setMaxSpread(double maxSpread) {
        if(maxSpread >= 0){
            this.maxSpread = maxSpread;
        } else {
            this.maxSpread = 0.0;
        }
    }

    public double getMaxSlippage() {
        return maxSlippage;
    }
    public void setMaxSlippage(double maxSlippage) {
        if(maxSlippage >= 0){
            this.maxSlippage = maxSlippage;
        } else {
            this.maxSlippage = 0.0;
        }
    }
    
}
