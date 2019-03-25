# Forex News Trading App for Dukascopy API

## Main Features

My basic strategy with Forex News Trading App is to exploit the volatility and price jumps during the economic news events, but this trading algo has lots of parameters and options, so it is easy to try out totally different strategies with it. To be able to use this trader the user needs some experience with the JForex platform of Dukascopy https://www.dukascopy.com/europe/hu/forex/dealstation/?c1#JForex for trading and some experience with the JForex SDK for testing https://www.dukascopy.com/wiki/en/development/get-started-api/use-jforex-sdk/download-jforex-sdk.

I usually open multiple pending orders (10-60) before the economic news events with different parameters both long and short positions. We need to open the positions before the event because to open positions on a faraway server takes time, and if we try to open the position when the news comes out we are too late. If the parameters are properly set the price jump fills the closest pending order on the long or short side and the positions on the opposite side are closed. With this strategy, I don't really care about the numbers of the economic news, because I assume that others get the news milliseconds earlier, and the price moves too much by the time I could check the news itself. The strategy tries to ride the jump of the price. I use different parameters (stopLoss, pointsAway, breakEvenDistance . . .) to decrease my risk, so for example if the jump is too small only a small part of my capital will suffer because the positions with larger pointsAway distance won't fire. But if the jump is large my closest pending orders can be in profit by the time the further pending orders will be filled.

Unfortunately, during news events we have to face very large slippage, which can ruin this strategy. It is very important to analyze the price movement during the news. I spent more time analyzing these events than building the trading app. I plan to summarize my findings in a later medium.com article. There are lots of events and currency pairs wich most of the time results losses. With a similar strategy is very important to find the events and currency pairs where we have a good chance. I only trade a very few events, and mostly only the EUR/USD/JPY pairs.

