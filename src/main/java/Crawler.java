import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.TreeMap;

public class Crawler {

//    private final static Logger logger = LoggerFactory.getLogger(Crawler.class.getName());
    private final static Logger logger = Logger.getGlobal();

    public static TreeMap<LocalDateTime, String> timeTableCrawler() {

        /*
        상영시간표 정보를 담을 HashMap
        - Key: 상영날짜+상영시간(12자리, 숫자형태의 String 값 -> LocalDateTime 타입으로 변경)
        - Value: 영화 제목
        */
        TreeMap<LocalDateTime, String> imaxScreenMoviesMap = new TreeMap<>();

        LocalDate now = LocalDate.now();
        String strDate = localDateToString(now);

        // 예매 페이지 URL
        String URL = "http://www.cgv.co.kr/common/showtimes/iframeTheater.aspx" +
                "?areacode=01" +
                "&theatercode=0013" +
                "&screenratingcode=02" +
                "&regioncode=07" +
                "&date=";          // 이 부분이 가변형태의 데이터가 되어 삽입되어야 한다.

        int i = 1;  // plusDate에 사용할 인자
        boolean duplCheck = true;   // key 중복일 경우 while문을 빠져나오기 위해 사용
        while (duplCheck) {

            try {
                // connection 생성
                Document doc = Jsoup.connect(URL+strDate).get();
                logger.info(strDate);

                // Atrribute 탐색
                Elements scheduleList = doc.getElementsByClass("col-times");

                if (scheduleList.isEmpty() && stringToLocalDateTime(strDate, "0000").isAfter(now.atStartOfDay())){  // 조회 아에 안되는 경우
                    duplCheck = false;
                    break;
                }

                for (Element schedule : scheduleList) {
                    // title 검색
                    String movieTitle = schedule.select(".info-movie strong").text();

                    Elements timetableElements = schedule.select(".info-timetable a");
                    // timetable 검색
                    for (Element timetableElement : timetableElements) {

                        String playDate = timetableElement.attr("data-playymd");
                        String playTime = timetableElement.attr("data-playstarttime");

                        LocalDateTime playDateTime = stringToLocalDateTime(playDate, playTime);
                        logger.info(Integer.toString(playTime.length()));

                        if (playTime.length() >= 1) {   // 상영 시작 시간은 뜨지만 예매가 마감된 경우, 시간표가 존재하지 않는 경우를 제외하기 위해서

                            /*
                             * 키로 등록할 값이 이미 존재하는 경우 while문 탈출
                             * ('sect-schedule'에 등록되지 않은 날짜를 조회하는 경우 예매가능한 가장 최근 날짜의 페이지로 redirect 되기 때문)
                             */
                            if (imaxScreenMoviesMap.containsKey(playDateTime)) {
                                duplCheck = false;
                                break;
                            } else {
                                logger.info("key: " + playDateTime.toString() + ", value: " + movieTitle);
                                imaxScreenMoviesMap.put(playDateTime, movieTitle);
                            }
                        }
                    }

                }// for문

                // 조회할 날짜 데이터 증가
                LocalDate date = now.plusDays(i);
                strDate = localDateToString(date);
                i++;

            } catch (IOException e) {
                e.printStackTrace();
            }

        }// while
        for (LocalDateTime key : imaxScreenMoviesMap.keySet()) {
            logger.info("키: " + key.toString() + ", 값: " + imaxScreenMoviesMap.get(key));
        }
        return imaxScreenMoviesMap;
    }

    /*
    * imax 상영관에서 개봉하는 영화 리스트 크롤링(예매 예정작인 영화 이름을 알아야 하기 때문에)
    * */
    public static List<String> returnImaxLineUp() {
        ArrayList<String> imaxLineUpList = new ArrayList<>();

        String URL = "http://www.cgv.co.kr/theaters/special/theater-line-up.aspx?regioncode=07";

        try {
            Document document = Jsoup.connect(URL).get();
            Elements elements = document.select(".box-contents a strong");

            System.out.println("elements = " + elements);

            for (Element element : elements) {
                String movieTitle = element.text();
                imaxLineUpList.add(movieTitle);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return imaxLineUpList;
    }

    // 크롤링한 예매 시간표 데이터를 Telegram bot이 보낼 수 있도록 메세지를 생성하는 메서드
    public static String timeTableMessage(TreeMap<LocalDateTime, String> timeTableMap, LocalDate userInputDate){

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM월 dd일 ");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH : mm");

        LocalDateTime inputDate = userInputDate.atStartOfDay();

        StringBuilder sb = new StringBuilder()
                .append(inputDate.format(dateFormatter))
                .append("용산 IMAX 시간표\n");

        for (LocalDateTime key : timeTableMap.keySet()) {

            long between = ChronoUnit.HOURS.between(inputDate, key);

            if (between >= 4 && between <= 27) {  // 조회 시점을 기준으로 같은 날인지 여부 체크 (심야 상영 체크를 위해 시간으로 비교)
                logger.info(String.valueOf(between));
                sb.append(timeTableMap.get(key))
                        .append(key.format(dateFormatter))
                        .append(" ")
                        .append(key.format(timeFormatter))
                        .append("\n");
            }
        }
        return sb.toString();
    }

    // LocalDate -> String 타입으로 변환 (예: 2021-05-14 -> 20210514)
    private static String localDateToString(LocalDate date) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return date.format(dateFormatter);
    }

    // String(날짜, 시간) -> LocalDateTime 타입으로 변환 (예: 20210514, 2230 -> 2021-05-14T22:30)
    private static LocalDateTime stringToLocalDateTime(String date, String time) {
        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(4, 6));
        int day = Integer.parseInt(date.substring(6, 8));

        int hour = Integer.parseInt(time.substring(0, 2));
        int minute = Integer.parseInt(time.substring(2, 4));

        // 새벽타임 (예: 26:30 ) 예외처리 (다음날 새벽 시간으로 변환)
        if (hour >= 24) {
            hour -= 24;
            day += 1;
        }

        return LocalDateTime.of(year, month, day, hour, minute);
    }

    // String(날짜) -> LocalDate 타입으로 변환 (예: 0519 -> 2021-05-19)
    public static LocalDate stringToLocalDate(String date) {
        int year = LocalDate.now().getYear();
        int month = Integer.parseInt(date.substring(0, 2));
        int day = Integer.parseInt(date.substring(2, 4));

        return LocalDate.of(year, month, day);
    }

}
