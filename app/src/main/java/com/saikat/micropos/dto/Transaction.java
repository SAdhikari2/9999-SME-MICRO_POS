package com.saikat.micropos.dto;

import lombok.Data;

@Data
public class Transaction {
    private String cashEntry;
    private String itemValues;
    private String paymentStatus;
    private String paymentType;
    private String totalValue;
    private String transactionId;
    private String transactionTime;
    private String userId;
}
