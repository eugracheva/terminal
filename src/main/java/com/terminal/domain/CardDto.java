package com.terminal.domain;

import lombok.Data;

@Data
public class CardDto {
    private long id;
    private String cardNumber;
    private ClientDto client;
    private String pin;
    private String statusCard;
    private Double balance;
}
