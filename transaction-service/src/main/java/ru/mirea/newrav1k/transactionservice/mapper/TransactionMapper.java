package ru.mirea.newrav1k.transactionservice.mapper;

import ru.mirea.newrav1k.transactionservice.model.dto.TransactionResponse;
import ru.mirea.newrav1k.transactionservice.model.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper {

    TransactionResponse toTransactionResponse(Transaction transaction);

}