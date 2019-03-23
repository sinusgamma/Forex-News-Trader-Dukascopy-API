/*
 *  Containes the Report data on eventloop in the tester
 *  This is the summa of different subruns with the same setting
 */

package com.madar.tester;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.system.ITesterClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyReportData {

    private int scenarioId;
    private List<String[]> parameterValues;
    private double commission;
    private double turnover;
    private double initialDeposit;
    private double finishDeposit;        
    private List<IOrder> openOrders;
    private List<IOrder> closedOrders;
    private int subRuns;

    public MyReportData() {
        this.parameterValues = null;
        this.commission = 0;
        this.turnover = 0;
        this.initialDeposit = 0;
        this.finishDeposit = 0;
        this.openOrders = new ArrayList<>();
        this.closedOrders = new ArrayList<>();
        this.subRuns = 0;
    }

    public int getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(int scenarioNumber) {
        this.scenarioId = scenarioNumber;
    }

    public List<String[]> getParameterValues() {
        return parameterValues;
    }

    public void setParameterValues(List<String[]> parameterValues) {
        this.parameterValues = parameterValues;
    }

    public double getCommission() {
        return commission;
    }
    public void setCommission(double commission) {
        this.commission = commission;
    }

    public void addToCommission(double commission) {
        this.commission += commission;
    }        

    public double getTurnover() {
        return turnover;
    }
    public void setTurnover(double turnover) {
        this.turnover = turnover;
    }

    public void addToTurnover(double turnover) {
        this.turnover += turnover;
    }        

    public double getInitialDeposit() {
        return initialDeposit;
    }

    public void setInitialDeposit(double initialDeposit) {
        this.initialDeposit = initialDeposit;
    } 

    public double getFinishDeposit() {
        return finishDeposit;
    }
    public void setFinishDeposit(double finishDeposit) {
        this.finishDeposit = finishDeposit;
    }

    public List<IOrder> getOpenOrders() {
        return openOrders;
    }
    public void addToOpenOrders(IOrder openOrder) {
        this.openOrders.add(openOrder);
    }

    public List<IOrder> getClosedOrders() {
        return closedOrders;
    }
    public void addToClosedOrders(IOrder closedOrder) {
        this.closedOrders.add(closedOrder);
    }

    public int getSubRuns() {
        return subRuns;
    }

    public void addToSubRuns() {
        this.subRuns += 1;
    }

    
    // update summaReportData after every strategy run
    public static void updateSummaReport(long processId, ITesterClient client, Map<Long, Integer> scenarioSyncronizer, List<MyReportData> summaReportList){
        int scenarioId = scenarioSyncronizer.get(processId);
        MyReportData summaReportData = summaReportList.get(scenarioId);
        
        if(summaReportList.get(scenarioId).getParameterValues() == null){ // update the scenarioId, strategy parameters and initial deposit only once per scenario
            summaReportData.setScenarioId(scenarioId);
            summaReportData.setParameterValues(client.getReportData(processId).getParameterValues());
            summaReportData.setInitialDeposit(client.getReportData(processId).getInitialDeposit());
        }
        
        summaReportData.setFinishDeposit(client.getReportData(processId).getFinishDeposit());
        summaReportData.addToTurnover(client.getReportData(processId).getTurnover());
        summaReportData.addToCommission(client.getReportData(processId).getCommission());
        summaReportData.addToSubRuns();
        
        List<IOrder> closedOrders = client.getReportData(processId).getClosedOrders();
        if(!closedOrders.isEmpty()){
            for (IOrder order : closedOrders) {
                summaReportData.addToClosedOrders(order);
            }
        }
        
        List<IOrder> openOrders = client.getReportData(processId).getOpenOrders();
        if(!openOrders.isEmpty()){
            for (IOrder order : openOrders) {
                summaReportData.addToOpenOrders(order);
            }
        }
    }  
} // end MyReportData
