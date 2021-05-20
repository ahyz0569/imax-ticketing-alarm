import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.TimerTask;

public class TimerCrawler extends TimerTask {

    private final String chatId;
    private final String bookMovieTitle;
    private final LocalDate bookDate;

    public TimerCrawler(String chatId, String bookMovieTitle, LocalDate bookDate) {
        this.chatId = chatId;
        this.bookMovieTitle = bookMovieTitle;
        this.bookDate = bookDate;
    }

    @Override
    public void run() {
        LocalDateTime localDateTime = LocalDateTime.now();
        System.out.println("RUN TimerCrawler at " + localDateTime.toString());

        // 예매 페이지 URL
        String URL = "http://www.cgv.co.kr/common/showtimes/iframeTheater.aspx" +
                "?areacode=01" +
                "&theatercode=0013" +
                "&screenratingcode=02" +
                "&regioncode=07" +
                "&date=";

        try {
            // connection 생성
            Document doc = Jsoup.connect(URL+bookDate).get();

            // Atrribute 탐색
            Elements scheduleList = doc.getElementsByClass("col-times");

            if (!scheduleList.isEmpty()){

                for (Element schedule : scheduleList) {
                    // title 검색
                    String movieTitle = schedule.select(".info-movie strong").text();

                    if (movieTitle.equals(bookMovieTitle)){
                        MyImaxAlarmBot alarmBot = new MyImaxAlarmBot();
                        alarmBot.alertOpenMovieTime(this.chatId, bookMovieTitle, bookDate);
                    }

                }// for문
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
