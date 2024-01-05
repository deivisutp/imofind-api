package br.com.deivisutp.imofindapi.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Address {
    private final String street;
    private final int number;
    private final String bairro;
    private final String zipCode;
    private final String city;
    private final String state;
    private final String country;


    private Address(AddressBuilder builder) {
        this.street = builder.street;
        this.number = builder.number;
        this.bairro = builder.bairro;
        this.zipCode = builder.zipCode;
        this.city = builder.city;
        this.state = builder.state;
        this.country = builder.country;
    }

    public static class AddressBuilder {
        private String street;
        private int number;
        private String bairro;
        private String zipCode;
        private String city;
        private String state;
        private String country;

        public AddressBuilder() {
        }

        public AddressBuilder street(String street) {
            this.street = street;
            return this;
        }
        public AddressBuilder number(int number) {
            this.number = number;
            return this;
        }
        public AddressBuilder bairro(String bairro) {
            this.bairro = bairro;
            return this;
        }
        public AddressBuilder zipCode(String zipCode) {
            this.zipCode = zipCode;
            return this;
        }
        public AddressBuilder city(String city) {
            this.city = city;
            return this;
        }
        public AddressBuilder state(String state) {
            this.state = state;
            return this;
        }
        public AddressBuilder country(String country) {
            this.country = country;
            return this;
        }

        public Address build() {
            Address address =  new Address(this);
            return address;
        }
    }
}
