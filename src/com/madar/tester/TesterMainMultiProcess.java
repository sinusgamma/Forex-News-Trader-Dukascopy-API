/*
* The launcher of the MyTestManagerMultiProcess classes jar with parameters for scenario periods
*
*/
package com.madar.tester;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TesterMainMultiProcess {

    public static TesterMainMultiProcess tester;
    private MyTestSetUp testSetUp;
    private int maxRunPerProcess;
    private int testSize;
    private int processSize;
    private int maxScenarioPerProcess;
    private List<MyTestSetUp.RunParameter> parameterList;
    private LinkedHashMap<ZonedDateTime, ZonedDateTime> dateEntries;
    private int actualProcessNb;


    public static void main(String[] args) throws Exception {
          tester = new TesterMainMultiProcess();
          tester.runTest();
    }


    // the function to run the tester logic
    public void runTest(){
        initializeEnviroment();
        checkTestSize();
        loopProcesses();
    }


    private void initializeEnviroment(){
        this.testSetUp = new MyTestSetUp(); // initialize setup    
        parameterList = testSetUp.buildRunParameterList(); // get the scenario parameters
        dateEntries = testSetUp.getEventPeriods(); // get the iterator for the event periods
    }


    private void checkTestSize() {
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
 
        
        Scanner reader = new Scanner(System.in);  // Reading from System.in
        System.out.println("Continue test? (0 no, 1 yes )");
        int n = reader.nextInt(); // Scans the next token of the input as an int.

        if(n == 0){
            System.exit(0);
        }
        //once finished
        reader.close();
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
                Logger.getLogger(TesterMainMultiProcess.class.getName()).log(Level.SEVERE, null, ex);
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
                    ProcessBuilder("java", "-jar", "F:\\JavaProjects\\NetBeansProjects\\JForex-3-SDK-330\\target\\MultiProcessManager-jar-with-dependencies.jar", firstScenario, lastScenario, String.valueOf(actualProcessNb));
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

} // END CLASS TesterMainMultiProcess
