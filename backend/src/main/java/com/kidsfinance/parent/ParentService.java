package com.kidsfinance.parent;

import com.kidsfinance.child.ChildProfile;
import com.kidsfinance.common.exception.ConflictException;
import com.kidsfinance.common.exception.ResourceNotFoundException;
import com.kidsfinance.family.ParentChildLinkRepository;
import com.kidsfinance.parent.dto.request.CreateParentRequest;
import com.kidsfinance.parent.dto.response.ParentChildResponse;
import com.kidsfinance.parent.dto.response.ParentResponse;
import com.kidsfinance.user.User;
import com.kidsfinance.user.UserRepository;
import com.kidsfinance.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParentService {

    private final UserRepository userRepository;
    private final ParentProfileRepository parentProfileRepository;
    private final ParentChildLinkRepository parentChildLinkRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ParentResponse createParent(CreateParentRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException(
                    "Username already exists"
            );
        }

        if (parentProfileRepository.existsByEmail(request.email())) {
            throw new ConflictException(
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

    @Transactional(readOnly = true)
    public List<ParentChildResponse> getMyChildren(Long userId) {

        ParentProfile parent = parentProfileRepository
                .findByUser_Id(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Parent profile not found for authenticated user"
                        )
                );

        return parentChildLinkRepository
                .findAllByParent_Id(parent.getId())
                .stream()
                .map(link -> {

                    ChildProfile child = link.getChild();

                    return new ParentChildResponse(
                            child.getId(),
                            child.getDisplayName(),
                            child.getAge(),
                            child.getXp(),
                            child.getLevel()
                    );
                })
                .toList();
    }
}