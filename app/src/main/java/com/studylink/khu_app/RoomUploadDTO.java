package com.studylink.khu_app;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class RoomUploadDTO implements Serializable {

    private String title;
    private String writing_content;
    private String uploadername;
    private List<String> filename;
    private Date time;
    private String userId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWriting_content() {
        return writing_content;
    }

    public void setWriting_content(String writing_content) {
        this.writing_content = writing_content;
    }

    public String getUploadername() {
        return uploadername;
    }

    public void setUploadername(String uploadername) {
        this.uploadername = uploadername;
    }

    public List<String> getFilename() {
        return filename;
    }

    public void setFilename(List<String> filename) {
        this.filename = filename;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
