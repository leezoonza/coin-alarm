package com.zoonza.coinalarm.member.internal.adapter.in;

import com.zoonza.coinalarm.common.response.ApiResponse;
import com.zoonza.coinalarm.member.internal.adapter.in.dto.LoginIdAvailabilityResponse;
import com.zoonza.coinalarm.member.internal.adapter.in.dto.MemberRegisterRequest;
import com.zoonza.coinalarm.member.internal.application.dto.MemberRegisterCommand;
import com.zoonza.coinalarm.member.internal.application.port.in.MemberCommandUseCase;
import com.zoonza.coinalarm.member.internal.application.port.in.MemberQueryUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.Instant;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final Clock clock;
    private final MemberQueryUseCase memberQueryUseCase;
    private final MemberCommandUseCase memberCommandUseCase;

    @GetMapping("/login-id/availability")
    public ApiResponse<LoginIdAvailabilityResponse> checkLoginIdAvailability(
            @RequestParam String loginId
    ) {
        boolean available = memberQueryUseCase.isLoginIdAvailable(loginId);

        return ApiResponse.success(new LoginIdAvailabilityResponse(available));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> register(
            @Valid @RequestBody MemberRegisterRequest request
    ) {
        Instant now = Instant.now(clock);

        MemberRegisterCommand command = request.toCommand(now);

        memberCommandUseCase.register(command);

        return ApiResponse.success();
    }
}
