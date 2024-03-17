package com.saikat.micropos.persistance.entity;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionHistory {
    String itemValues, totalValue, cashEntry, paymentType, paymentStatus;
}
