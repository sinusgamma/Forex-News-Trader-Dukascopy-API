/*
 * Set the upcoming event, end deletes the obsolate ones 
 * 
 */
package com.madar.library.newsstraddle;

import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.ITick;
import com.dukascopy.api.ITimedData;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;
import com.dukascopy.api.feed.IFeedDescriptor;
import com.madar.strategies.NewsStradleStrategy;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MyEventManager {

    
    private final IContext context;
    private final IEngine engine;
    private final IConsole console;
    private MyEventType actualEventType = null;
    private long actualEventTime;
    private HashMap<Instrument, MyInstrumentManager> instrumentManagers;
    private MyEventState eventState;
    private MyEventStateMachine eventStateMachine;
    private TreeMap<Long, MyEventType> eventSchedule;
    private NewsStradleStrategy strategy;

    
    // GtrStr
    public MyEventType getActualEventType() {return actualEventType;}
    public void setActualEventType(MyEventType actualEvent) {this.actualEventType = actualEvent;}
    public Long getActualTime() {return actualEventTime;}
    public void setActualTime(Long actualEventTime) {this.actualEventTime = actualEventTime;}
    public HashMap<Instrument, MyInstrumentManager> getInstrumentManagers() {return instrumentManagers;}
    public MyEventState getEventState() {return eventState;}

    
    // CONSTRUCTOR
    public MyEventManager(TreeMap eventSchedule, NewsStradleStrategy strategy, IContext context) {
        this.context = context;
        this.engine = context.getEngine();
        this.console = context.getConsole();
        this.eventSchedule = eventSchedule;
        this.eventStateMachine = new MyEventStateMachine();
        this.strategy = strategy;
    }

    
    //-------------------------------------------------------------------------------------------------------------------------------
    
    
    public void manageEventOnTick(Instrument instrument, ITick tick) {
        eventState = checkEventState(tick); // check eventState
        if (eventState == MyEventState.CALM || actualEventType == null) {return;} // go back if CALM or actualEvent isnt ready
        
        for (Instrument eventInstrument : actualEventType.getTraidableInstruments()) { // check for the tradable instruments - to process only them
            if (eventInstrument != null && eventInstrument == instrument) {
                instrumentManagers.get(instrument).manageInstrumentOnTick(eventState, tick);
            }
        }
    } // end manageEventOnTick
    
    
    public void manageEventOnFeed(IFeedDescriptor feedDescriptor) { 
        if(feedDescriptor.getPeriod() != Period.TWO_SECS){return;} // we don't need to do it every tick for preparations, only this
        prepareUpcomingEvent();       
    } // end manageEventOnBar
    
    public void manageEventOnBar(Period period) { 
        if(!period.equals(Period.TEN_SECS)){return;} // we don't need to do it every tick for preparations, only this
        prepareUpcomingEvent();       
    } // end manageEventOnBar    

    
    public void manageEventOnMessage(IMessage message) {
        // delegate order message handling to the corresponding instrumentManager by key:instrument
        if (message.getOrder() != null) {
            MyInstrumentManager actualInstrumentManager = instrumentManagers.get(message.getOrder().getInstrument());
            actualInstrumentManager.manageInstrumentOnMessage(message);
        }
        
    } // end manageEventOnMessage

    
    //--------------------------------------------------------------------------------------------------------------------------------------
    
    
    // prepare the manager for the next event: update the actual event and delete old event from schedule, build the instrumentManagers
    public void prepareUpcomingEvent() {
        
        try {
            if (eventState == MyEventState.OVER && eventSchedule.size() > 1 && engine.getOrders().isEmpty()) { // so if the actual event OVER and the schedule contain member yet and there is no more order alive
                actualEventType = null;
                actualEventTime = 0;
                eventSchedule.remove(eventSchedule.firstKey()); // remove the first element, which is obsolate
            }
        } catch (JFException ex) {
            Logger.getLogger(MyEventManager.class.getName()).log(Level.SEVERE, null, ex);
            console.getOut().println("EXCEPTION - prepareUpcomingEvent - engine.getOrders()");
        }
        
        if (actualEventType == null && eventSchedule.size() > 0) { // if not updated then update
            actualEventTime = eventSchedule.firstKey();  // casting Long to long
            actualEventType = eventSchedule.firstEntry().getValue();
            buildInstrumentManagers();
        }
        
        if (eventSchedule.isEmpty()) {  // close the strategy if there isn't any news left
            console.getOut().println("NO MORE EVENT");
        }
    } // end prepareUpcomingEvent

    
    // check eventState on tick
    public MyEventState checkEventState(ITick tick) {     
        
        eventState = MyEventState.CALM;
        if (actualEventType != null) {
            eventState = eventStateMachine.setEventState(tick, actualEventTime, actualEventType);  // + boolean to check if the actualEventType is prepared so not null
            
            // console.getOut().println(eventState + " " + actualEventType.getEventName() + " / actualEventTime: " + ZonedDateTime.ofInstant(Instant.ofEpochMilli(actualEventTime), ZoneId.of("UTC")) + " time: " + ZonedDateTime.ofInstant(Instant.ofEpochMilli(tick.getTime()), ZoneId.of("UTC")));
        }
        return eventState;
    } // end checkEventState

    
    // build the map of instrumentManagers, key: instrument
    public void buildInstrumentManagers() {
        instrumentManagers = new HashMap<>();
        if(strategy.isMultiScenarioRun == false){
            console.getOut().println("USING: MyInstrumentManagerBasic");
            for (Instrument inst : actualEventType.getTraidableInstruments()) {
                instrumentManagers.put(inst, new MyInstrumentManagerBasic(inst, actualEventTime, actualEventType, context));  // here we INSTANTIATE the Basic logic managers
            }
        } else {
            console.getOut().println("USING: MyInstrumentManagerBasicMulti");
            for (Instrument inst : actualEventType.getTraidableInstruments()) {
                instrumentManagers.put(inst, new MyInstrumentManagerBasicMulti(inst, actualEventTime, actualEventType, context));  // here we INSTANTIATE the Basic logic managers
            }
        }
    } // end buildInstrumentManagers

    
} // END MyEventManager
