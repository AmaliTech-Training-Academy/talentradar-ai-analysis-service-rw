package com.talentradar.talentradaraianalysis.repository;

import com.talentradar.talentradaraianalysis.model.AssessmentInput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AssessmentInputRepository extends JpaRepository<AssessmentInput, UUID> {
}
