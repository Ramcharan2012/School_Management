package com.school.management.audit.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

/**
 * Custom Revision Entity for Hibernate Envers.
 *
 * Every time an @Audited entity is changed, Envers creates a row in the
 * REVINFO table using this class. We extend DefaultRevisionEntity (which
 * stores revisionNumber + timestamp) and add "modifiedBy" so we know
 * WHICH USER made the change.
 *
 * This creates a full audit trail: WHO changed WHAT and WHEN.
 */
@Entity
@Table(name = "REVINFO")
@RevisionEntity(CustomRevisionListener.class)
@Getter
@Setter
public class CustomRevisionEntity extends DefaultRevisionEntity {

    private static final long serialVersionUID = 1L;

    /**
     * The email/username of the user who triggered this revision.
     * Populated automatically by CustomRevisionListener from the SecurityContext.
     */
    private String modifiedBy;
}
