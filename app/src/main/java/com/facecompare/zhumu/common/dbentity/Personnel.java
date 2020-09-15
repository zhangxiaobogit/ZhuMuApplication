package com.facecompare.zhumu.common.dbentity;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Personnel {
    @Id
    Long id;
    String guid;
    String idName;
    String idNum;
    String idSex;
    String idNation;
    String idBirthday;
    String idAddress;
    String idPolice;
    String idPhoto;
    String photoPath;
    String validityTime;
    byte[] feature;
    String phone;

    public Personnel(byte[] feature, String idName) {
        this.idName = idName;
        this.feature = feature;
    }

    @Generated(hash = 1582017080)
    public Personnel(Long id, String guid, String idName, String idNum,
            String idSex, String idNation, String idBirthday, String idAddress,
            String idPolice, String idPhoto, String photoPath, String validityTime,
            byte[] feature, String phone) {
        this.id = id;
        this.guid = guid;
        this.idName = idName;
        this.idNum = idNum;
        this.idSex = idSex;
        this.idNation = idNation;
        this.idBirthday = idBirthday;
        this.idAddress = idAddress;
        this.idPolice = idPolice;
        this.idPhoto = idPhoto;
        this.photoPath = photoPath;
        this.validityTime = validityTime;
        this.feature = feature;
        this.phone = phone;
    }

    @Generated(hash = 1519428993)
    public Personnel() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGuid() {
        return this.guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getIdName() {
        return this.idName;
    }

    public void setIdName(String idName) {
        this.idName = idName;
    }

    public String getIdNum() {
        return this.idNum;
    }

    public void setIdNum(String idNum) {
        this.idNum = idNum;
    }

    public String getIdSex() {
        return this.idSex;
    }

    public void setIdSex(String idSex) {
        this.idSex = idSex;
    }

    public String getIdNation() {
        return this.idNation;
    }

    public void setIdNation(String idNation) {
        this.idNation = idNation;
    }

    public String getIdBirthday() {
        return this.idBirthday;
    }

    public void setIdBirthday(String idBirthday) {
        this.idBirthday = idBirthday;
    }

    public String getIdAddress() {
        return this.idAddress;
    }

    public void setIdAddress(String idAddress) {
        this.idAddress = idAddress;
    }

    public String getIdPolice() {
        return this.idPolice;
    }

    public void setIdPolice(String idPolice) {
        this.idPolice = idPolice;
    }

    public String getIdPhoto() {
        return this.idPhoto;
    }

    public void setIdPhoto(String idPhoto) {
        this.idPhoto = idPhoto;
    }

    public String getPhotoPath() {
        return this.photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getValidityTime() {
        return this.validityTime;
    }

    public void setValidityTime(String validityTime) {
        this.validityTime = validityTime;
    }

    public byte[] getFeature() {
        return this.feature;
    }

    public void setFeature(byte[] feature) {
        this.feature = feature;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}
