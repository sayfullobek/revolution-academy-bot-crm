package universalacademy.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import universalacademy.bot.entity.Group;

public interface GroupRepository extends JpaRepository<Group, Integer> {
    Group findGroupByName(String name);

    boolean existsGroupByNameEqualsIgnoreCase(String name);
}
