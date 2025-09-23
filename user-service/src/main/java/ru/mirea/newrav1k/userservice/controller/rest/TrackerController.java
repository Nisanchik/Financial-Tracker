package ru.mirea.newrav1k.userservice.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mirea.newrav1k.userservice.model.dto.ChangePasswordRequest;
import ru.mirea.newrav1k.userservice.model.dto.ChangePersonalInfoRequest;
import ru.mirea.newrav1k.userservice.model.dto.ChangeUsernameRequest;
import ru.mirea.newrav1k.userservice.model.dto.TrackerResponse;
import ru.mirea.newrav1k.userservice.security.core.TrackerPrincipal;
import ru.mirea.newrav1k.userservice.security.token.JwtToken;
import ru.mirea.newrav1k.userservice.service.TrackerService;

import java.util.UUID;

@Tag(name = "Tracker Controller",
        description = "Контроллер для управления клиентами")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trackers")
public class TrackerController {

    private final TrackerService trackerService;

    @Operation(summary = "Получение списка клиентов",
            description = "Получает список клиентов. Доступно только для администратора")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public PagedModel<TrackerResponse> getAllTrackers(@PageableDefault Pageable pageable) {
        log.info("Request to get all trackers");
        return new PagedModel<>(this.trackerService.findAll(pageable));
    }

    @Operation(summary = "Получение персональной информации",
            description = "Получает персональную информацию пользователя по его идентификатору")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<TrackerResponse> me(@AuthenticationPrincipal TrackerPrincipal principal) {
        log.info("Request to get information about me");
        TrackerResponse tracker = this.trackerService.findById(principal.getTrackerId());
        return ResponseEntity.ok(tracker);
    }

    @Operation(summary = "Изменение персональных данных",
            description = "Изменяет персональные данные пользователя по его идентификатору")
    @ApiResponses(value = {@ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные")})
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me")
    public ResponseEntity<TrackerResponse> changePersonalInfo(@Valid @RequestBody ChangePersonalInfoRequest request,
                                                              @AuthenticationPrincipal TrackerPrincipal principal) {
        log.info("Request to change personal information");
        TrackerResponse tracker = this.trackerService.changePersonalInfo(request, principal.getTrackerId());
        return ResponseEntity.ok(tracker);
    }

    @Operation(summary = "Изменение пароля",
            description = "Изменяет пароль пользователя по его идентификатору")
    @ApiResponses(value = {@ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные")})
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me/change-password")
    public ResponseEntity<JwtToken> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                   @AuthenticationPrincipal TrackerPrincipal principal) {
        log.info("Request to change password");
        JwtToken jwtToken = this.trackerService.changePassword(request, principal.getTrackerId());
        return ResponseEntity.ok(jwtToken);
    }

    @Operation(summary = "Изменение почты пользователя",
            description = "Изменяет почты пользователя по его идентификатору")
    @ApiResponses(value = {@ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные")})
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me/change-username")
    public ResponseEntity<JwtToken> changeUsername(@Valid @RequestBody ChangeUsernameRequest request,
                                                   @AuthenticationPrincipal TrackerPrincipal principal) {
        log.info("Request to change username");
        JwtToken jwtToken = this.trackerService.changeUsername(request, principal.getTrackerId());
        return ResponseEntity.ok(jwtToken);
    }

    @Operation(summary = "Удаление своего аккаунта",
            description = "Удаляет аккаунт пользователя по его идентификатору")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteSelf(@AuthenticationPrincipal TrackerPrincipal principal) {
        log.info("Request to delete self");
        this.trackerService.deleteById(principal.getTrackerId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удаление своего аккаунта",
            description = "Удаляет аккаунт пользователя по его идентификатору. Доступно только для администратора")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{trackerId}")
    public ResponseEntity<Void> deleteTracker(@PathVariable("trackerId") UUID trackerId) {
        log.info("Request to delete tracker");
        this.trackerService.deleteById(trackerId);
        return ResponseEntity.noContent().build();
    }

}