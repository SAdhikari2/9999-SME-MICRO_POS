package com.saikat.micropos.persistance.entity;

import java.io.Serializable;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionHistory implements Serializable {
    String transactionId, itemValues, totalValue, cashEntry, paymentType, paymentStatus, transactionTime;
}
