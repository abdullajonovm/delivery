package uz.tirgo.delivery.bot.supplier.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.tirgo.delivery.entity.Supplier;
import uz.tirgo.delivery.repository.SupplierRepository;


@Service
@RequiredArgsConstructor
public class SuplierService {

    private final SupplierRepository supplierRepository;

    public void saveUser(Message message) {
        Supplier supplier = new Supplier(message);
        if (message.hasContact()) {
            supplier.setPhoneNumber(message.getContact().getPhoneNumber());
        }
        supplierRepository.save(supplier);
    }

    public Supplier getById(Long id) {
        return supplierRepository.findById(id).get();
    }

}
