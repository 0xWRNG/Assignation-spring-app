package com.example.auth_spring.utils;

import java.time.LocalTime;

public class TimeInterval{
    LocalTime start;
    LocalTime end;
    public TimeInterval(LocalTime start, LocalTime end){
        this.start = start;
        this.end = end;
    }
    public LocalTime getStart(){
        return start;
    }
    public LocalTime getEnd(){
        return end;
    }

}