package kr.farmily.api.user.service;

import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import kr.farmily.api.user.domain.User;
import kr.farmily.api.user.dto.AccountUpdateRequest;
import kr.farmily.api.user.dto.MyPageResponse;
import kr.farmily.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepository;

    @Transactional
    public MyPageResponse.Account update(long userId, AccountUpdateRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.updateProfile(req.name(), req.email());
        return new MyPageResponse.Account(user.getName(), null, user.getEmail());
    }
}
