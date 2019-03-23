/*
 * Class to buidl and set up the parameters for the test
 * 
 */
package com.madar.tester;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.system.ITesterClient;
import com.madar.dataio.MyCSVManager;
import com.madar.dataio.MyPostgresManager;
import com.madar.dataio.MySingleEventHolder;
import com.madar.library.newsstraddle.MyCurrency;
import com.madar.library.newsstraddle.MyOrderDirection;
import com.madar.strategies.NewsStradleStrategy;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MyTestSetUp {
    
    private int enabledRunPerProcess = 500; // used only in multiprocess tests
    private String eventName = "United States ISM NonManufacturing PMI"; // need exact name from above file - so spaces as well
    private MyCurrency currencyOfEvent = MyCurrency.USD;
    private Instrument testInstrument = Instrument.USDJPY;
    
    private String pathEventCalendarCSV = "F:\\FOREX\\nsresource\\news-schedule\\gbp_manufacturingpmi.csv"; // the file containing the events to run if we dont use the database
    private boolean scheduleFromDB = true; // only the tester uses DB for schedule, the strategy will use csv, provided from DB
    private int dbEventLimit = 40;
    private String earliestDate = "2015-01-01 00:00:00";
    
    
    // set the period begin and end shift in secs
    private final int beginShiftSecs = 20;
    private final int endShiftSecs = 600;
    // initial deposit
    private final double initialDeposit = 1000;
    private List<RunParameter> parameterList; // list of parameter scenarios
    private LinkedHashMap<ZonedDateTime,ZonedDateTime> eventPeriods; // event-period download boundaries
    
    public String getPathEventCalendarCSV() {return pathEventCalendarCSV;}
    public String getEventName() {return eventName;}
    public MyCurrency getCurrency() {return currencyOfEvent;}
    public Instrument getTestInstrument() {return testInstrument;}
    public int getBeginShiftSecs() {return beginShiftSecs;}
    public int getEndShiftSecs() {return endShiftSecs;}
    public double getInitialDeposit() {return initialDeposit;}
    public List<RunParameter> getParameterList() {return parameterList;}
    public LinkedHashMap<ZonedDateTime, ZonedDateTime> getEventPeriods() {return eventPeriods;}
    public int getEnabledRunPerProcess() {return enabledRunPerProcess;}  

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    public void setCurrencyOfEvent(MyCurrency currencyOfEvent) {
        this.currencyOfEvent = currencyOfEvent;
    }
    public void setTestInstrument(Instrument testInstrument) {
        this.testInstrument = testInstrument;
    }
    public void setParameterList(List<RunParameter> parameterList) {
        this.parameterList = parameterList;
    }
    
    public MyTestSetUp() {
        parameterList = new ArrayList<>();
        // the event period download boundaries
        eventPeriods = setDownloadableTimePeriods();
    }
    
    public MyTestSetUp(String eventName, MyCurrency currencyOfEvent, Instrument testInstrument){
        this.eventName = eventName;
        this.currencyOfEvent = currencyOfEvent;
        this.testInstrument = testInstrument;
        parameterList = new ArrayList<>();
        // the event period download boundaries
        eventPeriods = setDownloadableTimePeriods();
    }

    
    // build the list of strategies on every period with arrays - ugly, but good for single event run, can here set up the scenarios
    public List<RunParameter> buildRunParameterList(){
       
        double pointsAwayAr[] = {3.0, 6.0};
        double takeProfitAr[] = {0.0, 6.0};
        double stopLossAr[] = {2.0};
        long secondsAfterNewsAr[] = {5};
        double breakevenTriggerAr[] = {0.0};
        double breakevenDistanceAr[] = {2.0};            
        double trailingStopAr[] = {1.0, 1.5};  
        double trailingAfterAr[] = {0.0, 1.5};      
        boolean trailImmediatelyAr[] = {true};
        double maxSpreadAr[] = {0};
        double maxSlippageAr[] = {0.0};
        MyOrderDirection enabledDirectionAr[] = {MyOrderDirection.SHORT, MyOrderDirection.LONG};
        
        // don!t change loop order !!!
        pointsAwayLoop: for (int IpA = 0;  IpA < pointsAwayAr.length; IpA++) {
        takeProfitLoop: for (int ItP = 0;  ItP < takeProfitAr.length; ItP++) {
        stopLossLoop: for (int IsL = 0;  IsL < stopLossAr.length; IsL++) {               
        secondsAfterNewsLoop: for (int IsA = 0;  IsA < secondsAfterNewsAr.length; IsA++) { 
        breakevenTriggerLoop: for (int IbET = 0;  IbET < breakevenTriggerAr.length; IbET++) { 
        breakevenDistanceLoop: for (int IbED = 0;  IbED < breakevenDistanceAr.length; IbED++) {            
        trailingStopLoop: for (int ItS = 0;  ItS < trailingStopAr.length; ItS++) {  
        trailingAfterLoop: for (int ItA = 0;  ItA < trailingAfterAr.length; ItA++) {         
        trailImmediatelyLoop: for (int ItI = 0;  ItI < trailImmediatelyAr.length; ItI++) {
        maxSpreadLoop: for (int ImSp = 0;  ImSp < maxSpreadAr.length; ImSp++) { 
        maxSlippageLoop: for (int ImSl = 0;  ImSl < maxSlippageAr.length; ImSl++) {
        OrderDirectionLoop: for(int oDi = 0; oDi < enabledDirectionAr.length; oDi++){    
            
            // run strategy only with reasonable parameters - break to closer loop on condition for safety - this way will not miss usefull scenario on wrong break
            //if(stopLossAr[IsL] < trailingStopAr[ItS]){break trailingStopLoop;} // if SL is smaller than trailing don't examine - will not use BUT CAN MATTER
            if(trailImmediatelyAr[ItI] == true && ItA > 0){break trailImmediatelyLoop;} // if trailImmediatelly true trailingAfter doesn't matter, so loop it only with the first parameter
            if(breakevenTriggerAr[IbET] == 0.0 && IbED > 0){break breakevenDistanceLoop;}
            if(breakevenDistanceAr[IbED] > breakevenTriggerAr[IbET]){break breakevenDistanceLoop;} // if breakevenDistance is too large would kill the position

                RunParameter runParameter = new RunParameter();
                    runParameter.orderDirection = enabledDirectionAr[oDi];
                    runParameter.pointsAway = pointsAwayAr[IpA];
                    runParameter.takeProfit = takeProfitAr[ItP];
                    runParameter.stopLoss = stopLossAr[IsL];
                    runParameter.secondsAfterNews = secondsAfterNewsAr[IsA];
                    runParameter.breakevenTrigger = breakevenTriggerAr[IbET];
                    runParameter.breakevenDistance = breakevenDistanceAr[IbED];
                    runParameter.trailingStop = trailingStopAr[ItS];
                    runParameter.trailingAfter = trailingAfterAr[ItA];
                    runParameter.trailImmediately = trailImmediatelyAr[ItI];
                    runParameter.maxSpread = maxSpreadAr[ImSp];
                    runParameter.maxSlippage = maxSlippageAr[ImSl];
                    
                parameterList.add(runParameter);
            
        }}}}}}}}}}}}
        return parameterList;
    } // end buildRunParameterList
    
    
        // build the list of strategies on every period with arrays - overwrite the above function
    public List<RunParameter> buildRunParameterList(
        List<Double> pointsAwayList,
        List<Double> takeProfitList,
        List<Double> stopLossList,
        List<Long> secondsAfterNewsList,
        List<Double> breakevenTriggerList,
        List<Double> breakevenDistanceList,            
        List<Double> trailingStopList,  
        List<Double> trailingAfterList,      
        List<Boolean> trailImmediatelyList,
        List<Double> maxSpreadList,
        List<Double> maxSlippageList,
        List<MyOrderDirection> enabledDirectionList    
    ){
        
        // don!t change loop order !!!
        pointsAwayLoop: for (int IpA = 0;  IpA < pointsAwayList.size(); IpA++) {
        takeProfitLoop: for (int ItP = 0;  ItP < takeProfitList.size(); ItP++) {    
        stopLossLoop: for (int IsL = 0;  IsL < stopLossList.size(); IsL++) {               
        secondsAfterNewsLoop: for (int IsA = 0;  IsA < secondsAfterNewsList.size(); IsA++) { 
        breakevenTriggerLoop: for (int IbET = 0;  IbET < breakevenTriggerList.size(); IbET++) { 
        breakevenDistanceLoop: for (int IbED = 0;  IbED < breakevenDistanceList.size(); IbED++) {            
        trailingStopLoop: for (int ItS = 0;  ItS < trailingStopList.size(); ItS++) {  
        trailingAfterLoop: for (int ItA = 0;  ItA < trailingAfterList.size(); ItA++) {         
        trailImmediatelyLoop: for (int ItI = 0;  ItI < trailImmediatelyList.size(); ItI++) {
        maxSpreadLoop: for (int ImSp = 0;  ImSp < maxSpreadList.size(); ImSp++) { 
        maxSlippageLoop: for (int ImSl = 0;  ImSl < maxSlippageList.size(); ImSl++) {
        OrderDirectionLoop: for(int oDi = 0; oDi < enabledDirectionList.size(); oDi++){    
            
            // run strategy only with reasonable parameters - break to closer loop on condition for safety - this way will not miss usefull scenario on wrong break
            // if(stopLossList.get(IsL) < trailingStopList.get(ItS)){break trailingStopLoop;} // if SL is smaller than trailing don't examine - will not use
            if(trailImmediatelyList.get(ItI) == true && ItA > 0){break trailImmediatelyLoop;} // if trailImmediatelly true trailingAfter doesn't matter, so loop it only with the first parameter
            if(breakevenTriggerList.get(IbET) == 0.0 && IbED > 0){break breakevenDistanceLoop;}
            if(breakevenDistanceList.get(IbED) > breakevenTriggerList.get(IbET)){break breakevenDistanceLoop;} // if breakevenDistance is too large would kill the position

                RunParameter runParameter = new RunParameter();
                    runParameter.orderDirection = enabledDirectionList.get(oDi);
                    runParameter.pointsAway = pointsAwayList.get(IpA);
                    runParameter.takeProfit = takeProfitList.get(ItP);
                    runParameter.stopLoss = stopLossList.get(IsL);
                    runParameter.secondsAfterNews = secondsAfterNewsList.get(IsA);
                    runParameter.breakevenTrigger = breakevenTriggerList.get(IbET);
                    runParameter.breakevenDistance = breakevenDistanceList.get(IbED);
                    runParameter.trailingStop = trailingStopList.get(ItS);
                    runParameter.trailingAfter = trailingAfterList.get(ItA);
                    runParameter.trailImmediately = trailImmediatelyList.get(ItI);
                    runParameter.maxSpread = maxSpreadList.get(ImSp);
                    runParameter.maxSlippage = maxSlippageList.get(ImSl);
                    
                parameterList.add(runParameter);
            
        }}}}}}}}}}}}
        return parameterList;
    } // end buildRunParameterList
    
    
    // generate a strategy from an other with parameters from parameterList
    public NewsStradleStrategy parameterizeStrategy(NewsStradleStrategy strategy, int scenarioId){
        
        strategy.closeOnShutDown = true;
        strategy.isTest = true;
        strategy.isMultiScenarioRun = false;
        
        strategy.pathEventCalendarCSV = this.pathEventCalendarCSV;
        strategy.eventName = this.eventName;
        strategy.currencyOfEvent =this.currencyOfEvent;
        strategy.testInstrument = this.testInstrument;
        
        strategy.enabledOrderDirection = parameterList.get(scenarioId).getEnabledOrderDirection();
        strategy.pointsAway = parameterList.get(scenarioId).getPointsAway();
        strategy.takeProfit = parameterList.get(scenarioId).getTakeProfit();
        strategy.stopLoss = parameterList.get(scenarioId).getStopLoss();  
        strategy.modifyGap = 0.1;
        strategy.secondsBeforePending = 10;
        strategy.secondsBeforeModify = 0;
        strategy.secondsAfterNews = parameterList.get(scenarioId).getSecondsAfterNews();
        strategy.secondsAfterNewsOffset = 0;
        strategy.breakevenTrigger = parameterList.get(scenarioId).getBreakevenTrigger();
        strategy.breakevenDistance = parameterList.get(scenarioId).getBreakevenDistance();            
        strategy.trailingStop = parameterList.get(scenarioId).getTrailingStop();  
        strategy.trailingAfter = parameterList.get(scenarioId).getTrailingAfter();
        strategy.trailImmediately = parameterList.get(scenarioId).isTrailImmediately();
        strategy.isOCO = true;
        strategy.manageMoney = true;
        strategy.calcAmountWithMaxSpread = false;    
        strategy.riskPercent = 0.01; 
        strategy.amount = 0.001;
        strategy.maxSpread = parameterList.get(scenarioId).getMaxSpread();
        strategy.maxSlippage = parameterList.get(scenarioId).getMaxSlippage();
        return strategy;
    }
   
    
    // prepaires the periods to download around events from csv
    private LinkedHashMap<ZonedDateTime, ZonedDateTime> setDownloadableTimePeriods() {
        LinkedHashMap<ZonedDateTime, ZonedDateTime> collection = new LinkedHashMap();
        List<MySingleEventHolder> eventList;
        if(scheduleFromDB){
            pathEventCalendarCSV = "F:\\FOREX\\nsresource\\news-schedule\\schedule-from-db-" + this.eventName + ".csv"; // if event data is from DB, then the strategy will run from this pregenerated csv
            eventList = MyPostgresManager.readForexCalendarDB(eventName, currencyOfEvent, dbEventLimit, earliestDate);
            try {
                MyCSVManager.writeEventScheduleToCSV(eventList, pathEventCalendarCSV); // if schedule data for test is from DB must write a csv, because the strategy uses only csv
            } catch (IOException ex) {
                Logger.getLogger(MyTestSetUp.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            eventList = MyCSVManager.readForexCalendarCsv(pathEventCalendarCSV);
        }
        for (MySingleEventHolder singleEventHolder : eventList) {
            collection.put(singleEventHolder.getEventTime().minusSeconds(beginShiftSecs), singleEventHolder.getEventTime().plusSeconds(endShiftSecs));
        }
        return collection;
    }    


    // set the instrument the test will use, the test runs only one instrument at a time
    public void subscribeTestedInstrument(ITesterClient client){
        Set<Instrument> instruments;
        //set instruments that will be used in testing
        instruments = new HashSet<>();
        instruments.add(testInstrument);
        System.out.println("Subscribing instruments...");
        client.setSubscribedInstruments(instruments);        
    } 
    
    
    // store a single parameter scenario
    public class RunParameter{
        private MyOrderDirection orderDirection;
        private double pointsAway;
        private double takeProfit;
        private double stopLoss;               
        private long secondsAfterNews;
        private double breakevenTrigger;
        private double breakevenDistance;            
        private double trailingStop;  
        private double trailingAfter;      
        private boolean trailImmediately;
        private double maxSpread;
        private double maxSlippage;

        public MyOrderDirection getEnabledOrderDirection() {return orderDirection;}
        public double getPointsAway() {return pointsAway;}
        public double getTakeProfit() {return takeProfit;}
        public double getStopLoss() {return stopLoss;}
        public long getSecondsAfterNews() {return secondsAfterNews;}
        public double getBreakevenTrigger() {return breakevenTrigger;}
        public double getBreakevenDistance() {return breakevenDistance;}
        public double getTrailingStop() {return trailingStop;}
        public double getTrailingAfter() {return trailingAfter;}
        public boolean isTrailImmediately() {return trailImmediately;}
        public double getMaxSpread() {return maxSpread;}
        public double getMaxSlippage() {return maxSlippage;}
    } // end inner class runParameters
   
} // END CLASS MyTestSetUp
