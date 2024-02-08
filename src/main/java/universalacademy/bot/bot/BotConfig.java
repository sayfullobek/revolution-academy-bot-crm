package universalacademy.bot.bot;

import org.telegram.telegrambots.meta.api.objects.User;
import universalacademy.bot.entity.Course;
import universalacademy.bot.entity.Group;
import universalacademy.bot.entity.Teacher;
import universalacademy.bot.entity.Users;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface BotConfig {

    String BOT_USERNAME = "Revolution_AcademyBot";

    String BOT_TOKEN = "6772497708:AAGjcxCOzlP_vBNEe-P8GXazR-cj3Oc20tg";

    Long ADMIN_CHAT_ID_1 = 5555360669L;

    Long ADMIN_CHAT_ID_2 = 1982587198L;

    Long ADMIN_CHAT_ID_3 = 5036811697L;

    Long ADMIN_CHAT_ID_4 = 5831831292L;

    Long TEACHER_CHAT_ID = 5013235753L;

    Map<Long, String> IS_PUPIL = new HashMap<>();

    Map<Long, String> PUPIL_NAME = new HashMap<>();

    Map<Long, String> PUPIL_SURNAME = new HashMap<>();

    Map<Long, String> PUPIL_NUMBER = new HashMap<>();

    Map<Long, String> PUPIL_SEARCH = new HashMap<>();

    Map<Long, String> IS_TEACHER = new HashMap<>();

    Map<Long, String> IS_DATA = new HashMap<>();

    Map<Long, String> IS_MESSAGE = new HashMap<>();

    Map<Long, String> IS_MESSAGE_P = new HashMap<>();

    Map<Long, String> TEACHER_NAME = new HashMap<>();

    Map<Long, String> TEACHER_SURNAME = new HashMap<>();

    Map<Long, String> TEACHER_CHATID = new HashMap<>();

    Map<Long, String> TEACHER_NUMBER = new HashMap<>();

    Map<Long, String> TEACHER_SEARCH = new HashMap<>();

    List<String> PUPIL_BTN = Arrays.asList("salom", "sgbdchs", "sdhfg", "sdvv");

    List<String> ADMIN_BTN = Arrays.asList("Kurslar", "Guruhlar", "O'qituvchilar", "Ota-ona", "O'quvchilar\uD83E\uDDD1\u200D\uD83D\uDCBC");

    List<String> ADMIN_PARENTS_BTN = Arrays.asList("Ota-ona qo'shish➕", "Orqaga 🔙");

    List<String> TEACHER_BTN = Arrays.asList("Guruhlarim〽️", "O'quvchilarim soni");

    List<String> USER_BTN = Arrays.asList("Kurslar 📚", "Bog'lanish 📲");

    List<String> TANLA_ADMIN_BTN = Arrays.asList("Kurs qo'shish ➕", "Kurs o'chirish ❌", "Kurslar ro'yxati 📋", "Orqaga 🔙");

    List<String> ADMIN_GROUP_BTN = Arrays.asList("Guruh yaratish➕", "Guruh o'chirish❌", "Guruhlar ro'yhati 📋", "Orqaga 🔙");

    List<String> ADMIN_TEACHER_BTN = Arrays.asList("O'qituvchi qo'shish➕", "O'qituvchi o'chirish❌", "O'qituvchilar ro'yxati 📋", "Orqaga 🔙");

    List<String> ADMIN_PUPIL_BTN = Arrays.asList("O'quvchi qo'shish➕", "O'quvchi o'chirish❌", "O'quvchilar ro'yxati 📋", "Orqaga 🔙");

    Map<Long, String> IS_PARENTS = new HashMap<>();

    Map<Long, Users> PARENTS_PUPIL = new HashMap<>();

    Map<Long, String> PARENTS_CHATID = new HashMap<>();

    Map<Long, String> IS_COURSE = new HashMap<>();

    Map<Long, String> COURSE_NAME = new HashMap<>();

    Map<Long, String> COURSE_BIO = new HashMap<>();

    Map<Long, Integer> COURSE_PRICE = new HashMap<>();

    Map<Long, String> IS_GROUP = new HashMap<>();

    Map<Long, String> GROUP_NAME = new HashMap<>();

    Map<Long, Teacher> GROUP_TEACHER = new HashMap<>();

    Map<Long, Users> GROUP_PUPILS = new HashMap<>();

    Map<Long, Course> GROUP_COURSE = new HashMap<>();

}
