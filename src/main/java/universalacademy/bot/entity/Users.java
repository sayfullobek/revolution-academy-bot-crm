package universalacademy.bot.entity;

import lombok.*;
import universalacademy.bot.entity.enums.RoleName;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String chatId;

    private String pId;

    private String firstName;

    @Enumerated(value = EnumType.STRING)
    private RoleName roleName;

    @Column(unique = true)
    private String number;

    @Column(nullable = false, unique = true)
    private String searchId;
}
