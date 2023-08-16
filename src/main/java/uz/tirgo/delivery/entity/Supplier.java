package uz.tirgo.delivery.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Message;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Supplier {
    @Id
    private Long id;
    private String lastName;
    private String firstName;
    private String phoneNumber;
    private String userName;

    public Supplier(Message message) {
        this.id = message.getChatId();
        this.lastName = message.getFrom().getLastName();
        this.firstName = message.getFrom().getFirstName();
        this.userName = message.getFrom().getUserName();
    }
}
