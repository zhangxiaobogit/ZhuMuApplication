package com.facecompare.zhumu.common.dbentity;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class Personnel {


    String workUnitId;
    ;//是	String	使用单位id
    String workUnitName;//是	String	部门名称
    String date;//是	String	获取数据时间用于 增量更新
    String usersData;//否	String	数据集
    String userId;//否	String	用户id
    String userName;//否	String	用户名称

    String userBirthday;//是	String	出生日期
    String userSex;//否	String	性别	（0男1女）
    String userNation;//否	String	民族
    String userPolice;//否	String	用户签发机关
    String userAddress;//否	String	用户住址
    String userCardNum;//否	String	用户身份证号
    String userCardPhoto;//是	String	用户证件照	图片路径
    String userNowPhoto;//是	String	用户生活	图片路径
    String userCardStartTime;//否	String	用户有效开始日期
    String userCardEndTime;//否	String	用户有效结束日期
    String logonScore;//否	String	注册分数

    String userFeature;//是	String	特征值
    String updatedate;//否	String	人员更新时间
    String logonTime;//否	String	人员注册时间
    String userPlace;//否	userPlace	人员籍贯


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
