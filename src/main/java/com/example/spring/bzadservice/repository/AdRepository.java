package com.example.spring.bzadservice.repository;

import com.example.spring.bzadservice.entity.Ad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdRepository extends JpaRepository<Ad, Long> {
    // 추가적인 쿼리 메서드는 여기서 정의할 수 있습니다.
    @Query("SELECT a FROM Ad a ORDER BY a.id DESC") // 또는 Ad 엔티티의 createdAt 기준
    List<Ad> findAllByOrderByCreatedAtDesc();

}