package com.skhuweather.repository;

import com.skhuweather.entity.SchoolNotice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchoolNoticeRepository extends JpaRepository<SchoolNotice, Long> {

    List<SchoolNotice> findTop8ByOrderByIdAsc();
}
