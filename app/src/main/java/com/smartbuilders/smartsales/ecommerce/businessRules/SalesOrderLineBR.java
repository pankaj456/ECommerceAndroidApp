package com.smartbuilders.smartsales.ecommerce.businessRules;

import com.smartbuilders.smartsales.ecommerce.model.SalesOrderLine;

/**
 * Created by stein on 27/5/2016.
 */
public class SalesOrderLineBR {

    public static double getTotalLine(SalesOrderLine orderLine){
        double totalLine = 0;
        try {
            totalLine = (orderLine.getPrice() * orderLine.getQuantityOrdered())
                    + (orderLine.getPrice() * orderLine.getQuantityOrdered() * (orderLine.getTaxPercentage()/100));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return totalLine;
    }
}