package universalacademy.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import universalacademy.bot.entity.Users;

public interface UserRepository extends JpaRepository<Users, Integer> {
    Users findUsersBySearchId(String searchId);

    Users findUsersByChatId(String searchId);

    boolean existsUsersByNumberEqualsIgnoreCase(String number);

    boolean existsUsersBySearchIdEqualsIgnoreCase(String searchId);

    Users findUsersByFirstName(String firstName);

    Users findFirstByOrderByIdDesc();

    Users findUsersByNumber(String number);

}
