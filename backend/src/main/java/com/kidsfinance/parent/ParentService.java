package com.kidsfinance.parent;

import com.kidsfinance.parent.dto.CreateParentRequest;
import com.kidsfinance.parent.dto.ParentResponse;
import com.kidsfinance.user.User;
import com.kidsfinance.user.UserRepository;
import com.kidsfinance.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParentService {

    private final UserRepository userRepository;
    private final ParentProfileRepository parentProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ParentResponse createParent(CreateParentRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalStateException(
                    "Username already exists"
            );
        }

        if (parentProfileRepository.existsByEmail(request.email())) {
            throw new IllegalStateException(
                    "Email already exists"
            );
        }

        User user = User.builder()
                .username(request.username())
                .passwordHash(
                        passwordEncoder.encode(request.password())
                )
                .role(UserRole.PARENT)
                .build();

        User savedUser = userRepository.save(user);

        ParentProfile parent = ParentProfile.builder()
                .user(savedUser)
                .fullName(request.fullName())
                .email(request.email())
                .build();

        ParentProfile savedParent =
                parentProfileRepository.save(parent);

        return new ParentResponse(
                savedParent.getId(),
                savedUser.getId(),
                savedUser.getUsername(),
                savedParent.getFullName(),
                savedParent.getEmail()
        );
    }
}