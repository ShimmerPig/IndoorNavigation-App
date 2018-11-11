package com.fengmap.FMDemoNavigationAdvance.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.fengmap.FMDemoNavigationAdvance.bean.AMapPoint;

import java.util.Date;

public class StepCountLocationService extends Service {

    private boolean threadDisable=false;
    protected static final int UPDATE = 3;

    //传感器
    SensorManager sensorManager;
    //加速度监听器
    AccelerometerListener accelerometerListener = new AccelerometerListener();
    boolean first_orietation = true;
    //方向监听器
    OrientationListener orientationListener = new OrientationListener();
    float currentDegree = 0;
    float lastDegree = 0;
    float degree = 0;
    //linwei,
    public boolean flag=true;
    //保存前后两个值
    public double[] a=new double[2];
    public int n=0;
    public double max;
    public double[] gravity=new double[3];
    //判断是否是跨过波峰
    public boolean f1=false;
    public int derection=-1;
    public int count=0;
    public int step=0;

    public int len=0;
    public double[] x=new double[3];
    public double[] y=new double[3];
    public double[] z=new double[3];
    public float[] acceValus=new float[3];
    public double s=0f;
    public Thread count_thread;
    public Thread count_step;
    //用户的身高体重性别
    public float height;
    public float weight;
    public String sex;
    public float lenOfStep;
    //service
    private Date startdate;//运动开始时间
    private Date nowdate;//目前时间
    private float walktime=0;//步行时间 单位：小时
    private float walkenergy=0;//步行消耗能量 单位：kCal
    private float walklength=0;//步行距离 单位：km
    private float walkspeed=0;//步行速度 单位：km/h
    //常量
    final float alpha = 0.99f;//alpha=t/t+dt
    StepLocation stepLocation=StepLocation.getInstance();
    Handler handler;
    private int oldStep=0;
    public void init(){
        startdate = new Date();
//        height = (float)data[0];
//        weight = (float)data[1];
        height=170;
        weight=60;
        count=1;
        sex = "男";
        max=10.78;
        if(sex.equals("男"))
        {
            lenOfStep=(float) (0.415*height);
        }else{
            lenOfStep=(float) (0.413*height);
        }
        count_thread=new Thread(CountTime);
        count_thread.start();
        count_step=new Thread(CountSpeed);
        count_step.start();
    }
    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("pig","service onCreate");
        init();
        //启动的时候对UPDATE消息做处理
        handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE:
                        SendLocation();
                        //其他的消息交给父类做处理
                    default:
                        super.handleMessage(msg);
                }
            }
        };
    }
    public void SendLocation() {
        Log.i("pig","SendLocation 开始");
        //如果在原地不动
//        if (lastAMapPoint.equal(location)) {
//            //什么都不做
//        } else {
//            lastAMapPoint = location;
//            if (location.getState() == 1) customApplcation.GeoPoint(lastAMapPoint);
//            Log.i("zjx", location.toString());
//            //发送广播
//            Intent intent = new Intent();
//            intent.putExtra("ax", location.getX());
//            intent.putExtra("ay", location.getY());
//            intent.putExtra("az", location.getZ());
//            intent.putExtra("astate", location.getState());
//            intent.putExtra("amapid", location.getAmapId());
//            intent.putExtra("adetail", location.getDetailAddress());
//            intent.setAction("com.example.amap.service.StepCountLocationService");
//            sendOrderedBroadcast(intent, null);
//        }
    }
    //加速度监听器
    private class AccelerometerListener implements SensorEventListener {

        //传感器的精度改变调用此方法
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

        //传感器的值改变调用此方法
        @Override
        public void onSensorChanged(SensorEvent event) {
            acceValus[0]=event.values[0];
            acceValus[1]=event.values[1];
            acceValus[2]=event.values[2];
            //Log.i("pig",step+"");
            //System.out.println(System.currentTimeMillis());
            x[len]=acceValus[0];
            y[len]=acceValus[1];
            z[len]=acceValus[2];
            len=(len+1)%3; //1 3 2 4 3 5    ->移动窗口得到2 3 3 4
            //中值滤波 。。取连续三次中间那个值
            //gravity 的x y z 取三次的每个属性的中值
            if(x[0]>x[1])
            {
                if(x[0]<x[2])		gravity[0]=x[0];
                else{
                    if(x[1]>x[2])	gravity[0]=x[1];
                    else			gravity[0]=x[2];
                }
            }else{
                if(x[0]>x[2])		gravity[0]=x[0];
                else{
                    if(x[1]>x[2])	gravity[0]=x[2];
                    else			gravity[0]=x[1];
                }
            }
            if(y[0]>y[1])
            {
                if(y[0]<y[2])		gravity[1]=y[0];
                else{
                    if(y[1]>y[2])	gravity[1]=y[1];
                    else			gravity[1]=y[2];
                }
            }else{
                if(y[0]>y[2])		gravity[1]=y[0];
                else{
                    if(y[1]>y[2])	gravity[1]=y[2];
                    else			gravity[1]=y[1];
                }
            }
            if(z[0]>z[1])
            {
                if(z[0]<z[2])		gravity[2]=z[0];
                else{
                    if(z[1]>z[2])	gravity[2]=z[1];
                    else			gravity[2]=z[2];
                }
            }else{
                if(z[0]>z[2])		gravity[2]=z[0];
                else{
                    if(z[1]>z[2])	gravity[2]=z[2];
                    else			gravity[2]=z[1];
                }
            }
            //s->x,y,z中值的长
            s=Math.sqrt(gravity[0]*gravity[0]+gravity[1]*gravity[1]+gravity[2]*gravity[2]);

            //分离重力加速度分量，并且校正
            if(s!=0)
            {
                s = gravity[0]*(alpha *gravity[0]+ (1 - alpha) *acceValus[0])/s
                        + gravity[1]*(alpha * gravity[1] + (1 - alpha) *acceValus[1])/s
                        + gravity[2]*(alpha * gravity[2] + (1 - alpha) *acceValus[2])/s;
            }
            a[n]=s;//n=0,1
            //----------判断是否跨过波峰----------------------
            //Log.i("Main",a[n]+" last: "+a[(n+1)%2]+"");
            //Log.i("Main",(a[n]-a[(n+1)%2)+"");
            //Log.i("Main", n+" "+(n+1)%2);?>
//            Log.i("Main","n%2="+n%2+" (n+1)%2="+(n+1)%2);
            if((a[n%2]-a[(n+1)%2])*derection>0)
            {
                //如何判断阀值呢？;
                //确定频率 count>=10;
                //跨过波峰
                derection*=-1;
                if(derection==1&&a[n%2]>max)
                {
                    //Log.i("pig", "跨过波峰");
                    if(count==0)
                        count++;
                    if(count>=20)
                    {
                        //在正常时间窗口内
                        step++; //计步成功
//                        text.setText(step+"");
                        count=1;
                    }
                    else{
                        //时间窗口外则更新波峰的值;
                    }
                }
                else
                {
                    //Log.i("pig", "跨过波谷");
                }
            }
            n=(n+1)%2;
        }
    }
    Runnable CountTime = new Runnable(){
        public void run(){
            while(!threadDisable)
            {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                count++;
            }
        }
    };

    Runnable CountSpeed = new Runnable(){
        public void run(){
            while(!threadDisable)
            {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


//                int stepSubb=step-oldStep;
//                oldStep=step;
//                //根据差计算步长
//                Log.i("pig", "隔2s的步数差：" + stepSubb);
                //TODO 下面的30.0表示：在地图上 从入口进去的时候指南针的角度 30为预设
                double degreeSub=(30.0+90.0+360.0-lastDegree)%360; //相对地球竖直向上↑的角度差 角度从逆时针算
//                Log.i("zjx","degreeSub:"+degreeSub);
                int stepSub=0;
                if((stepSub=(step-stepLocation.lastAMapPoint.getStep()))!=0){
                    double xMap,yMap;
                    Intent intent = new Intent();
                    if(stepSub<0){
                        xMap=stepLocation.lastAMapPoint.getX();
                        yMap=stepLocation.lastAMapPoint.getY();
                    }
                    else {
                        double lenStep = stepSub * 0.5;//走的距离 需要转换
                        double dia = Math.toRadians(degreeSub);
                        //下面的400表示：总室内长约400m
                        double sinStepY = lenStep * Math.sin(dia) / 400;//y+-
                        double cosStepX = lenStep * Math.cos(dia) / 400;//x+-
                        xMap = stepLocation.lastAMapPoint.getX() + cosStepX;
                        yMap = stepLocation.lastAMapPoint.getY() + sinStepY;
                        //更新到CustomApplcation
                        //CustomApplcation.lastPoint=stepLocation.lastAMapPoint;
                  //      Log.i("pig", "location:" + xMap + ";;;" + yMap+":::"+stepLocation.lastAMapPoint.getZ());
                    }
                    intent.putExtra("ax",xMap);
                    intent.putExtra("ay",yMap);
                    intent.putExtra("az",stepLocation.lastAMapPoint.getZ());
                    intent.putExtra("step", step);
                    intent.putExtra("degree", lastDegree);
                    intent.putExtra("astate", 10086);
                    intent.setAction("com.fengmap.FMDemoNavigationAdvance.location.StepCountLocationService");
                    //发送广播
                    sendOrderedBroadcast(intent, null);
                    //sendBroadcast(intent);
                    int zTemp=stepLocation.lastAMapPoint.getZ();
                    stepLocation.lastAMapPoint =new AMapPoint(xMap,yMap,zTemp,lastDegree,step);
                }
            }
        }
    };
    private class OrientationListener implements SensorEventListener {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub
        }
        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub

            if(first_orietation){
                lastDegree = event.values[0];
                first_orietation = false;
            }else{
                currentDegree = event.values[0];
                if(Math.abs(currentDegree - lastDegree) > 5){
                    degree = lastDegree;
                    //改变方向
                    lastDegree = -currentDegree;
                 //   Log.i("pig","lastDegree:"+lastDegree);
                }
            }
        }

    }
    public void startDeadReckoningService(){

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        //加速度传感器
        Sensor acceleromererSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //方向传感器
        Sensor orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorManager.registerListener(accelerometerListener, acceleromererSensor,10000);
        sensorManager.registerListener(orientationListener,orientationSensor,SensorManager.SENSOR_DELAY_FASTEST);
        System.out.println("deadReckoningService started");
    }
//    如果一个 Service 已经被启动，
//    其他代码再试图调用 startService() 方法，
//    是不会执行 onCreate() 的，
//    但会重新执行一次 onStart() 。

    //不用bindService()启动的原因，多个activity会启动service 不能以为某个调用Activity退出就也退出service了，手动stop
    @Override
    public void onStart(Intent intent, int startId){
        Log.i("pig","StepService onStart");
        super.onStart(intent, startId);
        startDeadReckoningService();
    }
    public void unRegisterSensors(){
        sensorManager.unregisterListener(accelerometerListener);
        sensorManager.unregisterListener(orientationListener);

    }
    @Override
    public void onDestroy() {
        threadDisable=true;
        Log.i("pig", "service destroy");
        unRegisterSensors();
        StepLocation.refresh();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
