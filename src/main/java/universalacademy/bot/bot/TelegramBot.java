package universalacademy.bot.bot;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import universalacademy.bot.entity.Course;
import universalacademy.bot.entity.Group;
import universalacademy.bot.entity.Teacher;
import universalacademy.bot.entity.Users;
import universalacademy.bot.entity.enums.RoleName;
import universalacademy.bot.repository.CourseRepository;
import universalacademy.bot.repository.GroupRepository;
import universalacademy.bot.repository.TeacherRepository;
import universalacademy.bot.repository.UserRepository;

import javax.ws.rs.NotFoundException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final GroupRepository groupRepository;
    private final TeacherRepository teacherRepository;

    @Override
    public String getBotUsername() {
        return BotConfig.BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BotConfig.BOT_TOKEN;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            String text = message.getText();
            Long chatId = message.getChatId();
            Long userId = message.getFrom().getId();
            if (userId.equals(BotConfig.ADMIN_CHAT_ID_1) || userId.equals(BotConfig.ADMIN_CHAT_ID_2) || userId.equals(BotConfig.ADMIN_CHAT_ID_3) || userId.equals(BotConfig.ADMIN_CHAT_ID_4)) {
                AdminCommands(chatId, text, message);
            } else {
                Teacher teacher = teacherRepository.findTeacherByChatId(chatId.toString());
                if (teacher != null) {
                    TeacherCommands(userId, text, teacher, message);
                } else {
                    UserCommands(chatId, text);
                }
            }
        } else if (update.hasCallbackQuery()) {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            String is_parents = BotConfig.IS_PARENTS.get(chatId);
            if (BotConfig.IS_GROUP.containsKey(chatId) && !BotConfig.IS_GROUP.get(chatId).equals("list-group") && !BotConfig.IS_GROUP.get(chatId).equals("list-student") && !"deleteGroup".equals(BotConfig.IS_DATA.get(chatId)) && !"deleteONE".equals(BotConfig.IS_PUPIL.get(chatId)) && !"deleteTeacher".equals(BotConfig.IS_DATA.get(chatId)) && !"deleteCourse".equals(BotConfig.IS_MESSAGE.get(chatId)) && !"deletePupil".equals(BotConfig.IS_DATA.get(chatId)) && !"list-course".equals(BotConfig.IS_COURSE.get(chatId)) && !BotConfig.IS_GROUP.get(chatId).equals("course") && !BotConfig.IS_GROUP.get(chatId).equals("teacher") && !BotConfig.IS_GROUP.get(chatId).equals("groupslar") && !"pupil".equals(is_parents) && (chatId.equals(BotConfig.ADMIN_CHAT_ID_1) || chatId.equals(BotConfig.ADMIN_CHAT_ID_2) || chatId.equals(BotConfig.ADMIN_CHAT_ID_3) || chatId.equals(BotConfig.ADMIN_CHAT_ID_4))) {
                CallbackQuery callbackQuery = update.getCallbackQuery();
                String data = callbackQuery.getData();
                Course course = courseRepository.findCourseByName(data);
                if (course != null) {
                    courseRepository.delete(course);
                    sendMsg(chatId, "Kurs o'chirildi");

                } else {
                    sendMsg(chatId, "Kurs mavjud emass");
                }
            } else if ("list-course".equals(BotConfig.IS_COURSE.get(chatId))) {
                CallbackQuery callbackQuery = update.getCallbackQuery();
                String data = callbackQuery.getData();

                List<Course> courses = courseRepository.findCoursesByName(data);
                if (!courses.isEmpty()) {
                    Course course = courses.get(0);
                    byte[] videoData = course.getPhoto();
                    InputStream inputStream = new ByteArrayInputStream(videoData);
                    InputFile inputFile = new InputFile().setMedia(inputStream, "photo.mp4");

                    execute(SendPhoto.builder()
                            .chatId(chatId)
                            .photo(inputFile)
                            .caption("#courses\n\n" +
                                    "Kurs nomi \uD83D\uDCD9 : " + course.getName() +
                                    "\nKurs haqida \uD83D\uDCD6 : " + course.getDescription() +
                                    "\nNarxi \uD83D\uDCB0: " + course.getPrice() + "\n\n" +
                                    "👉 https://universalacademy.uz/ ")
                            .build());

                    sendDeleteMsg(chatId, Integer.parseInt(BotConfig.IS_DATA.get(chatId)));

                    BotConfig.IS_DATA.clear();
                    BotConfig.IS_DATA.remove(chatId);
                    BotConfig.IS_COURSE.remove(chatId);
                    BotConfig.IS_COURSE.clear();
                } else {
                    sendMsg(chatId, "Kurslar mavjud emas.");
                }
            } else if ("deletePupil".equals(BotConfig.IS_DATA.get(chatId))) {
                CallbackQuery callbackQuery = update.getCallbackQuery();
                String data = callbackQuery.getData();

                Group group = groupRepository.findGroupByName(data);

                List<String> user = new ArrayList<>();
                for (Users pupil : group.getPupils()) {
                    user.add(pupil.getFirstName() + " : " + pupil.getNumber());
                }
                List<List<InlineKeyboardButton>> inlineButtonRows = getInlineButtonRows(user);

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                inlineKeyboardMarkup.setKeyboard(inlineButtonRows);

                sendReplyMarkupMsg(chatId, "O'chirmoqchi bo'lgan o'quvchini tanlang\n"
                        + "O'quvchilar ro'yxati \uD83E\uDDD1\u200D\uD83D\uDCBB:", inlineKeyboardMarkup);

                sendDeleteMsg(chatId, Integer.parseInt(BotConfig.IS_MESSAGE.get(chatId)));
                BotConfig.IS_GROUP.put(chatId, group.getName());
                BotConfig.IS_MESSAGE.clear();
                BotConfig.IS_DATA.clear();
                BotConfig.IS_PUPIL.put(chatId, "deleteONE");
            } else if ("deleteONE".equals(BotConfig.IS_PUPIL.get(chatId))) {
                CallbackQuery callbackQuery = update.getCallbackQuery();
                String data = callbackQuery.getData();
                Group group = groupRepository.findGroupByName(BotConfig.IS_GROUP.get(chatId));

                Users usersByFirstName = userRepository.findUsersByNumber(data);

                userRepository.findUsersByNumber(data);

                Integer id = usersByFirstName.getId();
                for (Users groupPupil : group.getPupils()) {
                    if (groupPupil.getId().equals(id)) {
                        group.getPupils().remove(groupPupil);
                        groupRepository.save(group);
                        sendMsg(chatId, "O'quvchi o'chirildi ❌.");
                        Users users = userRepository.findById(id).orElseThrow(() -> new NotFoundException("Zaybale"));
                        userRepository.delete(users);
                        break;
                    }
                }
                BotConfig.IS_GROUP.clear();
                BotConfig.IS_DATA.clear();
                BotConfig.IS_PUPIL.clear();
                BotConfig.IS_MESSAGE.clear();
            } else if ("deleteCourse".equals(BotConfig.IS_MESSAGE.get(chatId))) {
                CallbackQuery callbackQuery = update.getCallbackQuery();
                String data = callbackQuery.getData();
                try {
                    Course courseByName = courseRepository.findCourseByName(data);
                    courseRepository.delete(courseByName);
                    sendMsg(chatId, "Kurs o'chirildi ❌.");
                    BotConfig.IS_DATA.clear();
                    BotConfig.IS_MESSAGE.clear();
                } catch (Exception e) {
                    sendMsg(chatId, "Kurs o'chirilmadi. Kursdan guruhlarda foydalanilmoqda.");
                }
                BotConfig.IS_DATA.remove(chatId);
                BotConfig.IS_DATA.clear();
            } else if ("deleteTeacher".equals(BotConfig.IS_DATA.get(chatId))) {
                CallbackQuery callbackQuery = update.getCallbackQuery();
                String data = callbackQuery.getData();
                try {
                    Teacher teacherByFirstName = teacherRepository.findTeacherByFirstName(data);
                    teacherRepository.delete(teacherByFirstName);
                    sendMsg(chatId, "O'qituvchi o'chirildi.");
                    BotConfig.IS_DATA.clear();
                    BotConfig.IS_MESSAGE.clear();
                } catch (Exception e) {
                    sendMsg(chatId, "O'qituvchi o'chirilmadi.");
                }
                BotConfig.IS_DATA.remove(chatId);
                BotConfig.IS_DATA.clear();
            } else if ("list-student".equals(BotConfig.IS_GROUP.get(chatId))) {
                CallbackQuery callbackQuery = update.getCallbackQuery();
                String data = callbackQuery.getData();
                Users usersByFirstName = userRepository.findUsersByFirstName(data);
                if (usersByFirstName != null) {
                    sendMsg(chatId, "O'quvchi haqida ma'lumot ✅\n\n" +
                            "Ism familiya: " + usersByFirstName.getFirstName() + "\n" +
                            "Telefon: " + usersByFirstName.getNumber() + "\n" +
                            "Ota-ona id : " + usersByFirstName.getPId() + "\n" +
                            "Qidiruv Id: " + usersByFirstName.getSearchId());
                    sendDeleteMsg(chatId, Integer.parseInt(BotConfig.IS_MESSAGE.get(chatId)));
                    BotConfig.IS_MESSAGE.clear();
                    BotConfig.IS_GROUP.clear();
                } else {
                    sendMsg(chatId, "O'quvchi ma'lumotlari topilmadi.");
                }
            } else if ("list-group".equals(BotConfig.IS_GROUP.get(chatId))) {
                CallbackQuery callbackQuery = update.getCallbackQuery();
                String data = callbackQuery.getData();
                Group group = groupRepository.findGroupByName(data);
                if (group != null) {
                    if (group.getPupils().isEmpty()) {
                        sendMsg(chatId, "Guruh haqida ma'lumot✅\n\n" +
                                "Kurs nomi: " + group.getCourse().getName() +
                                "\nNomi \uD83C\uDFC6: " + group.getName() +
                                "\nO'qituvchi \uD83E\uDDD1\u200D\uD83C\uDFEB: " + group.getTeacher().getFirstName() +
                                "\nO'quvchilar soni \uD83E\uDDD1\u200D\uD83D\uDCBB: " + group.getPupils().size() + "\n");
                        sendDeleteMsg(chatId, Integer.parseInt(BotConfig.IS_MESSAGE.get(chatId)));
                        BotConfig.IS_MESSAGE.clear();
                    } else {
                        List<String> user = new ArrayList<>();
                        for (Users pupil : group.getPupils()) {
                            user.add(pupil.getFirstName());
                        }
                        List<List<InlineKeyboardButton>> inlineButtonRows = getInlineButtonRows(user);

                        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                        inlineKeyboardMarkup.setKeyboard(inlineButtonRows);

                        Integer messageId = execute(SendMessage.builder().chatId(chatId).text("Guruh haqida ma'lumot✅\n\n" +
                                "Kurs nomi : " + group.getCourse().getName() + "\n" +
                                "Nomi \uD83C\uDFC6: " + group.getName() + "\n" +
                                "O'qituvchi \uD83E\uDDD1\u200D\uD83C\uDFEB: " + group.getTeacher().getFirstName() + "\n"
                                + "O'quvchilar soni: " + group.getPupils().size() + "\n"
                                + "O'quvchilar ro'yxati \uD83E\uDDD1\u200D\uD83D\uDCBB:").replyMarkup(inlineKeyboardMarkup).build()).getMessageId();
                        sendDeleteMsg(chatId, Integer.parseInt(BotConfig.IS_MESSAGE.get(chatId)));

                        BotConfig.IS_PARENTS.put(chatId, messageId.toString());

                        BotConfig.IS_GROUP.clear();
                        BotConfig.IS_MESSAGE.clear();
                        BotConfig.IS_PUPIL.put(chatId, "msgParent");
                    }
                } else {
                    sendMsg(chatId, "Guruh ma'lumotlari topilmadi.");
                }
            } else if ("msgParent".equals(BotConfig.IS_PUPIL.get(chatId))) {
                CallbackQuery callbackQuery = update.getCallbackQuery();
                String data = callbackQuery.getData();
                Users usersByFirstName = userRepository.findUsersByFirstName(data);
                if (usersByFirstName != null && usersByFirstName.getPId() != null) {
                    long parentChatId = Long.parseLong(usersByFirstName.getPId());
                    sendDeleteMsg(chatId, Integer.parseInt(BotConfig.IS_PARENTS.get(chatId)));
                    sendMsg(chatId, "Yubormoqchi bo'lgan xabaringizni kiriting:");
                    BotConfig.IS_MESSAGE_P.put(chatId, "textP");
                    BotConfig.IS_PARENTS.clear();
                    BotConfig.IS_DATA.put(chatId, Long.toString(parentChatId));
                } else {
                    sendMsg(chatId, "Ota-ona ro'yxatdan o'tkazilmagan.");
                }
            } else if ("deleteGroup".equals(BotConfig.IS_DATA.get(chatId))) {
                String data = update.getCallbackQuery().getData();
                try {
                    Group groupByName = groupRepository.findGroupByName(data);

                    groupRepository.delete(groupByName);
                    sendMsg(chatId, "Guruh o'chirildi.");
                } catch (Exception e) {
                    sendMsg(chatId, "Guruh o'chirilmadi.");
                }
                BotConfig.IS_DATA.remove(chatId);
                BotConfig.IS_DATA.clear();
            } else if ("pupil".equals(is_parents)) {
                CallbackQuery callbackQuery = update.getCallbackQuery();
                String data = callbackQuery.getData();
                Users firstName = userRepository.findUsersByFirstName(data);
                BotConfig.IS_PARENTS.remove(chatId);
                BotConfig.PARENTS_PUPIL.put(chatId, firstName);
                firstName.setPId(BotConfig.PARENTS_CHATID.get(chatId));
                userRepository.save(firstName);
                BotConfig.IS_PARENTS.remove(chatId);
                getBtn(chatId.toString(), "Saqlandi ✔", BotConfig.ADMIN_PARENTS_BTN);
            } else if ("teacher".equals(BotConfig.IS_GROUP.get(chatId))) {
                CallbackQuery callbackQuery = update.getCallbackQuery();
                String data = callbackQuery.getData();

                List<Course> all = courseRepository.findAll();

                if (all.isEmpty()) {
                    sendMsg(chatId, "Kurslar mavjud emas.");
                } else {

                    List<String> teacherName = new ArrayList<>();

                    for (Course course : all) {
                        teacherName.add(course.getName());
                    }

                    List<List<InlineKeyboardButton>> inlineButtonRows = getInlineButtonRows(teacherName);

                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    inlineKeyboardMarkup.setKeyboard(inlineButtonRows);

                    execute(SendMessage.builder().chatId(chatId).text("Kurslar ro'yxati").replyMarkup(inlineKeyboardMarkup).build());
                }

                Teacher teacherByFirstName = teacherRepository.findTeacherByFirstName(data);

                BotConfig.IS_GROUP.remove(chatId);
                BotConfig.GROUP_TEACHER.put(chatId, teacherByFirstName);
                BotConfig.IS_GROUP.put(chatId, "course");


            } else if (BotConfig.IS_GROUP.get(chatId).equals("course")) {
                CallbackQuery callbackQuery = update.getCallbackQuery();
                String data = callbackQuery.getData();

                Course courseByName = courseRepository.findCourseByName(data);

                BotConfig.GROUP_COURSE.put(chatId, courseByName);

                Group build = Group.builder()
                        .name(BotConfig.GROUP_NAME.get(chatId))
                        .teacher(BotConfig.GROUP_TEACHER.get(chatId))
                        .course(BotConfig.GROUP_COURSE.get(chatId))
                        .build();
                groupRepository.save(build);
                sendMsg(chatId, "Saqlandi ✔");
                BotConfig.IS_GROUP.remove(chatId);
                BotConfig.IS_GROUP.clear();
            } else if (BotConfig.IS_GROUP.get(chatId).equals("groupslar")) {
                CallbackQuery callbackQuery = update.getCallbackQuery();
                String data = callbackQuery.getData();

                Users firstByOrderByIdDesc = userRepository.findFirstByOrderByIdDesc();
                BotConfig.GROUP_PUPILS.put(chatId, firstByOrderByIdDesc);

                Group groupByName = groupRepository.findGroupByName(data);
                groupByName.getPupils().add(BotConfig.GROUP_PUPILS.get(chatId));
                groupRepository.save(groupByName);
                sendMsg(chatId, "O'quvchi saqlandi.");
                BotConfig.IS_GROUP.remove(chatId);
                BotConfig.IS_GROUP.clear();
            }
        }
    }

    // panels
    public void AdminCommands(Long chatId, String text, Message message) throws TelegramApiException {
        if ("/start".equals(text)) {
            getBtn(chatId.toString(), "Assalomu alaykum admin !", BotConfig.ADMIN_BTN);
        } else if (BotConfig.IS_MESSAGE_P.size() > 0) {
            SendMsgParent(chatId, text, message);
        } else if ("/mychatid".equals(text)) {
            execute(SendMessage.builder().parseMode("Markdown").chatId(chatId).text("Sizning telegram id: " + "`" + chatId + "`").build());
        } else if ("/help".equals(text)) {
            sendMsg(chatId, "Siz adminsiz, bu buyruq faqat userlar uchun ishlaydi\uD83D\uDE41");
        } else if ("Kurslar".equals(text)) {
            getBtn(chatId.toString(), "Kurslar bo'limi", BotConfig.TANLA_ADMIN_BTN);
        } else if ("Guruhlar".equals(text)) {
            getBtn(chatId.toString(), "Guruhlar", BotConfig.ADMIN_GROUP_BTN);
        } else if ("Guruh o'chirish❌".equals(text)) {
            getGroups(chatId);
            BotConfig.IS_DATA.put(chatId, "deleteGroup");
        } else if ("O'qituvchi o'chirish❌".equals(text)) {
            List<Teacher> teachers = teacherRepository.findAll();
            if (teachers.isEmpty()) {
                sendMsg(chatId, "Hozirda o'qituvchilar mavjud emas");
            } else {
                List<String> teacherName = new ArrayList<>();
                for (Teacher teacher : teachers) {
                    teacherName.add(teacher.getFirstName());
                }
                List<List<InlineKeyboardButton>> inlineButtonRows = getInlineButtonRows(teacherName);
                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                inlineKeyboardMarkup.setKeyboard(inlineButtonRows);

                sendReplyMarkupMsg(chatId, "O'chirmoqchi bo'lgan o'qituvchini tanlang.", inlineKeyboardMarkup);
                BotConfig.IS_DATA.put(chatId, "deleteTeacher");
            }
        } else if (".".equals(text)) {
            System.out.println(".");
        } else if ("Guruh yaratish➕".equals(text)) {
            sendMsg(chatId, "Guruhning nomini kiriting:");
            BotConfig.IS_GROUP.put(chatId, "group-name");
        } else if ("O'qituvchilar".equals(text)) {
            getBtn(chatId.toString(), "O'qituvchilar", BotConfig.ADMIN_TEACHER_BTN);
        } else if ("O'qituvchilar ro'yxati \uD83D\uDCCB".equals(text)) {
            List<Teacher> teachers = teacherRepository.findAll();
            if (teachers.isEmpty()) {
                sendMsg(chatId, "Hozirda o'qituvchilar mavjud emas");
            } else {
                List<String> teacherName = new ArrayList<>();
                for (Teacher teacher : teachers) {
                    teacherName.add(teacher.getFirstName());
                }

                List<List<InlineKeyboardButton>> inlineButtonRows = getInlineButtonRows(teacherName);

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                inlineKeyboardMarkup.setKeyboard(inlineButtonRows);

                sendReplyMarkupMsg(chatId, "O'qituvchilar ro'yxati \uD83D\uDCCB", inlineKeyboardMarkup);
            }
        } else if ("Guruhlar ro'yhati 📋".equals(text)) {
            getGroups(chatId);
            BotConfig.IS_GROUP.put(chatId, "list-group");
        } else if ("O'quvchilar ro'yxati 📋".equals(text)) {
            getPupils(chatId);
            BotConfig.IS_GROUP.put(chatId, "list-student");
        } else if ("Kurs qo'shish ➕".equals(text)) {
            sendMsg(chatId, "Kursning nomini kiriting :");
            BotConfig.IS_COURSE.put(chatId, "course-name");
        } else if ("Ota-ona qo'shish➕".equals(text)) {
            sendMsg(chatId, "Ota-onani chat idisini kiriting");
            BotConfig.IS_PARENTS.put(chatId, "parents-id");
        } else if ("Ota-ona".equals(text)) {
            getBtn(chatId.toString(), "Ota-ona", BotConfig.ADMIN_PARENTS_BTN);
        } else if ("Kurslar ro'yxati 📋".equals(text)) {
            getCourse(chatId);
            BotConfig.IS_COURSE.put(chatId, "list-course");
        } else if ("O'quvchi o'chirish❌".equals(text)) {
            getGroups(chatId);
            BotConfig.IS_DATA.put(chatId, "deletePupil");
        } else if ("Kurs o'chirish ❌".equals(text)) {
            getCourse(chatId);
            BotConfig.IS_MESSAGE.put(chatId, "deleteCourse");
        } else if ("Orqaga 🔙".equals(text)) {
            BotConfig.IS_GROUP.clear();
            BotConfig.IS_TEACHER.clear();
            BotConfig.IS_PUPIL.clear();
            BotConfig.IS_PARENTS.clear();
            BotConfig.IS_COURSE.clear();
            getBtn(chatId.toString(), "Asosiy bo'lim", BotConfig.ADMIN_BTN);
        } else if ("O'quvchilar\uD83E\uDDD1\u200D\uD83D\uDCBC".equals(text)) {
            getBtn(chatId.toString(), "O'quvchilar\uD83E\uDDD1\u200D\uD83D\uDCBC", BotConfig.ADMIN_PUPIL_BTN);
        } else if ("O'quvchi qo'shish➕".equals(text)) {
            sendMsg(chatId, "O'quvchining ism familiasini kiriting:");
            BotConfig.IS_PUPIL.put(chatId, "name");
        } else if ("O'qituvchi qo'shish➕".equals(text)) {
            sendMsg(chatId, "O'qituvchining ism familiasini kiriting:");
            BotConfig.IS_TEACHER.put(chatId, "teacher-name");
        } else if (BotConfig.IS_PUPIL.size() > 0) {
            AddPupil(chatId, text);
        } else if (BotConfig.IS_COURSE.size() > 0) {
            AddCourse(chatId, text, message);
        } else if (BotConfig.IS_GROUP.size() > 0) {
            AddGroup(chatId, text);
        } else if (BotConfig.IS_TEACHER.size() > 0) {
            AddTeacher(chatId, text);
        } else if (BotConfig.IS_PARENTS.size() > 0) {
            AddParents(chatId, text);
        } else {
            sendMsg(chatId, "Siz mumkin bo'lmagan buyruq berdingiz ❌");
        }
    }

    public void TeacherCommands(Long chatId, String text, Teacher teacher, Message message) throws TelegramApiException {
        if ("/start".equals(text)) {
            getBtn(chatId.toString(), "Assalomu alaykum o'qituvchi !", BotConfig.TEACHER_BTN);
        } else if ("/mychatid".equals(text)) {
            execute(SendMessage.builder().parseMode("Markdown").chatId(chatId).text("Sizning telegram id: " + "`" + chatId + "`").build());
        } else if ("/help".equals(text)) {
            sendMsg(chatId, "Siz o'qituvchisiz, qandaydir muammoga uchragan bo'lsangiz adminga murojat qiling\uD83D\uDE15");
        } else if ("Guruhlarim〽️".equals(text)) {
            for (Group group : groupRepository.findAll()) {
                getTeacherGroups(chatId, teacher.getChatId(), group);
            }
        } else if ("Orqaga 🔙".equals(text)) {
            getBtn(chatId.toString(), "Asosiy bo'lim", BotConfig.TEACHER_BTN);
        } else if ("O'quvchilarim soni".equals(text)) {
            for (Group group : groupRepository.findAll()) {
                if (group.getTeacher().getChatId().equals(chatId.toString())) {
                    sendMsg(chatId, "Sizda hozirda \"" + group.getName() + "\" guruhida " + group.getPupils().size() + "ta o'quvchi mavjud😉");
                }
            }
        } else if (BotConfig.IS_MESSAGE_P.size() > 0) {
            SendMsgParent(chatId, text, message);
        } else {
            sendMsg(chatId, "Siz mumkin bo'lmagan buyruq berdingiz ❌");
        }
    }

    public void UserCommands(Long chatId, String text) throws TelegramApiException {
        if ("/start".equals(text)) {
            getBtn(chatId.toString(), "Assalomu alaykum❕ Revolution Academy o'quv markazining botiga hush kelibsiz\uD83D\uDE0A", BotConfig.USER_BTN);
        } else if ("/mychatid".equals(text)) {
            execute(SendMessage.builder().parseMode("Markdown").chatId(chatId).text("Sizning telegram id: " + "`" + chatId + "`").build());
        } else if ("/help".equals(text)) {
            sendMsg(chatId, "Qanaqadir muammoga uchragan bo'lsangiz admin bilan bog'laning.\n");
            sendInlineKeyboard(chatId);
        } else if ("Kurslar 📚".equals(text)) {
            getCourse(chatId);
            BotConfig.IS_COURSE.put(chatId, "list-course");
        } else if ("Bog'lanish 📲".equals(text)) {
            sendInlineKeyboard(chatId);
        } else if (BotConfig.IS_PUPIL.size() > 0) {
            SearchId(chatId, text);
        }else {
            sendMsg(chatId, "Siz mumkin bo'lmagan xabar yubordingiz❌.\n");
        }
    }
    // panels

    // add method
    private void AddParents(Long chatId, String text) {
        String is_parents = BotConfig.IS_PARENTS.get(chatId);
        if ("parents-id".equals(is_parents)) {
            try {
                List<Users> users = userRepository.findAll();
                if (users.isEmpty()) {
                    sendMsg(chatId, "Hozirda o'quvchilar mavjud emas.");
                } else {
                    List<String> pupilNames = new ArrayList<>();
                    for (Users users1 : users) {
                        pupilNames.add(users1.getFirstName());
                    }
                    List<List<InlineKeyboardButton>> inlineButtonRows = getInlineButtonRows(pupilNames);
                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    inlineKeyboardMarkup.setKeyboard(inlineButtonRows);

                    sendReplyMarkupMsg(chatId, "Qaysi o'quvchining ota-onasi tanlang \uD83D\uDC47", inlineKeyboardMarkup);

                    BotConfig.IS_PARENTS.remove(chatId);
                    BotConfig.PARENTS_CHATID.put(chatId, text);
                    BotConfig.IS_PARENTS.put(chatId, "pupil");
                }
            } catch (NumberFormatException e) {
                sendMsg(chatId, "Iltimos faqat son yuboring.");
            }
        }
    }

    private void AddCourse(Long chatId, String text, Message message) {
        String isCourseValue = BotConfig.IS_COURSE.get(chatId);
        if ("course-name".equals(isCourseValue)) {
            boolean b = courseRepository.existsCourseByNameEqualsIgnoreCase(text);
            if (!b) {
                sendMsg(chatId, "Kurs haqida kiriting:");

                BotConfig.IS_COURSE.remove(chatId);
                BotConfig.COURSE_NAME.put(chatId, text);

                BotConfig.IS_COURSE.put(chatId, "description");
            } else {
                sendMsg(chatId, "Bunday kurs oldindan mavjud ☝️");
            }
        } else if (isCourseValue != null && isCourseValue.equals("description")) {
            sendMsg(chatId, "Kurs narxini kiriting:");

            BotConfig.IS_COURSE.remove(chatId);
            BotConfig.COURSE_BIO.put(chatId, text);

            BotConfig.IS_COURSE.put(chatId, "price");
        } else if ("price".equals(isCourseValue)) {
            try {
                BotConfig.COURSE_PRICE.put(chatId, Integer.parseInt(text));
                BotConfig.IS_COURSE.remove(chatId);
                BotConfig.IS_COURSE.put(chatId, "photo");
                sendMsg(chatId, "Kurs rasmini yuboring:");
            } catch (NumberFormatException e) {
                sendMsg(chatId, "Iltimos faqat son yuboring");
            }
        } else if ("photo".equals(isCourseValue)) {
            if (message.hasPhoto()) {
                try {
                    List<PhotoSize> photoSizes = message.getPhoto();
                    String fileId = photoSizes.get(photoSizes.size() - 1).getFileId();
                    byte[] photoBytes = getFile(fileId);
                    Course newCourse = Course.builder()
                            .name(BotConfig.COURSE_NAME.get(chatId))
                            .description(BotConfig.COURSE_BIO.get(chatId))
                            .price(BotConfig.COURSE_PRICE.get(chatId))
                            .photo(photoBytes).build();
                    courseRepository.save(newCourse);
                    sendMsg(chatId, "Kurs muvaffaqiyatli saqlandi ✔️");
                } catch (Exception e) {
                    sendMsg(chatId, "Kurs saqlashda xatolik ro'y berdi.");
                }
                BotConfig.IS_COURSE.remove(chatId);
                BotConfig.IS_COURSE.clear();
            } else {
                sendMsg(chatId, "Iltimos faqat rasm yuboring.");
            }
        }
    }

    private void AddPupil(Long chatId, String text) {
        String value = BotConfig.IS_PUPIL.get(chatId);
        if (value != null && value.equals("name")) {
            sendMsg(chatId, "O'quvchining telefon raqamini kiriting:");
            BotConfig.IS_PUPIL.remove(chatId);
            BotConfig.PUPIL_NAME.put(chatId, text);
            BotConfig.IS_PUPIL.put(chatId, "phone");
        } else if (value != null && value.equals("phone")) {
            try {
                boolean b = userRepository.existsUsersByNumberEqualsIgnoreCase(text);
                if (!b) {
                    if (text.length() == 9) {
                        sendMsg(chatId, "O'quvchining qidiruv idsini kiriting:");

                        BotConfig.IS_PUPIL.remove(chatId);
                        BotConfig.PUPIL_NUMBER.put(chatId, text);

                        BotConfig.IS_PUPIL.put(chatId, "searchId");
                    } else {
                        sendMsg(chatId, "Telefon raqam xato.");
                    }
                } else {
                    sendMsg(chatId, "Bunday telefon raqam oldindan mavjud ☝️");
                }
            } catch (NumberFormatException e) {
                sendMsg(chatId, "Telefon raqam xato.");
            }
        } else if (value != null && value.equals("searchId")) {
            boolean b = userRepository.existsUsersBySearchIdEqualsIgnoreCase(text);
            if (!b) {
                BotConfig.PUPIL_SEARCH.put(chatId, text);
                Users users = Users.builder()
                        .chatId(chatId.toString())
                        .firstName(BotConfig.PUPIL_NAME.get(chatId))
                        .number(BotConfig.PUPIL_NUMBER.get(chatId))
                        .roleName(RoleName.PUPIL)
                        .searchId(BotConfig.PUPIL_SEARCH.get(chatId))
                        .build();
                userRepository.save(users);

                BotConfig.IS_GROUP.put(chatId, "groupslar");
                BotConfig.IS_PUPIL.remove(chatId);
                BotConfig.IS_PUPIL.clear();

                List<Group> groups = groupRepository.findAll();
                if (groups.isEmpty()) {
                    sendMsg(chatId, "Hozirda guruhlar mavjud emas");
                } else {
                    List<String> groupsNames = new ArrayList<>();
                    for (Group group : groups) {
                        groupsNames.add(group.getName());
                    }
                    List<List<InlineKeyboardButton>> inlineButtonRows = getInlineButtonRows(groupsNames);
                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    inlineKeyboardMarkup.setKeyboard(inlineButtonRows);

                    sendReplyMarkupMsg(chatId, "Barcha guruhlar", inlineKeyboardMarkup);
                }
            } else {
                sendMsg(chatId, "Bunday qidiruv id oldindan mavjud ☝️");
            }
        }
    }

    public void AddGroup(Long chatId, String text) {
        String isGroupValue = BotConfig.IS_GROUP.get(chatId);
        if ("group-name".equals(isGroupValue)) {
            boolean b = groupRepository.existsGroupByNameEqualsIgnoreCase(text);
            if (!b) {
                List<Teacher> all = teacherRepository.findAll();
                if (all.isEmpty()) {
                    sendMsg(chatId, "O'qituvchilar mavjud emas");
                } else {
                    List<String> teacherName = new ArrayList<>();
                    for (Teacher teacher : all) {
                        teacherName.add(teacher.getFirstName());
                    }
                    List<List<InlineKeyboardButton>> inlineButtonRows = getInlineButtonRows(teacherName);

                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    inlineKeyboardMarkup.setKeyboard(inlineButtonRows);

                    sendReplyMarkupMsg(chatId, "O'qituvchilar ro'yxati", inlineKeyboardMarkup);
                }

                BotConfig.IS_GROUP.remove(chatId);
                BotConfig.GROUP_NAME.put(chatId, text);
                BotConfig.IS_GROUP.put(chatId, "teacher");

            } else {
                sendMsg(chatId, "Bunday guruh oldindan mavjud ☝️");
            }
        }
    }

    private void AddTeacher(Long chatId, String text)  {
        String value = BotConfig.IS_TEACHER.get(chatId);
        if (value != null && value.equals("teacher-name")) {
            sendMsg(chatId, "O'qituvchining chat idsini kiriting:");

            BotConfig.IS_TEACHER.remove(chatId);
            BotConfig.TEACHER_NAME.put(chatId, text);

            BotConfig.IS_TEACHER.put(chatId, "teacher-chatId");
        } else if (value != null && value.equals("teacher-chatId")) {
            try {
                boolean b = teacherRepository.existsTeacherByChatIdEqualsIgnoreCase(text);
                if (!b) {
                    BotConfig.IS_TEACHER.remove(chatId);
                    BotConfig.TEACHER_CHATID.put(chatId, text);

                    BotConfig.IS_TEACHER.put(chatId, "phone");
                    sendMsg(chatId, "O'qituvchining telefon raqamini kiriting:");
                } else {
                    sendMsg(chatId, "Bunday chat id oldindan mavjud ☝️");
                }

            } catch (NumberFormatException e) {
                sendMsg(chatId, "Chat Id sondan iborat bo'lsin.");
            }
        } else if (value != null && value.equals("phone")) {
            try {
                if (text.length() == 9) {
                    boolean b = teacherRepository.existsTeacherByNumberEqualsIgnoreCase(text);
                    if (!b) {
                        sendMsg(chatId, "Saqlandi ✔");
                        BotConfig.TEACHER_NUMBER.put(chatId, text);
                        Teacher teacher = Teacher.builder()
                                .chatId(BotConfig.TEACHER_CHATID.get(chatId))
                                .firstName(BotConfig.TEACHER_NAME.get(chatId))
                                .number(BotConfig.TEACHER_NUMBER.get(chatId))
                                .roleName(RoleName.TEACHER).build();
                        teacherRepository.save(teacher);
                        BotConfig.IS_TEACHER.remove(chatId);
                        BotConfig.IS_TEACHER.clear();
                    } else {
                        sendMsg(chatId, "Bunday telefon raqam oldindan mavjud ☝️");
                    }
                } else {
                    sendMsg(chatId, "Telefon raqam xato.");
                }
            } catch (NumberFormatException e) {
                sendMsg(chatId, "Telefon raqam xato.");
            }
        }
    }
    // sdd method

    // button methods
    public void getBtn(String chatId, String text, List<String> btns) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        int tr = 0;
        for (int i = 0; i < btns.size() / 2; i++) {
            KeyboardRow row = new KeyboardRow();
            for (int j = 0; j < 2; j++) {
                KeyboardButton build = KeyboardButton.builder().text(text.equals(btns.get(tr)) ? btns.get(tr) + " 📌" : btns.get(tr)).build();
                row.add(build);
                tr++;
            }
            rows.add(row);
        }
        if (btns.size() % 2 != 0) {
            KeyboardRow row = new KeyboardRow();
            KeyboardButton build = KeyboardButton.builder().text(btns.get(btns.size() - 1)).build();
            row.add(build);
            rows.add(row);
        }
        replyKeyboardMarkup.setKeyboard(rows);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        try {
            execute(SendMessage.builder().chatId(chatId).text(text).replyMarkup(replyKeyboardMarkup).build());
        } catch (TelegramApiException e) {
            System.err.println("Not Btn");
        }
    }

    public List<List<InlineKeyboardButton>> getInlineButtonRows(List<String> data) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        int length = data.size() % 2 != 0 ? data.size() - 1 : data.size();
        for (int i = 0; i < length; i += 2) {
            List<InlineKeyboardButton> inlineButton = new ArrayList<>();
            if (data.get(i).split(" : ").length == 2) {
                inlineButton.add(getInlineButton(data.get(i).split(" : ")[0], data.get(i).split(" : ")[1]));
                inlineButton.add(getInlineButton(data.get(i + 1).split(" : ")[0], data.get(i + 1).split(" : ")[1]));
            } else {
                inlineButton.add(getInlineButton(data.get(i), data.get(i)));
                inlineButton.add(getInlineButton(data.get(i + 1), data.get(i + 1)));
            }
            rows.add(inlineButton);
        }
        if (data.size() % 2 != 0) {
            String text = data.get(data.size() - 1);
            if (text.split(" : ").length == 2) {
                rows.add(Collections.singletonList(getInlineButton(text.split(" : ")[0], text.split(" : ")[1])));
            } else {
                rows.add(Collections.singletonList(getInlineButton(text, text)));
            }
        }
        return rows;
    }

    public InlineKeyboardButton getInlineButton(String text, String callback) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setCallbackData(callback);
        inlineKeyboardButton.setText(text);
        return inlineKeyboardButton;
    }

    public InlineKeyboardMarkup Admins() {
        List<List<InlineKeyboardButton>> lists = new ArrayList<>();

        List<InlineKeyboardButton> link = new ArrayList<>();
        List<InlineKeyboardButton> ceo = new ArrayList<>();
        List<InlineKeyboardButton> manager = new ArrayList<>();

        InlineKeyboardButton linkButton = new InlineKeyboardButton();
        InlineKeyboardButton ceoButton = new InlineKeyboardButton();
        InlineKeyboardButton managerButton = new InlineKeyboardButton();

        linkButton.setText("Admin");
        linkButton.setUrl("https://t.me/MUHAMMADALI_ABDUMANNONOV");

        ceoButton.setText("Direktor");
        ceoButton.setUrl("https://t.me/Sayfullo_dev");

        managerButton.setText("Menejer");
        managerButton.setUrl("https://t.me/MukhammadiyevMustafo");


        link.add(linkButton);
        ceo.add(ceoButton);
        manager.add(managerButton);

        lists.add(ceo);
        lists.add(manager);
        lists.add(link);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(lists);
        return markup;
    }

    private void sendInlineKeyboard(Long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = Admins();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Biz bilan bog'lanish uchun \uD83D\uDCDE \n☎ Telefon raqam: +998918103246");

        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    //button methods

    //get methods
    private void getPupils(Long chatId) throws TelegramApiException {
        List<Users> all = userRepository.findAll();
        if (all.isEmpty()) {
            sendMsg(chatId, "Afsuski o'quvchilar mavjud emas 😔");
        } else {
            List<String> userNames = new ArrayList<>();
            for (Users users : all) {
                userNames.add(users.getFirstName());
            }
            List<List<InlineKeyboardButton>> inlineButtonRows = getInlineButtonRows(userNames);

            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            inlineKeyboardMarkup.setKeyboard(inlineButtonRows);

            Integer all_pupils = execute(SendMessage.builder().chatId(chatId).text("Barcha o'quvchilar").replyMarkup(inlineKeyboardMarkup).build()).getMessageId();
            BotConfig.IS_MESSAGE.put(chatId, all_pupils.toString());
        }
    }

    private void getTeacherGroups(Long chatId, String teacherChatId, Group groups) throws TelegramApiException {
        BotConfig.IS_GROUP.put(chatId, "list-group");
        if (groups == null) {
            sendMsg(chatId, "Hozirda guruhlar mavjud emas");
        } else {
            if (groups.getTeacher().getChatId().equals(teacherChatId)) {
                List<String> groupsNames = new ArrayList<>();
                groupsNames.add(groups.getName());
                List<List<InlineKeyboardButton>> inlineButtonRows = getInlineButtonRows(groupsNames);

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                inlineKeyboardMarkup.setKeyboard(inlineButtonRows);

                Integer sizning_guruhlariz = execute(SendMessage.builder().chatId(chatId).text("Sizning guruhlariz").replyMarkup(inlineKeyboardMarkup).build()).getMessageId();
                BotConfig.IS_MESSAGE.put(chatId, sizning_guruhlariz.toString());
            }

        }
    }

    private void getGroups(Long chatId) throws TelegramApiException {
        List<Group> groups = groupRepository.findAll();
        if (groups.isEmpty()) {
            sendMsg(chatId, "Afsuski guruhlar mavjud emas 😔");
        } else {
            List<String> groupsNames = new ArrayList<>();
            for (Group group : groups) {
                groupsNames.add(group.getName());
            }
            List<List<InlineKeyboardButton>> inlineButtonRows = getInlineButtonRows(groupsNames);

            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            inlineKeyboardMarkup.setKeyboard(inlineButtonRows);

            Integer all_groups = execute(SendMessage.builder().chatId(chatId).text("Barcha guruhlar").replyMarkup(inlineKeyboardMarkup).build()).getMessageId();
            BotConfig.IS_MESSAGE.put(chatId, all_groups.toString());
        }
    }

    private void getCourse(Long chatId) throws TelegramApiException {
        List<Course> courses = courseRepository.findAll();
        if (courses.isEmpty()) {
            sendMsg(chatId, "Hozirda kurslar mavjud emas.");
        } else {
            List<String> groupsNames = new ArrayList<>();
            for (Course course : courses) {
                groupsNames.add(course.getName());
            }
            List<List<InlineKeyboardButton>> inlineButtonRows = getInlineButtonRows(groupsNames);

            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            inlineKeyboardMarkup.setKeyboard(inlineButtonRows);

            Integer barcha_kurslar = execute(SendMessage.builder().chatId(chatId).text("Hozirda mavjud kurslar ro'yxati.").replyMarkup(inlineKeyboardMarkup).build()).getMessageId();
            BotConfig.IS_DATA.put(chatId, barcha_kurslar.toString());
        }
    }
    //get methods

    //other methods
    public void SearchId(Long chatId, String text) {
        if (BotConfig.IS_PUPIL.get(chatId).equals("search")) {
            Users usersBySearchId = userRepository.findUsersBySearchId(text);
            if (usersBySearchId != null) {
                sendMsg(chatId, "Ism: " + usersBySearchId.getFirstName());
                BotConfig.IS_PUPIL.remove(chatId);
                BotConfig.IS_PUPIL.clear();
            } else {
                BotConfig.IS_PUPIL.remove(chatId);
                BotConfig.IS_PUPIL.clear();
                sendMsg(chatId, "Bunday o'quvchi topilmadi 😔");
            }
        }
    }

    private void SendMsgParent(Long chatId, String text, Message message) {
        try {
            String userName = message.getFrom().getUserName();
            String value = BotConfig.IS_MESSAGE_P.get(chatId);
            if (value.equals("textP")) {
                BotConfig.IS_TEACHER.put(chatId, text);
                sendMsg(Long.parseLong(BotConfig.IS_DATA.get(chatId)), "\uD83D\uDDDE Yangi bildirishnoma \uD83D\uDD14\n\n" + BotConfig.IS_TEACHER.get(chatId) + "\n\n \uD83D\uDCF1 Yuboruvchi: @" + userName);
                sendMsg(chatId, "Xabar yuborildi");
                BotConfig.IS_DATA.clear();
                BotConfig.IS_MESSAGE_P.clear();
                BotConfig.IS_PUPIL.clear();
                BotConfig.IS_TEACHER.clear();
            } else {
                sendMsg(chatId, "Xabar bormadi");
            }
        } catch (Exception e) {
            sendMsg(chatId, "Xabar bormadi");
        }
    }

    @SneakyThrows
    private byte[] getFile(String fileId) {
        try {
            GetFile getFileMethod = new GetFile();
            getFileMethod.setFileId(fileId);
            org.telegram.telegrambots.meta.api.objects.File file = execute(getFileMethod);

            try (InputStream inputStream = new URL(file.getFileUrl(getBotToken())).openStream()) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                return byteArrayOutputStream.toByteArray();
            }
        } catch (TelegramApiException e) {
            throw e;
        } catch (Exception e) {
            throw new TelegramApiException("Error retrieving file", e);
        }
    }

    public void sendMsg(Long chatId, String text) {
        try {
            execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .build());
        } catch (Exception e) {
            System.out.println("No Text");
        }
    }

    public void sendDeleteMsg(Long chatId, Integer messageId) {
        try {
            execute(DeleteMessage.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .build());
        } catch (Exception e) {
            System.err.println("No Id");
        }
    }

    public void sendReplyMarkupMsg(Long chatId, String text, InlineKeyboardMarkup inlineKeyboardMarkup) {
        try {
            execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(inlineKeyboardMarkup)
                    .build()).getMessageId();
        } catch (Exception e) {
            System.err.println("Not text reply");
        }
    }
    //other methods
}
