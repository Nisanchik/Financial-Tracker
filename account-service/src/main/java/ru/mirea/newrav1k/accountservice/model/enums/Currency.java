package ru.mirea.newrav1k.accountservice.model.enums;

public enum Currency {
    RUB, USD, EUR;

    public static Currency findCurrency(String currency) {
        for (Currency curr : Currency.values()) {
            if (curr.name().equalsIgnoreCase(currency)) {
                return curr;
            }
        }
        throw new IllegalArgumentException("Invalid currency: " + currency);
    }

}