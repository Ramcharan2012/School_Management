package com.school.management.audit.entity;

import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Automatically called by Hibernate Envers before saving a new revision.
 * Reads the currently authenticated user's email from the SecurityContext
 * and stamps it on the revision record.
 *
 * Result: every row in *_AUD tables will know who made the change.
 */
public class CustomRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity revision = (CustomRevisionEntity) revisionEntity;
        String modifiedBy = "SYSTEM";

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                modifiedBy = auth.getName(); // returns email (username in UserDetails)
            }
        } catch (Exception ignored) {
            // fallback to SYSTEM if SecurityContext is unavailable
        }

        revision.setModifiedBy(modifiedBy);
    }
}
