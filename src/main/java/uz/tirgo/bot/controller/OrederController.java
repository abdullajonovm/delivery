package uz.tirgo.bot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.tirgo.bot.service.SupplierService;

@RestController(value = "/order")
@RequiredArgsConstructor
public class OrederController {

    private final SupplierService supplierService;

    @PostMapping("/send")
    public String sendOrder(@RequestParam Long userId, @RequestParam Long orderId) {
        return supplierService.sendOrder(userId, orderId);
    }

}
