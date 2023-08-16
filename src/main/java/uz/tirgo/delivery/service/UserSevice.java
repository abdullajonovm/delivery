package uz.tirgo.delivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import uz.tirgo.delivery.entity.Seller;
import uz.tirgo.delivery.entity.Supplier;
import uz.tirgo.delivery.repository.SellerRepository;
import uz.tirgo.delivery.repository.SupplierRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserSevice {
    private final SupplierRepository supplierRepository;
    private final SellerRepository sellerRepository;

    public boolean existsById(String id) {
        return sellerRepository.existsById(Long.valueOf(id));
    }

    public Seller finById(Long supplierId) {
        Optional<Seller> byId = sellerRepository.findById(supplierId);
        return byId.get();
    }

    public Supplier finBySupplierId(Long supplierId) {
        Optional<Supplier> byId = supplierRepository.findById(supplierId);
        return byId.get();
    }

    public void saveSeller(Message message) {
        Seller seller = new Seller(message);
        sellerRepository.save(seller);
    }
}
