package br.com.deivisutp.imofindapi.util;

import java.text.*;
import java.util.Date;

public class DataUtil {

    public static String formataDateEmString(Date data, String mask) {
        DateFormat formatter = new SimpleDateFormat(mask);
        return formatter.format(data);
    }
}
