/*
 * Subscribes to the tradable instruments and extra feeds
 * 
 */
package com.madar.library.newsstraddle;

import com.dukascopy.api.Filter;
import java.util.HashSet;
import java.util.Set;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.IContext;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.feed.IFeedDescriptor;
import com.dukascopy.api.feed.IFeedListener;
import com.dukascopy.api.feed.util.TimePeriodAggregationFeedDescriptor;
import com.madar.strategies.NewsStradleStrategy;

public class MySubscriber {
    
    private final IContext context;  
    private final Set<Instrument> instruments;
    private final Set<Period> periods;
    
    public Set<Instrument> getInstruments() {return instruments;}
 
    // CONSTRUCTOR
    public MySubscriber(IContext context) {
        this.context = context;
        this.instruments = new HashSet<>();
        this.periods = new HashSet<>();
    }
    
    
    // subscribe for the testInstrument for normal run ot test and for the feed
    public void subscribe(NewsStradleStrategy strategy){
        if(strategy.isTest == true){
            subscribeToTestInstrument(strategy);
        } else {
            subscribeToAllTradable();
        }      
        //subscribeToFeeds(strategy);
    }
    
    
    // subscribe for the test testInstrument
    public void subscribeToTestInstrument(NewsStradleStrategy strategy){
        instruments.add(strategy.testInstrument);
        context.setSubscribedInstruments(instruments, true); 
        System.out.println("SUBSCRIBED TO THESE INSTRUMENTS: " + context.getSubscribedInstruments());
    }
    
    
    // subscribe for all tradable instruments by this strategy
    public void subscribeToAllTradable (){
        // subscribe to instruments
        instruments.add(Instrument.EURUSD);
        instruments.add(Instrument.USDJPY);
        instruments.add(Instrument.AUDUSD);
        instruments.add(Instrument.USDCAD);
        instruments.add(Instrument.NZDUSD);
        instruments.add(Instrument.GBPUSD);
        instruments.add(Instrument.EURGBP);
        instruments.add(Instrument.EURJPY);
        context.setSubscribedInstruments(instruments, true); 
        System.out.println("SUBSCRIBED TO THESE INSTRUMENTS: " + context.getSubscribedInstruments());
    } // end subscribe
    
    
    // subscribe for the datafeed
    public void subscribeToFeeds(IFeedListener strategy){
        // subscribe to periods for every testInstrument
        IFeedDescriptor feedDescriptor;
        periods.add(Period.TWO_SECS);        
        for (Instrument instrument : instruments) {
            for(Period period : periods){
                feedDescriptor = new TimePeriodAggregationFeedDescriptor(instrument, period, OfferSide.ASK, Filter.WEEKENDS);
                context.subscribeToFeed(feedDescriptor, strategy);
                System.out.println("SUBSCRIBED TO THESE PERIODS: " + feedDescriptor);
            }    
        }        
    }
    
}  // END CLASS MySubscriber
