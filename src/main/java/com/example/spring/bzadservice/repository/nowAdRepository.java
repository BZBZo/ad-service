package com.example.spring.bzadservice.repository;

import com.example.spring.bzadservice.dto.GetResolvesTimesRequestDTO;
import com.example.spring.bzadservice.entity.Ad;
import com.example.spring.bzadservice.entity.nowAd;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface nowAdRepository extends JpaRepository<nowAd, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM nowAd n WHERE n.adPosition = :adPosition")
    void deleteByAdPosition(@Param("adPosition") String adPosition);

    // 해당 ad_position에 맞는 데이터의 개수를 반환하는 메서드
    long countByAdPosition(String adPosition);
}