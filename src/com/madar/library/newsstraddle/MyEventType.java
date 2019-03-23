/*
 * Containes the characteristics of the distinct news and other events
 * 
 */
package com.madar.library.newsstraddle;


import com.dukascopy.api.Instrument;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyEventType {
    
    private String eventName;
    private MyCurrency currency; 
    private MyOrderDirection enabledOrderDirection;
    
    private long secondsBeforePending;
    private long secondsBeforeModify;
    private long secondsAfterNews;
    
    private boolean isOCO;    
    private boolean manageMoney;
    private boolean calcAmountWithMaxSpread;
    
    private HashMap<Instrument, HashMap<MyOrderDirection, MyOrderScenario>> simpleDirectionalSettings;  // the variables varying on instrument/direction/ensemble are in map 
    private HashMap<Instrument, HashMap<MyOrderDirection, List<MyOrderScenario>>> multiDirectionalSettings;  // the variables varying are in map
    
    // Gtr
    public String getEventName() {return eventName;}
    public MyCurrency getCurrency() {return currency;}
    public MyOrderDirection getEnabledOrderDirection() {return enabledOrderDirection;}  
    public long getSecondsBeforePending() {return secondsBeforePending;}
    public long getSecondsBeforeModify() {return secondsBeforeModify;}
    public long getSecondsAfterNews() {return secondsAfterNews;}
    public boolean isIsOCO() {return isOCO;}
    public boolean isManageMoney() {return manageMoney;}
    public boolean isCalcAmountWithMaxSpread() {return calcAmountWithMaxSpread;}
    
    public HashMap<Instrument, HashMap<MyOrderDirection, MyOrderScenario>> getSimpleDirectionalSettings() {return simpleDirectionalSettings;}
    public HashMap<Instrument, HashMap<MyOrderDirection, List<MyOrderScenario>>> getMultiDirectionalSettings() {return multiDirectionalSettings;}   
    
    //Str
    public void setEventName(String eventName) {this.eventName = eventName;} 
    
    public void setCurrency(MyCurrency currency) {this.currency = currency;}  
    
    public void setEnabledOrderDirection(MyOrderDirection enabledOrderDirection) {this.enabledOrderDirection = enabledOrderDirection;}
    
    public void setSecondsBeforePending(long secondsBeforePending) {
        if(secondsBeforePending >= 0){
            this.secondsBeforePending = secondsBeforePending;
        } else {
            this.secondsBeforePending = 0;
        }
    }
    
    public void setSecondsBeforeModify(long secondsBeforeModify) {
        if(secondsBeforeModify >= 0){
            this.secondsBeforeModify = secondsBeforeModify;
        } else {
            this.secondsBeforeModify = 0;
        }
    }
    
    public void setSecondsAfterNews(long secondsAfterNews) {
        if(secondsAfterNews >= 0){
            this.secondsAfterNews = secondsAfterNews;
        } else {
            this.secondsAfterNews = 0;
        }
    }  
    
    public void setIsOCO(boolean isOCO) {this.isOCO = isOCO;}
    
    public void setManageMoney(boolean manageMoney) {this.manageMoney = manageMoney;}

    public void setCalcAmountWithMaxSpread(boolean calcAmountWithMaxSpread) {this.calcAmountWithMaxSpread = calcAmountWithMaxSpread;}
    
    public void setSimpleDirectionalSettings(HashMap<Instrument, HashMap<MyOrderDirection, MyOrderScenario>> simpleDirectionalSettings) {
        this.simpleDirectionalSettings = simpleDirectionalSettings;
    }
    
    public void setMultiDirectionalSettings(HashMap<Instrument, HashMap<MyOrderDirection, List<MyOrderScenario>>> multiDirectionalSettings) {
        this.multiDirectionalSettings = multiDirectionalSettings;
    }  

    // CONSTRUCTOR
    public MyEventType(String eventName, MyCurrency currency) {
        this.eventName = eventName;
        this.currency = currency;
    }
    public MyEventType() {

    }
       
    
    public List<Instrument> getTraidableInstruments() {
        List<Instrument> traidableInstruments = new ArrayList<>(simpleDirectionalSettings.keySet());
        return traidableInstruments;   
    }
    
    
    // get orderscenarios by instrument and direction
    public MyOrderScenario getSimpleSetting(Instrument instrument, MyOrderDirection orderDirection){
        MyOrderScenario orderScenario; 
        orderScenario = this.getSimpleDirectionalSettings().get(instrument).get(orderDirection);
        return orderScenario;
    }

}
