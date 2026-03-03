package com.school.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * School Management System - Main Application Entry Point
 *
 * Modules:
 * - user : User management (Admin, Teacher, Student accounts)
 * - admission : Student admission form and approval workflow
 * - academic : Departments, Academic Years, Classes, Subjects, Assignments
 * - teacher : Teacher profiles and management
 * - student : Student profiles and management
 * - attendance : Daily subject attendance tracking
 * - marks : Exams and student marks/grades
 * - fee : Fee structures and payment tracking
 * - notice : School-wide notices and announcements
 * - timetable : Weekly class timetables
 * - leave : Leave requests for students and teachers
 */
@SpringBootApplication
public class SchoolManagementApplication {
	public static void main(String[] args) {
		SpringApplication.run(SchoolManagementApplication.class, args);
	}
}
