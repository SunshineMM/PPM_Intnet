package com.example.npttest.constant;

import android.os.Environment;

/**
 * Created by liuji on 2017/8/12.
 */

public class Constant {
    public static String OSURL="http://ws.parkos.cn/jcont";
    public static String DOMEURL="http://ws.boleyun.cn/jcont";
    public static String TYPE="3";
    public static String DSV;
    public static String CODE;
    public static String DOMECODE="ppm_test_parkos";
    public static String ID="id";
    public static String PASS="pass";
    public static String SET_CITY="set_city";//是否设置常用城市
    public static String COM_CITY="com_city";//常用城市
    public static String COM_CITY_A="COM_CITY_A";//保存常用城市
    public static String DEVICE_ADDRESS="device_address";//蓝牙地址0.
    public static String BLUETOOTH_NAME="bluetooth_name";//蓝牙名称
    public static String URL="url";//请求地址
    public static boolean domeLoginBoo;
    public static String USERNAME="username";//用户名
    public static String PHONE="phone";//用户名
    public static String SN="CRIA2EP4TYYWKPX3"; //已绑定 大头手持机  (已出货)
    //public static String SN="6QULB4MEEIYWKQQN";//sony
    //public static String SN="3DK7LXLZMQYWLC7R";//宜融
    //public static String SN="6CJVJ5J7YQYWLXPK";//测试
    public static String EndPoint;
    public static String ppmBucket;
    public static String OssImgUrl;
    public static String AccessKeyId;
    public static String AccessKeySecret;
    public static String SecurityToken;
    public static String username;
    public static String adds;
    public static String pname;
    public static String wxUrl;
    public static String aliUrl;
    public static String lat;
    public static String lng;

    public static String carnum;
    public static int ctype;
    public static String jfType;
    public static long itime;
    public static long ctime;
    public static String comfirmYy;
    public static String snmon;
    public static String srmon;
    public static String ssmon;
    public static int cdtp;
    public static boolean pvrefresh;
    public static String pktime;
    public static String sid;

    public static long wtime;
    public static int logintype;
    public static String LOGINTYPE="logintype";
    public static String pvcar;

    public static int enfree; //是否设置免费放行的权限

    public static String testusername="testuser";
    public static String testuserpwd="123456";
    public static String domeUrl;
    public static String nocompressPath= Environment.getExternalStorageDirectory().toString() + "/DCIM/PlatePic/";//没压缩图片路径
    public static String compressPath=Environment.getExternalStorageDirectory() + "/ppm/";//压缩后图片路径


    /**
     * 数据库名
     */
    public static final String DB_NAME="nptdb";

    /**
     * 数据库版本
     */

    public static final int VERSION=2;

    /**
     * 表名_1
     */
    public static final String TABLE_USER="r_remarks";

    /**
     * 字段名_1
     */
    public static final String RRSTRING="rrstring";

    /**
     * 表2
     */

    public static final String TABLE_UNAME="user_name";

    /**
     * 字段1
     */

    public static final String UNAME="uname";

    /**
     * 字段2
     */
    public static final String UTIME="utime";

}
