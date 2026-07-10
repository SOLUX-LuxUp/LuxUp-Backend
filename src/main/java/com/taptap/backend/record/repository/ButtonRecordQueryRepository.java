package com.taptap.backend.button.repository;

import com.taptap.backend.record.entity.ButtonRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ButtonRecordQueryRepository extends JpaRepository<ButtonRecord, Long> {

    @Query("SELECT br.buttonId, MAX(br.recordedAt) FROM ButtonRecord br " +
            "WHERE br.buttonId IN :buttonIds AND br.deletedAt IS NULL " +
            "GROUP BY br.buttonId")
    List<Object[]> findLatestRecordedAtByButtonIds(@Param("buttonIds") List<Long> buttonIds);
}