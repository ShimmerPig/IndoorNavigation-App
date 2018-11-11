package com.fengmap.FMDemoNavigationAdvance.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * 方向传感器。
 */
public class SensorAPI {

	private static SensorAPI instance = null;
	private Context context;
	private SensorManager sensorMagager = null;
	private Sensor accSensor = null;
	private Sensor magSensor = null;
	private float[] accelerationValues = new float[3];
	private float[] magneticValues = new float[3];
	private float[] values = new float[3];
	private float[] rotate = new float[9];
	private float angle = 0;

	/**
	 * 单例获取。
	 * 
	 * @return
	 */
	public static SensorAPI getInstance() {
		if (instance == null) {
			synchronized (SensorAPI.class) {
				instance = new SensorAPI();
			}
		}
		return instance;
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	public void init(Context context) {
		this.context = context;
		sensorMagager = (SensorManager) this.context.getSystemService(Context.SENSOR_SERVICE);
		accSensor = sensorMagager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magSensor = sensorMagager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}

	/**
	 * 开始搜集数据
	 */
	public void start() {
		if (sensorMagager != null) {
			sensorMagager.registerListener(sensorEventListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
			sensorMagager.registerListener(sensorEventListener, magSensor, SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	/**
	 * 停止搜集数据
	 */
	public void stop() {
		if (sensorMagager != null) {
			sensorMagager.unregisterListener(sensorEventListener, accSensor);
			sensorMagager.unregisterListener(sensorEventListener, magSensor);
		}
	}

	public float getAngle() {
		return angle;
	}

	private final SensorEventListener sensorEventListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				accelerationValues = event.values;
			}
			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				magneticValues = event.values;
			}

			SensorManager.getRotationMatrix(rotate, null, accelerationValues, magneticValues);
			SensorManager.getOrientation(rotate, values);
			// 得到的values值为弧度转换为角度

			float tmp = (float) Math.toDegrees(values[0]);
			if (tmp < 0) {
				tmp += 360;
			}
			tmp = 360 - tmp;
			if (Math.abs(angle - tmp) < 11) {
			} else if (Math.abs(angle - tmp) < 30 && Math.abs(angle - tmp) > 10) {
				angle = (float) (angle * 0.8 + tmp * 0.2);
			} else {
				angle = tmp;
			}
		}

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {

		}

	};

	private SensorAPI() {

	}
}
