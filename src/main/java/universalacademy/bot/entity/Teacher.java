package universalacademy.bot.entity;

import lombok.*;
import universalacademy.bot.entity.enums.RoleName;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "teachers")
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String chatId;

    @Column(nullable = false)
    private String firstName;

    @Enumerated(value = EnumType.STRING)
    private RoleName roleName;

    @Column(nullable = false)
    private String number;
}
