/*
 * Abstract class that has to extend the different instrumentmanager logics
 * 
 */
package com.madar.library.newsstraddle;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;

public abstract class MyInstrumentManager {
    
    private Instrument managedInstrument;
    private Long actualEventTime;
    private MyEventType actualEventType;    
    
    
    public abstract void manageInstrumentOnTick(MyEventState eventState, ITick tick); // is used in strategy class onTick method
    
    
    public abstract void manageInstrumentOnMessage(IMessage message); // is used in strategy class onTick method
    
}
