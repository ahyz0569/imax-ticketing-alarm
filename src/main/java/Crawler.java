import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class Crawler {
    public static void main(String[] args) {

        /*
        상영시간표 정보를 담을 HashMap 생성
        - Key: 상영날짜+상영시간(12자리, 숫자형태의 String 값)
        - Value: 영화 제목
        */
        HashMap<String, String> imaxScreenMoviesMap = new HashMap<>();

        LocalDate now = LocalDate.now();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
        String strDate = dateFormat.format(now);

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
//                System.out.println("strDate = " + strDate);

                // Atrribute 탐색
                Elements scheduleList = doc.getElementsByClass("col-times");

                for (Element schedule : scheduleList) {
                    // title 검색
                    String movieTitle = schedule.select(".info-movie strong").text();
//                    System.out.println("title = " + movieTitle);

                    Elements timetableElements = schedule.select(".info-timetable a");
                    // timetable 검색
                    for (Element timetableElement : timetableElements) {
//                        System.out.println(timetableElement.toString());
//                        System.out.println("=======");

                        String playDate = timetableElement.attr("data-playymd");
                        String playTime = timetableElement.attr("data-playstarttime");

                        if (playTime.length() >= 1) {   // 상영 시작 시간은 뜨지만 예매가 마감된 경우, 시간표가 존재하지 않는 경우를 제외하기 위해서

                            /*
                            * 키로 등록할 값이 이미 존재하는 경우 while문 탈출
                            * ('sect-schedule'에 등록되지 않은 날짜를 조회하는 경우 예매가능한 가장 최근 날짜의 페이지로 redirect 되기 때문)
                            */
                            if (imaxScreenMoviesMap.containsKey(playDate + playTime)) { 
                                duplCheck = false;
                                break;
                            } else {
//                                System.out.println(playDate + playTime + " :: " + movieTitle);
                                imaxScreenMoviesMap.put(playDate + playTime, movieTitle);
                            }
                        }
                    }

                }// for문
                // 조회할 날짜 데이터 증가
                LocalDate date = now.plusDays(i);
                strDate = dateFormat.format(date);
                i++;

            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }// while

        // HashMap 출력하기
        for (String key : imaxScreenMoviesMap.keySet()) {
            System.out.println(String.format("키 : %s, 값: %s", key, imaxScreenMoviesMap.get(key)));
        }
    }
}
