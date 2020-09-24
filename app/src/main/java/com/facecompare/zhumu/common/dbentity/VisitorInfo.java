package com.facecompare.zhumu.common.dbentity;

import com.facecompare.zhumu.util.NumUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/9/26.
 */
@Entity
public class VisitorInfo implements Serializable {
    @Transient
    private static final long serialVersionUID = 1L;
    @Id
    Long id;
    String visitName;
    String visitSex;
    String visitNation;
    String visitNum;
    String visitBirthday;
    String visitAddress;
    String visitPolice;
    String visitValidityTime;
    String visitTime;
    String visitIdCardImg;
    String visitNowImg;
    String visitCompareFlag;//0未通过 1通过
    String visitCompareScore;
    String visitCompareType;//0人证 1人脸
    String uploadFlag;//-1老数据 0未上传 1已上传
    String uuid;

    @Generated(hash = 967270817)
    public VisitorInfo(Long id, String visitName, String visitSex,
            String visitNation, String visitNum, String visitBirthday,
            String visitAddress, String visitPolice, String visitValidityTime,
            String visitTime, String visitIdCardImg, String visitNowImg,
            String visitCompareFlag, String visitCompareScore,
            String visitCompareType, String uploadFlag, String uuid) {
        this.id = id;
        this.visitName = visitName;
        this.visitSex = visitSex;
        this.visitNation = visitNation;
        this.visitNum = visitNum;
        this.visitBirthday = visitBirthday;
        this.visitAddress = visitAddress;
        this.visitPolice = visitPolice;
        this.visitValidityTime = visitValidityTime;
        this.visitTime = visitTime;
        this.visitIdCardImg = visitIdCardImg;
        this.visitNowImg = visitNowImg;
        this.visitCompareFlag = visitCompareFlag;
        this.visitCompareScore = visitCompareScore;
        this.visitCompareType = visitCompareType;
        this.uploadFlag = uploadFlag;
        this.uuid = uuid;
    }

    @Generated(hash = 848093406)
    public VisitorInfo() {
    }

    public void personel2VistorInfo(Personnel personnel) {
        visitName = personnel.idName;
        visitSex = personnel.getIdSex();
        visitNation = personnel.getIdNation();
        visitNum = personnel.getIdNum();
        visitBirthday = personnel.getIdBirthday();
        visitAddress = personnel.getIdAddress();
        visitPolice = personnel.getIdPolice();
        visitValidityTime = personnel.getValidityTime();
//        visitTime = ;
        visitIdCardImg = personnel.getPhotoPath();
//         visitnowImg=personnel.;
//         visitcompareFlag=personnel ;//0未通过 1通过
//         visitcompareScore=personnel ;
//         visitcompareType=personnel.;//0人证 1人脸
        uuid = NumUtils.getUUID();
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVisitName() {
        return this.visitName;
    }

    public void setVisitName(String visitName) {
        this.visitName = visitName;
    }

    public String getVisitSex() {
        return this.visitSex;
    }

    public void setVisitSex(String visitSex) {
        this.visitSex = visitSex;
    }

    public String getVisitNation() {
        return this.visitNation;
    }

    public void setVisitNation(String visitNation) {
        this.visitNation = visitNation;
    }

    public String getVisitNum() {
        return this.visitNum;
    }

    public void setVisitNum(String visitNum) {
        this.visitNum = visitNum;
    }

    public String getVisitBirthday() {
        return this.visitBirthday;
    }

    public void setVisitBirthday(String visitBirthday) {
        this.visitBirthday = visitBirthday;
    }

    public String getVisitAddress() {
        return this.visitAddress;
    }

    public void setVisitAddress(String visitAddress) {
        this.visitAddress = visitAddress;
    }

    public String getVisitPolice() {
        return this.visitPolice;
    }

    public void setVisitPolice(String visitPolice) {
        this.visitPolice = visitPolice;
    }

    public String getVisitValidityTime() {
        return this.visitValidityTime;
    }

    public void setVisitValidityTime(String visitValidityTime) {
        this.visitValidityTime = visitValidityTime;
    }

    public String getVisitTime() {
        return this.visitTime;
    }

    public void setVisitTime(String visitTime) {
        this.visitTime = visitTime;
    }

    public String getVisitIdCardImg() {
        return this.visitIdCardImg;
    }

    public void setVisitIdCardImg(String visitIdCardImg) {
        this.visitIdCardImg = visitIdCardImg;
    }

    public String getVisitNowImg() {
        return this.visitNowImg;
    }

    public void setVisitNowImg(String visitNowImg) {
        this.visitNowImg = visitNowImg;
    }

    public String getVisitCompareFlag() {
        return this.visitCompareFlag;
    }

    public void setVisitCompareFlag(String visitCompareFlag) {
        this.visitCompareFlag = visitCompareFlag;
    }

    public String getVisitCompareScore() {
        return this.visitCompareScore;
    }

    public void setVisitCompareScore(String visitCompareScore) {
        this.visitCompareScore = visitCompareScore;
    }

    public String getVisitCompareType() {
        return this.visitCompareType;
    }

    public void setVisitCompareType(String visitCompareType) {
        this.visitCompareType = visitCompareType;
    }

    public String getUploadFlag() {
        return this.uploadFlag;
    }

    public void setUploadFlag(String uploadFlag) {
        this.uploadFlag = uploadFlag;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}
