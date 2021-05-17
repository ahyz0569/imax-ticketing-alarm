import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.logging.Logger;

public class MyImaxAlarmBot extends TelegramLongPollingBot {
    private final static Logger logger = Logger.getGlobal();

    @Override
    public String getBotUsername() {
        return "DragonMountainIMAX_bot";
    }

    @Override
    public String getBotToken() {
        return System.getenv("TELEGRAM_BOT_TOKEN");
    }

    @Override
    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage message = new SendMessage();    // Create a SendMessage object with mandatory fields
            message.setChatId(update.getMessage().getChatId().toString());

//            message.setText(update.getMessage().getText());
            message.setText(Crawler.timeTableMessage());
            logger.info(message.getText());

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

    }
}
