import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
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

        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage message = new SendMessage();
            message.setChatId(update.getMessage().getChatId().toString());

            List<String> imaxLineUp = Crawler.returnImaxLineUp();
            TreeMap<LocalDateTime, String> timeTableMap = Crawler.timeTableCrawler();

            String messageText = update.getMessage().getText();
            StringBuilder sb = new StringBuilder();

            /*
             * 영화 제목과 예매 날짜를 입력했을 때
             * 예) 스파이럴, 0519
             * */
            if (messageText.contains(",")) {    // 입력 양식 검사(유효성)
                String[] userInput = messageText.split(",");
                LocalDate userInputDate = Crawler.stringToLocalDate(userInput[1].trim());  // 이 부분도 사실 검사해야 함
                LocalDate now = LocalDate.now();

                if (imaxLineUp.contains(userInput[0]) && !userInputDate.isBefore(now)) {  // 영화가 예매 라인업에 존재하고, 입력한 날짜가 현재보다 미래인 경우

                    for (LocalDateTime playTime : timeTableMap.keySet()) {
                        LocalDate playDate = playTime.toLocalDate();

                        if (playDate.isEqual(userInputDate)) {   // 입력한 날짜에 이미 예매가 시작된 경우
                            sb.append("그 영화 이미 예매 시작함");
//                                .append("예매 시간표는 이거임");
//                                .append(Crawler.timeTableMessage()) // timeTableMessage() 메소드를 수정하여 시간표 메세지로 전달하자.
                            break;
                        }
                    }

                    if (sb.length() == 0) { // 입력한 날짜에 예매가 열리지 않은 경우
                        sb.append("ㅇㅋ 예매 시작하면 알려주겠음");

                        TimerTask task = new TimerCrawler(message.getChatId(), userInput[0], userInputDate);
                        Timer timer = new Timer();
                        timer.schedule(task, 1_000, 300_000);  // 1초 후 5분마다 task 수행
                    }

                } else if (ChronoUnit.DAYS.between(userInputDate, now) >= 15) {  // 다소 먼 미래인 경우
                    sb.append("14일 이내에 들어와서 다시 입력하셈. \n");
                    
                } else {    // 예매 예정작에 없는 경우
                    sb.append("그 영화는 예매 예정작에 없음 \n");

                    for (String movieTitle : imaxLineUp) {
                        sb.append(movieTitle).append("\n");
                    }
                    sb.append("위 영화 중에서 입력하셈");
                }
            } else {
                sb.append("입력이 잘못 되었음. 다시 입력 하셈");
            }

            message.setText(sb.toString());

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

    }

    /*
     * 예매시간표가 업데이트 되면 바로 메세지 전송해주는 메소드
     * */
    public void alertOpenMovieTime(String chatId, String movieTitle, LocalDate bookDate) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        
        message.setText(bookDate.toString() + "에 상영하는 " +movieTitle+ " 예매 시작됨 ㄱㄱㄱ");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
