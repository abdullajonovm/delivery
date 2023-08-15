package uz.tirgo.delivery.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Contact {
    @Id
    private Long id;
    private String lastName;
    private String firstName;
    private String phoneNumber;

    public Contact(org.telegram.telegrambots.meta.api.objects.Contact contact) {
        this.id = contact.getUserId();
        this.firstName = contact.getFirstName();
        this.lastName = contact.getLastName();
        this.phoneNumber = contact.getPhoneNumber();
    }

}
