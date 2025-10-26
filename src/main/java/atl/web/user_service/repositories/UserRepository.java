package atl.web.user_service.repositories;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import atl.web.user_service.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Page<User> findAll(Pageable pageable);

    Optional<User> findUserByEmail(String email);

    Boolean existsByEmail(String email);

    Page<User> findByNameAndSurname(String name, String surname, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.birthDate < :birthDate")
    Page<User> findUsersOlder(@Param("birthDate") LocalDate birthDate, Pageable pageable);

    @Query(nativeQuery = true, value = "select u from users u inner join card_info ci on ci.user_id = u.id where count(ci.id) >= :minCards group by u.id")
    Page<User> findUsersWithMinCards(@Param("minCards") Integer minCards, Pageable pageable);
}
