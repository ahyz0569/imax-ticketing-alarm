# 용산 IMAX 예매 오픈 알림 텔레그램봇
Telegram Bot API를 이용하여 용산 CGV의 IMAX 상영관에 예매가 오픈되었을 때 알림 메세지를 전송하는 봇
(학습 및 개인 사용 목적으로 제작)

## 기능 요구 사항
- 예매 시작 알림을 받고 싶은 영화 제목과 예매 날짜를 채팅창에 입력하면, 조건에 맞는 입력값을 기준으로 예매 오픈 여부를 5분마다 조회한다.
- 입력 받은 영화 제목과 예매 날짜를 기준으로 상영 시간표가 업데이트 되었음을 감지할 경우, 메세지를 통해 알려준다. 메세지에는 예매가 시작된 영화 이름과 예매 날짜가 포함된다.
- 예매 시작 알림에 등록할 수 있는 영화는 CGV 홈페이지 내 IMAX 상영 라인업에 등록되어 있어야 한다. 라인업에 존재하지 않는 영화명을 입력한 경우에는 현재 IMAX 상영 라인업에 있는 영화 제목들을 메세지로 전달한다.
- 예매 시작 알림으로 등록할 수 있는 날짜는 메세지를 보낸 시점 보다 미래(14일 이내)여야 한다.
- 입력 받은 예매 날짜에 이미 예매 시간표가 공개되어 있는 경우, 이미 예매가 시작되었음을 알림과 동시에 해당 날짜의 예매 시간표를 함께 전송한다.
- command 메세지를 받은 경우에는, 해당 봇의 이용 가이드 메세지를 전송한다.

## Requirements
JDK 1.11, Maven 3.6.2, jsoup 1.13.1, Telegram bot API 5.2.0

## Dependencies
jsoup 라이브러리
```
<!-- https://mvnrepository.com/artifact/org.jsoup/jsoup -->
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.13.1</version>
</dependency>
```

Telegram bot API
```
<!-- https://mvnrepository.com/artifact/org.telegram/telegrambots -->
<dependency>
    <groupId>org.telegram</groupId>
    <artifactId>telegrambots</artifactId>
    <version>5.2.0</version>
</dependency>
```

## 사용 예시(스크린샷)
![imax-ticketing-alarmbot-example](https://user-images.githubusercontent.com/57691047/119026569-49fc0c80-b9e0-11eb-8707-4ddb6cb09c91.jpg)
