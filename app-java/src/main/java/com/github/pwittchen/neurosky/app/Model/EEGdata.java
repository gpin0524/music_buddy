package com.github.pwittchen.neurosky.app.Model;

import java.util.ArrayList;

public class EEGdata {
    public Integer ATTENTION;
    public Integer MEDITATION;
    public Integer HIGH_ALPHA;
    public Integer LOW_ALPHA;
    public Integer HIGH_GAMMA;
    public Integer DELTA;



    public EEGdata(){
    }

    public EEGdata(Integer ATTENTION, Integer MEDITATION, Integer HIGH_ALPHA,Integer LOW_ALPHA,Integer DELTA, Integer HIGH_GAMMA) {
        this.ATTENTION = ATTENTION;
        this.MEDITATION = MEDITATION;
        this.HIGH_ALPHA = HIGH_ALPHA;
        this.LOW_ALPHA = LOW_ALPHA;
        this.DELTA = DELTA;
        this.HIGH_GAMMA = HIGH_GAMMA;

    }

    public Integer getATTENTION() {
        return ATTENTION;
    }

    public void setATTENTION(Integer ATTENTION) {
        this.ATTENTION = ATTENTION;
    }

    public Integer getMEDITATION() {
        return MEDITATION;
    }

    public void setMEDITATION(Integer MEDITATION) {
        this.MEDITATION = MEDITATION;
    }

    public Integer getHIGH_ALPHA() {
        return HIGH_ALPHA;
    }

    public void setHIGH_ALPHA(Integer HIGH_ALPHA) {
        this.HIGH_ALPHA = HIGH_ALPHA;
    }

    public Integer getLOW_ALPHA() {
        return LOW_ALPHA;
    }

    public void setLOW_ALPHA(Integer LOW_ALPHA) {
        this.LOW_ALPHA = LOW_ALPHA;
    }

    public Integer getDELTA() {
        return DELTA;
    }

    public void setDELTA(Integer DELTA) {
        this.DELTA = DELTA;
    }

    public Integer getHIGH_GAMMA() {
        return HIGH_GAMMA;
    }

    public void setHIGH_GAMMA(Integer HIGH_GAMMA) {
        this.HIGH_GAMMA = HIGH_GAMMA;
    }

}
