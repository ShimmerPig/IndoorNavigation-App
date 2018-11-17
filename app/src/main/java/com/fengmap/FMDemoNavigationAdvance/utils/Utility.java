package com.fengmap.FMDemoNavigationAdvance.utils;

import android.app.job.JobInfo;
import android.text.TextUtils;

import com.fengmap.FMDemoNavigationAdvance.bean.Store;
import com.google.gson.JsonIOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//解析和处理服务器返回的json数据
public class Utility {
    //处理store的json数据--response
    public static boolean handleStoreResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try{
                //json数据转换成一个数组
                JSONArray allStores=new JSONArray(response);
                //对数组进行遍历，创建store对象，并且保存到数据库中
                for(int i=0;i<allStores.length();i++){
                    JSONObject storeObject=allStores.getJSONObject(i);
                    Store store=new Store();
                    store.setFID(storeObject.getString("FID"));
                    store.setNAME(storeObject.getString("NAME"));
                    store.setTYPE(String.valueOf(storeObject.getInt("TYPE")));
                    store.setFLOOR(storeObject.getString("FLOOR"));
                    store.setGROUP(String.valueOf(storeObject.getString("GROUP")));
                    store.setX(storeObject.getString("X"));
                    store.setY(storeObject.getString("Y"));
                    store.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
}
