package atl.web.user_service.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import atl.web.user_service.model.CardInfo;

@Repository
public interface CardInfoRepository extends JpaRepository<CardInfo,Long> {
    
    List<CardInfo> findByUserId(Long userId);
    Page<CardInfo> findByUserId(Long userId, Pageable pageable);
    Optional<CardInfo> findByNumber(String number);
    Boolean existsByNumber(String number);

    @Query("SELECT c FROM CardInfo c WHERE LOWER(c.holder) = LOWER(:holder)")
    Page<CardInfo> findByHolder(@Param("holder") String holder, Pageable pageable);

    @Query(nativeQuery = true,
    value = "select u from users u inner join card_info ci on ci.user_id = u.id where ci.expirationDate >= now()")
    Page<CardInfo> findAllActiveCardsByUserId(@Param("user_id") Long user_id, Pageable pageable);
}
