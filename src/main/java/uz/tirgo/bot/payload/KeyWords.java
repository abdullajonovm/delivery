package uz.tirgo.bot.payload;

import java.util.HashMap;

public interface KeyWords {

    HashMap<Long, Boolean> userLanguage = new HashMap<>();
    String LANGUAGE_RUS = "Русский \uD83C\uDDF7\uD83C\uDDFA",
            LANGUAGE_UZB = "O'zbekcha \uD83C\uDDFA\uD83C\uDDFF",
            CONTACT_INPUT_UZB = "Telefon raqamingizni kiriting ☎️",
            CONTACT_INPUT_RUS = "Введите свой номер телефона ☎️",
            ACEPTED_ORDER_UZB = "Buyurtmani qabu qilish ✅",
            ACEPTED_ORDER_RUS = "Принятие заказа ✅",
            DONT_ACCEPTED_ORDER_UZB = "Buyurtmani qabul qilmaslik ❌",
            DONT_ACCEPTED_ORDER_RUS = "Непринятие заказа ❌",
            CONFIRM_ORDER_RESPONSE_UZB = "Iltimos buyurtmani tasdiqlang",
            CONFIRM_ORDER_RESPONSE_RUS = "Пожалуйста, подтвердите заказ",
            MY_ORDERS_UZB = "Mening buyrutmalarim \uD83D\uDDD1",
            MY_ORDERS_RUS = "Мои заказы \uD83D\uDDD1",
            NEW_ORDER_UZB = "Yangi buyurtma yaratish ➕",
            NEW_ORDER_RUS = "Создать новый заказ ➕",
            NEW_ORDER_RESPONSE_UZB = "Buyurtma ma'lumotlarini kiritishliginigiz mumkin. Barcha ma'lumotlarni kiritib bo'lganinggizdan so'ng «Buyurtmani rasmiylashitirish» tugmasiga bosing",
            NEW_ORDER_RESPONSE_RUS = "Вы можете ввести всю информацию о заказе. После ввода всей информации нажмите на кнопку «Обработка заказа».",
            CLOSE_ORDER_UZB = "Buyurtmani bekor qilish",
            CLOSE_ORDER_RUS = "Отмена заказа",
            CLOSE_ORDER_RESPONSE_UZB = "Buyurtma bekor qilindi",
            CLOSE_ORDER_RESPONSE_RUS = "Заказ отменен",
            SAVE_ORDER_UZB = "Buyurtmani rasmiylashitirish 📝",
            SAVE_ORDER_RUS = "Обработка заказа 📝",
            SAVE_ORDER_RESPONSE_UZB = "Buyurtma muvaffaqiyatli qabul qilindi",
            SAVE_ORDER_RESPONSE_RUS = "Заказ получен успешно",
            NOT_FOUND_ORDER_UZB = "Sizning buyurtmangiz topilmadi",
            NOT_FOUND_ORDER_RUS = "Ваш заказ не найден",
            ADD_INFO_UZB = "Ma'lumot qo'shildi",
            ADD_INFO_RUS = "Добавлена \u200B\u200Bинформация",
            HELLO_UZB = "Assalomu alaykum",
            HELLO_RUS = "Привет",
            NOT_FOUND_USER_UZB = "Siz bizning mijozlar ro'yxati ichidan chiqmadingiz iltimos boshqatdan /start ni bosing",
            NOT_FOUND_USER_RUS = "Вы не удалены из нашего списка клиентов, нажмите /start еще раз",
            SELECT_MESSAGE_RUS = "Пожалуйста выберите",
            SELECT_MESSAGE_UZB = "Iltimos tanlang",
            CONFIRMATION_ORDER_UZB = "Barcha ma'lumotlarni kiritib bo'ldim. Buyurtmani rasmiylashtirish ✅",
            CONFIRMATION_ORDER_RUS = "Я ввел всю информацию. Обработка заказа ✅",
            GO_BACK_UZB = "Ortga qaytish \uD83D\uDD04",
            GO_BACK_RUS = "Возвращаться \uD83D\uDD04",
            Warning_ORDER_UZB = "Diqqat buyurtmaning maksimal hajmi hajmi 20sm x 20sm x 20sm dan katta bo'lishi mumkun emas",
            Warning_ORDER_RUS = "Обратите внимание, что максимальный размер заказа не может быть больше 20 см x 20 см x 20 см.",
            CHEKING_COUNTINUE_ORDER_MESSAGE_UZB = "Davom etish ✅",
            CHEKING_COUNTINUE_ORDER_MESSAGE_RUS = "Продолжать ✅",
            CHEKING_STOP_ORDER_MESSAGE_UZB = "Ortga qaytish ❌",
            CHEKING_STOP_ORDER_MESSAGE_RUS = "Аннулирование ❌";


}
