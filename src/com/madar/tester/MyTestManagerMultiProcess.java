/*
 * tests the scenarios from a start to an and ID
 *
 */
package com.madar.tester;


import com.dukascopy.api.IStrategy;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.system.ISystemListener;
import com.dukascopy.api.system.ITesterClient;
import com.dukascopy.api.system.JFVersionException;
import com.madar.dataio.MyCSVManager;
import com.madar.strategies.NewsStradleStrategy;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MyTestManagerMultiProcess {

    //url of the DEMO jnlp
    private static final String JNLPURL = "http://platform.dukascopy.com/demo/jforex.jnlp";
    //user name 2 weeks
    private static final String USERNAME = MyPassword.values()[0].name();
    //password 2 weeks
    private static final String PASSWORD = MyPassword.values()[1].name();

    private static int firstScenario;
    private static int lastScenario;
    private static int processNb;
    private static int setUpNb;
    private static boolean isMultiEventTest = false; // if true we need to find the proper event for this process from testparameters.csv
    private static String testParameterPath;
    int scenariosInLoop;
    private ITesterClient client;
    private MyTestSetUp testSetUp;
    private List<MyTestSetUp.RunParameter> parameterList;
    private Iterator<Map.Entry<ZonedDateTime, ZonedDateTime>> dateEntriesIterator;
    private List<MyReportData> summaReportList;
    private int eventCounter;
    private Map<Long, Integer> scenarioSyncronizer;

    public MyTestManagerMultiProcess() {
    }

    public static void main(String[] args) throws Exception {
        switch (args.length) {
            // whit 3 args this is called from TesterMainMultiProcess - only one event type
            case 3:
                firstScenario = Integer.parseInt(args[0]);
                lastScenario = Integer.parseInt(args[1]);
                processNb = Integer.parseInt(args[2]);
                System.out.println("ARGS FROM INPUT: ");
                System.out.println("firstScenario: " + firstScenario);
                System.out.println("lastScenario: " + lastScenario);
                System.out.println("processNb: " + processNb);
                isMultiEventTest = false;
                break;
            // whit 5 args this is called from TesterMainMultiProcessLoop - many event type    
            case 5:
                firstScenario = Integer.parseInt(args[0]);
                lastScenario = Integer.parseInt(args[1]);
                processNb = Integer.parseInt(args[2]);
                setUpNb = Integer.parseInt(args[3]);
                testParameterPath = args[4];
                System.out.println("ARGS FROM INPUT: ");
                System.out.println("firstScenario: " + firstScenario);
                System.out.println("lastScenario: " + lastScenario);
                System.out.println("processNb: " + processNb);
                System.out.println("setUpNb: " + setUpNb);
                System.out.println("testparameters: " + testParameterPath);
                isMultiEventTest = true;
                break;
            default:
                System.out.println("NO ARGS! DEFAULT: firstScenario = 0, lastScenario = 1");
                firstScenario = 0;
                lastScenario = 1;
                isMultiEventTest = false;
                break;
        }
        MyTestManagerMultiProcess testManager = new MyTestManagerMultiProcess();
        testManager.runTest();
    }


    // the function to run the tester logic
    public void runTest(){
        initializeEnviroment();
        if(isMultiEventTest == false){
            initializeInManagerSetUp();
        } else {
            initializeCsvSetUp();
        }
        initializeClient();
        nextEventIteration();
    }


    // called when a strategy stopped
    private void onStrategyStopped(long processId){
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logger.getLogger(MyTestManagerMultiProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("standaloneStr stopped: " + processId);
        System.out.println("standaloneStr initial Dep: " + client.getReportData(processId).getInitialDeposit());
        System.out.println("standaloneStr finished Dep: " + client.getReportData(processId).getFinishDeposit());
        System.out.println("standaloneStr orders: " + client.getReportData(processId).getClosedOrders().size());

        MyCSVManager.makeSingleReportFile(processId, client);
        MyReportData.updateSummaReport(processId, client, scenarioSyncronizer, summaReportList);
            
        if(client.getStartedStrategies().isEmpty() && dateEntriesIterator.hasNext()){
            nextEventIteration();
        } else if (client.getStartedStrategies().isEmpty()) {
            finishTest();
        }
    }


    private void initializeEnviroment(){
        eventCounter = 0;
        scenarioSyncronizer = new HashMap<>();
        summaReportList = new ArrayList<>();
        scenariosInLoop = lastScenario - firstScenario + 1;
        System.out.println("scenariosInLoop: " + scenariosInLoop);
    }
    
    
    // the setup is from the manager class, used for single setup runs
    private void initializeInManagerSetUp(){
        this.testSetUp = new MyTestSetUp(); // initialize setup
        parameterList = testSetUp.buildRunParameterList(); // get the scenario parameters
        dateEntriesIterator = testSetUp.getEventPeriods().entrySet().iterator(); // get the iterator for the event periods
    }
    
    
    // the setup from csv, used for multi or single run from the csv
    private void initializeCsvSetUp(){
        this.testSetUp = MyCSVManager.readMultiTestSetupParameters(testParameterPath).get(setUpNb);
        parameterList = testSetUp.getParameterList();
        dateEntriesIterator = testSetUp.getEventPeriods().entrySet().iterator(); // get the iterator for the event periods
    }


    // iterate trough events
    private void nextEventIteration(){
        eventCounter += 1;
        Map.Entry<ZonedDateTime, ZonedDateTime> dateEntry = dateEntriesIterator.next();
        downloadAndSetPeriodData(dateEntry);
        setUpStrategy(); // start the scenarioz from zero ID first
    }


    // initialize client
    private void initializeClient(){
        if(client != null){
            client.disconnect();
            while (client.isConnected() == true) {
                System.out.println("DISCONNECTING");
            }
            client = null;
            System.gc();
        }
        // set client
        try {
            Class dcClientImpl = Thread.currentThread().getContextClassLoader().loadClass("com.dukascopy.api.impl.connect.TesterClientImpl");
            client = (ITesterClient) dcClientImpl.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(MyTestManagerMultiProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        client.setCacheDirectory(new File("F:\\FOREX\\JForex_cache"));

        // set listener
        setSystemEventListener(client);
        // connect to Dukascopy
        try {
            connectToDukascopy(client);
        } catch (Exception ex) {
            Logger.getLogger(MyTestManagerMultiProcess.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("EXCEPTION - CONNECTION PROBLEM - connectToDukascopy()");
        }
        // subscribe test instrument
        testSetUp.subscribeTestedInstrument(client);
    }


    // set up invidual strategy run for one period
    private void setUpStrategy() {
        for(int scenarioIdReal = firstScenario, scenarioId = 0; scenarioIdReal <= lastScenario; scenarioIdReal++, scenarioId++){
            
            System.out.println("in loop scenarioIdReal: " + scenarioIdReal);
            System.out.println("in loop scenarioId: " + scenarioId);
            
            if(summaReportList.size() < scenariosInLoop){ // this case the report list isn't initialized for every strategy, this is first event
                summaReportList.add(scenarioId, new MyReportData()); // to begin with 0 index
                client.setInitialDeposit(Instrument.EURUSD.getPrimaryJFCurrency(), testSetUp.getInitialDeposit()); // here comes the initial deposit
            } else { // summaReportList isn't empty, so this isn't the first run, get the last runs finishedDeposit
                client.setInitialDeposit(Instrument.EURUSD.getPrimaryJFCurrency(), summaReportList.get(scenarioId).getFinishDeposit()); 
            }

            // set up strategy
            IStrategy strategy = testSetUp.parameterizeStrategy(new NewsStradleStrategy(), scenarioIdReal); // here we need scenarioId real to get the proper scenario from parameterList
            //start the strategy
            long processId = client.startStrategy(strategy);

            scenarioSyncronizer.put(processId, scenarioId);  // keep track the which scenario the strategy belongs to
        } // end for
    } // end setUpStrategy


    //set the listener that will receive system events
    private void setSystemEventListener(ITesterClient client){
        client.setSystemListener(new ISystemListener() {
            @Override
            public void onStart(long processId) {
                System.out.println("Strategy started: " + processId + " period: " + eventCounter);
            }

            @Override
            public void onStop(long processId) {
                onStrategyStopped(processId);
            }

            @Override
            public void onConnect() {
                System.out.println("Connected");
            }

            @Override
            public void onDisconnect() {
            }
        });
    } // end setSystemEventListener


    // connect to Dukascopy server
    private void connectToDukascopy(ITesterClient client) throws JFVersionException, Exception{
        System.out.println("Connecting...");
        //connect to the server using jnlp, user name and password
        //connection is needed for data downloading
        client.connect(JNLPURL, USERNAME, PASSWORD);

        //wait for it to connect
        int i = 10; //wait max ten seconds
        while (i > 0 && !client.isConnected()) {
            Thread.sleep(1000);
            i--;
        }
        if (!client.isConnected()) {
            System.out.println("Failed to connect Dukascopy servers");
            System.exit(1);
        }
    } // end connectToDukascopy


    // downloads the data for the periods
    private void downloadAndSetPeriodData(Map.Entry<ZonedDateTime, ZonedDateTime> dateEntry){
        // set the period to download
        ZonedDateTime dateFrom = dateEntry.getKey();
        ZonedDateTime dateTo = dateEntry.getValue();

        client.setDataInterval(ITesterClient.DataLoadingMethod.ALL_TICKS, dateFrom.toInstant().toEpochMilli(), dateTo.toInstant().toEpochMilli());

        //load data
        System.out.println("Downloading data");
        Future<?> future = client.downloadData(null);

        try {
            //wait for downloading to complete           
            future.get();
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(MyTestManagerMultiProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

    }


    // finish the test
    public void finishTest() {
        try {
            MyCSVManager.writeTestReportToCSV(summaReportList, testSetUp);
        } catch (IOException ex) {
            Logger.getLogger(MyTestManagerMultiProcess.class.getName()).log(Level.SEVERE, null, ex);
        }


        for (MyReportData scenarioReportData : summaReportList) {
            System.out.println("\nFINAL STATISTICS: ");
            System.out.println("SCENARIO: " + scenarioReportData.getScenarioId());
            System.out.println("INITIAL DEPOSIT: " + scenarioReportData.getInitialDeposit());
            System.out.println("FINISHED DEPOSIT: " + scenarioReportData.getFinishDeposit());
            System.out.println("TURNOVER: " + scenarioReportData.getTurnover());
            System.out.println("COMMISSION: " + scenarioReportData.getCommission());
            System.out.println("SUBRUNS: " + scenarioReportData.getSubRuns());
        }
        System.exit(0);
    }

} // END CLASS MyTestManagerMultiProcess
