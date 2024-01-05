package br.com.deivisutp.imofindapi.factory;

import java.util.Date;

public class DateHandler implements Handler<Date> {
    @Override
    public void handle(Date date) {
        System.out.println(date);
    }
}
