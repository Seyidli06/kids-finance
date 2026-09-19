package com.kidsfinance.family;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParentChildLinkRepository
        extends JpaRepository<ParentChildLink, Long> {

    boolean existsByParent_IdAndChild_Id(
            Long parentId,
            Long childId
    );

    boolean existsByParent_User_IdAndChild_Id(
            Long parentUserId,
            Long childProfileId
    );

    List<ParentChildLink> findAllByParent_Id(Long parentId);

    List<ParentChildLink> findAllByChild_Id(Long childId);
}