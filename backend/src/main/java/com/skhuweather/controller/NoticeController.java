package com.skhuweather.controller;

import com.skhuweather.entity.SchoolNotice;
import com.skhuweather.repository.SchoolNoticeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    @Autowired
    private SchoolNoticeRepository noticeRepo;

    @GetMapping
    public List<SchoolNotice> getNotices() {
        return noticeRepo.findTop8ByOrderByIdAsc();
    }
}
