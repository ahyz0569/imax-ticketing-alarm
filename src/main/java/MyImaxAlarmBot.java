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

            if (update.getMessage().getEntities()!=null){
                update.getMessage().getEntities().stream()
                        .filter(messageEntity -> messageEntity.getType().equals("bot_command"))
                        .forEach(messageEntity -> message.setText("안녕하세요. 이 봇은 용산CGV IMAX 상영관의 시간표를 안내해주는 봇입니다."));
            }

            logger.info(message.getText());

            if (message.getText()==null){
                message.setText(Crawler.timeTableMessage());
                logger.info(message.getText());
            }
//            message.setText(update.getMessage().getText());

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

    }
}
