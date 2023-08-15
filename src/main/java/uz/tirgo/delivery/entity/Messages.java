package uz.tirgo.delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Messages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Seller from;

    @ManyToOne
    private Order order;

    @NotNull
    @Column(name = "chat_id", nullable = false)
    private String chatId;


    @Column(name = "text")
    private String text;
    @Column(name = "file_url")
    private String fileURL;

    @Column(name = "caption")
    private String caption;

    @Column(name = "date")
    private Integer date;

    @ManyToOne
    private Contact contact;

    @OneToOne
    private Location location;


    public Messages(Order order, org.telegram.telegrambots.meta.api.objects.Message message) {
        this.order = order;
        this.caption = message.getCaption();
        this.text = message.getText();
        this.chatId = String.valueOf(message.getChatId());
        this.date = message.getDate();
    }
}
