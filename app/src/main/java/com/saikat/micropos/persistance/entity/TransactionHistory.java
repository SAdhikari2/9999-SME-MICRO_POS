package com.saikat.micropos.persistance.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionHistory implements Serializable {
    String userId,transactionId, itemValues, totalValue, cashEntry, paymentType, paymentStatus, transactionTime;
}
