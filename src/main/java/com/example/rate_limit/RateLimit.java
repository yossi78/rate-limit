package com.example.rate_limit;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.Duration;




public class RateLimit {
    private Map<String, Map<LocalDateTime,Integer>> userTimeMap;
    private int maxSizeOfRequests;
    private int lifespanInSeconds;


    public RateLimit() {
        this.userTimeMap=new HashMap<>();
        maxSizeOfRequests =5;
        lifespanInSeconds=5;
    }


    public  Boolean addRequest(String userId){
        LocalDateTime dateTime = getLocalDateTimeNow();
        if (!IsAllowed(userId, dateTime)) {
            return false;
        }
        Map<LocalDateTime, Integer> requests = userTimeMap.get(userId);
        requests.put(dateTime, 1);
        userTimeMap.put(userId, requests);
        return true;
    }

    public synchronized Boolean IsAllowed(String userId,LocalDateTime dateTime){
        int requestQuantity = getRequestQuantity(userId,dateTime);
        if(requestQuantity>= maxSizeOfRequests){
            System.out.println("[" + dateTime+ "] - " + "The userID="+userId + " is not allowed" + " - Stack Size="+userTimeMap.get("A1").size());
            return false;
        }
        System.out.println("[" + dateTime+ "] - " + "The userID="+userId + " is allowed" + " - Stack Size="+userTimeMap.get("A1").size());
        return true;
    }



    private  int getRequestQuantity(String userId,LocalDateTime dateTime) {
        int sum = 0;
        Map<LocalDateTime,Integer> requests  =userTimeMap.get(userId);
        if(requests==null || requests.size()==0){
            requests=new HashMap<>();
            userTimeMap.put(userId,requests);
            return  0;
        }
        sum = findSumOfOpenRequests(requests);
        userTimeMap.put(userId,requests);
        return sum;
    }

    private int findSumOfOpenRequests(Map<LocalDateTime,Integer> requests){
        removeOutdated(requests);
        int sum=0;
        for(LocalDateTime current:requests.keySet()){
            sum = sum+ requests.get(current);
        }
        return sum;
    }

    private void removeOutdated(Map<LocalDateTime,Integer> requests){
        int allowedGap = -lifespanInSeconds;
        List<LocalDateTime> outdatedRequests= new ArrayList<>();
        for(LocalDateTime current:requests.keySet()){
            if(getSecondsBetween(current,getLocalDateTimeNow())<allowedGap){
                outdatedRequests.add(current);
            }
        }
        outdatedRequests.stream().forEach(c->{
            requests.remove(c);
        });
    }

    public Long getSecondsBetween(LocalDateTime first, LocalDateTime second) {
        Duration duration = Duration.between(second, first);
        return duration.getSeconds();
    }

    private LocalDateTime getLocalDateTimeNow(){
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }



    public static void main(String[] args) throws InterruptedException {
        RateLimit rateLimit = new RateLimit();
        int interval = 1000;
        for(int i=1;i<=20;i++){
            rateLimit.addRequest("A1");
            Thread.sleep(interval);
        }


    }
}
