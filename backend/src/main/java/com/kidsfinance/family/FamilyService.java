package com.kidsfinance.family;

import com.kidsfinance.child.ChildProfile;
import com.kidsfinance.child.ChildProfileRepository;
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
    public ParentChildLink linkParentToChild(
            Long parentProfileId,
            Long childProfileId
    ) {
        ParentProfile parent = parentProfileRepository
                .findById(parentProfileId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Parent profile not found: "
                                        + parentProfileId
                        )
                );

        ChildProfile child = childProfileRepository
                .findById(childProfileId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Child profile not found: "
                                        + childProfileId
                        )
                );

        boolean alreadyLinked =
                parentChildLinkRepository
                        .existsByParent_IdAndChild_Id(
                                parentProfileId,
                                childProfileId
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

        return parentChildLinkRepository.save(link);
    }
}