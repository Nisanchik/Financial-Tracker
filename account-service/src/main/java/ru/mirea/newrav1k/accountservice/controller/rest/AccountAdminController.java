package ru.mirea.newrav1k.accountservice.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mirea.newrav1k.accountservice.model.dto.AccountFilter;
import ru.mirea.newrav1k.accountservice.model.dto.AccountResponse;
import ru.mirea.newrav1k.accountservice.service.AccountCommandService;
import ru.mirea.newrav1k.accountservice.service.AccountQueryService;

import java.util.UUID;

@Tag(name = "Account Admin Controller",
        description = "Контроллер администратора для управления аккаунтами")
@Slf4j
@RestController
@RequestMapping("/api/admin/accounts")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AccountAdminController {

    private final AccountCommandService accountCommandService;

    private final AccountQueryService accountQueryService;

    @Operation(summary = "Загрузка аккаунтов",
            description = """
                    Загружает список всех аккаунтов с возможностью фильтрации.
                    Доступно только для пользователей с правами администратора.
                    
                    **Особенности:**
                            - Автоматическая фильтрация по trackerId текущего пользователя
                            - Поддержка пагинации и сортировки
                            - Фильтрация по имени, валюте и дате создания
                    """,
            responses = {
                    @ApiResponse(responseCode = "403", description = "У пользователя отсутствуют необходимые права")
            },
            parameters = {
                    @Parameter(name = "trackerId", hidden = true),
                    @Parameter(name = "name", description = "Имя аккаунта", example = "Основной аккаунт"),
                    @Parameter(name = "currency", description = "Валюта аккаунта", example = "RUB"),
                    @Parameter(name = "createdAtFrom", description = "Дата создания от (включительно)", example = "2024-01-01T00:00:00Z"),
                    @Parameter(name = "createdAtTo", description = "Дата создания до (включительно)", example = "2024-12-31T23:59:59Z"),
                    @Parameter(name = "page", description = "Номер страницы (начиная с 0)", example = "0"),
                    @Parameter(name = "size", description = "Размер страницы", example = "20"),
                    @Parameter(name = "sort", description = "Поле для сортировки (например: name,asc или createdAt,desc)", example = "createdAt,desc")
            }
    )
    @GetMapping
    public PagedModel<AccountResponse> getAllAccounts(@ModelAttribute AccountFilter filter,
                                                      @PageableDefault Pageable pageable) {
        log.info("Admin request to get all accounts");
        Page<AccountResponse> accounts = this.accountQueryService.findAll(filter, pageable);
        return new PagedModel<>(accounts);
    }

    @Operation(summary = "Загрузка аккаунта",
            description = """
                    Загружает аккаунт пользователя по его уникальному идентификатору.
                    Доступно только для пользователей с правами администратора.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Аккаунт успешно найден"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
                    @ApiResponse(responseCode = "403", description = "У пользователя отсутствуют необходимые права")
            },
            parameters = {
                    @Parameter(name = "accountId", description = "Идентификатор аккаунта",
                            example = "48c6cc60-cea6-4872-9333-634516e9e66f", in = ParameterIn.PATH)
            }
    )
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable("accountId") UUID accountId) {
        log.info("Admin request to get account by id: accountId={}", accountId);
        AccountResponse account = this.accountQueryService.findById(accountId);
        return ResponseEntity.ok(account);
    }

    @Operation(summary = "Удаление аккаунта",
            description = """
                    Удаляет аккаунт пользователя по его уникальному идентификатору.
                    Доступно только для аутентифицированных пользователей.
                    """,
            responses = {
                    @ApiResponse(responseCode = "204", description = "Аккаунт успешно удалился"),
                    @ApiResponse(responseCode = "400", description = """
                            - На аккаунте имеется кредитная задолженность
                            - На аккаунте имеются денежные средства
                            """),
                    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
                    @ApiResponse(responseCode = "403", description = "У пользователя отсутствуют необходимые права")
            },
            parameters = {
                    @Parameter(name = "accountId", description = "Идентификатор аккаунта",
                            example = "48c6cc60-cea6-4872-9333-634516e9e66f", in = ParameterIn.PATH)
            }
    )
    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccountById(@PathVariable("accountId") UUID accountId) {
        log.info("Admin request to delete account by id: accountId={}", accountId);
        this.accountCommandService.hardDeleteById(accountId);
        return ResponseEntity.noContent().build();
    }

}