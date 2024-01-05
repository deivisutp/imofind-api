package br.com.deivisutp.imofindapi.factory;

import java.util.Date;
import java.util.HashMap;

public class HandlerFactory {
    public <T> Handler<T> getHandler(Class<T> c) {
        if (c == String.class) {
            return Record.STRING.make();
        }
        if (c == Date.class) {
            return Record.DATE.make();
        }
        return null;
    }
}
