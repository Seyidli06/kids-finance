package com.kidsfinance.child;

import com.kidsfinance.child.dto.request.CreateChildRequest;
import com.kidsfinance.child.dto.response.ChildResponse;
import com.kidsfinance.common.exception.ConflictException;
import com.kidsfinance.common.exception.ResourceNotFoundException;
import com.kidsfinance.family.ParentChildLink;
import com.kidsfinance.family.ParentChildLinkRepository;
import com.kidsfinance.parent.ParentProfile;
import com.kidsfinance.parent.ParentProfileRepository;
import com.kidsfinance.user.User;
import com.kidsfinance.user.UserRepository;
import com.kidsfinance.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChildService {

    private final UserRepository userRepository;
    private final ChildProfileRepository childProfileRepository;
    private final ParentProfileRepository parentProfileRepository;
    private final ParentChildLinkRepository parentChildLinkRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ChildResponse createChildForParent(
            Long parentUserId,
            CreateChildRequest request
    ) {

        ParentProfile parent = parentProfileRepository
                .findByUser_Id(parentUserId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Parent profile not found for authenticated user"
                        )
                );

        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException(
                    "Username already exists"
            );
        }

        User user = User.builder()
                .username(request.username())
                .passwordHash(
                        passwordEncoder.encode(request.password())
                )
                .role(UserRole.CHILD)
                .build();

        User savedUser = userRepository.save(user);

        ChildProfile child = ChildProfile.builder()
                .user(savedUser)
                .displayName(request.displayName())
                .age(request.age())
                .build();

        ChildProfile savedChild =
                childProfileRepository.save(child);

        ParentChildLink link = ParentChildLink.builder()
                .parent(parent)
                .child(savedChild)
                .build();

        parentChildLinkRepository.save(link);

        return new ChildResponse(
                savedChild.getId(),
                savedUser.getId(),
                savedUser.getUsername(),
                savedChild.getDisplayName(),
                savedChild.getAge(),
                savedChild.getXp(),
                savedChild.getLevel()
        );
    }
}