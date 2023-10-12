package br.com.deivisutp.imofindapi.util;

import java.text.*;
import java.util.Date;

public class DataUtil {

    public static String formataDateEmString(Date data, String mask) {
        DateFormat formatter = new SimpleDateFormat(mask);
        return formatter.format(data);
    }

    public static String getDataString(String fromDto, String fromEntity) {
        return fromDto != null && !fromDto.isEmpty() ? fromDto : fromEntity;
    }
}
