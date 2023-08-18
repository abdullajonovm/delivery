package uz.tirgo.delivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.tirgo.delivery.entity.Location;
import uz.tirgo.delivery.entity.Order;
import uz.tirgo.delivery.entity.Seller;
import uz.tirgo.delivery.entity.Supplier;
import uz.tirgo.delivery.entity.enums.OrderStatus;
import uz.tirgo.delivery.payload.KeyWords;
import uz.tirgo.delivery.repository.OrderRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    private final UserSevice userSevice;

    private final MessageService messageService;

    public List<Order> myAllOrders(String chatId) {
        return orderRepository.findAllByCustomerId(Long.valueOf(chatId));
    }

    public List<Order> supplierOrders(String supplierId, OrderStatus status) {
        return orderRepository.findAllBySupplierIdAndOrderStatus(Long.valueOf(supplierId), status);
    }

    public void createOrder(String chatId) {
        Order order = new Order();
        Seller seller = userSevice.finById(Long.valueOf(chatId)); // chat Id bilan Seller olib kelish
        deletOverdueOrder(Long.valueOf(chatId)); // cutomerni eski overdue da qolib ketgan habarlarini o'chirib yuborish, buyrtma berayotgan vaqtda yangitdan /start ni bosib yuborgan bo'lsa xatolik chiqmasligi uchun
        order.setCustomer(seller);
        order.setOrderStatus(OrderStatus.OVERDUE);
        orderRepository.save(order);
    }

    public void deletOverdueOrder(Long chatId) {
        for (Order order : orderRepository.findAllByOrderStatusAndCustomerId(OrderStatus.OVERDUE, chatId)) {
            messageService.deleteMessages(order.getId());
            orderRepository.deleteById(order.getId());
        }
    }

    public boolean saveOrder(Message message) {
        for (Order order : orderRepository.findAllByOrderStatusAndCustomerId(OrderStatus.OVERDUE, message.getChatId())) {
            order.setOrderStatus(OrderStatus.IN_PROGRESS);
            orderRepository.save(order);
            return true;
        }
        return false;
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
        Supplier supplier = userSevice.finBySupplierId(supplierId);
        order.setSupplier(supplier);
        orderRepository.save(order);
    }

    public boolean acceptedSupplierOrder(Message message) {
        if (message.getText().equals(KeyWords.DONT_ACCEPTED_ORDER_UZB) || message.getText().equals(KeyWords.DONT_ACCEPTED_ORDER_RUS)) {
            List<Order> bySupplierChatId = orderRepository.findAllBySupplierId(message.getChatId());
            for (Order order : bySupplierChatId) {
                if (order.getOrderStatus().equals(OrderStatus.IN_PROGRESS) || order.getOrderStatus().equals(OrderStatus.OVERDUE) || order.getOrderStatus().equals(OrderStatus.UN_COMPLETED)) {
                    order.setSupplier(null);
                }
                orderRepository.save(order);
            }
            return false;
        } else {
            for (Order order : orderRepository.findAllBySupplierIdAndOrderStatus(message.getChatId(), OrderStatus.UN_COMPLETED)) {
                order.setOrderStatus(OrderStatus.TAKING_AWAY);
                orderRepository.save(order);
                break;
            }
            return true;
        }
    }


    public List<Order> getOrders(Long chatId, OrderStatus status) {
        List<Order> all = orderRepository.findAllByOrderStatusAndCustomerId(status, chatId);

        return all;
    }

    public List<Order> getOrdersSupplier(Long chatId, OrderStatus status) {
        return orderRepository.findAllBySupplierIdAndOrderStatus(chatId, status);
    }

    public List<Order> getAllOrders(OrderStatus orderStatus) {
        return orderRepository.findByOrderStatus(orderStatus);
    }

    public void setSellerPoint(Message message, Location location) {
        KeyWords.lastRequestSeller.put(message.getChatId(), "setSellerPoint(Message message, Location location)");
        for (Order order : orderRepository.findAllByOrderStatusAndCustomerId(OrderStatus.OVERDUE, message.getChatId())) {
            order.setSellerPoint(location);
            orderRepository.save(order);
        }

    }

    public void setBuyerPoint(Message message, Location location) {
        KeyWords.lastRequestSeller.put(message.getChatId(), "setBuyerPoint(Message message, Location location)");
        for (Order order : orderRepository.findAllByOrderStatusAndCustomerId(OrderStatus.OVERDUE, message.getChatId())) {
            order.setBuyerPoint(location);
            orderRepository.save(order);
        }

    }

    public Order findOverdueOrder(Long chatId) {
        for (Order order : orderRepository.findAllByOrderStatusAndCustomerId(OrderStatus.OVERDUE, chatId)) {
            return order;
        }
        return null;
    }

    public void deletOrder(long orderId) {
        messageService.deleteMessages(orderId);
        orderRepository.deleteById(orderId);
    }

    public Order getById(Long id) {
        return orderRepository.findById(id).get();
    }

    public void acceptedSupplierOrder(Long orderId, Supplier supplier) {
        Optional<Order> byId = orderRepository.findById(orderId);
        Order order = byId.get();
        order.setOrderStatus(OrderStatus.UN_COMPLETED);
        order.setSupplier(supplier);
        orderRepository.save(order);
    }

    public void dontAcceptedOrder(String chatId) {
        for (Order order : orderRepository.findAllBySupplierIdAndOrderStatus(Long.valueOf(chatId), OrderStatus.UN_COMPLETED)) {
            order.setOrderStatus(OrderStatus.IN_PROGRESS);
            order.setSupplier(null);
            orderRepository.save(order);
        }
    }

    public Order acceptedSupplier(Message message) {
        if (message.getText().equals(KeyWords.DONT_ACCEPTED_ORDER_UZB) || message.getText().equals(KeyWords.DONT_ACCEPTED_ORDER_RUS)) {
            List<Order> bySupplierChatId = orderRepository.findAllBySupplierId(message.getChatId());
            for (Order order : bySupplierChatId) {
                if (order.getOrderStatus().equals(OrderStatus.IN_PROGRESS) || order.getOrderStatus().equals(OrderStatus.OVERDUE) || order.getOrderStatus().equals(OrderStatus.UN_COMPLETED)) {
                    order.setSupplier(null);
                }
                orderRepository.save(order);
            }
            return null;
        } else {
            for (Order order : orderRepository.findAllBySupplierIdAndOrderStatus(message.getChatId(), OrderStatus.UN_COMPLETED)) {
                order.setOrderStatus(OrderStatus.TAKING_AWAY);
                orderRepository.save(order);
                return order;
            }
            return null;
        }
    }


    public Order complateOrder(Long orderId) {
        Optional<Order> byId = orderRepository.findById(orderId);
        Order order = byId.get();
        order.setOrderStatus(OrderStatus.COMPLETE);
        return orderRepository.save(order);
    }
}
