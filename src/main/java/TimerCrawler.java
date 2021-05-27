import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimerTask;
import java.util.logging.Logger;

public class TimerCrawler extends TimerTask {

    private final String chatId;            // 메세지를 전달할 Chat ID 
    private final String bookMovieTitle;    // 메세지를 통해 전달받은 영화명
    private final LocalDate bookDate;       // 메세지를 통해 전달받은 예매 알림 날짜

    private final static Logger logger = Logger.getGlobal();

    public TimerCrawler(String chatId, String bookMovieTitle, LocalDate bookDate) {
        this.chatId = chatId;
        this.bookMovieTitle = bookMovieTitle;
        this.bookDate = bookDate;
    }

    @Override
    public void run() {
        LocalDateTime localDateTime = LocalDateTime.now();
        logger.info("RUN TimerCrawler at " + localDateTime.toString());

        // 예매 페이지 URL
        String URL = "http://www.cgv.co.kr/common/showtimes/iframeTheater.aspx" +
                "?areacode=01" +
                "&theatercode=0013" +
                "&screenratingcode=02" +
                "&regioncode=07" +
                "&date=";

        try {
            // date 쿼리에 사용될 날짜를 형식에 맞춰 변경 ( 예) 2021-05-20 -> 20210520 )
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String dateQuery = bookDate.format(dateFormatter);

            // connection 생성
            Document doc = Jsoup.connect(URL+dateQuery).get();
            logger.info(dateQuery);

            // Atrribute 탐색
            Elements scheduleList = doc.getElementsByClass("col-times");

            if (!scheduleList.isEmpty()){   // 시간표가 업데이트 되었는지 판단

                for (Element schedule : scheduleList) {
                    // title 검색
                    String openMovieTitle = schedule.select(".info-movie strong").text();
                    logger.info(openMovieTitle);

                    // 업데이트 된 시간표의 영화명이 메세지로 전달받은 영화명과 일치하는 지 여부 판단
                    if (openMovieTitle.equals(bookMovieTitle)) {
                        MyImaxAlarmBot alarmBot = new MyImaxAlarmBot();
                        alarmBot.alertOpenMovieTime(this.chatId, bookMovieTitle, bookDate);
                    } else {    // 시간표는 업데이트 되었지만 시간표에 영화명이 메세지로 전달받은 영화명과 다름
                        MyImaxAlarmBot alarmBot = new MyImaxAlarmBot();
                        alarmBot.alertNotOpenMovie(this.chatId, bookMovieTitle, openMovieTitle, bookDate);
                    }

                }// for문
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
