
/**
* Filename:    CircleInfoBean.java
* Copyright:   Copyright (c)2010
* Company:     Founder Mobile Media Technology(Beijing) Co.,Ltd.g
* @version:    1.0
* @since:       JDK 1.6.0_21
* Create at:   2015-6-11 下午4:01:51
* Description:
* Modification History:
* Date     Author           Version           Description
* ------------------------------------------------------------------
* 2015-6-11    王涛             1.0          1.0 Version
*/
package com.pet.bean;
public class CircleInfoBean {
    //大图片
    public String bigImageUrl;
    //小图片
    public String iconImageUrl;
    //宠物名称
    public String petName;
    //圈子内容
    public String info;
    //发布时间
    public String publicTime;
    //评论数
    public String commentCount;
    //收藏数
    public String favCount;
    
    public boolean isShowComment = false ;
    
    public String getBigImageUrl() {
        return bigImageUrl;
    }
    public void setBigImageUrl(String bigImageUrl) {
        this.bigImageUrl = bigImageUrl;
    }
    public String getIconImageUrl() {
        return iconImageUrl;
    }
    public void setIconImageUrl(String iconImageUrl) {
        this.iconImageUrl = iconImageUrl;
    }
    public String getPetName() {
        return petName;
    }
    public void setPetName(String petName) {
        this.petName = petName;
    }
    public String getInfo() {
        return info;
    }
    public void setInfo(String info) {
        this.info = info;
    }
    public String getPublicTime() {
        return publicTime;
    }
    public void setPublicTime(String publicTime) {
        this.publicTime = publicTime;
    }
    public String getCommentCount() {
        return commentCount;
    }
    public void setCommentCount(String commentCount) {
        this.commentCount = commentCount;
    }
    public String getFavCount() {
        return favCount;
    }
    public void setFavCount(String favCount) {
        this.favCount = favCount;
    }
    
}



