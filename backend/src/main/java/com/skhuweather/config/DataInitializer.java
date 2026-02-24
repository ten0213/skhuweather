package com.skhuweather.config;

import com.skhuweather.entity.SchoolNotice;
import com.skhuweather.repository.SchoolNoticeRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    @Autowired
    private SchoolNoticeRepository noticeRepository;

    @PostConstruct
    public void init() {
        if (noticeRepository.count() == 0) {
            noticeRepository.save(new SchoolNotice("2024학년도 후기 학위수여식 안내", "80001"));
            noticeRepository.save(new SchoolNotice("2025학년도 1학기 수강신청 안내", "80002"));
            noticeRepository.save(new SchoolNotice("2025학년도 전기 장학생 선발 안내", "80003"));
            noticeRepository.save(new SchoolNotice("도서관 이용 시간 변경 안내", "80004"));
            noticeRepository.save(new SchoolNotice("학생 식당 운영 시간 안내", "80005"));
            noticeRepository.save(new SchoolNotice("2025학년도 1학기 현장실습 신청 안내", "80006"));
            noticeRepository.save(new SchoolNotice("캠퍼스 내 불법 주정차 단속 안내", "80007"));
            noticeRepository.save(new SchoolNotice("성공회대학교 SW교육원 특강 안내", "80008"));
        }
    }
}
