package universalacademy.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import universalacademy.bot.entity.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Teacher findTeacherByChatId(String chatId);

    boolean existsTeacherByNumberEqualsIgnoreCase(String number);

    boolean existsTeacherByChatIdEqualsIgnoreCase(String chatId);

    Teacher findTeacherByFirstName(String firstName);
}
