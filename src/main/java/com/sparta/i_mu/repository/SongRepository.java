package com.sparta.i_mu.repository;

import com.sparta.i_mu.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SongRepository extends JpaRepository<Song, Long> {

    Optional<Song> findBySongNum(String songNum);

    @Query("SELECT link.song FROM PostSongLink link JOIN link.post p WHERE p.category.id = :categoryId GROUP BY link.song ORDER BY COUNT(link.song) DESC")
    List<Song> findByCategoryIdOrderByPostCountDesc(Long categoryId);
}
