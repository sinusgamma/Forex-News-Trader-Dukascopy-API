/*
* The launcher of the MyTestManagerMultiProcess classes jar with parameters for scenario periods
*
*/
package com.madar.tester;

import com.madar.dataio.MyCSVManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TesterMainMultiProcessLoop {

    public static TesterMainMultiProcessLoop tester;
    private MyTestSetUp testSetUp;
    private int maxRunPerProcess;
    private int testSize;
    private int processSize;
    private int maxScenarioPerProcess;
    private List<MyTestSetUp> testSetUpList;
    private List<MyTestSetUp.RunParameter> parameterList;
    private LinkedHashMap<ZonedDateTime, ZonedDateTime> dateEntries;
    private int actualProcessNb;
    private int actualTestSetupNb = 0;
    private static String testParameterPath = "F:\\FOREX\\nsresource\\testparameters\\testparameters.csv";


    public static void main(String[] args) throws Exception {               
          tester = new TesterMainMultiProcessLoop();
          tester.runTest();
    }


    // the function to run the tester logic
    public void runTest(){
        // get the test setup list
        testSetUpList = MyCSVManager.readMultiTestSetupParameters(testParameterPath);
        // check the time for the tests
        for (MyTestSetUp myTestSetUp : testSetUpList) {
            this.testSetUp = myTestSetUp;       
            initializeEnviroment();
            checkTestSize();
        }
        
        enableTest();
        
        // do the tests
        for (MyTestSetUp myTestSetUp : testSetUpList) {
            this.testSetUp = myTestSetUp;
            
            initializeEnviroment();
            checkTestSize();
            loopProcesses();
            actualTestSetupNb ++; // marks the testsetup number in testparameter.csv - the MyTestManagerMultiProcess has to find the same setup
        }
    }

    private void initializeEnviroment(){  
        parameterList = testSetUp.getParameterList(); // get the scenario parameters
        dateEntries = testSetUp.getEventPeriods(); // get the iterator for the event periods
    }


    private void checkTestSize() {
        System.out.println("*****************TESTPROPERTIES***************************************************");
        System.out.println("NAME: " + testSetUp.getEventName() + " | Currency: " + testSetUp.getCurrency() + " on instrument: " + testSetUp.getTestInstrument());      
        System.out.println("Source of event time csv: " + testSetUp.getPathEventCalendarCSV());
        
        testSize = (parameterList.size() * dateEntries.size());
        maxScenarioPerProcess = testSetUp.getEnabledRunPerProcess() / dateEntries.size(); // this automatically rounds down to the integer
        maxRunPerProcess = maxScenarioPerProcess * dateEntries.size();
        processSize = (int) Math.ceil((double)testSize / (double)maxRunPerProcess);

        System.out.println("enabledRunPerProcess: " + testSetUp.getEnabledRunPerProcess());
        System.out.println("testSize = SCENARIOS:" + parameterList.size() + " * EVENTS:" + dateEntries.size() + " = " + testSize);
        System.out.println("estimated RUNTIME: " + testSize * 0.2 + " sec / " + testSize  * 0.003333 + " min");
        System.out.println("maxScenarioPerProcess: " + maxScenarioPerProcess);
        System.out.println("maxRunPerProcess: " + maxRunPerProcess);
        System.out.println("processSize: " + processSize);
    }


    // loops trough the data (parameter scenarios and date entries with multiple processes to "avoid" memory leak)
    private void loopProcesses() {
        for(int processID = 0; processID < processSize; processID++){
            int firstScenario = processID * maxScenarioPerProcess;
            int lastScenario = ((processID + 1) * maxScenarioPerProcess) - 1;
            if (lastScenario >= parameterList.size()) {
                lastScenario = parameterList.size() - 1; // the last loop most of the time doesn't run the maxScenarioPerProcess
            }
            launchProcess("" + firstScenario, "" + lastScenario);
            try {
                System.out.println("Sleep 10 sec before reconnect to server");
                Thread.sleep(10 * 1000); // to avoid too early reconnect to server
            } catch (InterruptedException ex) {
                Logger.getLogger(TesterMainMultiProcessLoop.class.getName()).log(Level.SEVERE, null, ex);
            }
        } // end for processes
    } // end loopProcesses()


    // launch a process from jar with two arguments
    private void launchProcess(String firstScenario, String lastScenario){
        System.out.println("*************ProcessBuilder**********");
        actualProcessNb ++;
        try {
            ProcessBuilder pb = new
                    // needs new folder for all version project
                    ProcessBuilder("java", "-jar", "F:\\JavaProjects\\NetBeansProjects\\JForex-3-SDK-330\\target\\MultiProcessManager-jar-with-dependencies.jar", firstScenario, lastScenario, String.valueOf(actualProcessNb), String.valueOf(actualTestSetupNb), testParameterPath);
            pb.redirectErrorStream(true);
            final Process p=pb.start();
            BufferedReader br=new BufferedReader(
                    new InputStreamReader(
                            p.getInputStream()));
            String line;
            while((line=br.readLine())!=null){
                System.out.println(line);
            }
            System.out.println("process ended with: " + p.waitFor());  // wait for the end of process before going forward
        } catch (IOException | InterruptedException ex) {
            System.out.println(ex);
        }
        System.out.println("************************************");
    }
    
    
    // enable test start, or not
    private void enableTest(){
        Scanner reader = new Scanner(System.in);  // Reading from System.in
        System.out.println("Continue test? (0 no, 1 yes )");
        int n = reader.nextInt(); // Scans the next token of the input as an int.

        if(n == 0){
            System.exit(0);
        }
        //once finished
        reader.close();        
    }

} // END CLASS TesterMainMultiProcess
