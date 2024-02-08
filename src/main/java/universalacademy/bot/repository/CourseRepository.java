package universalacademy.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import universalacademy.bot.entity.Course;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Integer> {

    Course findCourseByName(String name);

    boolean existsCourseByNameEqualsIgnoreCase(String name);

    List<Course> findCoursesByName(String data);
}
