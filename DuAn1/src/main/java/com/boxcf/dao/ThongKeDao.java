/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.boxcf.dao;

import com.box.utils.JdbcHelper;
import com.box.utils.XDate;
import com.boxcf.models.LichSu;
import com.boxcf.models.ModelStatistical;
import com.boxcf.models.ThongKeNhanVien;
import com.boxcf.models.ThongKeSanPham;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ThongKeDao {

    Date dateStart = XDate.getHour("23:59:59");
    Date dateEnd = XDate.getHour("00:00:00");

    private final String DateNowSql = "where NgayTao <= '" + XDate.toString(dateStart, "yyyy-MM-dd HH:mm:ss") + "' and NgayTao >= ' " + XDate.toString(dateEnd, "yyyy-MM-dd HH:mm:ss") + " '";

    public static ThongKeDao getInstant() {
        return new ThongKeDao();
    }

    private String toDate(String value) {
        if (value.length() < 6) {
            return value;
        }
        String year = value.substring(0, 4);
        String mont = value.substring(4, 6);
        String day = value.substring(6, value.length());
        return day + "/" + mont + "/" + year;
    }

    public double revenueOfTheDay() {

        double revenue = 0;
        String sql = "select SUM(TongTien) from HoaDon hd \n"
                + DateNowSql;

        try {

            ResultSet responce = JdbcHelper.query(sql);

            // admission a ResultSet return a Box
            if (responce.next()) {
                revenue = responce.getDouble(1);
            }
            responce.getStatement().getConnection().close();
        } catch (Exception e) {
            throw new Error("The Error in revenueOfTheDay Combo !");
        }
        return revenue;
    }

    public double productOfTheDay() {

        double revenue = 0;
        String sql = "select SUM(TongTien) from HoaDon hd\n"
                + DateNowSql + " and MaHD not in (select MaHD from PhieuDatBox)";

        try {

            ResultSet responce = JdbcHelper.query(sql);

            // admission a ResultSet return a Box
            if (responce.next()) {
                revenue = responce.getDouble(1);
            }
            responce.getStatement().getConnection().close();
        } catch (Exception e) {
            throw new Error("The Error in productOfTheDay Thong Ke !");
        }
        return revenue;
    }

    public double boxOfTheDay() {
        double revenue = 0;
        Date dateStart = XDate.getHour("23:59:59");
        Date dateEnd = XDate.getHour("00:00:00");

        String sql = "select SUM(TongTien) from PhieuDatBox pd\n"
                + "join HoaDon ct on ct.MaHD = pd.MaHD\n"
                + DateNowSql;

        try {

            ResultSet responce = JdbcHelper.query(sql);

            // admission a ResultSet return a Box
            if (responce.next()) {
                revenue = responce.getDouble(1);
            }
            responce.getStatement().getConnection().close();
        } catch (Exception e) {
            System.out.println(e);
            throw new Error("The Error in boxOfTheDay Combo !");
        }
        return revenue;
    }

    public double comboOfTheDay() {

        double revenue = 0;
//        
        return revenue;
    }

    public List<ModelStatistical> revenue(String type, Date... date) {
        List<ModelStatistical> list = new ArrayList<>();
        String sql = "";

        String sqlDay = "SELECT top " + 7
                + "CONVERT(VARCHAR(10),NgayTao,112), SUM(TongTien)\n"
                + "FROM HoaDon\n"
                + "GROUP BY CONVERT(VARCHAR(10),NgayTao,112)\n"
                + "order by CONVERT(VARCHAR(10),NgayTao,112) desc";

        String sqlMonth = "select top " + 7
                + "MONTH(NgayTao), SUM(TongTien) from HoaDon\n"
                + "group by MONTH(NgayTao)\n"
                + "order by MONTH(NgayTao) desc";

        String sqlYear = "select top " + 7
                + "YEAR(NgayTao), SUM(TongTien) from HoaDon\n"
                + "group by YEAR(NgayTao)\n"
                + "order by YEAR(NgayTao) desc";

        String sqlProc = "{ call sp_select_dt  ( ?, ? ) }";

        switch (type) {
            case "day":
                sql = sqlDay;
                break;
            case "month":
                sql = sqlMonth;
                break;
            case "year":
                sql = sqlYear;
                break;
            case "period":
                sql = sqlProc;
                break;
            default:
                break;
        }

        try {
            if (sql.equals("")) {
                return null;
            }

            ResultSet responce = null;

            if (date.length >= 2) {
                responce = JdbcHelper.query(sql, XDate.toString(date[0], "yyyy-MM-dd"), XDate.toString(date[1], "yyyy-MM-dd"));
            } else {
                responce = JdbcHelper.query(sql);
            }
            // admission a ResultSet return a Box
            while (responce.next()) {
                list.add(new ModelStatistical(toDate(responce.getString(1)), responce.getLong(2), null));
            }
            responce.getStatement().getConnection().close();
        } catch (Exception e) {
            System.out.println(e);
            throw new Error("The Error in revenueOfDay Combo !");
        }
        Collections.reverse(list);

        return list;
    }

    private String sqlProduct(String conditon) {
        return "select ct.MaSP, sp.TenSP, sp.Gia, sum(SoLuong) as soluong from HoaDonCT ct\n"
                + "join HoaDon hd on hd.MaHD = ct.MaHD\n"
                + "join SanPham sp on sp.MaSP = ct.MaSP \n"
                + conditon
                + " group by ct.MaSP, sp.TenSP, sp.Gia\n"
                + "order by sum(SoLuong) desc";
    }

    private String sqlStaff(String conditon) {
        return "select top 10  hd.MaNV,TenNV, NgayVaoLam, SUM(SoLuong) as soluong from HoaDon hd\n"
                + "join HoaDonCT ct on ct.MaHD = hd.MaHD\n"
                + "join NhanVien nv on nv.MaNV = hd.MaNV \n"
                + conditon
                + "group by  hd.MaNV, TenNV, NgayVaoLam";
    }

    public List<ModelStatistical> product(String type, Date... date) {
        List<ModelStatistical> list = new ArrayList<>();
        String sql = "";

        String sqlDay = "where ct.MaSP is not null and CONVERT(VARCHAR(10),NgayTao,112) in (select distinct top " + 7 + " CONVERT(VARCHAR(10),NgayTao,112) from HoaDon\n"
                + "order by  CONVERT(VARCHAR(10),NgayTao,112) desc)";

        String sqlMonth = " where ct.MaSP is not null and MONTH(NgayTao) >= MONTH(NgayTao) - " + 7;

        String sqlYear = " where ct.MaSP is not null and YEAR(NgayTao) >= YEAR(NgayTao) - " + 7;

        String sqlProc = "{ call sp_doanhthu_sanpham ( ?, ? ) }";

        switch (type) {
            case "day":
                sql = sqlProduct(sqlDay);
                break;
            case "month":
                sql = sqlProduct(sqlMonth);
                break;
            case "year":
                sql = sqlProduct(sqlYear);
                break;
            case "period":
                sql = sqlProc;
                break;
            default:
                return null;
        }

        try {

            ResultSet responce = null;
            if (date.length >= 2) {
                responce = JdbcHelper.query(sql, XDate.toString(date[0], "yyyy-MM-dd"), XDate.toString(date[1], "yyyy-MM-dd"));
            } else {
                responce = JdbcHelper.query(sql);
            }

            // admission a ResultSet return a Box
            while (responce.next()) {
                list.add(new ModelStatistical(responce.getString(2), responce.getLong(4),
                        new ThongKeSanPham(responce.getString(1), responce.getString(2), responce.getLong(3), responce.getInt(4))));
            }
            responce.getStatement().getConnection().close();
        } catch (Exception e) {
            System.out.println(e);
            throw new Error("The Error in revenueOfDay Combo !");
        }
        Collections.reverse(list);

        return list;
    }

    public List<ModelStatistical> staff(String type, Date... date) {
        List<ModelStatistical> list = new ArrayList<>();
        String sql = "";

        String sqlDay = " where TrangThai = 1 and CONVERT(VARCHAR(10),NgayTao,112) in (select distinct top " + 7 + " CONVERT(VARCHAR(10),NgayTao,112) from HoaDon\n"
                + "order by  CONVERT(VARCHAR(10),NgayTao,112) desc) ";

        String sqlMonth = " where TrangThai = 1 and MONTH(NgayTao) >= MONTH(NgayTao) - " + 7;

        String sqlYear = " where TrangThai = 1 and YEAR(NgayTao) >= YEAR(NgayTao) - " + 7;

        String sqlProc = "{ call sp_nangxuat_nv ( ?, ? ) }";

        switch (type) {
            case "day":
                sql = sqlStaff(sqlDay);
                break;
            case "month":
                sql = sqlStaff(sqlMonth);
                break;
            case "year":
                sql = sqlStaff(sqlYear);
                break;
            case "period":
                sql = sqlProc;
                break;
            default:
                return null;
        }

        try {

            ResultSet responce = null;
            if (date.length >= 2) {
                responce = JdbcHelper.query(sql, XDate.toString(date[0], "yyyy-MM-dd"), XDate.toString(date[1], "yyyy-MM-dd"));
            } else {
                responce = JdbcHelper.query(sql);
            }

            // admission a ResultSet return a Box
            while (responce.next()) {
                list.add(new ModelStatistical(responce.getString(2), responce.getLong(4),
                        new ThongKeNhanVien(responce.getString(1), responce.getString(2), responce.getDate(3), responce.getInt(4))));
            }
            responce.getStatement().getConnection().close();
        } catch (Exception e) {
            System.out.println(e);
            throw new Error("The Error in staff ThongKe !");
        }
        Collections.reverse(list);

        return list;
    }

    public List<LichSu> history(String keyword) {
        List<LichSu> list = new ArrayList<>();
        String sqlKeyword = "select hd.MaHD, NgayTao, TenKH, TenNV, TenSP, SoLuong, ThanhTien, TongTien, PhanTram from HoaDon hd\n"
                + "join HoaDonCT ct on ct.MaHD = hd.MaHD\n"
                + "join NhanVien nv on nv.MaNV = hd.MaNV\n"
                + "join SanPham sp on sp.MaSP = ct.MaSP\n"
                + "join KhuyenMai km on km.MaKM = hd.MaKM\n"
                + "where hd.MaHD like ? or TenKH like ? or TenNV like ? or ThanhTien like ? or TongTien like ? or PhanTram like ? \n"
                + "order by NgayTao desc";

        String sql = "select hd.MaHD, NgayTao, TenKH, TenNV, TenSP, SoLuong, ThanhTien, TongTien, PhanTram from HoaDon hd\n"
                + "join HoaDonCT ct on ct.MaHD = hd.MaHD\n"
                + "join NhanVien nv on nv.MaNV = hd.MaNV\n"
                + "join SanPham sp on sp.MaSP = ct.MaSP\n"
                + "join KhuyenMai km on km.MaKM = hd.MaKM\n"
                + "order by NgayTao desc";

        try {
            ResultSet responce = null;
            if (keyword.equals("")) {
                responce = JdbcHelper.query(sql);
            } else {
                keyword = "%" + keyword + "%";
                responce = JdbcHelper.query(sqlKeyword, keyword, keyword, keyword, keyword, keyword, keyword);
            }

            // admission a ResultSet return a Box
            while (responce.next()) {
                list.add(new LichSu(responce.getInt(1),
                        responce.getDate(2),
                        responce.getString(3),
                        responce.getString(4),
                        responce.getString(5),
                        responce.getInt(6),
                        responce.getLong(7),
                        responce.getLong(8),
                        responce.getString(9) + "%"));
            }
            responce.getStatement().getConnection().close();
        } catch (Exception e) {
            System.out.println(e);
            throw new Error("The Error in history ThongKe !");
        }

        return list;
    }

    public static void main(String[] args) {
        for (LichSu object : ThongKeDao.getInstant().history("75000")) {
            System.out.println(object);
        }
    }
}
