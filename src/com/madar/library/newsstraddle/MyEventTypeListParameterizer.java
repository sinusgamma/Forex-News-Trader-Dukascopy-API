/*
 * Assign the characteristics to the distinct events, fill and instantiate the MyEventType-s
 * later could be made from pregenerated csv, long list of order scenario settings
 */
package com.madar.library.newsstraddle;

import com.dukascopy.api.Instrument;
import com.madar.dataio.MyCSVManager;
import com.madar.strategies.NewsStradleStrategy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MyEventTypeListParameterizer {
    
    private List<MyEventType> eventTypeList;
    
    public List<MyEventType> getEventTypeList() {return eventTypeList;}
    
    //CONSTRUCTOR
    public MyEventTypeListParameterizer() {
        eventTypeList = new ArrayList<>();
    }
    
    
    // choose between the @Configurable parameters for test, or the predefined ones.
    public void setEventTypeProperties(NewsStradleStrategy strategy){
        if(strategy.isTest == true && strategy.isMultiScenarioRun == false){
            setTestEventTypeProperties(strategy);
        } else {
            setEventTypePropertiesFromCSV(strategy.pathMultiSettingCSV);       
        }
    }
    
    
    // set the properties from the @Configurable of the strategy
    public void setTestEventTypeProperties(NewsStradleStrategy strategy){
        MyEventType eventType;
        HashMap<Instrument, HashMap<MyOrderDirection, MyOrderScenario>> instrumentMap = new HashMap<>();
        HashMap<MyOrderDirection, MyOrderScenario> directionMap;
        MyOrderScenario orderScenario;
        
        //
        // Test Event ////////////////////////////////////////////////////////////////////////////////
        //
        eventType = new MyEventType(strategy.eventName, strategy.currencyOfEvent);
            eventType.setEnabledOrderDirection(strategy.enabledOrderDirection);
            eventType.setSecondsBeforePending(strategy.secondsBeforePending);
            eventType.setSecondsBeforeModify(strategy.secondsBeforeModify);
            eventType.setSecondsAfterNews(strategy.secondsAfterNews);
            eventType.setIsOCO(strategy.isOCO);
            eventType.setManageMoney(strategy.manageMoney);
            eventType.setCalcAmountWithMaxSpread(strategy.calcAmountWithMaxSpread);
            // VARYING SETTINGS:    
                    directionMap = new HashMap<>();
                        orderScenario = new MyOrderScenario();
                            orderScenario.setWeight(1.0); // on test the scenarios run alone - thye are competing, not composing
                            orderScenario.setPointsAway(strategy.pointsAway);
                            orderScenario.setTakeProfit(strategy.takeProfit);
                            orderScenario.setStopLoss(strategy.stopLoss);
                            orderScenario.setModifyGap(strategy.modifyGap);              
                            orderScenario.setSecondsAfterNewsOffset(strategy.secondsAfterNewsOffset);
                            orderScenario.setBreakevenTrigger(strategy.breakevenTrigger);
                            orderScenario.setBreakevenDistance(strategy.breakevenDistance);
                            orderScenario.setTrailingStop(strategy.trailingStop);
                            orderScenario.setTrailingAfter(strategy.trailingAfter);
                            orderScenario.setTrailImmediately(strategy.trailImmediately);
                            orderScenario.setRiskPercent(strategy.riskPercent);
                            orderScenario.setAmount(strategy.amount);
                            orderScenario.setMaxSpread(strategy.maxSpread);
                            orderScenario.setMaxSlippage(strategy.maxSlippage);
                    directionMap.put(MyOrderDirection.LONG, orderScenario);
                    directionMap.put(MyOrderDirection.SHORT, orderScenario);   // long and short parameters are the same to have not too much parameters on start / but can use onlyBuy and onlySell scenarios to test either         
                instrumentMap.put(strategy.testInstrument, directionMap);           
            eventType.setSimpleDirectionalSettings(instrumentMap);            
        eventTypeList.add(eventType);
    }
    
    
    // set the properties from this predefined list
    // @deprecated
    public void setSimpleEventTypeProperties() {
        // Helper object references
        MyEventType eventType;
        HashMap<Instrument, HashMap<MyOrderDirection, MyOrderScenario>> instrumentMap = new HashMap<>();
        HashMap<MyOrderDirection, MyOrderScenario> directionMap;
        MyOrderScenario orderScenario;
        
        //
        // Core CPI m/m USD ////////////////////////////////////////////////////////////////////////////////
        //
        eventType = new MyEventType("Core CPI mm", MyCurrency.USD);
            eventType.setEnabledOrderDirection(MyOrderDirection.BOTHDIRECTION);
            eventType.setSecondsBeforePending(30);
            eventType.setSecondsBeforeModify(0);
            eventType.setSecondsAfterNews(20);
            eventType.setIsOCO(true);
            eventType.setManageMoney(true);
            eventType.setCalcAmountWithMaxSpread(true);
            
            // VARYING SETTINGS:    
            
                // EURUSD-------------------------------------------------------------------
                    directionMap = new HashMap<>();
                        // for LONG:
                        orderScenario = new MyOrderScenario();
                            orderScenario.setWeight(1.0);
                            orderScenario.setPointsAway(5.0);
                            orderScenario.setTakeProfit(0);
                            orderScenario.setStopLoss(2.0);
                            orderScenario.setModifyGap(0.1);              
                            orderScenario.setSecondsAfterNewsOffset(0);
                            orderScenario.setBreakevenTrigger(1);
                            orderScenario.setBreakevenDistance(0.5);
                            orderScenario.setTrailingStop(10.0);
                            orderScenario.setTrailingAfter(0.5);
                            orderScenario.setTrailImmediately(false);
                            orderScenario.setRiskPercent(1);
                            orderScenario.setAmount(0.001);
                            orderScenario.setMaxSpread(20);
                            orderScenario.setMaxSlippage(50);
                    directionMap.put(MyOrderDirection.LONG, orderScenario);
                        // for SHORT:
                        orderScenario = new MyOrderScenario();
                            orderScenario.setWeight(1.0);
                            orderScenario.setPointsAway(5.0);
                            orderScenario.setTakeProfit(0);
                            orderScenario.setStopLoss(2.0);
                            orderScenario.setModifyGap(0.1);              
                            orderScenario.setSecondsAfterNewsOffset(0);
                            orderScenario.setBreakevenTrigger(1);
                            orderScenario.setBreakevenDistance(0.5);
                            orderScenario.setTrailingStop(1.0);
                            orderScenario.setTrailingAfter(0.5);
                            orderScenario.setTrailImmediately(false);
                            orderScenario.setRiskPercent(1);
                            orderScenario.setAmount(0.001);
                            orderScenario.setMaxSpread(20);
                            orderScenario.setMaxSlippage(50);
                    directionMap.put(MyOrderDirection.SHORT, orderScenario);                
                instrumentMap.put(Instrument.EURUSD, directionMap);

                // USDJPY ---------------------------------------------------------------------------
                    directionMap = new HashMap<>();
                        // for LONG:
                        orderScenario = new MyOrderScenario();
                            orderScenario.setWeight(1.0);
                            orderScenario.setPointsAway(5.0);
                            orderScenario.setTakeProfit(0.0);
                            orderScenario.setStopLoss(2.0);
                            orderScenario.setModifyGap(0.1);              
                            orderScenario.setSecondsAfterNewsOffset(0);
                            orderScenario.setBreakevenTrigger(4.0);
                            orderScenario.setBreakevenDistance(0.5);
                            orderScenario.setTrailingStop(3.0);
                            orderScenario.setTrailingAfter(1.0);
                            orderScenario.setTrailImmediately(true);
                            orderScenario.setRiskPercent(1);
                            orderScenario.setAmount(0.001);
                            orderScenario.setMaxSpread(20);
                            orderScenario.setMaxSlippage(50);
                    directionMap.put(MyOrderDirection.LONG, orderScenario);
                        // for SHORT:
                        orderScenario = new MyOrderScenario();
                            orderScenario.setWeight(1.0);
                            orderScenario.setPointsAway(5.0);
                            orderScenario.setTakeProfit(0.0);
                            orderScenario.setStopLoss(2.0);
                            orderScenario.setModifyGap(0.0);              
                            orderScenario.setSecondsAfterNewsOffset(0);
                            orderScenario.setBreakevenTrigger(1.0);
                            orderScenario.setBreakevenDistance(0.5);
                            orderScenario.setTrailingStop(3.0);
                            orderScenario.setTrailingAfter(1.0);
                            orderScenario.setTrailImmediately(true);
                            orderScenario.setRiskPercent(1);
                            orderScenario.setAmount(0.001);
                            orderScenario.setMaxSpread(20);
                            orderScenario.setMaxSlippage(50);
                    directionMap.put(MyOrderDirection.SHORT, orderScenario);                
                instrumentMap.put(Instrument.USDJPY, directionMap);
            
            eventType.setSimpleDirectionalSettings(instrumentMap);
            
        eventTypeList.add(eventType);
        //
        // END Core CPI m/m USD ////////////////////////////////////////////////////////////////////////////////
        //
        
        //
        // Core Very Important Event USD ////////////////////////////////////////////////////////////////////////////////
        //
        eventType = new MyEventType("Very Important Event", MyCurrency.USD);
            eventType.setEnabledOrderDirection(MyOrderDirection.BOTHDIRECTION);
            eventType.setSecondsBeforePending(30);
            eventType.setSecondsBeforeModify(0);
            eventType.setSecondsAfterNews(20);
            eventType.setIsOCO(true);
            eventType.setManageMoney(true);
            eventType.setCalcAmountWithMaxSpread(true);
            
            // VARYING SETTINGS:    
            
                // EURUSD-------------------------------------------------------------------
                    directionMap = new HashMap<>();
                        // for LONG:
                        orderScenario = new MyOrderScenario();
                            orderScenario.setWeight(1.0);
                            orderScenario.setPointsAway(5.0);
                            orderScenario.setTakeProfit(0);
                            orderScenario.setStopLoss(2.0);
                            orderScenario.setModifyGap(0.1);              
                            orderScenario.setSecondsAfterNewsOffset(0);
                            orderScenario.setBreakevenTrigger(1);
                            orderScenario.setBreakevenDistance(0.5);
                            orderScenario.setTrailingStop(10.0);
                            orderScenario.setTrailingAfter(0.5);
                            orderScenario.setTrailImmediately(false);
                            orderScenario.setRiskPercent(1);
                            orderScenario.setAmount(0.001);
                            orderScenario.setMaxSpread(20);
                            orderScenario.setMaxSlippage(50);
                    directionMap.put(MyOrderDirection.LONG, orderScenario);
                        // for SHORT:
                        orderScenario = new MyOrderScenario();
                            orderScenario.setWeight(1.0);
                            orderScenario.setPointsAway(5.0);
                            orderScenario.setTakeProfit(0);
                            orderScenario.setStopLoss(2.0);
                            orderScenario.setModifyGap(0.1);              
                            orderScenario.setSecondsAfterNewsOffset(0);
                            orderScenario.setBreakevenTrigger(1);
                            orderScenario.setBreakevenDistance(0.5);
                            orderScenario.setTrailingStop(1.0);
                            orderScenario.setTrailingAfter(0.5);
                            orderScenario.setTrailImmediately(false);
                            orderScenario.setRiskPercent(1);
                            orderScenario.setAmount(0.001);
                            orderScenario.setMaxSpread(20);
                            orderScenario.setMaxSlippage(50);
                    directionMap.put(MyOrderDirection.SHORT, orderScenario);                
                instrumentMap.put(Instrument.EURUSD, directionMap);

                // USDJPY ---------------------------------------------------------------------------
                    directionMap = new HashMap<>();
                        // for LONG:
                        orderScenario = new MyOrderScenario();
                            orderScenario.setWeight(1.0);
                            orderScenario.setPointsAway(5.0);
                            orderScenario.setTakeProfit(0.0);
                            orderScenario.setStopLoss(2.0);
                            orderScenario.setModifyGap(0.1);              
                            orderScenario.setSecondsAfterNewsOffset(0);
                            orderScenario.setBreakevenTrigger(4.0);
                            orderScenario.setBreakevenDistance(0.5);
                            orderScenario.setTrailingStop(3.0);
                            orderScenario.setTrailingAfter(1.0);
                            orderScenario.setTrailImmediately(true);
                            orderScenario.setRiskPercent(1);
                            orderScenario.setAmount(0.001);
                            orderScenario.setMaxSpread(20);
                            orderScenario.setMaxSlippage(50);
                    directionMap.put(MyOrderDirection.LONG, orderScenario);
                        // for SHORT:
                        orderScenario = new MyOrderScenario();
                            orderScenario.setWeight(1.0);
                            orderScenario.setPointsAway(5.0);
                            orderScenario.setTakeProfit(0.0);
                            orderScenario.setStopLoss(2.0);
                            orderScenario.setModifyGap(0.0);              
                            orderScenario.setSecondsAfterNewsOffset(0);
                            orderScenario.setBreakevenTrigger(1.0);
                            orderScenario.setBreakevenDistance(0.5);
                            orderScenario.setTrailingStop(3.0);
                            orderScenario.setTrailingAfter(1.0);
                            orderScenario.setTrailImmediately(true);
                            orderScenario.setRiskPercent(1);
                            orderScenario.setAmount(0.001);
                            orderScenario.setMaxSpread(20);
                            orderScenario.setMaxSlippage(50);
                    directionMap.put(MyOrderDirection.SHORT, orderScenario);                
                instrumentMap.put(Instrument.USDJPY, directionMap);
            
            eventType.setSimpleDirectionalSettings(instrumentMap);
            
        eventTypeList.add(eventType);
        //
        // END Very Important Event USD ////////////////////////////////////////////////////////////////////////////////
        //
        
    } // end function setSimpleEventTypeProperties()  
    
    
    // sets the trading properties for the multi and single trade list as well from csv - the first row of event goes to the single list
    public void setEventTypePropertiesFromCSV(String filePath){
        
        eventTypeList = MyCSVManager.readMultiSettingsCsv(filePath);
        
        System.out.println("SIZE OF eventTypeList: " + eventTypeList.size());
        
        for (MyEventType myEventType : eventTypeList) {
            System.out.println(
                "IN SET:" + 
                myEventType.getEventName() + "| " + 
                myEventType.getCurrency() + "| " + 
                myEventType.getEnabledOrderDirection() + "| " +
                myEventType.getSecondsBeforePending() + "| " + 
                myEventType.getSecondsBeforeModify() + "| " +
                myEventType.getSecondsAfterNews() + "| " + 
                myEventType.isIsOCO() + "| " + 
                myEventType.isManageMoney() + "| " + 
                myEventType.isCalcAmountWithMaxSpread()
            );
            Iterator<HashMap.Entry<Instrument, HashMap<MyOrderDirection, List<MyOrderScenario>>>> instruementIterator = myEventType.getMultiDirectionalSettings().entrySet().iterator();
            while (instruementIterator.hasNext()) {
                Map.Entry<Instrument, HashMap<MyOrderDirection, List<MyOrderScenario>>> instrumentContainer = instruementIterator.next();
                System.out.println("    " + instrumentContainer.getKey());
                
                Iterator<HashMap.Entry<MyOrderDirection, List<MyOrderScenario>>> directionIterator = instrumentContainer.getValue().entrySet().iterator();
                while (directionIterator.hasNext()) {
                    Map.Entry<MyOrderDirection, List<MyOrderScenario>> directionContainer = directionIterator.next();
                    System.out.println("        " + directionContainer.getKey());
                    
                    for (MyOrderScenario myOrderScenario : directionContainer.getValue()) {
                        System.out.println("        scenarioId: " + 
                            myOrderScenario.getScenarioId() + "| " +
                            myOrderScenario.getWeight() + "| " +
                            myOrderScenario.getPointsAway() + "| " +
                            myOrderScenario.getTakeProfit() + "| " +
                            myOrderScenario.getStopLoss() + "| " +
                            myOrderScenario.getModifyGap() + "| " +
                            myOrderScenario.getSecondsAfterNewsOffset() + "| " +
                            myOrderScenario.getBreakevenTrigger() + "| " +
                            myOrderScenario.getBreakevenDistance() + "| " +
                            myOrderScenario.getTrailingStop() + "| " +
                            myOrderScenario.getTrailingAfter() + "| " +
                            myOrderScenario.isTrailImmediately() + "| " +
                            myOrderScenario.getRiskPercent() + "| " +
                            myOrderScenario.getAmount() + "| " +
                            myOrderScenario.getMaxSpread() + "| " +
                            myOrderScenario.getMaxSlippage()
                        );
                    }
                }              
            }            
        }
    }
    
} // END Class MyEventTypeListParameterizer
