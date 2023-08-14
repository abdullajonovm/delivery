package uz.tirgo.bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.tirgo.bot.entity.Order;
import uz.tirgo.bot.entity.User;
import uz.tirgo.bot.entity.enums.OrderStatus;
import uz.tirgo.bot.payload.KeyWords;
import uz.tirgo.bot.repository.OrderRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    private final UserSevice userSevice;

    private final MessageService messageService;

    public List<Order> myAllOrders(String chatId) {
        return orderRepository.findAllByCustomerChatId(chatId);
    }

    public void createOrder(Message message) {
        Order order = new Order();
        User customer = userSevice.finByChatId(String.valueOf(message.getChatId())); // chat Id bilan Customerni olib kelish
        deletOverdueOrder(message.getChatId()); // cutomerni eski overdue da qolib ketgan habarlarini o'chirib yuborish, buyrtma berayotgan vaqtda yangitdan /start ni bosib yuborgan bo'lsa xatolik chiqmasligi uchun
        order.setCustomer(customer);
        order.setOrderStatus(OrderStatus.OVERDUE);
        orderRepository.save(order);
    }

    private void deletOverdueOrder(Long chatId) {
        Optional<Order> byCustomerChatId = orderRepository.findByCustomerChatIdAndOrderStatus(String.valueOf(chatId), OrderStatus.OVERDUE);
        if (byCustomerChatId.isEmpty()) return;

        messageService.deleteMessages(byCustomerChatId.get().getId());
        orderRepository.deleteById(byCustomerChatId.get().getId());
    }

    public Boolean closeOrder(Message message) {
        String chatId = String.valueOf(message.getChatId());
        Optional<Order> byCustomerChatIdAndOrderStatus = orderRepository.findByCustomerChatIdAndOrderStatus(chatId, OrderStatus.OVERDUE);
        if (byCustomerChatIdAndOrderStatus.isPresent()) {
            Order order = byCustomerChatIdAndOrderStatus.get();
            order.setOrderStatus(OrderStatus.CANCELED);
            orderRepository.save(order);
            return true;
        } else {
            return false;
        }
    }

    public boolean saveOrder(Message message) {
        String chatId = String.valueOf(message.getChatId());
        Optional<Order> byCustomerChatIdAndOrderStatus = orderRepository.findByCustomerChatIdAndOrderStatus(chatId, OrderStatus.OVERDUE);
        if (byCustomerChatIdAndOrderStatus.isEmpty())
            return false;
        Order order = byCustomerChatIdAndOrderStatus.get();
        order.setOrderStatus(OrderStatus.IN_PROGRESS);
        orderRepository.save(order);
        return true;
    }

    public boolean deletOrder(Message message) {
        String chatId = String.valueOf(message.getChatId());
        Optional<Order> byCustomerChatIdAndOrderStatus = orderRepository.findByCustomerChatIdAndOrderStatus(chatId, OrderStatus.OVERDUE);
        if (byCustomerChatIdAndOrderStatus.isEmpty())
            return false;
        Order order = byCustomerChatIdAndOrderStatus.get();
        orderRepository.deleteById(order.getId());
        return true;
    }

    public Order findByChatId(String chatyId) {
        Optional<Order> byCustomerChatIdAndOrderStatus = orderRepository.findByCustomerChatIdAndOrderStatus(chatyId, OrderStatus.OVERDUE);
        return byCustomerChatIdAndOrderStatus.get();
    }


    public List<Order> inprogressOrders() {
        return orderRepository.findByOrderStatus(OrderStatus.IN_PROGRESS);
    }

    public Boolean existOrderById(Long orderId) {
        return orderRepository.existsById(orderId);
    }

    public void addSupplier(Long orderId, Long supplierId) {
        Optional<Order> byId = orderRepository.findById(orderId);
        Order order = byId.get();
        User user = userSevice.finById(supplierId);
        order.setSupplier(user);
        orderRepository.save(order);
    }

    public boolean acceptedSupplierOrder(Message message) {
        if (message.getText().equals(KeyWords.DONT_ACCEPTED_ORDER_UZB) || message.getText().equals(KeyWords.DONT_ACCEPTED_ORDER_RUS)) {
            List<Order> bySupplierChatId = orderRepository.findAllBySupplierChatId(String.valueOf(message.getChatId()));
            for (Order order : bySupplierChatId) {
                if (order.getOrderStatus().equals(OrderStatus.IN_PROGRESS) || order.getOrderStatus().equals(OrderStatus.OVERDUE)) {
                    order.setSupplier(null);
                }
                orderRepository.save(order);
            }
            return false;
        } else {
            Optional<Order> bySupplierChatId = orderRepository.findBySupplierChatId(String.valueOf(message.getChatId()));
            Order order = bySupplierChatId.get();
            order.setOrderStatus(OrderStatus.TAKING_AWAY);
            orderRepository.save(order);
            return true;
        }
    }
}
