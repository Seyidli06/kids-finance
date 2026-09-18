package com.kidsfinance.family;

import com.kidsfinance.child.ChildProfile;
import com.kidsfinance.child.ChildProfileRepository;
import com.kidsfinance.family.dto.request.LinkParentChildRequest;
import com.kidsfinance.family.dto.response.ParentChildLinkResponse;
import com.kidsfinance.parent.ParentProfile;
import com.kidsfinance.parent.ParentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private final ParentProfileRepository parentProfileRepository;
    private final ChildProfileRepository childProfileRepository;
    private final ParentChildLinkRepository parentChildLinkRepository;

    @Transactional
    public ParentChildLinkResponse linkParentToChild(
            LinkParentChildRequest request
    ) {

        ParentProfile parent = parentProfileRepository
                .findById(request.parentProfileId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Parent profile not found: "
                                        + request.parentProfileId()
                        )
                );

        ChildProfile child = childProfileRepository
                .findById(request.childProfileId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Child profile not found: "
                                        + request.childProfileId()
                        )
                );

        boolean alreadyLinked =
                parentChildLinkRepository
                        .existsByParent_IdAndChild_Id(
                                parent.getId(),
                                child.getId()
                        );

        if (alreadyLinked) {
            throw new IllegalStateException(
                    "Parent and child are already linked"
            );
        }

        ParentChildLink link = ParentChildLink.builder()
                .parent(parent)
                .child(child)
                .build();

        ParentChildLink savedLink =
                parentChildLinkRepository.save(link);

        return new ParentChildLinkResponse(
                savedLink.getId(),
                savedLink.getParent().getId(),
                savedLink.getChild().getId(),
                savedLink.getCreatedAt()
        );
    }
}