package uz.tirgo.delivery.payload;

import uz.tirgo.delivery.entity.Location;

import java.util.HashMap;

public class KeyWords {

    public static final HashMap<Long, Location> supplierLocation = new HashMap<>();
    public static final HashMap<Long, Boolean> userLanguage = new HashMap<>();
    public static final HashMap<Long, Boolean> customerLanguage = new HashMap<>();
    public static final HashMap<Long, Boolean> supplierLanguage = new HashMap<>();
    public static final HashMap<Long, String> lastRequestSupplier = new HashMap<>();
    public static final HashMap<Long, String> lastRequestSeller = new HashMap<>();
    public static final String LANGUAGE_RUS = "Русский \uD83C\uDDF7\uD83C\uDDFA",
            LANGUAGE_UZB = "O'zbekcha \uD83C\uDDFA\uD83C\uDDFF",
            CONTACT_INPUT_UZB = "Telefon raqamingizni kiriting ☎️",
            CONTACT_INPUT_RUS = "Введите свой номер телефона ☎️",
            ACEPTED_ORDER_UZB = "Buyurtmani qabul qilish ✅",
            ACEPTED_ORDER_RUS = "Принять заказ ✅",
            DONT_ACCEPTED_ORDER_UZB = "Buyurtmani rad etish ❌",
            DONT_ACCEPTED_ORDER_RUS = "Отказаться от заказа ❌",
            CONFIRM_ORDER_RESPONSE_UZB = "Iltimos buyurtmani tasdiqlang",
            CONFIRM_ORDER_RESPONSE_RUS = "Пожалуйста, подтвердите заказ",
            MY_ORDERS_UZB = "Mening buyrutmalarim \uD83D\uDDD1",
            MY_ORDERS_RUS = "Мои заказы \uD83D\uDDD1",
            NEW_ORDER_UZB = "Buyurtma berish ➕",
            NEW_ORDER_RUS = "Создать новый заказ ➕",
            NEW_ORDER_SUPPLIER_UZB = "Yangi buyurtma so'rash",
            NEW_ORDER_SUPPLIER_RUS = "Запросить новый заказ",
            NEW_ORDER_RESPONSE_UZB = "Qabul qiluvchining telefon raqamini qoldiring: \n" +
                    "Qo'shimcha ma'lumotlarni ham kiritishingiz mumkin",
            NEW_ORDER_RESPONSE_RUS = "Введите контактные данные получателя: \n" +
                    "Так же можете ввести дополнительную информацию",
            CLOSE_ORDER_UZB = "Buyurtmani bekor qilish ❌",
            CLOSE_ORDER_RUS = "Отменить заказ ❌",
            CLOSE_ORDER_RESPONSE_UZB = "Buyurtma bekor qilindi",
            CLOSE_ORDER_RESPONSE_RUS = "Заказ отменен",
            SAVE_ORDER_UZB = "Buyurtmani rasmiylashtirish 📝",
            SAVE_ORDER_RUS = "Оформить заказ 📝",
            SAVE_ORDER_RESPONSE_UZB = "Buyurtma muvaffaqiyatli qabul qilindi",
            SAVE_ORDER_RESPONSE_RUS = "Заказ успешно получен",
            NOT_FOUND_ORDER_UZB = "Sizning buyurtmangiz topilmadi",
            NOT_FOUND_ORDER_RUS = "Ваш заказ не найден",
            ADD_INFO_UZB = "Ma'lumot qo'shildi \u200B",
            ADD_INFO_RUS = "Информация добавлена \u200B",
            HELLO_UZB = "Assalomu alaykum",
            HELLO_RUS = "Здравствуйте",
            NOT_FOUND_USER_UZB = "Siz ro'yxatdan o'tmagansiz, iltimos /start ni bosing",
            NOT_FOUND_USER_RUS = "Вы не зарегистрированы, пожалуйста, нажмите /start ",
            SELECT_MESSAGE_RUS = "Пожалуйста выберите",
            SELECT_MESSAGE_UZB = "Iltimos tanlang",
            CONFIRMATION_ORDER_UZB = "Barcha ma'lumotlarni kiritib bo'ldim✅",
            CONFIRMATION_ORDER_RUS = "Я ввел всю информацию✅",
            GO_BACK_UZB = "Ortga qaytish \uD83D\uDD04",
            GO_BACK_RUS = "Назад \uD83D\uDD04",
            Warning_ORDER_UZB = "Diqqat! buyurtmaning maksimal hajmi 70sm x 70sm x 70sm",
            Warning_ORDER_RUS = "Внимание! максимальный обьем заказа 70см x 70см x 70см",
            CHEKING_COUNTINUE_ORDER_MESSAGE_UZB = "Davom etish ✅",
            CHEKING_COUNTINUE_ORDER_MESSAGE_RUS = "Продолжить ✅",
            CHEKING_STOP_ORDER_MESSAGE_UZB = "Bekor qilish ❌",
            CHEKING_STOP_ORDER_MESSAGE_RUS = "Аннулирование ❌",
            SUPPLIER_FINISHED_ORDER_RUS = "Архив заказов",
            SUPPLIER_FINISHED_ORDER_UZB = "Buyurtmalar tarixi",
            SUPPLIER_INPROGRESS_ORDER_RUS = "Активные заказы",
            SUPPLIER_INPROGRESS_ORDER_UZB = "Faol buyurtmalar",
            SUBMIT_LOACTION_RUS = "Отправьте свое местоположение",
            SUBMIT_LOACTION_UZB = "Lokatsiyangizni yuboring",
            INPUT_SELLER_LOCATION_MESSAGE_RUS = "Ввод в письменной форме ✍️",
            INPUT_SELLER_LOCATION_MESSAGE_UZB = "Yozma shaklda kiritish ✍️",
            INPUT_SELLER_LOCATION_RUS = "Отправить своё местоположение",
            INPUT_SELLER_LOCATION_UZB = "Joylashgan joyingizni jo'natish",
            MY_IN_PROGRESS_ORDERS_RUS = "Заказы в ожидании",
            MY_IN_PROGRESS_ORDERS_UZB = "Kutayotgan buyurtmalarim",
            MY_TAKING_AWAY_ORDERS_RUS = "Заказы, принятые курьером",
            MY_TAKING_AWAY_ORDERS_UZB = "Kuryerdagi buyurtmalarim",
            MY_COMPLETE_ORDERS_RUS = "Мои доставленные заказы",
            MY_COMPLETE_ORDERS_UZB = "Mening yetkazib berilgan buyurtmalarim",
            CONFIRMATION_LOCATION_RUS = "Подтверждение адреса ✅",
            CONFIRMATION_LOCATION_UZB = "Manzilni tasdiqlash ✅",
            REENTER_CONFIRMATION_LOCATION_RUS = "Повторно введите адрес \uD83D\uDD04",
            REENTER_CONFIRMATION_LOCATION_UZB = "Manzilni boshqatdan kiritish \uD83D\uDD04";

}
