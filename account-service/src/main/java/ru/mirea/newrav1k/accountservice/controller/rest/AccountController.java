package ru.mirea.newrav1k.accountservice.controller.rest;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import ru.mirea.newrav1k.accountservice.model.dto.AccountCreateRequest;
import ru.mirea.newrav1k.accountservice.model.dto.AccountFilter;
import ru.mirea.newrav1k.accountservice.model.dto.AccountResponse;
import ru.mirea.newrav1k.accountservice.model.dto.AccountUpdateRequest;
import ru.mirea.newrav1k.accountservice.security.HeaderAuthenticationDetails;
import ru.mirea.newrav1k.accountservice.service.AccountCommandService;
import ru.mirea.newrav1k.accountservice.service.AccountQueryService;
import ru.mirea.newrav1k.accountservice.service.BalanceOperationService;

import java.math.BigDecimal;
import java.util.UUID;

@Tag(name = "Account Controller",
        description = "Контроллер для управления аккаунтами")
@Slf4j
@RestController
@RequestMapping("/api/accounts")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class AccountController {

    private final BalanceOperationService balanceOperationService;

    private final AccountCommandService accountCommandService;

    private final AccountQueryService accountQueryService;

    @Operation(summary = "Загрузка аккаунтов",
            description = """
                    Загружает список всех аккаунтов с возможностью фильтрации.
                    Доступно только для аутентифицированных пользователей.
                    
                    **Особенности:**
                            - Автоматическая фильтрация по trackerId текущего пользователя
                            - Поддержка пагинации и сортировки
                            - Фильтрация по имени, валюте и дате создания
                    """,
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
    public PagedModel<AccountResponse> getAllAccounts(@AuthenticationPrincipal HeaderAuthenticationDetails authentication,
                                                      @ParameterObject @ModelAttribute AccountFilter filter,
                                                      @ParameterObject @PageableDefault Pageable pageable) {
        log.info("Request to get all accounts");
        Page<AccountResponse> accounts =
                this.accountQueryService.findAllAccountsByTrackerId(authentication.getTrackerId(), filter, pageable);
        return new PagedModel<>(accounts);
    }

    @Operation(summary = "Загрузка аккаунта",
            description = """
                    Загружает аккаунт пользователя по его уникальному идентификатору.
                    Доступно только для аутентифицированных пользователей.""",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Аккаунт успешно найден"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
            },
            parameters = {
                    @Parameter(name = "accountId", description = "Идентификатор аккаунта",
                            example = "48c6cc60-cea6-4872-9333-634516e9e66f", in = ParameterIn.PATH)
            }
    )
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccount(@AuthenticationPrincipal HeaderAuthenticationDetails authentication,
                                                      @PathVariable("accountId") UUID accountId) {
        log.info("Request to get account: accountId={}", accountId);
        AccountResponse account = this.accountQueryService.findByTrackerIdAndAccountId(authentication.getTrackerId(), accountId);
        return ResponseEntity.ok(account);
    }

    @Operation(summary = "Создание аккаунта",
            description = """
                    Создает новый аккаунт для пользователя.
                    Доступно только для аутентифицированных пользователей.""",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Аккаунт успешно создан"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации или аккаунт с таким именем уже зарегистрирован у пользователя"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания аккаунта",
                    required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountCreateRequest.class))
            )
    )
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@AuthenticationPrincipal HeaderAuthenticationDetails authentication,
                                                         @Valid @RequestBody AccountCreateRequest request,
                                                         UriComponentsBuilder uriBuilder) {
        log.info("Request to create account: request={}", request);
        AccountResponse account = this.accountCommandService.createAccount(authentication.getTrackerId(), request);
        return ResponseEntity.created(uriBuilder
                        .replacePath("/api/accounts/{accountId}")
                        .build(account.id()))
                .body(account);
    }

    @Operation(summary = "Обновление аккаунта",
            description = """
                    Обновляет аккаунт пользователя по его уникальному идентификатору.
                    Доступно только для аутентифицированных пользователей.""",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Аккаунт успешно обновлен"),
                    @ApiResponse(responseCode = "400", description = """
                            - Ошибка валидации
                            - Аккаунт с таким названием уже существует
                            - На аккаунте имеются средства, поэтому невозможно изменить валюту
                            """),
                    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для обновления аккаунта пользователя",
                    required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccountUpdateRequest.class))
            ),
            parameters = {
                    @Parameter(name = "accountId", description = "Идентификатор аккаунта",
                            example = "48c6cc60-cea6-4872-9333-634516e9e66f", in = ParameterIn.PATH)
            }
    )
    @PutMapping("/{accountId}")
    public ResponseEntity<AccountResponse> updateAccount(@AuthenticationPrincipal HeaderAuthenticationDetails authentication,
                                                         @PathVariable("accountId") UUID accountId,
                                                         @Valid @RequestBody AccountUpdateRequest request) {
        log.info("Request to update account: accountId={}, request={}", accountId, request);
        AccountResponse account = this.accountCommandService.updateAccount(authentication.getTrackerId(), accountId, request);
        return ResponseEntity.ok(account);
    }

    @Operation(summary = "Частичное обновление аккаунта",
            description = """
                    Частично обновляет аккаунт пользователя по его уникальному идентификатору.
                    Доступно только для аутентифицированных пользователей.""",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Аккаунт успешно обновлен"),
                    @ApiResponse(responseCode = "400", description = """
                            - Ошибка валидации
                            - Аккаунт с таким названием уже существует
                            - На аккаунте имеются средства, поэтому невозможно изменить валюту
                            """),
                    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
                    @ApiResponse(responseCode = "500", description = "Сервер временно не отвечает")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для частичного обновления аккаунта",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Название аккаунта",
                                            summary = "Изменение названия аккаунта",
                                            value = """
                                                    {
                                                        "name": "Tinkoff"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Изменение валюты",
                                            summary = "Обновление валюты (только для пустых счетов)",
                                            value = """
                                                    {
                                                        "currency": "USD"
                                                    }
                                                    """
                                    )
                            })
            ),
            parameters = {
                    @Parameter(name = "accountId", description = "Идентификатор аккаунта",
                            example = "48c6cc60-cea6-4872-9333-634516e9e66f", in = ParameterIn.PATH)
            }
    )
    @PatchMapping("/{accountId}")
    public ResponseEntity<AccountResponse> patchAccount(@AuthenticationPrincipal HeaderAuthenticationDetails authentication,
                                                        @PathVariable("accountId") UUID accountId,
                                                        @RequestBody JsonNode jsonNode) {
        log.info("Request to patch account: accountId={}, jsonNode={}", accountId, jsonNode);
        AccountResponse account = this.accountCommandService.patchAccount(authentication.getTrackerId(), accountId, jsonNode);
        return ResponseEntity.ok(account);
    }

    @Operation(summary = "Удаление аккаунта",
            description = """
                    Удаляет аккаунт пользователя по его уникальному идентификатору.
                    Доступно только для аутентифицированных пользователей.""",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Аккаунт успешно удалился"),
                    @ApiResponse(responseCode = "400", description = """
                            - На аккаунте имеется кредитная задолженность
                            - На аккаунте имеются денежные средства
                            """),
                    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
            },
            parameters = {
                    @Parameter(name = "accountId", description = "Идентификатор аккаунта",
                            example = "48c6cc60-cea6-4872-9333-634516e9e66f", in = ParameterIn.PATH)
            }
    )
    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal HeaderAuthenticationDetails authentication,
                                              @PathVariable("accountId") UUID accountId) {
        log.info("Request to delete account: accountId={}", accountId);
        this.accountCommandService.softDeleteById(authentication.getTrackerId(), accountId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Обновление баланса",
            description = """
                    Обновляет баланс аккаунта пользователя по его уникальному идентификатору.
                    
                    **Логика операции:**
                                    - Положительное значение: пополнение счета
                                    - Отрицательное значение: снятие средств
                                    - Для кредитных карт учитывается доступный лимит
                    
                    **Идемпотентность:** операция гарантированно выполняется только один раз\s
                    для каждого transactionId (защита от дублирования).
                    
                    Доступно только для аутентифицированных пользователей.""",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Баланс аккаунта успешно обновился"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
            },
            parameters = {
                    @Parameter(name = "accountId", description = "Идентификатор аккаунта",
                            example = "48c6cc60-cea6-4872-9333-634516e9e66f", in = ParameterIn.PATH),
                    @Parameter(name = "transactionId", description = "Идентификатор транзакции",
                            example = "4ef81a12-7510-47d5-9ddf-b8642e4106d7", in = ParameterIn.QUERY),
                    @Parameter(name = "amount", description = "Стоимость", example = "1337", in = ParameterIn.QUERY)
            }
    )
    @PostMapping("/{accountId}/update-balance") // @PostMapping для корректной работы FeignClient
    public ResponseEntity<Void> updateAccountBalance(@PathVariable("accountId") UUID accountId,
                                                     @RequestParam("transactionId") UUID transactionId,
                                                     @RequestParam("amount") BigDecimal amount,
                                                     @AuthenticationPrincipal HeaderAuthenticationDetails authentication) {
        log.info("Request to update account balance: accountId={}, transactionId={}", accountId, transactionId);
        this.balanceOperationService.updateBalance(authentication.getTrackerId(), accountId, transactionId, amount);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Перевод денег на другой аккаунт пользователя",
            description = """
                    Переводит деньги пользователя с одного аккаунта на другой аккаунт.
                    Доступно только для аутентифицированных пользователей.""",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Деньги успешно перевелись"),
                    @ApiResponse(responseCode = "400", description = "Нельзя переводить деньги на этот же аккаунт"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
            },
            parameters = {
                    @Parameter(name = "fromAccountId", description = "Идентификатор аккаунта отправителя",
                            example = "48c6cc60-cea6-4872-9333-634516e9e66f", in = ParameterIn.PATH),
                    @Parameter(name = "toAccountId", description = "Идентификатор аккаунта получателя",
                            example = "e060355e-fdd1-46c0-ba7c-69cd85ae0264", in = ParameterIn.PATH),
                    @Parameter(name = "transactionId", description = "Идентификатор транзакции",
                            example = "4ef81a12-7510-47d5-9ddf-b8642e4106d7", in = ParameterIn.QUERY),
                    @Parameter(name = "amount", description = "Стоимость", example = "1337", in = ParameterIn.QUERY),
            }
    )
    @PostMapping("/{fromAccountId}/transfer/{toAccountId}")
    public ResponseEntity<Void> transferAccount(@PathVariable("fromAccountId") UUID fromAccountId,
                                                @PathVariable("toAccountId") UUID toAccountId,
                                                @RequestParam("transactionId") UUID transactionId,
                                                @RequestParam("amount") BigDecimal amount,
                                                @AuthenticationPrincipal HeaderAuthenticationDetails authentication) {
        log.info("Request to transfer: fromAccountId={}, toAccountId={}", fromAccountId, toAccountId);
        this.balanceOperationService.transferFunds(authentication.getTrackerId(), fromAccountId, toAccountId, transactionId, amount);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Управление активацией аккаунта",
            description = """
                    Управляет активацией аккаунта пользователя по его уникальному идентификатору.
                    
                    **Логика операции:**
                                    - Если параметр activation=true аккаунт активируется
                                    - Если параметр activation=false аккаунт деактивируется
                    
                    Доступно только для аутентифицированных пользователей.
                    """,
            responses = {
                    @ApiResponse(responseCode = "204", description = "Статус аккаунта успешно изменился"),
                    @ApiResponse(responseCode = "400", description = """
                            - Аккаунт уже удален
                            - На аккаунте имеется кредитная задолженность
                            - На аккаунте имеются денежные средства
                            """),
                    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
            },
            parameters = {
                    @Parameter(name = "accountId", description = "Идентификатор аккаунта",
                            example = "48c6cc60-cea6-4872-9333-634516e9e66f", in = ParameterIn.PATH),
                    @Parameter(name = "activated", description = "Статус активации", example = "true", in = ParameterIn.QUERY)
            }
    )
    @PostMapping("/{accountId}/activation")
    public ResponseEntity<Void> setActivationAccount(@PathVariable("accountId") UUID accountId,
                                                     @RequestParam("activation") boolean activated,
                                                     @AuthenticationPrincipal HeaderAuthenticationDetails authentication) {
        log.info("Request to set activation: accountId={}, activation={}", accountId, activated);
        this.accountCommandService.setActivationStatus(authentication.getTrackerId(), accountId, activated);
        return ResponseEntity.noContent().build();
    }

}