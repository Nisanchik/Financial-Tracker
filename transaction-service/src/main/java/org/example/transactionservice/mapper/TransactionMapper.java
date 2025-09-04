package org.example.transactionservice.mapper;

import org.example.transactionservice.model.dto.TransactionResponse;
import org.example.transactionservice.model.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper {

    TransactionResponse toTransactionResponse(Transaction transaction);

}