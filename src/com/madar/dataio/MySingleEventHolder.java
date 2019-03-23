/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.madar.dataio;

import com.madar.library.newsstraddle.MyCurrency;
import java.time.ZonedDateTime;

    // to hold the single news calendar events
    public class MySingleEventHolder {

        private final ZonedDateTime newsTime;
        private final String newsName;
        private final MyCurrency currency;

        // CONSTRUCTOR
        public MySingleEventHolder(ZonedDateTime testTime, String newsName, MyCurrency currency) {
            this.newsTime = testTime;
            this.newsName = newsName;
            this.currency = currency;
        }

        public ZonedDateTime getEventTime() {
            return newsTime;
        }

        public String getEventName() {
            return newsName;
        }

        public MyCurrency getCurrency() {
            return currency;
        }
    } // end class SingleEventHolder  
