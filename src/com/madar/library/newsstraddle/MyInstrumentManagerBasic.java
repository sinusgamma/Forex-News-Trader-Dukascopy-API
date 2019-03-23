/*
 * The basic logic to trade instruments on news
 * 
 */
package com.madar.library.newsstraddle;

import com.dukascopy.api.IConsole;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.JFUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MyInstrumentManagerBasic extends MyInstrumentManager{
    
    
    private final IContext context;
    private final IEngine engine;
    private final IConsole console;
    private final JFUtils utils;
    private final Instrument managedInstrument;
    private Long actualEventTime;
    private MyEventType actualEventType;
    
    private List<IOrder> instrumentOrders;
    private int orderCounter;
    private boolean triggeredOpen;
    private boolean triggeredOCO; // if the manager triggered OCO yet
    private boolean triggeredTrail; // if the manager triggered Trail yet
    private boolean triggeredBreakEven; // if triggered breakeven

    public List<IOrder> getInstrumentOrders() {return instrumentOrders;}
    
    
    // CONSTRUCTOR
    public MyInstrumentManagerBasic(Instrument managedInstrument, Long actualEventTime, MyEventType actualEventType, IContext context) {
        this.context = context;
        this.engine = context.getEngine();
        this.console = context.getConsole();
        this.utils = context.getUtils();
        this.managedInstrument = managedInstrument;
        this.actualEventTime = actualEventTime;
        this.actualEventType = actualEventType;
        this.instrumentOrders = new ArrayList<>();
        this.orderCounter = 0;
        this.triggeredOpen = false;
        this.triggeredOCO = false;
        this.triggeredTrail = false;
        this.triggeredBreakEven = false;
        console.getOut().println(managedInstrument + " instrumentManager for " + actualEventType.getEventName() + " is ready");
    }
        
    
    //------------------------------------------------------------------------------------------------------------------------------
    
    
    @Override
    public void manageInstrumentOnTick(MyEventState eventState, ITick tick) {
                
        double frontPrice;
        double backPrice;
        int a;
        MyOrderDirection orderDirection;
        
        openPositions(eventState, tick);
        
        if(! instrumentOrders.isEmpty()) { // no meaning to modify not existing orders
            for (IOrder order : instrumentOrders) {
                
                orderDirection = order.isLong() ? MyOrderDirection.LONG : MyOrderDirection.SHORT;
                a = getShortLongMirror(orderDirection);
                frontPrice = getFrontPrice(orderDirection, tick);
                backPrice = getBackPrice(orderDirection, tick);
                
                MyOrderScenario scenario = actualEventType.getSimpleSetting(managedInstrument, orderDirection);

                closePendingOverMaxSpread(scenario, order, frontPrice, backPrice);
                modifyPendingOrder(scenario, order, eventState, a, frontPrice, backPrice);
                trailFilledOrder(scenario, order, a, backPrice);
                breakEven(scenario, order, a, backPrice);
            } // end for instrumentOrders
        } // end if ! instrumentOrders.isEmpty()
//        try {
//            console.getOut().println("Order Count in " + managedInstrument + " list: " + instrumentOrders.size() + " - Active Orders: " + engine.getOrders());
//        } catch (JFException ex) {
//            Logger.getLogger(MyInstrumentManagerBasic.class.getName()).log(Level.SEVERE, null, ex);
//            console.getOut().println("EXCEPTION - manageInstrumentOnTick - problem getOrders()");
//        }
    } // end manageInstrumentOnTick

    
    @Override
    public void manageInstrumentOnMessage(IMessage message) { 
        IOrder order = message.getOrder();
        removeInactiveOrder(order);
        orderCancelOrder(order);
    } // end manageInstrumentOnMessage
    
    
    //---------------------------------------------------------------------------------------------------------------------------------

    
    // Open 1 or 2 orders IF in this basic strategy there is NO order yet for the instrument
    private void openPositions(MyEventState eventState, ITick tick) {
        
        if(eventState != MyEventState.BEFORE || triggeredOpen == true || !instrumentOrders.isEmpty()) return;

        if(actualEventType.getEnabledOrderDirection() == MyOrderDirection.BOTHDIRECTION || actualEventType.getEnabledOrderDirection() == MyOrderDirection.LONG){
            openPending(MyOrderDirection.LONG, tick); // open pending buy
        }
        if(actualEventType.getEnabledOrderDirection() == MyOrderDirection.BOTHDIRECTION || actualEventType.getEnabledOrderDirection() == MyOrderDirection.SHORT){
            openPending(MyOrderDirection.SHORT, tick); // open pending sell
        }
          
        triggeredOpen = true; // if orders are triggered once don't open new orders

    } // end openPositions
    

    // if spread is over maxspread close the orders for this instrument
    private void closePendingOverMaxSpread(MyOrderScenario scenario, IOrder order, double frontPrice, double backPrice){
        double maxSpread = managedInstrument.getPipValue() * scenario.getMaxSpread();
        if(doubleEquals(maxSpread, 0)) {return;} // max spread set zero we don't use max sprea
        
        double spread = Math.abs(frontPrice - backPrice);
        if(spread > maxSpread){
            if(order.getState() == IOrder.State.OPENED){
                try {
                    order.close();
                    console.getOut().println("close order request send because of large spread: " + spread + " maxSpread: " + maxSpread);
                } catch (JFException ex) {
                    Logger.getLogger(MyInstrumentManagerBasic.class.getName()).log(Level.SEVERE, null, ex);
                    console.getOut().println("EXEPTION - closePendingOverMaxSpread");
                }
            }
        }
    }
    

    // checks if the pending order should be modified and modifies it in the BEFORE state
    private void modifyPendingOrder(MyOrderScenario scenario, IOrder order, MyEventState eventState, int a, double frontPrice, double backPrice) {
        
        if(eventState != MyEventState.BEFORE) return; // modify only BEFORE situation           
        if(order.getState() != IOrder.State.OPENED){return;} // modify only OPENED
            
        double actualOpenPrice;
        double shouldOpenPrice;        
        double modifyGap;
        double pointsAway;
        double stopLoss;
        double takeProfit;        
        double takeProfitPrice;
        double stopLossPrice;            

        modifyGap = managedInstrument.getPipValue() * scenario.getModifyGap();
        pointsAway = managedInstrument.getPipValue() * scenario.getPointsAway();
        
        actualOpenPrice = order.getOpenPrice();
        shouldOpenPrice = frontPrice + a * pointsAway;

        // if pendingPrice diff >= modifyGap we should modify the prices
        if(Math.abs(actualOpenPrice - shouldOpenPrice) >= modifyGap){   
            try {
                order.setOpenPrice(roundToPippette(shouldOpenPrice));
                
                if(!doubleEquals(order.getTakeProfitPrice(), 0)){ // only set takeProfitPrice if it isn't zero
                    takeProfit = managedInstrument.getPipValue() * scenario.getTakeProfit();
                    takeProfitPrice = frontPrice + a * takeProfit;
                    order.setTakeProfitPrice(roundToPippette(takeProfitPrice));
                }
                
                if(!doubleEquals(order.getStopLossPrice(), 0)){ // only set stopLossPrice if it isn't zero
                    stopLoss = managedInstrument.getPipValue() * scenario.getStopLoss();
                    stopLossPrice = backPrice - a * stopLoss;
                    if(!doubleEquals(order.getStopLossPrice(), roundToPippette(stopLossPrice))){ // if frontPrice changes backPrice can be the same
                        order.setStopLossPrice(roundToPippette(stopLossPrice));
                    }
                }
            } catch (JFException ex) {
                Logger.getLogger(MyInstrumentManagerBasic.class.getName()).log(Level.SEVERE, null, ex);
                console.getOut().println("EXEPTION - modifyPendingOrder");
            }
        }

    } // end modifyPendingOrders    
    
    
    // trail filled order
    private void trailFilledOrder(MyOrderScenario scenario, IOrder order, int a, double backPrice) {

        if(order.getState() != IOrder.State.FILLED){return;} // trail only FILLED orders
 
        boolean isTrailImmediately = scenario.isTrailImmediately();
        double distanceAfterProfit;
        double trailingAfterDistance;
        double trailingSLDistance = managedInstrument.getPipValue() * scenario.getTrailingStop();    
        double actualSLDistance;
        
        if(triggeredTrail == false && isTrailImmediately == false){ // check for conditional trailings
            
            distanceAfterProfit = managedInstrument.getPipValue() * order.getProfitLossInPips();
            trailingAfterDistance = managedInstrument.getPipValue() * scenario.getTrailingAfter();           
                        
            if(distanceAfterProfit <= 0){ // we aren't in profit yet, then NO trail
                return;
            } else if(!doubleEquals(trailingAfterDistance, 0) && distanceAfterProfit <= (trailingAfterDistance + trailingSLDistance)) { // if IS trailing after and we haven't got enough profit, then No trail
                return;
            } else {
                triggeredTrail = true;
            }
        }       

        actualSLDistance = Math.abs(backPrice - order.getStopLossPrice());
        
        if(actualSLDistance > trailingSLDistance){ try {
            // if actualSL is further than the trailingSLDistance
            order.setStopLossPrice(roundToPippette(backPrice - a * trailingSLDistance));            
            } catch (JFException ex) {
                Logger.getLogger(MyInstrumentManagerBasic.class.getName()).log(Level.SEVERE, null, ex);
                console.getOut().println("EXEPTION - trailFilledOrder");
            }
        }            

    } // end trailFilledOrder    
    

    // break even after trigger distance
    private void breakEven(MyOrderScenario scenario, IOrder order, int a, double backPrice) {  
        
        if(order.getState() != IOrder.State.FILLED || triggeredBreakEven == true) {return;} 
        
        double breakEvenTrigger = managedInstrument.getPipValue() * scenario.getBreakevenTrigger(); // from openPice
        
        if(doubleEquals(breakEvenTrigger, 0)) {return;}
               
        double breakEvenDistance = managedInstrument.getPipValue() * scenario.getBreakevenDistance(); // from openPice
        
        if(breakEvenDistance >= breakEvenTrigger){
            console.getOut().println("breakEvenDistance is too large, would kill the trade immediatelly - is set to 0!");
            breakEvenDistance = 0;
        }
        
        double distanceAfterProfit = managedInstrument.getPipValue() * order.getProfitLossInPips(); // from openPrice
        
        if(distanceAfterProfit < breakEvenTrigger){ return;} // if we aren't far enough then return
        
        double actualSLDistance = Math.abs(backPrice - order.getStopLossPrice()); // from actualPrice
             
        if(breakEvenDistance >= 0 && breakEvenDistance < actualSLDistance){ // if breakEvenDistance too large would kill the trade
            try {            
                order.setStopLossPrice(roundToPippette(order.getOpenPrice() + a * breakEvenDistance));                              
            } catch (JFException ex) {
                Logger.getLogger(MyInstrumentManagerBasic.class.getName()).log(Level.SEVERE, null, ex);
                console.getOut().println("EXEPTION - breakEven");
            }
            triggeredBreakEven = true;
        }   
    } // end breakEven
    
    
    // open pending buy order for the actual event type
    private void openPending(MyOrderDirection orderDirection, ITick tick) {
        
        double frontPrice = getFrontPrice(orderDirection, tick); // the ask if we want long - and bid if want short
        double backPrice = getBackPrice(orderDirection, tick);
        int a = getShortLongMirror(orderDirection); // for *-1 if short order
        IEngine.OrderCommand orderCommand = getOrderCommand(orderDirection);
        
        IOrder order = null;
        double amount;
        double slippage;
        double pendingPrice;
        double pointsAway;
        double stopLossPrice;
        double takeProfitPrice;
        long goodForTime;
        
        if(frontPrice == 0 || backPrice == 0 || a == 0 || orderCommand == null) {return;} // do nothing if the parameters aren't set
        
        MyOrderScenario scenario = actualEventType.getSimpleSetting(managedInstrument, orderDirection);
        
        if(actualEventType.isManageMoney() == true){
            amount = calculateOrderAmount(scenario);
        } else {
            amount = scenario.getAmount();
        }
       
        slippage = managedInstrument.getPipValue() * scenario.getMaxSlippage();
        
        pointsAway = managedInstrument.getPipValue() * scenario.getPointsAway();
        pendingPrice = frontPrice + a * pointsAway;
      
        if(doubleEquals(scenario.getStopLoss(), 0)){
            stopLossPrice = 0;
        } else {
            double slDistance = managedInstrument.getPipValue() * scenario.getStopLoss();
            stopLossPrice = backPrice - a * slDistance;
        }
        
        if(doubleEquals(scenario.getTakeProfit(), 0)){
            takeProfitPrice = 0; // 0 for no take profit
        } else {
            double tpDistance = managedInstrument.getPipValue() * scenario.getTakeProfit();
            takeProfitPrice = frontPrice + a * tpDistance;
        }
        
        goodForTime = actualEventTime + (actualEventType.getSecondsAfterNews() * 1000); //withdraw after beforePending + afterNews secs, so after news by secondsAfterNews
        console.getOut().println("goodForTime: " + ZonedDateTime.ofInstant(Instant.ofEpochMilli(goodForTime), ZoneId.of("UTC")));
        try {
            // open the order
            order = engine.submitOrder(getLabel(managedInstrument),
                    managedInstrument,
                    orderCommand,
                    amount, // in millions
                    roundToPippette(pendingPrice),
                    slippage,
                    roundToPippette(stopLossPrice),
                    roundToPippette(takeProfitPrice),
                    goodForTime
            );            
        } catch (JFException ex) {
            Logger.getLogger(MyInstrumentManagerBasic.class.getName()).log(Level.SEVERE, null, ex);
            console.getOut().println("EXEPTION - openPending");
        }
       
        if(order != null){
        instrumentOrders.add(order);
        }
    } // end openPending
    
    
    // calculates the amount of order by percent of equity for Single instrumental order
    private double calculateOrderAmount(MyOrderScenario scenario){
        double buyAmount;
        double stopLossInPip = scenario.getStopLoss();
        double maxSpreadInPip = 0.0;
        double riskPercent = scenario.getRiskPercent();        
        double pipValueInAcCur = 0.0;
        double equity = context.getAccount().getEquity();
        double leverage = context.getAccount().getLeverage();
        double riskableAmount = equity * riskPercent;
        boolean calcAmountWithMaxSpread = actualEventType.isCalcAmountWithMaxSpread();
        
        try {
            pipValueInAcCur = utils.convertPipToCurrency(managedInstrument, context.getAccount().getAccountCurrency());
        } catch (JFException ex) {
            Logger.getLogger(MyInstrumentManagerBasic.class.getName()).log(Level.SEVERE, null, ex);
            console.getOut().println("EXCEPTION - calculateOrderAmount" + ex.getMessage());
        }
        
        if(calcAmountWithMaxSpread == true){
            maxSpreadInPip = scenario.getMaxSpread();
            buyAmount = (riskableAmount / ((stopLossInPip + maxSpreadInPip) * pipValueInAcCur)) / 1000000;
        } else {
            buyAmount = (riskableAmount / (stopLossInPip * pipValueInAcCur)) / 1000000;
        }       
        
        return buyAmount;
    }
    
    
    // Removes not opened or filled orderes from instrumentOrders
    private void removeInactiveOrder(IOrder order){
        if(order == null || !instrumentOrders.contains(order)){return;} // if no order or ordered isn't in this.instrumentOrders go back
        if(order.getState() == IOrder.State.CLOSED || order.getState() == IOrder.State.CANCELED){
            instrumentOrders.remove(order);
            console.getOut().println("ORDER removed from list ---------------------------------------------------------------------");
        }
    }
    
    
    // makes OCO if isn't triggered so far
    private void orderCancelOrder(IOrder order) {
        if(actualEventType.isIsOCO() == false || triggeredOCO == true){return;} // if OCO disabled or happened no need to do it again
        if(order == null || !instrumentOrders.contains(order)){return;} // if no order or order isn't in this.instrumentOrders go back       
        if(order.getState() != IOrder.State.FILLED){return;} // we want a FILLED order to trigger OCO
                    
        boolean orderIsLong = order.isLong();
        
        for (IOrder otherOrder : instrumentOrders) {           
            if(otherOrder.isLong() != orderIsLong){
                try {
                    otherOrder.close();
                    triggeredOCO = true;
                } catch (JFException ex) {
                    Logger.getLogger(MyInstrumentManagerBasic.class.getName()).log(Level.SEVERE, null, ex);
                    console.getOut().println("EXEPTION - orderCancelOrder");
                }
            }
        }                    
    } // end orderCancelOrder
       
    
    // generates the label for the orders of this strategy
    private String getLabel(Instrument instrument) {
                String label = "NSS_" + instrument.name() + "_" + (orderCounter ++);
                return label;
    } // end getLabel  
    
    
    // get actual tick pendingPrice in the direction of order
    private double getFrontPrice(MyOrderDirection orderDirection, ITick tick){
        double frontPrice = 0;
        switch(orderDirection){  
            case LONG: 
                frontPrice = tick.getAsk();
                break;
            case SHORT:  
                frontPrice = tick.getBid();
                break;
        }        
        return frontPrice;
    }    
    
    
    // get actual tick pendingPrice opposite side of the direction of order
    private double getBackPrice(MyOrderDirection orderDirection, ITick tick){
        double backPrice = 0;
        switch(orderDirection){  
            case LONG: 
                backPrice = tick.getBid();
                break;
            case SHORT:  
                backPrice = tick.getAsk();
                break;
        }        
        return backPrice;
    }
    
    
    // get the +1, -1 multiplicator for the order sides to use in calculations
    private int getShortLongMirror(MyOrderDirection orderDirection){
        int mirror = 0;
        switch(orderDirection){  
            case LONG: 
                mirror = 1;
                break;
            case SHORT:  
                mirror = -1;
                break;
        }        
        return mirror;
    }
    
    
    // get the +1, -1 multiplicator for the order sides to use in calculations
    private IEngine.OrderCommand getOrderCommand(MyOrderDirection orderDirection){
        IEngine.OrderCommand orderCommand = null;
        switch(orderDirection){  
            case LONG: 
                orderCommand = IEngine.OrderCommand.BUYSTOP;
                break;
            case SHORT:  
                orderCommand = IEngine.OrderCommand.SELLSTOP;
                break;
        }        
        return orderCommand;
    }
    
    
    //we need such function since floating point values are not exact
    private boolean doubleEquals(double d1, double d2){
        return Math.abs(d1-d2) < 0.000001;
    } 
    
    
    private double roundToPippette(double amount) {
        return round(amount, managedInstrument.getPipScale() + 1);
    }

    
    private double round(double amount, int decimalPlaces) {
        return (new BigDecimal(amount)).setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
    
      
} // END class MyInstrumentManagerBasic
