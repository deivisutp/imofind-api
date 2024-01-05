package br.com.deivisutp.imofindapi.factory;

public class StringHandler implements Handler<String> {
    @Override
    public void handle(String str) {
        System.out.println(str);
    }
}