A trading example in test environment:
[linkname](https://youtu.be/t5W2jWQOW_Y)

## Disclaimer:
The news-trader was built for a personal purpose, it isn't very user-friendly, but if you are building a similar application some parts of the code can be useful to you. The main trading logic can be found in src.com.madar.library.newsstradle.MyInstrumentManagerBasicMulti.java file.
I uploaded the full Dukascopy SDK, but so far I didn't alter the SDK itself. My codes are under src.com.madar, apart from that only the pom.xml was modified directly.

## Parameters:

    boolean closeOnShutDown:
    If true all position will be closed it the trader or the Dukascopy Jforex platform is closed. 
    
    String pathEventCalendarCSV:
    Path to file containing the events we want to trade.
    
    String pathMultiSettingCSV:
    Path to file containing the different parameters for the events.
        
    boolean isTest:
    Determines if we use the @Configurable parameters of the strategy class or get our parameters from an outer source (csv)
    
    boolean isMultiScenarioRun:
    Determines if we trade only one scenario (parameter settings) or multiple scenarios in the same time
    
    String eventName:
    Name of the event we want to trade. 

    MyCurrency currencyOfEvent:
    Currency of the country where the event is released.

    Instrument testInstrument:
    Currency pair or other assets to trade.
    
    MyOrderDirection enabledOrderDirection:
    Enabled orderdirections: short, long, both.

    double pointsAway:
    How far is originally the pending long position from the ask price or the pending short position from the bid price.
    0.1 is 1 pip.

    double takeProfit:
    How many pip in profit to close the position. If 0 we don't close the position with takeProfit.

    double stopLoss:
    The original stopLoss in pips. If 0 we don't have stopLoss (Never set it to 0 with news trading.)

    double modifyGap:
    Before the news event, we want continuously modify all our pending orders to retain the distance from the actual price. This parameter determines how large shift in the price is needed to upgrade the parameters of our pending orders.
    
    long secondsBeforePending:
    Seconds before news time, we open the pending orders.

    long secondsBeforeModify:
    Seconds before news time, from this time we don't modify pending orders.

    long secondsAfterNews:
    How long are the unfilled pending orders open after news time.

    long secondsAfterNewsOffset:
    If we use multi-parameter strategy with this parameter we can have positions which are open for a longer time than the secondsAfterNews.
    
    double breakevenTrigger:
    How much in profit must be the position to make a break even action (pull the stopLoss closer). If 0 there is no breakEven.

    double breakevenDistance:
    The stopLoss after we reached the breakevenTrigger.

    double trailingStop:
    The distance of the trailing stopLoss.

    double trailingAfter:
    How much in profit must be the position to start trailing the stopLoss.

    boolean trailImmediately:
    If true we don't use trailingAfter, we start trailing immediately.
    
    boolean isOCO:
    If true when the first order is filled on one side, every order on the other side is closed.
    
    boolean manageMoney:
    If true we need only set the riskPercent, every position will be calculated automatically.

    boolean calcAmountWithMaxSpread:
    Determines if we use the max spread when calculating amounts to trade.

    double riskPercent:
    What percent of our capital we want to trade. 0.01 is 1%.

    double amount:
    If we don't use the riskPercent we can set the amount of a position directly.
    
    double maxSpread:
    The maximum spread before news. If the spread is larger than maxSpread the position will be closed before the event. This can help to avoid some very risky situation.

    double maxSlippage:   
    Dukascopy enables slippagecontrol. If the slippage is too large you can choose to close the position.

The News-Trader has two main modes, trading mode and multi-parameter testing mode.


## Trading mode

When we trade with the news-trader we use it from the JForex platform of Dukascopy. Under the Navigator tab we can import and compile strategies we want to run. 

Our NewsStradleStrategy.java file is in the src.com.madar.strategies folder. The other files needed for the strategy are in the src.com.madar.library.newsstradle folder. We need to import only the NewsStradleStrategy.java file, if our SDK is deployed properly the other dependencies will be found during the compile process.

The strategy needs two resource files to trade:
run-trade-schedule.csv - this file contains the names of events we want to trade and the time of the event
multisettings.csv - this file contains the multiple parameter scenarios for the events we want to trade
Some example files for proper formatting can be found in the example_data_files folder, but before running the strategy we can import our own files in the JForex platform.

If we start the strategy it will wait for the proper time before the news events to open the positions determined in the multisettings.csv file and manage everything until all of our positions are closed.

In the image below you can see an example of a semi-automatically generated scenario bunch (part of it). This isn't generated with this app, it is written in excel/vba, but shows you how many parameters can you set for your strategies.

![alt text](https://github.com/sinusgamma/Forex-News-Trader-Dukascopy-API/blob/master/example_data_files/multisettings.JPG)


## Testing mode

For multiparameter testing you need to add an enum file to com.madar.tester package:
```
package com.madar.tester;

public enum MyPassword {
    USERNAME, PASSWORD
}
```
These are your Dukascopy account usernames and passwords. If you use DEMO account you have to ask for an other account every two weeks.

The multiparameter testing mode isn't finished, to use it needs altering the code in some places.
You can test your parameter settings in JForex platform, but if you want to test thousands of scenarios you can run it from my tester.
I run the tester from Netbeans, and in the near future, I don't plan to do it any other way.
At the moment the tester is connected to a database, and without that, it isn't possible to run the tester.
I use a PostgreSQL database where the fxs_news table stores all the economic event from the last years. The calendar can be reached here: https://www.fxstreet.com/economic-calendar
My table in the database looks like this:

![alt text](https://github.com/sinusgamma/Forex-News-Trader-Dukascopy-API/blob/master/example_data_files/news_database.JPG)

The database connection is solved in the src.com.madar.dataio.MyPostrgerManager.java file.

Another file we need for testing is the testparameters.csv, this files contains all values for the parameters we want to test. From this csv the model generates all the meaningful combinations and we get our parameter scenarios. Later in an article, I plan to show how I am choosing the testparameters for an event, but that is a lot of data analysis, here I want only to show how the trader works.
An excelized version of this file can be seen below, and a csv is in the example_data_files folder:

![alt text](https://github.com/sinusgamma/Forex-News-Trader-Dukascopy-API/blob/master/example_data_files/testparameters.JPG) 

During test we want to test our parameter scenarios on a news events, for example, the US Non-farm Payroll release. The tester searches the Postgres database for these release dates, and tests the parameter scenarios with all the US Non-farm Payroll releases from the past years.

At the beginning of the test we can check what are we testing:

![alt text](https://github.com/sinusgamma/Forex-News-Trader-Dukascopy-API/blob/master/example_data_files/starttest.JPG)

After the test, we can find how our individual scenarios performed. The report_USD_United_States_Existing_Home_Sales_MoM_USDJPY.csv in the example_data_files folder is an example of that. 
If you have your report you can consider to run another test with different parameters, or examine your scenarios. Here is an example of an excel file based on the report where I calculate different other statistics and sort, colorize the results:

![alt text](https://github.com/sinusgamma/Forex-News-Trader-Dukascopy-API/blob/master/example_data_files/report.JPG)

From this excel file later I generate the ensemble of scenarios I will use in live trading.


## Main problems:

My tester tried to use the Dukascopy test environment in a way it wasn't assumed by its developers. Most of the user uses the Dukascopy tester with longer periods. Because of my strategy I had to test for very small periods but lots of times. Because of that, my tester caused a memory leak. I managed to deal with the memory leak by building a jar and calling it multiple times with different parameters in a loop (testerMainMultiprocessLoop.java). In newer versions of the sdk the memory leak is patched, but I didn't have time to alter the code. It works the same way, but this loop isn't necessary.

There is an annoying behavior what I didn't remedy so far. During news events, we can get the "Stop order has been rejected by interbank party. System will resubmit this order." message. This is because there is no demand/supply available for the order to be filled under the conditions I have set. If the order is canceled after this message no problem, I could live with it. No profit, no loss. But the order isn't canceled, instead, it is resubmitted, but it is resubmitted after the news with the parameters I wanted to trade THE news. As they say, they can't switch off this behavior for my account, I have to solve it in code. The problem is that I have to wait for the message that my order wasn't filled and will resubmit and after that, I have to send a message back to the server to delete this order. This is a lot of time.

Given enough time the next step will be to remedy the "resubmitted order" problem.




