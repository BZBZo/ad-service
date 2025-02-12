package com.example.spring.bzadservice.repository;

import com.example.spring.bzadservice.dto.GetResolvesTimesRequestDTO;
import com.example.spring.bzadservice.entity.Ad;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AdRepository extends JpaRepository<Ad, Long> {
    // 추가적인 쿼리 메서드는 여기서 정의할 수 있습니다.
    @Query("SELECT a FROM Ad a ORDER BY a.id DESC") // 또는 Ad 엔티티의 createdAt 기준
    List<Ad> findAllByOrderByCreatedAtDesc();

    @Query("SELECT new com.example.spring.bzadservice.dto.GetResolvesTimesRequestDTO(a.adStart, a.adEnd) " +
            "FROM Ad a " +
            "WHERE a.adPosition = :adPosition AND a.hits = 1")
    List<GetResolvesTimesRequestDTO> findReservedTimes(@Param("adPosition") String adPosition);


    Page<Ad> findAllByHits(int hits, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Ad a SET a.hits = 0 WHERE a.id = :id")
    void updateHitsToZero(@Param("id") Long id);  // Long 타입으로 수정

}