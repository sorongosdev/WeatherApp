package com.sorongos.weatherapp

import android.util.Log
import java.lang.Math.pow
import kotlin.math.*

class GeoPointConverter {

    //    map.Re = 6371.00877; // 지도반경
//    map.grid = 5.0; // 격자간격 (km)
//    map.slat1 = 30.0; // 표준위도 1
//    map.slat2 = 60.0; // 표준위도 2
//    map.olon = 126.0; // 기준점 경도
//    map.olat = 38.0; // 기준점 위도
//    map.xo = 210/map.grid; // 기준점 X좌표
//    map.yo = 675/map.grid; // 기준점 Y좌표
//    map.first = 0;
    private val NX = 149 //x격자수
    private val NY = 253 //y격자수

    private val RE = 6371.00877 //지도 반경
    private val GRID = 5.0 //격자간격
    private val SLAT1 = 30.0 //표준위도1
    private val SLAT2 = 60.0 //표준위도2
    private val OLON = 126.0 //기준점 경도
    private val OLAT = 38.0 //기준점 경도
    private val XO = 210 / GRID //기준점 X좌표
    private val YO = 675 / GRID //기준점 Y좌표

    private val DEGRAD = PI / 180.0
    private val RADDEG = 180.0 / PI

    private val re = RE / GRID
    private val slat1 = SLAT1 * DEGRAD
    private val slat2 = SLAT2 * DEGRAD
    private val olon = OLON * DEGRAD
    private val olat = OLAT * DEGRAD

    data class Point(val nx: Int, val ny: Int)

    fun convert(lon: Double, lat: Double): Point {

        var sn = tan(PI * 0.25 + slat2 * 0.5) / tan(PI * 0.25 + slat1 * 0.5)
        sn = log2(cos(slat1) / cos(slat2)) / log2(sn)
        var sf = tan(PI * 0.25 + slat1 * 0.5)
        sf = sf.pow(sn) * cos(slat1) / sn;
        var ro = tan(PI * 0.25 + olat * 0.5)
        ro = re * sf / ro.pow(sn)

        var ra = tan(PI * 0.25 + lat * DEGRAD * 0.5)
        ra = re * sf / ra.pow(sn)
        var theta = lon * DEGRAD - olon
        if (theta > PI) theta -= 2.0 * PI;
        if (theta < -PI) theta += 2.0 * PI;
        theta *= sn

        val nx = ra * sin(theta) + XO + 1.5
        val ny = ro - ra * cos(theta) + YO + 1.5;

        return Point(nx.toInt(), ny.toInt())

        /**

         *x = (float)(ra*sin(theta)) + (*map).xo;
         *y = (float)(ro - ra*cos(theta)) + (*map).yo;*/
    }

/*============================================================================*
* 좌표변환
*============================================================================*/
    /**
    int map_conv
    (
    float *lon, // 경도(degree)
    float *lat, // 위도(degree)
    float *x, // X격자 (grid)
    float *y, // Y격자 (grid)
    int code, // 0 (격자->위경도), 1 (위경도->격자)
    struct lamc_parameter map // 지도정보
    ) {
    float lon1, lat1, x1, y1;

    //
    // 위경도 -> (X,Y)
    //

    if (code == 0) {
    lon1 = *lon;
    lat1 = *lat;
    lamcproj(&lon1, &lat1, &x1, &y1, 0, &map);
     *x = (int)(x1 + 1.5);
     *y = (int)(y1 + 1.5);
    }

    return 0;
    }


    int lamcproj(lon, lat, x, y, code, map)

    float *lon, *lat; /* Longitude, Latitude [degree] */
    float *x, *y; /* Coordinate in Map [grid] */
    int code; /* (0) lon,lat ->x,y (1) x,y ->lon,lat */
    struct lamc_parameter *map;
    {
    static double PI, DEGRAD, RADDEG;
    static double re, olon, olat, sn, sf, ro;
    double slat1, slat2, alon, alat, xn, yn, ra, theta;






    } else {
    xn = *x - (*map).xo;
    yn = ro - *y + (*map).yo;
    ra = sqrt(xn*xn+yn*yn);
    if (sn< 0.0) -ra;
    alat = pow((re*sf/ra),(1.0/sn));
    alat = 2.0*atan(alat) - PI*0.5;
    if (fabs(xn) <= 0.0) {
    theta = 0.0;
    } else {
    if (fabs(yn) <= 0.0) {
    theta = PI*0.5;
    if(xn< 0.0 ) -theta;
    } else
    theta = atan2(xn,yn);
    }
    alon = theta/sn + olon;
     *lat = (float)(alat*RADDEG);
     *lon = (float)(alon*RADDEG);
    }
    return 0;
    }
     */
}