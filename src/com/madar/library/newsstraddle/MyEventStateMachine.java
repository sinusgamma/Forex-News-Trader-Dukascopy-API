/*
 * Checks if a scheduled event time is approaching and set the state of event
 * 
 */
package com.madar.library.newsstraddle;


import com.dukascopy.api.ITick;

public class MyEventStateMachine {
    
    //CONSTRUCTOR
    public MyEventStateMachine() {      
    }
    
    // divide the time to calm, prepare, before news, after news, over periods - use onTick()
    public MyEventState setEventState(ITick tick, long actualEventTime, MyEventType actualEventType){
        MyEventState eventState;
            
        if(tick.getTime() > (actualEventTime - actualEventType.getSecondsBeforePending() * 1000) && tick.getTime() < actualEventTime){  // * 1000 because millisecond is used
            eventState = MyEventState.BEFORE;
            return eventState;
        } else if(tick.getTime() >= actualEventTime && tick.getTime() < (actualEventTime + actualEventType.getSecondsAfterNews() * 1000)){
            eventState = MyEventState.AFTER;
            return eventState;
        } else if(tick.getTime() >= (actualEventTime + actualEventType.getSecondsAfterNews() * 1000)){
            eventState = MyEventState.OVER;
            return eventState;
        } else {
            eventState = MyEventState.CALM;
        }     
        return eventState;
    }
 
}
