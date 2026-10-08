package com.skinforge.upgrader.repository;


import com.skinforge.upgrader.model.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppConfigRepository
        extends JpaRepository<AppConfig, Long> {
}