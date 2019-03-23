
package com.madar.strategies;

import com.dukascopy.api.Configurable;
import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.ITimedData;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Library;
import com.dukascopy.api.Period;
import com.dukascopy.api.RequiresFullAccess;
import com.madar.library.newsstraddle.MyCurrency;
import com.madar.library.newsstraddle.MyEventManager;
import com.madar.library.newsstraddle.MyEventScheduleBuilder;
import com.madar.library.newsstraddle.MyEventTypeListParameterizer;
import com.madar.library.newsstraddle.MyOrderDirection;
import com.madar.library.newsstraddle.MySubscriber;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequiresFullAccess
@Library("F:\\JavaProjects\\NetBeansProjects\\JForex-3-SDK-330\\target\\JForex-3-SDK-3.3.0.jar;C:\\Users\\MadAr\\.m2\\repository\\org\\apache\\commons\\commons-csv\\1.5\\commons-csv-1.5.jar")

public class NewsStradleStrategy implements IStrategy{
    
    private IContext context;
    private IConsole console;
    private IEngine engine;
    
    private MyEventTypeListParameterizer eventTypeListParameterizer;
    private MyEventScheduleBuilder eventScheduleBuilder; 
    private MySubscriber subscriber;
    private MyEventManager eventManager;
    
    // configurables are set before onStart - they will be used instead of setting files if useManulaSetup is true
    @Configurable("closeOnShutDown")
    public boolean closeOnShutDown = true; 
    
    @Configurable("File path of the CSV storing the events with time")
    public String pathEventCalendarCSV = "F:\\FOREX\\nsresource\\news-schedule\\run-trade-schedule.csv"; // file containing the events to run
    
    @Configurable("File path of the CSV storing the multi-settings parameters for the trades")
    public String pathMultiSettingCSV = "F:\\FOREX\\nsresource\\parameters\\multiSettings.csv"; // file containing the events to run
        
    // if true the test uses the @Configurable parameters instead of the MyEventTypeListParameterizer and subscribes only for the testInstrument used in the test
    // the @Configurables are modified by testmanagers if the strategy is called from them
    @Configurable(value="Use this setup? (else default is used)", description="for testing this setup: true / for defaults in evenType: false - use only with one eventType")
    public boolean isTest = false; // let it false here, overwrite only from tester class
    
    @Configurable("isMulticSenarioRun") // false: only one scenario per run, so only one trade pair to open, true: multiple trade pairs to open
    public boolean isMultiScenarioRun = true; 
    
    @Configurable(value="eventName", description="case and space matters, used against the schedulBuilders list names")
    public String eventName = "Core CPI mm";
    @Configurable("currency")
    public MyCurrency currencyOfEvent = MyCurrency.USD;
    @Configurable("instrument")
    public Instrument testInstrument = Instrument.USDJPY;
    
    @Configurable("long/short/both")
    public MyOrderDirection enabledOrderDirection = MyOrderDirection.BOTHDIRECTION;
    
    @Configurable("pointsAway")
    public double pointsAway = 4;
    @Configurable("takeProfit")
    public double takeProfit = 0;
    @Configurable("stopLoss")
    public double stopLoss = 1.5;
    @Configurable("modifyGap")
    public double modifyGap = 0.1;
    
    @Configurable("secondsBefore")
    public long secondsBeforePending = 10;
    @Configurable("secondsBeforeModify")
    public long secondsBeforeModify = 0;
    @Configurable("secondsAfterNews")
    public long secondsAfterNews = 10;   
    @Configurable("secondsAfterNewsOffset")
    public long secondsAfterNewsOffset = 0;
    
    @Configurable("breakevenTrigger")
    public double breakevenTrigger = 0.0;
    @Configurable("breakevenDistance")
    public double breakevenDistance = 0.3;
    @Configurable("trailingStop")
    public double trailingStop = 2;
    @Configurable("trailingAfter")
    public double trailingAfter = 1.0;
    @Configurable("trailImmediately")
    public boolean trailImmediately = true;
    
    @Configurable("isOCO")
    public boolean isOCO = true;
    
    @Configurable("manageMoney")
    public boolean manageMoney = true;
    @Configurable("calcAmountWithMaxSpread")
    public boolean calcAmountWithMaxSpread = false;    
    @Configurable("riskPercent")
    public double riskPercent = 0.01; 
    @Configurable("lotSize")
    public double amount = 0.001;
    
    @Configurable("maxSpread")
    public double maxSpread = 4;
    @Configurable("maxSlippage")
    public double maxSlippage = 0;   
    // end @Configurable
    
    @Override
    public void onStart(IContext context) throws JFException {
        
       this.context = context;
       this.console = context.getConsole();
       this.engine = context.getEngine();
    
       this.subscriber = new MySubscriber(context); // subscribe to instruments and feeds
       subscriber.subscribe(this); // subscribe to all instruments or only the test - depends on isTest
       
       this.eventTypeListParameterizer = new MyEventTypeListParameterizer(); // make a list of event types
       eventTypeListParameterizer.setEventTypeProperties(this); // set the properties for the event types and decides to use @Conf or predefined parameters - depend on isTest
       
       this.eventScheduleBuilder = new MyEventScheduleBuilder(context); // assign even types to event times
       eventScheduleBuilder.buildEventSchedule(eventTypeListParameterizer.getEventTypeList(), pathEventCalendarCSV, isTest);
       
       this.eventManager = new MyEventManager(eventScheduleBuilder.getEventSchedule(), this, context);
              
    } // end onStart()
        

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
//        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
//        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
//        console.getOut().println("time: " + sdf.format(tick.getTime()) + " in epoch: " + tick.getTime());
        
        //long startTime = System.currentTimeMillis();
      
        eventManager.manageEventOnTick(instrument, tick);        

        //long endTime = System.currentTimeMillis();       
        //console.getOut().println("tickCalc: " + (endTime-startTime) + "ms \n");
        
    }
    
    
//    @Override
//    public void onFeedData(IFeedDescriptor feedDescriptor, ITimedData feedData) {
////        eventScheduleBuilder.filterOutObsolateEvents(feedData); // filter out the old events from eventschedule
////        eventManager.manageEventOnFeed(feedDescriptor);
//    }

    
    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        eventScheduleBuilder.filterOutObsolateEvents(askBar); // filter out the old events from eventschedule
        eventManager.manageEventOnBar(period); // set to TEN_SEC
    }

    
    @Override
    public void onMessage(IMessage message) throws JFException {
        //console.getOut().println("MESSAGE: " + message);
        eventManager.manageEventOnMessage(message);
    }

    
    @Override
    public void onAccount(IAccount account) throws JFException {        
    }

    
    @Override
    public void onStop() throws JFException { 
        if(closeOnShutDown == true){
            closeOrders();
        }
    }
    
    
    //------------------------------------------------------------------------------------
    
    
    public void closeOrders() {
        try {
            for (IOrder order : context.getEngine().getOrders()){
                if(order.getState() == IOrder.State.OPENED || order.getState() == IOrder.State.FILLED){
                    order.close();
                }
            }
        } catch (JFException ex) {        
            Logger.getLogger(NewsStradleStrategy.class.getName()).log(Level.SEVERE, null, ex);
            console.getOut().println("EXCEPTION - closeOrders");
        }
    }
    
} //END CLASS NewsStradleStrategy
