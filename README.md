# MovingAlarm

사용자가 알람이 울린 뒤 지정한 장소에 도착하면, 기록을 남겨주는 어플리케이션

사용 언어 : kotlin  
사용 라이브러리 : google map api, room, recycler view  

알람 : AlarmManager를 이용  
지도 : google map api 를 사용, 마커를 움직여 gps 좌표를 지정하며, geofence 를 이용해 도착 여부를 확인  
저장 : 사용자의 알람 설정은 shared preference 에 저장해둡니다.
기록 : room 을 이용해서 기록을 남기며, 열람시에는 MVVM 패턴을 사용하여 recycler view 에 표시합니다.
개별 기록을 터치하면 google map app을 실행해서 해당 좌표 위치를 보여줍니다.
