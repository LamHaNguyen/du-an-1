/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.boxcf.models;

import com.box.utils.MsgBox;
import com.box.utils.XDate;
import com.boxcf.components.material.BoxItem;
import com.boxcf.constands.BoxState;
import com.boxcf.dao.PhieuDatBoxDao;
import java.util.TimerTask;
import com.boxcf.store.Store;

/**
 *
 * @author PC
 */
public class Timer {

    BoxItem box;
    ModelItem data;

    public Timer() {
    }

    public Timer(BoxItem box, ModelItem data) {
        this.box = box;
        this.data = data;
    }

    public BoxItem getBox() {
        return box;
    }

    public void setBox(BoxItem box) {
        this.box = box;
    }

    public ModelItem getData() {
        return data;
    }

    public void setData(ModelItem data) {
        this.data = data;
    }

    public synchronized void setTime() {
        java.util.Timer timer = new java.util.Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                box.setSelected(BoxState.active);

                Time t = XDate.toTime(XDate.toString(data.getGioKT(), "MM/dd/yyyy HH:mm:ss"));

                if (t.getHour() < 0 && t.getMinute()< 0 && t.getSecond() < 0) {
                    PhieuDatBoxDao.getInstant().updateProc(box.getData(), BoxState.empty.toString(), box.getData().getGioKT());
                    
                    MsgBox.alert(Store.orderView, box.getData().getTen() + " đã hết thời gian !");
                    box.clearSelected();
                    timer.cancel();
                    return;
                }

                box.setRemainderTime(t);
            }
        };

        timer.scheduleAtFixedRate(task, 0, 1000);

    }

    @Override
    public String toString() {
        return "Timer{" + "box=" + box + ", data=" + data + '}';
    }
}
