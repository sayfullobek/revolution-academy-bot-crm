package universalacademy.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import universalacademy.bot.bot.TelegramBot;
import universalacademy.bot.repository.CourseRepository;
import universalacademy.bot.repository.GroupRepository;
import universalacademy.bot.repository.TeacherRepository;
import universalacademy.bot.repository.UserRepository;

@SpringBootApplication
public class BotApplication {

    private static UserRepository userRepository;
    private static CourseRepository courseRepository;
    private static GroupRepository groupRepository;
    private static TeacherRepository teacherRepository;

    public BotApplication(UserRepository userRepository, CourseRepository courseRepository, GroupRepository groupRepository, TeacherRepository teacherRepository) {
        BotApplication.userRepository = userRepository;
        BotApplication.courseRepository = courseRepository;
        BotApplication.groupRepository = groupRepository;
        BotApplication.teacherRepository = teacherRepository;
    }


    public static void main(String[] args) {
        try {
            SpringApplication.run(BotApplication.class, args);
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new TelegramBot(userRepository, courseRepository, groupRepository, teacherRepository));
        } catch (TelegramApiException err) {
            System.err.println("Telegram API exception: " + err.getMessage());
        }
    }
}

