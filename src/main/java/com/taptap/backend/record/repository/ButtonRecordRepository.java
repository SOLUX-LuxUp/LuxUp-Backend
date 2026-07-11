package com.taptap.backend.record.repository;

import com.taptap.backend.record.entity.ButtonRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ButtonRecordRepository extends JpaRepository<ButtonRecord, Long> {

    @Modifying
    @Query("UPDATE ButtonRecord br SET br.deletedAt = CURRENT_TIMESTAMP " +
            "WHERE br.buttonId IN :buttonIds AND br.deletedAt IS NULL")
    void softDeleteByButtonIds(@Param("buttonIds") List<Long> buttonIds);

}