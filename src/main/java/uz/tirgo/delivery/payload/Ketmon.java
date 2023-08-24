package uz.tirgo.delivery.payload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.tirgo.delivery.bot.supplier.config.SupplierBot;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;
@Service
public class Ketmon {
    @Autowired
    SupplierBot supplierBot;
//    public static void main(String[] args) {
//        Locale locale = new Locale("en", "US");
//
//        ResourceBundle labels = ResourceBundle.getBundle("i18n.MyBundle", locale);
//
//        System.out.println(labels.getString("label1"));
//    }
}
