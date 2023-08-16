package uz.tirgo.delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.tirgo.delivery.entity.enums.Language;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "users")
public class Seller {

    @Id
    private Long id;

    private String username;
    private String firstName;

    private String lastName;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Language language = Language.UZB;
    @ManyToOne
    private Role roles;

    private String login;
    private String password;
    private boolean enabled = true; //tizimdan foydalanish huquqi
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;

    public Seller(Message message) {
        this.id = message.getChatId();
        this.username = message.getFrom().getUserName();
        if (message.getContact() != null) {
            this.firstName = message.getContact().getFirstName();
            this.lastName = message.getContact().getLastName();
            this.phoneNumber = message.getContact().getPhoneNumber();
        } else {
            this.firstName = message.getFrom().getFirstName();
            this.lastName = message.getFrom().getLastName();
        }
    }
}
