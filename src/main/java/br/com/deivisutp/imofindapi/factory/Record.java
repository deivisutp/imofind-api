package br.com.deivisutp.imofindapi.factory;

import java.util.Date;

public enum Record {
    STRING {
        @Override
        public Handler<String> make() {
            return new StringHandler();
        }
    },
    DATE {
        @Override
        public Handler<Date> make() {
            return new DateHandler();
        }
    };

    public abstract <T> Handler<T> make();
}
