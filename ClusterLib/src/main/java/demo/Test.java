/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demo;

import java.time.temporal.ChronoUnit;
import java.util.Calendar;

/**
 *
 * @author thangpham
 */
public class Test {

    public static void main(String[] args) {
        Calendar cal = Calendar.getInstance();
        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.DATE, 3);
        System.out.println(cal.compareTo(cal1));
//        LocalDate.
//        ChronoUnit.DAYS.between(temporal1Inclusive, temporal2Exclusive);
    }
}
