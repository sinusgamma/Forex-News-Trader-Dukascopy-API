package com.madar.dataio;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.system.ITesterClient;
import com.madar.library.newsstraddle.MyCurrency;
import com.madar.library.newsstraddle.MyEventType;
import com.madar.library.newsstraddle.MyOrderDirection;
import com.madar.library.newsstraddle.MyOrderScenario;
import com.madar.tester.MyReportData;
import com.madar.tester.MyTestSetUp;
import java.io.BufferedWriter;
import java.io.File;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVPrinter;

public class MyCSVManager {

    public static void main(String[] args) throws IOException {

    }
    
    
    //reads the parameters from csv for multiple test loops ans sets the list of myTestSetups - ugly solution, but this is an organic, growing software
    public static List<MyTestSetUp> readMultiTestSetupParameters(String filePath){  
        
        List<MyTestSetUp> collection = new ArrayList<>();
        MyTestSetUp testSetup = null;
        
        List<Double> pointsAwayList = null;
        List<Double> takeProfitList = null;
        List<Double> stopLossList = null;
        List<Long> secondsAfterNewsList = null;
        List<Double> breakevenTriggerList = null;
        List<Double> breakevenDistanceList = null;            
        List<Double> trailingStopList = null;  
        List<Double> trailingAfterList = null;      
        List<Boolean> trailImmediatelyList = null;
        List<Double> maxSpreadList = null;
        List<Double> maxSlippageList = null;
        List<MyOrderDirection> enabledDirectionList = null;

        try (
            Reader reader = Files.newBufferedReader(Paths.get(filePath));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                    .withTrim());
        ) {
            for (CSVRecord csvRecord : csvParser.getRecords()) {
                if(csvRecord.get(0).equals("eventname")){
                    // build new testsetup for every new name in the list
                    testSetup = new MyTestSetUp(csvRecord.get(1), MyCurrency.valueOf(csvRecord.get(2)), Instrument.valueOf(csvRecord.get(3)));
                    //reset the arrays for this type of event
                    pointsAwayList = new ArrayList<>();
                    takeProfitList = new ArrayList<>();
                    stopLossList = new ArrayList<>();
                    secondsAfterNewsList = new ArrayList<>();
                    breakevenTriggerList = new ArrayList<>();
                    breakevenDistanceList = new ArrayList<>();
                    trailingStopList = new ArrayList<>();
                    trailingAfterList = new ArrayList<>();
                    trailImmediatelyList = new ArrayList<>();
                    maxSpreadList = new ArrayList<>();
                    maxSlippageList = new ArrayList<>();
                    enabledDirectionList = new ArrayList<>();
                    
                } else if (csvRecord.get(0).equals("param")){
                    switch (csvRecord.get(1)) {
                        case "pointsAwayAr":
                            for (int i = 2; i < csvRecord.size(); i++) {
                                if(!csvRecord.get(i).equals("")){
                                    pointsAwayList.add(Double.parseDouble(csvRecord.get(i)));
                                }                      
                            }
                            break;
                        case "takeProfitAr":
                            for (int i = 2; i < csvRecord.size(); i++) {
                                if(!csvRecord.get(i).equals("")){
                                    takeProfitList.add(Double.parseDouble(csvRecord.get(i)));
                                }                      
                            }
                            break;                            
                        case "stopLossAr":  
                            for (int i = 2; i < csvRecord.size(); i++) {
                                if(!csvRecord.get(i).equals("")){
                                    stopLossList.add(Double.parseDouble(csvRecord.get(i)));
                                }                      
                            }
                            break; 
                        case "secondsAfterNewsAr": 
                            for (int i = 2; i < csvRecord.size(); i++) {
                                if(!csvRecord.get(i).equals("")){
                                    secondsAfterNewsList.add(Long.parseLong(csvRecord.get(i)));
                                }                      
                            }
                            break;  
                        case "breakevenTriggerAr":  
                            for (int i = 2; i < csvRecord.size(); i++) {
                                if(!csvRecord.get(i).equals("")){
                                    breakevenTriggerList.add(Double.parseDouble(csvRecord.get(i)));
                                }                      
                            }
                            break;
                        case "breakevenDistanceAr":  
                            for (int i = 2; i < csvRecord.size(); i++) {
                                if(!csvRecord.get(i).equals("")){
                                    breakevenDistanceList.add(Double.parseDouble(csvRecord.get(i)));
                                }                      
                            }
                            break;
                        case "trailingStopAr":  
                            for (int i = 2; i < csvRecord.size(); i++) {
                                if(!csvRecord.get(i).equals("")){
                                    trailingStopList.add(Double.parseDouble(csvRecord.get(i)));
                                }                      
                            }
                            break;
                        case "trailingAfterAr":  
                            for (int i = 2; i < csvRecord.size(); i++) {
                                if(!csvRecord.get(i).equals("")){
                                    trailingAfterList.add(Double.parseDouble(csvRecord.get(i)));
                                }                      
                            }
                            break; 
                        case "trailImmediatelyAr":  
                            for (int i = 2; i < csvRecord.size(); i++) {
                                if(!csvRecord.get(i).equals("")){
                                    trailImmediatelyList.add(Boolean.parseBoolean(csvRecord.get(i)));
                                }                      
                            }
                            break; 
                        case "maxSpreadAr":  
                            for (int i = 2; i < csvRecord.size(); i++) {
                                if(!csvRecord.get(i).equals("")){
                                    maxSpreadList.add(Double.parseDouble(csvRecord.get(i)));
                                }                      
                            }
                            break; 
                        case "maxSlippageAr":  
                            for (int i = 2; i < csvRecord.size(); i++) {
                                if(!csvRecord.get(i).equals("")){
                                    maxSlippageList.add(Double.parseDouble(csvRecord.get(i)));
                                }                      
                            }
                            break; 
                        case "enabledDirectionAr":  
                            for (int i = 2; i < csvRecord.size(); i++) {
                                if(!csvRecord.get(i).equals("")){
                                    enabledDirectionList.add(MyOrderDirection.valueOf(csvRecord.get(i)));
                                }                      
                            }
                            break;    
                    }                 
                } else if (csvRecord.get(0).equals("end")){
                    testSetup.buildRunParameterList(
                            pointsAwayList,
                            takeProfitList,
                            stopLossList, 
                            secondsAfterNewsList, 
                            breakevenTriggerList, 
                            breakevenDistanceList, 
                            trailingStopList, 
                            trailingAfterList, 
                            trailImmediatelyList, 
                            maxSpreadList, 
                            maxSlippageList, 
                            enabledDirectionList);
                    collection.add(testSetup);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MyCSVManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return collection;
    }
            
    
    // read calendar data to list of MySingleEventHolder
    public static List<MySingleEventHolder> readForexCalendarCsv(String filePath){
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<MySingleEventHolder> collection = new ArrayList<>();        
        
        try (
            Reader reader = Files.newBufferedReader(Paths.get(filePath));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                    .withHeader("datetime", "currency", "impact", "newsname", "actual", "forecast", "previous", "modfrom", "ffId")
                    .withIgnoreHeaderCase()
                    .withTrim());
        ) {
            long counter = 1;
            for (CSVRecord csvRecord : csvParser.getRecords()) {
                // Accessing values by the names assigned to each column                
                String strDateTime = csvRecord.get("datetime").replace("\uFEFF", "");
                LocalDateTime ldt = LocalDateTime.parse(strDateTime, formatter);
                ZonedDateTime zdt = ldt.atZone(ZoneId.of("UTC"));                      
                String newsName = csvRecord.get("newsname");
                MyCurrency myCurrency = MyCurrency.valueOf(csvRecord.get("currency"));
                
                MySingleEventHolder singleEventHolder = new MySingleEventHolder(zdt, newsName, myCurrency);
                collection.add(singleEventHolder);                
            }
        } catch (IOException ex) {
            Logger.getLogger(MyCSVManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return collection;
    }
    
    
    // read the trading scenario settings from csv, very slow because of nested loops, but runs only at initialization
    // the function assumes that the scenarios of the same event come in succession rows in csv
    // during the function we test for multisettings hashmap only, because they are initialized with the singlesetting in the same time
    public static List<MyEventType> readMultiSettingsCsv(String filePath){
        
        List<MyEventType> collection = new ArrayList<>();        
        try (
            // get csvParser
            Reader reader = Files.newBufferedReader(Paths.get(filePath));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                    .withHeader("scenarioId",
                                "eventName",
                                "currencyOfEvent",
                                "instrument",
                                "enabledOrderDirection",
                                "orderDirection",
                                "secondsBeforePending",
                                "secondsBeforeModify",
                                "secondsAfterNews",
                                "isOCO",
                                "manageMoney",
                                "calcAmountWithMaxSpread",
                                "weight",
                                "pointsAway",
                                "takeProfit",
                                "stopLoss",
                                "modifyGap",
                                "secondsAfterNewsOffset",
                                "breakevenTrigger",
                                "breakevenDistance",
                                "trailingStop",
                                "trailingAfter",
                                "trailImmediately",        
                                "riskPercent",
                                "amount",
                                "maxSpread",	
                                "maxSlippage"
                    )
                    .withIgnoreHeaderCase()
                    .withTrim());
        ) {
            // loop csvParser
            boolean isFirstRow = true;
            MyEventType lastRowEventType = null; // here the eventtype of last row is preserved to be able to compare rows
            MyEventType actualRowEventType = null;
            
            for (CSVRecord csvRecord : csvParser.getRecords()) {
                // skipping header                
                if(isFirstRow == true){
                    isFirstRow = false;
                    continue;
                }
                
                // Accessing values of the COMMON scenario settings only once per event
                if(lastRowEventType == null || !lastRowEventType.getEventName().equals(csvRecord.get("eventName"))){   // if subsequent rows belongs to the different event then create new event - because of shoertcircuit || wont be nullpointerexpection             

                    actualRowEventType = new MyEventType();
                    actualRowEventType.setSimpleDirectionalSettings(new HashMap<Instrument, HashMap<MyOrderDirection, MyOrderScenario>>());
                    actualRowEventType.setMultiDirectionalSettings(new HashMap<Instrument, HashMap<MyOrderDirection, List<MyOrderScenario>>>());
                    
                    actualRowEventType.setEventName(csvRecord.get("eventName"));
                    actualRowEventType.setCurrency(MyCurrency.valueOf(csvRecord.get("currencyOfEvent")));
                    actualRowEventType.setEnabledOrderDirection(MyOrderDirection.valueOf(csvRecord.get("enabledOrderDirection")));
                    actualRowEventType.setSecondsBeforePending(Long.parseLong(csvRecord.get("secondsBeforePending")));
                    actualRowEventType.setSecondsBeforeModify(Long.parseLong(csvRecord.get("secondsBeforeModify")));
                    actualRowEventType.setSecondsAfterNews(Long.parseLong(csvRecord.get("secondsAfterNews")));
                    actualRowEventType.setIsOCO(Boolean.parseBoolean(csvRecord.get("isOCO")));
                    collection.add(actualRowEventType);
                } else {
                    actualRowEventType = lastRowEventType; // if the name is the same as last row, this is the same eventtype, so we can continue to upgrade it
                }

                // Accessing values of the INDIVIDUAL scenario settings at every row
                MyOrderScenario actualOrderScenario = new MyOrderScenario();
                actualOrderScenario.setScenarioId(Long.parseLong(csvRecord.get("scenarioId")));
                actualOrderScenario.setWeight(Double.parseDouble(csvRecord.get("weight")));
                actualOrderScenario.setPointsAway(Double.parseDouble(csvRecord.get("pointsAway")));
                actualOrderScenario.setTakeProfit(Double.parseDouble(csvRecord.get("takeProfit")));
                actualOrderScenario.setStopLoss(Double.parseDouble(csvRecord.get("stopLoss")));
                actualOrderScenario.setModifyGap(Double.parseDouble(csvRecord.get("modifyGap")));
                actualOrderScenario.setSecondsAfterNewsOffset(Long.parseLong(csvRecord.get("secondsAfterNewsOffset")));
                actualOrderScenario.setBreakevenTrigger(Double.parseDouble(csvRecord.get("breakevenTrigger")));
                actualOrderScenario.setBreakevenDistance(Double.parseDouble(csvRecord.get("breakevenDistance")));
                actualOrderScenario.setTrailingStop(Double.parseDouble(csvRecord.get("trailingStop")));
                actualOrderScenario.setTrailingAfter(Double.parseDouble(csvRecord.get("trailingAfter")));
                actualOrderScenario.setTrailImmediately(Boolean.parseBoolean(csvRecord.get("trailImmediately")));
                actualOrderScenario.setRiskPercent(Double.parseDouble(csvRecord.get("riskPercent")));
                actualOrderScenario.setAmount(Double.parseDouble(csvRecord.get("amount")));
                actualOrderScenario.setMaxSpread(Double.parseDouble(csvRecord.get("maxSpread")));
                actualOrderScenario.setMaxSlippage(Double.parseDouble(csvRecord.get("maxSlippage")));
                
                // getting the Instrument and OrderDirection
                Instrument actualInstrument = Instrument.valueOf(csvRecord.get("instrument"));
                MyOrderDirection actualOrderDirection = MyOrderDirection.valueOf(csvRecord.get("orderDirection"));
                
                // if there is a new Instrument (and because of that new OrderDirection) make the containers for them
                if(actualRowEventType.getMultiDirectionalSettings().containsKey(actualInstrument) == false){                    
                    
                    HashMap<MyOrderDirection, MyOrderScenario> singleDirectionContainer = new HashMap<>(); // the single setting map
                    HashMap<MyOrderDirection, List<MyOrderScenario>> multiDirectionContainer = new HashMap<>(); // the single setting map
                    
                    List<MyOrderScenario> scenarioList = new ArrayList<>();
                    scenarioList.add(actualOrderScenario);
                    
                    // here we build the directional containers
                    singleDirectionContainer.put(actualOrderDirection, actualOrderScenario);
                    multiDirectionContainer.put(actualOrderDirection, scenarioList); 
                    
                    // here we build the instrumental containers
                    actualRowEventType.getSimpleDirectionalSettings().put(actualInstrument, singleDirectionContainer);
                    actualRowEventType.getMultiDirectionalSettings().put(actualInstrument, multiDirectionContainer);
                                 
                // here Instrument exists, check if direction exists - if no: make conatiners
                } else if (actualRowEventType.getMultiDirectionalSettings().get(actualInstrument).containsKey(actualOrderDirection) == false){
                    
                    HashMap<MyOrderDirection, MyOrderScenario> singleDirectionContainer = new HashMap<>(); // the single setting map
                    HashMap<MyOrderDirection, List<MyOrderScenario>> multiDirectionContainer = new HashMap<>(); // the single setting map
                    
                    List<MyOrderScenario> scenarioList = new ArrayList<>();
                    scenarioList.add(actualOrderScenario);
                    
                    // here we build the directional containers
                    singleDirectionContainer.put(actualOrderDirection, actualOrderScenario);
                    multiDirectionContainer.put(actualOrderDirection, scenarioList);
                    
                    // here we build the instrumental containers
                    actualRowEventType.getSimpleDirectionalSettings().get(actualInstrument).put(actualOrderDirection, actualOrderScenario);
                    actualRowEventType.getMultiDirectionalSettings().get(actualInstrument).put(actualOrderDirection, scenarioList);
                    
                // if there is no new instrument or direction we update only the multi settings, because single settings are from the first row per direction
                } else {
                    actualRowEventType.getMultiDirectionalSettings().get(actualInstrument).get(actualOrderDirection).add(actualOrderScenario);
                }
                
                // Remember the earlier row to compare names to know if new eventtype comes
                lastRowEventType = actualRowEventType;
            }
        } catch (IOException ex) {
            Logger.getLogger(MyCSVManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return collection;
    }
    
    
    // writes the list of MySingleEventHolder to a csv
    public static void writeEventScheduleToCSV(List<MySingleEventHolder> collection, String goalFilePath) throws IOException {
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        try (              
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(goalFilePath));
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)
        ) {                 
        // write the event times
        List<String> rowValues = new ArrayList<>();
        for (MySingleEventHolder singleEventHolder : collection) {     
            rowValues = new ArrayList<>();    
            
            rowValues.add(singleEventHolder.getEventTime().format(formatter) + "");
            rowValues.add(singleEventHolder.getCurrency() + "");
            rowValues.add("");
            rowValues.add(singleEventHolder.getEventName() + "");
                        
            csvPrinter.printRecord(rowValues);
            }
        csvPrinter.flush();
        csvPrinter.close();
        }
        System.out.println("File is ready: " + goalFilePath);
    }
    
    
    // write test report to csv
    public static void writeTestReportToCSV(List<MyReportData> summaReportList, MyTestSetUp testSetUp) throws IOException{
        String eventName = testSetUp.getEventName();
        MyCurrency currency = testSetUp.getCurrency();
        Instrument testInstrument = testSetUp.getTestInstrument();
        int eventPeriodsNbr = testSetUp.getEventPeriods().size();
        
        String instrumentString = testInstrument.toString();
        instrumentString = instrumentString.replace("/", "");
        eventName = eventName.replace(" ", "_");
        eventName = eventName.replace("\\", "");
        eventName = eventName.replace("/", "");
        String goalFilePath = "F:\\FOREX\\nsresource\\news-report\\report_" + currency + "_" + eventName + "_" + instrumentString + ".csv";
        System.out.println("output File: " + goalFilePath);
        
        try (              
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(goalFilePath), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)
        ) {
            
        // write header names
        List<String> headerValues = new ArrayList<>();
        for (String[] parameterValue : summaReportList.get(0).getParameterValues()) { 
            headerValues.add(parameterValue[0].toString()); // 0 parameter name - 1 value
        }
            headerValues.add("initDep");
            headerValues.add("finishDep");
            headerValues.add("commission");
            headerValues.add("turnOver");
            headerValues.add("eventPeriodsNbr");
            headerValues.add("closedOrderNb");
            headerValues.add("buyOrders");
            headerValues.add("sellOrders");
            headerValues.add("maxProfit");
            headerValues.add("maxLoss");
            headerValues.add("profitNb");
            headerValues.add("lossNb");            
            headerValues.add("profitNbWithComm");
            headerValues.add("lossNbWithComm"); 
            headerValues.add("orderPercent");
            headerValues.add("profitPercent");
            headerValues.add("profitPercentWithComm");
            headerValues.add("avrPLperevent");
            headerValues.add("avrPLclosedorder");  
            headerValues.add("sumProfit");
            headerValues.add("sumLoss");
            headerValues.add("sumProfitCom");
            headerValues.add("sumLossCom");
            headerValues.add("PLrate");
            headerValues.add("PLrateCom");            
        csvPrinter.printRecord(headerValues);
        
        // write the scenario data
        List<String> rowValues = new ArrayList<>();
        for (MyReportData scenarioData : summaReportList) {     
            rowValues = new ArrayList<>();       
            
            for (String[] parameterValue : scenarioData.getParameterValues()) { 
                rowValues.add(parameterValue[1].toString()); // 0 parameter name - 1 value
            }    
            rowValues.add(scenarioData.getInitialDeposit() + "");
            rowValues.add(scenarioData.getFinishDeposit() + "");
            rowValues.add(scenarioData.getCommission() + "");
            rowValues.add(scenarioData.getTurnover() + "");
            
            int closedOrderNb = scenarioData.getClosedOrders().size();
            List<IOrder> closedOrderList = scenarioData.getClosedOrders();          
            int buyOrders = 0;
            int sellOrders = 0;
            int profitNb = 0;
            int lossNb = 0;
            double maxProfit = 0;
            double maxLoss = 0;
            int profitNbWithComm = 0;
            int lossNbWithComm = 0;
            double sumProfit = 0;
            double sumLoss = 0;
            double sumProfitCom = 0;
            double sumLossCom = 0;
            
            for (IOrder order : closedOrderList) {
                if(order.isLong()) {
                    buyOrders += 1;
                } else {
                    sellOrders += 1;
                }
                
                if(order.getProfitLossInAccountCurrency() > 0) {
                    sumProfit += order.getProfitLossInAccountCurrency();
                    profitNb += 1;
                } else if(order.getProfitLossInAccountCurrency() < 0) {
                    sumLoss += order.getProfitLossInAccountCurrency();
                    lossNb += 1;
                }
                
                if(order.getProfitLossInAccountCurrency() - order.getCommission() > 0) {
                    sumProfitCom += order.getProfitLossInAccountCurrency() - order.getCommission();
                    profitNbWithComm += 1;
                } else if(order.getProfitLossInAccountCurrency() - order.getCommission() < 0) {
                    sumLossCom += order.getProfitLossInAccountCurrency() - order.getCommission();
                    lossNbWithComm += 1;
                }
                
                if(order.getProfitLossInAccountCurrency() > maxProfit) {maxProfit = order.getProfitLossInAccountCurrency();}
                if(order.getProfitLossInAccountCurrency() < maxLoss) {maxLoss = order.getProfitLossInAccountCurrency();}
            }  // for closedOrderList  
            
            rowValues.add(eventPeriodsNbr + "");
            rowValues.add(closedOrderNb + "");
            rowValues.add(buyOrders + "");
            rowValues.add(sellOrders + "");
            rowValues.add(maxProfit + "");
            rowValues.add(maxLoss + "");
            rowValues.add(profitNb + "");
            rowValues.add(lossNb + "");            
            rowValues.add(profitNbWithComm + "");
            rowValues.add(lossNbWithComm + "");
            
            double orderPercent = 0.0;
            if(eventPeriodsNbr != 0) {orderPercent = (double)closedOrderNb / (double)eventPeriodsNbr;}
            
            double profitPercent = 0.0;
            if(closedOrderNb != 0) {profitPercent = (double)profitNb / (double)closedOrderNb;}
            
            double profitPercentWithComm = 0.0;
            if(closedOrderNb != 0) {profitPercentWithComm = (double)profitNbWithComm / (double)closedOrderNb;}  
            
            double avrPLperevent = 0.0;
            if(closedOrderNb != 0) {avrPLperevent = (scenarioData.getFinishDeposit() - scenarioData.getInitialDeposit()) / (double)eventPeriodsNbr;}            
            
            double avrPLclosedorder = 0.0;
            if(closedOrderNb != 0) {avrPLclosedorder = (scenarioData.getFinishDeposit() - scenarioData.getInitialDeposit()) / (double)closedOrderNb;}
            
            double PLrate = 0.0;
            if(sumLoss != 0) {
                PLrate = Math.abs(sumProfit / sumLoss);
            } else {
                PLrate = 999.0;
            }
            
            double PLrateCom = 0.0;
            if(sumLoss != 0) {
                PLrateCom = Math.abs(sumProfitCom / sumLossCom);
            } else {
                PLrateCom = 999.0;
            }            
            
            rowValues.add(orderPercent + "");
            rowValues.add(profitPercent + "");
            rowValues.add(profitPercentWithComm + "");
            rowValues.add(avrPLperevent + ""); 
            rowValues.add(avrPLclosedorder + ""); 
            rowValues.add(sumProfit + "");
            rowValues.add(sumLoss + "");
            rowValues.add(sumProfitCom + "");
            rowValues.add(sumLossCom + "");
            rowValues.add(PLrate + "");
            rowValues.add(PLrateCom + "");
            
            csvPrinter.printRecord(rowValues);
            }
        csvPrinter.flush();
        csvPrinter.close();
        }
    }
    
    
    // saves to file every single period strategy run
    public static void makeSingleReportFile(long processId, ITesterClient client){
        File reportFile = new File("F:\\FOREX\\nsresource\\single-reports\\report" + processId + ".html");
        try {
            client.createReport(processId, reportFile);
        } catch (IOException | IllegalStateException ex) {
            Logger.getLogger(MyReportData.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }    
   
} // END CLASS MyCSVManager
    
