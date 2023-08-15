package uz.tirgo.delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.tirgo.delivery.entity.enums.OrderStatus;

@Entity(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Seller customer;

    @ManyToOne
    private Seller supplier;

    @ManyToOne
    private Supplier supplier1;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @OneToOne
    private Location sellerPoint;

    @OneToOne
    private Location buyerPoint;

}
