package com.github.pwittchen.neurosky.app.Model;

import java.util.Comparator;

public class MusicDetails implements Comparable<MusicDetails>, java.io.Serializable{
    public String musicTitle, tag, videoId;
    public  double similarity;

    public MusicDetails(){}

    public MusicDetails(double similarity,String musicTitle, String tag, String videoId) {
        this.musicTitle = musicTitle;
        this.tag = tag;
        this.videoId = videoId;
        this.similarity = similarity;
    }

    public String getMusicTitle() {
        return musicTitle;
    }

    public void setMusicTitle(String musicTitle) {
        this.musicTitle = musicTitle;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    @Override
    public int compareTo(MusicDetails o) {
        return  Double.compare(this.similarity,o.similarity);
    }



   /* @Override
    public  int compareTo(MusicDetails compare){
        double comparage = ((MusicDetails)compare).getSimilarity();
        return  this.similarity-comparage;
    }

    @Override
    public  int compareTo(Object o) {
        return 0;
    }*/
    /*
    public int compareTo(MusicDetails arg0){
        //String、Integer、Double、Float等型別都實現有compareTo方法
        if(this.similarity.compareTo(arg0.similarity) == 0) {
            return Integer.valueOf(id).compareTo(Integer.valueOf(arg0.id));
        }else{
            return this.name.compareTo(arg0.name);
        }
    }
*/
}
