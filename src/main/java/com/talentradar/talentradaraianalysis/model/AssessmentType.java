package com.talentradar.talentradaraianalysis.model;

import lombok.Getter;


@Getter
public enum AssessmentType {
    SELF_ASSESSMENT(
        "Self-Assessment", 
        "Assessment completed by the individual themselves"
    ),
    MANAGER_FEEDBACK(
        "Manager Feedback", 
        "Assessment provided by the individual's manager"
    );

    private final String displayName;
    private final String description;

    AssessmentType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
