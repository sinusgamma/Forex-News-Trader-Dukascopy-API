/*
 * From the event calendar and list of event types builds a schedule, assign the event types to the event times
 * the eventSchedule uses time epoch for simpler calculations in the future
 */
package com.madar.library.newsstraddle;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.ITimedData;
import com.madar.dataio.MyCSVManager;
import com.madar.dataio.MySingleEventHolder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MyEventScheduleBuilder {

    private IContext context;
    private IConsole console;
    private TreeMap<Long, MyEventType> eventSchedule; // Long is a time epoch - treemap have order
    private List<MySingleEventHolder> simpleTimedEventList = new ArrayList<>(); // only in test version
    private boolean isFiltered;

    public TreeMap getEventSchedule() {
        return eventSchedule;
    }

    //CONSTRUCTOR
    public MyEventScheduleBuilder(IContext context) {
        this.context = context;
        this.console = context.getConsole();
        this.isFiltered = false;
    }
    
    
    public void buildEventSchedule(List eventTypeList, String pathEventCalendarCSV, boolean isTest){
            simpleTimedEventList = MyCSVManager.readForexCalendarCsv(pathEventCalendarCSV);
            eventSchedule = setEventSchedule(eventTypeList, simpleTimedEventList);
            System.out.println("test: " + isTest + " csv: " + pathEventCalendarCSV);
    }


    // remove old events from schedule on start - when is filtered is false yet - need to make from some feed to work in tests
    public void filterOutObsolateEvents(ITimedData feedData){
        if(isFiltered == true) {return;}
        
        long startTime = feedData.getTime();
        
        for (Iterator<Map.Entry<Long, MyEventType>> it = eventSchedule.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Long, MyEventType> entry = it.next();
            Long eventTime = entry.getKey();
            if(startTime > eventTime){
                it.remove();
            }           
        }
        isFiltered = true;
    }
    
    // remove old events from schedule on start - when is filtered is false yet - need to make from some feed to work in tests
    public void filterOutObsolateEvents(IBar bar){
        if(isFiltered == true) {return;}
        
        long startTime = bar.getTime();
        
        for (Iterator<Map.Entry<Long, MyEventType>> it = eventSchedule.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Long, MyEventType> entry = it.next();
            Long eventTime = entry.getKey();
            if(startTime > eventTime){
                it.remove();
            }           
        }
        isFiltered = true;
        System.out.println("The size of filtered eventSchedule: " + eventSchedule.size() + " events.");
    }    

    
    // go trough the event types and add the proper eventtype to the date, collect it in a map
    private TreeMap<Long, MyEventType> setEventSchedule(List typeList, List<MySingleEventHolder> timedList) {
        boolean eventTypeExist;
        TreeMap<Long, MyEventType> scheduleMap = new TreeMap<>();
                
        for (MySingleEventHolder singleEvent : timedList) {
            eventTypeExist = false;
            for (MyEventType eventType : (ArrayList<MyEventType>) typeList) {
                if (singleEvent.getEventName().equalsIgnoreCase(eventType.getEventName()) && singleEvent.getCurrency() == eventType.getCurrency()) {  // we have the object for the timedNews
                    scheduleMap.put(singleEvent.getEventTime().toInstant().toEpochMilli(), eventType);
                    eventTypeExist = true;
                    break;
                }
            } // for MyEventType
            if (eventTypeExist == false) {
                System.out.println("EVENT " + singleEvent.getEventName() + " - eventTypeExist: " + eventTypeExist + " / the scheduled event NOT READY, hasn't got a prepaired eventType in evenTypeList.");
            } else {
                System.out.println("EVENT " + singleEvent.getEventName() + " - eventTypeExist: " + eventTypeExist + " / the scheduled event READY TO TRADE.");
            }
        } // for timed list
        System.out.println("The size of original eventSchedule: " + scheduleMap.size() + " events.");
        return scheduleMap;
    }

} // END class MyEventScheduleBuilder
