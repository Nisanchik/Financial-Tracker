package ru.mirea.newrav1k.accountservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.mirea.newrav1k.accountservice.model.dto.AccountResponse;
import ru.mirea.newrav1k.accountservice.model.entity.Account;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "active", source = "active")
    AccountResponse toAccountResponse(Account account);

}