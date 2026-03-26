package com.school.management.transport.repository;

import com.school.management.transport.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    Optional<Route> findByRouteName(String routeName);
    List<Route> findByIsActiveTrue();
}
