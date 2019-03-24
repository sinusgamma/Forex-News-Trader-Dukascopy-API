# Forex News Trading App for Dukascopy API

## Main Features

My basic strategy with Forex News Trading App is to exploit the volatility and price jumps during the economic news events, but the model has lots of parameters and options, so it is easy to try out totally different strategies with it. To be able to use this trader the user needs some experience with the JForex platform of Dukascopy https://www.dukascopy.com/europe/hu/forex/dealstation/?c1#JForex for trading and some experience with the JForex SDK for testing https://www.dukascopy.com/wiki/en/development/get-started-api/use-jforex-sdk/download-jforex-sdk.

I usually open multiple pending orders (10-60) before the economic news events with different parameters both long and short positions. We need to open the positions before the event, because to open positions on a far away server takes time, and if we try to open the position when the news comes out we are too late. If the parameters are properly set the price jump filles the closest pending order on the long or short side and the positions on the opposite side are closed. With this strategy I don't really care about the numbers of the economic news, because I assume that others get the news milliseconds earlier, and the price moves too much by the time I could check the news itself. The strategy tries to ride the jump of the price. I use different parameters (stopLoss, pointsAway, breakEvenDistance . . .) to decrease my risk, so for example if the jump is too small only a small part of my capital will suffer, because the positions with larger pointsAway distance won't fire. But if the jump is large my closest pending orders can be in profit by the time the further pending orders will be filled.

Unfortunatelly during news events we have to face very large slippage, which can ruin this strategy. It is very important to analyse the price movement during the news. I spent more time analyzing these events than building the trading app. I plan to summarize my findings in a later medium.com article. There are lots of events and currency pairs wich most of the time results losses. With a similar strategy is very important to find the events and currency pairs where we have good chance. I only trade a very few events, and mostly only the EUR/USD/JPY pairs.

### Parameters:

    boolean closeOnShutDown:
    If true all position will be closed it the trader or the Dukascopy Jforex platfrom is closed. 
    
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
    Currency pair or other asset to trade.
    
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
    Before the news event we want continously modify all our pending orders to retain the distance from the actual price. This parameter determines how large shift in the price is needed to upgrade the parameters of our pending orders.
    
    long secondsBeforePending:
    Seconds before news time, we open the pending orders.

    long secondsBeforeModify:
    Seconds before news time, from this time we don't modify pending orders.

    long secondsAfterNews:
    How long are the unfilled pending orders open after news time.

    long secondsAfterNewsOffset:
    If we use multi-parameter strategy with this parameter we can have positions which are open for longer time than the secondsAfterNews.
    
    double breakevenTrigger:
    How much in profit must be the position to make a break even action (pull the stopLoss closer). If 0 there is no breakEven.

    double breakevenDistance:
    The stopLoss after we reached the breakevenTrigger.

    double trailingStop:
    The distance of the trailing stopLoss.

    double trailingAfter:
    How much in profit must be the position to start trailing the stopLoss.

    boolean trailImmediately:
    If true we don't use trailingAfter, we start trailing immediatelly.
    
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
run-trade-schedule.csv - this file containes the names of events we want to trade and the time of the event
multisettings.csv - this file containes the multiple parameter scenarios for the events we want to trade
Some example files for proper formating can be found in the example_data_files folder, but before running the strategy we can import our own files in the JForex platform.

If we start the strategy it will wait for the proper time before the news events to open the positions determined in the multisettings.csv file and manage everything until all of our positions are closed.

In the image bellow you can see an example of a semi-automatically generated scenario bunch (part of it). This isn't generated with this app, it is written in excel/vba, but shows you how many parameters can you set for your strategies.

![alt text](https://github.com/sinusgamma/Forex-News-Trader-Dukascopy-API/blob/master/example_data_files/multisettings.JPG)

## Testing mode
You can test your parameter settings in JForex platform, but if you want to test thousands of scenarios you can run it from my tester.
I run the tester from Netbeans, and in the near future I don't plan to do it any other way.
At the moment the tester is connected to a database, and without that it isn't possible to run the tester.
I use a PostgreSQL database where the fxs_news table stores all the economic event from the last years. The calendar can be reached here: https://www.fxstreet.com/economic-calendar
My table in the database looks like this: 
![alt text](https://github.com/sinusgamma/Forex-News-Trader-Dukascopy-API/blob/master/example_data_files/news_database.JPG)

