package com.esri.arcgisruntime.sample.navigateinar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.location.LocationDataSource;

import static java.util.Locale.ENGLISH;

public class MSLAdjustedARLocationDataSource extends LocationDataSource {
  public enum AltitudeAdjustmentMode {
    GPS_RAW_ELLIPSOID,
    NMEA_PARSED_MSL, // listen for NMEA messages and extract altitude that way
    LOCALLY_ADJUSTED_WITH_EGM2008
  }

  public MSLAdjustedARLocationDataSource(Context context){
    _context = context;
  }
  public MSLAdjustedARLocationDataSource(Context context, Criteria criteria, long minTime, float minDistance){
    this.criteria = criteria;
    this._context = context;
    checkTimeDistanceParameters(minTime, minDistance);
  }

  public MSLAdjustedARLocationDataSource(Context context, String provider, long mintime, long minDistance){
    _context = context;
    this.provider = provider;
    checkTimeDistanceParameters(mintime, minDistance);
  }

  private static final double ACCURACY_THRESHOLD_FACTOR = 2.0;
  private static final String EXCEPTION_MSG = "No location provider found on the device";
  private static final String NO_STARTED_MSG = "The location data source is not started yet";
  private static final String NO_PROVIDER_MSG = "No provider found for the given name : %s";
  private static final String PARAMETER_OUT_OF_BOUNDS_MSG = "Parameter %s is out of bounds";

  private LocationManager _locationManager;
  private SensorManager _sensorManager;
  private Context _context;

  // ============================= Altitude adjustment properties
  private AltitudeAdjustmentMode _adjustmentMode = AltitudeAdjustmentMode.GPS_RAW_ELLIPSOID;
  private double _manualOffset = 0;

  public void setManualOffset(double offset){
    _manualOffset = offset;
  }

  public double getManualOffset(){
    return _manualOffset;
  }

  public AltitudeAdjustmentMode getAltitudeAdjustmentMode() {
    return _adjustmentMode;
  }

  @SuppressLint("MissingPermission")
  public void setAltitudeAdjustmentMode(AltitudeAdjustmentMode mode){
    _adjustmentMode = mode;

    if (mode == AltitudeAdjustmentMode.NMEA_PARSED_MSL){
      internalNmeaListener = new InternalNmeaListener();
      getLocationManager().addNmeaListener(internalNmeaListener);
    } else if (internalNmeaListener != null) {
      getLocationManager().removeNmeaListener(internalNmeaListener);
      internalNmeaListener = null;
      lastNmeaHeight = Double.NaN;
    }
  }

  private double lastNmeaHeight = Double.NaN;
  private long lastNmeaUpdateTimestamp = 0;
  // ============================ / Altitude Adjustment properties

  // The minimum distance to change updates in meters
  private float minimumUpdateDistance = 0f; // meters

  // The minimum time between updates in milliseconds
  private Long minimumUpdateTime = 100l; // 0.1 second

  // The Android location manager
  private LocationManager getLocationManager(){
    if (_locationManager == null){
      _locationManager = (LocationManager)_context.getSystemService(_context.LOCATION_SERVICE);
    }
    return _locationManager;
  }

  private SensorManager getSensorManager(){
    if (_sensorManager == null){
      _sensorManager = (SensorManager)_context.getSystemService(Context.SENSOR_SERVICE);
    }
    return _sensorManager;
  }

  // The current selected location providers
  private ArrayList<String> selectedLocationProviders = new ArrayList<>();

  // The internal android location listener implementation
  private InternalLocationListener internalLocationListener = null;

  // The internal listener to update the heading for compass mode
  private InternalHeadingListener internalHeadingListener = null;

  private InternalNmeaListener internalNmeaListener = null;

  // The criteria for selecting the android location provider
  private Criteria criteria = null;

  // The user defined known provider
  private String provider = null;

  // The last updated location
  private Location lastLocation = null;

  private void requestLocationUpdates(Criteria criteria, long minTime, long minDistance){
    if (!isStarted()){
      throw new IllegalStateException(NO_STARTED_MSG);
    }

    selectedLocationProviders.clear();

    checkTimeDistanceParameters(minTime, minDistance);

    selectProviderByCriteria(criteria);

    if (selectedLocationProviders.isEmpty()){
      throw new IllegalStateException(EXCEPTION_MSG);
    }

    startLocationProviders();
  }

  private void requestLocationUpdates(String provider, long minTime, float minDistance){
    if (!isStarted()){
      throw new IllegalStateException(NO_STARTED_MSG);
    }

    checkTimeDistanceParameters(minTime, minDistance);

    selectProviderByUserDefined(provider);

    if (selectedLocationProviders.isEmpty()){
      throw new IllegalArgumentException(String.format(NO_PROVIDER_MSG, provider));
    }

    startLocationProviders();
  }

  @Override
  protected void onStart() {
    Handler handler = new Handler(_context.getMainLooper());

    handler.post(() -> {
      Throwable throwable = null;

      try {

        if (criteria != null) {
          selectProviderByCriteria(criteria);
        } else if (provider != null){
          selectProviderByUserDefined(provider);
        } else {
          selectProvidersByDefault();
        }

        if (selectedLocationProviders.isEmpty()){
          throw new IllegalStateException(String.format(NO_PROVIDER_MSG, "selectedLocationProviders"));
        }

        registerListeners();

      } catch (Exception exception){
        throwable = exception;
      }

      onStartCompleted(throwable);
    });
  }

  @SuppressLint("MissingPermission")
  private void registerListeners(){
    android.location.Location lastKnownLocation = getLocationManager().getLastKnownLocation(selectedLocationProviders.get(0));

    if (lastKnownLocation != null){
      lastKnownLocation.setSpeed(0f);
      lastKnownLocation.setBearing(0f);
      setLastKnownLocation(toEsriLocation(lastKnownLocation, true));
    }

    startLocationProviders();

    startUpdateHeading();
  }

  @Override
  protected void onStop() {
    getLocationManager().removeUpdates(internalLocationListener);
    internalLocationListener = null;

    stopUpdateHeading();
  }

  private void checkTimeDistanceParameters(long minTime, float minDistance){
    if (minTime < 0){
      throw new IllegalArgumentException(String.format(PARAMETER_OUT_OF_BOUNDS_MSG, "minTime"));
    }

    minimumUpdateTime = minTime;

    if (minDistance < 0){
      throw new IllegalArgumentException(String.format(PARAMETER_OUT_OF_BOUNDS_MSG, "minDistance"));
    }

    minimumUpdateDistance = minDistance;
  }

  private void selectProvidersByDefault(){
    if (getLocationManager() != null){
      if (getLocationManager().isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
        selectedLocationProviders.add(LocationManager.NETWORK_PROVIDER);
      }
      if (getLocationManager().isProviderEnabled(LocationManager.GPS_PROVIDER)){
        selectedLocationProviders.add(LocationManager.GPS_PROVIDER);
      }
    }
  }

  @SuppressLint("MissingPermission")
  private void startLocationProviders(){
    if (internalLocationListener == null){
      internalLocationListener = new InternalLocationListener();
    }

    for (String provider : selectedLocationProviders){
      getLocationManager().requestLocationUpdates(
          provider,
          minimumUpdateTime,
          minimumUpdateDistance,
          internalLocationListener);
    }
  }

  private void updateEsriLocation(android.location.Location location, Boolean lastKnown){
    if (location != null){
      // If new location accuracy is two times less than previous one, it will be ignored
      if (lastLocation != null){
        if (location.getAccuracy() > lastLocation.getHorizontalAccuracy() * ACCURACY_THRESHOLD_FACTOR){
          return;
        }
      }

      Location currentLocation = toEsriLocation(location, lastKnown);
      updateLocation(currentLocation);
      lastLocation = currentLocation;
    }
  }

  private void selectProviderByCriteria(Criteria criteria){
    if (getLocationManager() != null){
      String provider = getLocationManager().getBestProvider(criteria, true);
      if (provider != null){
        selectedLocationProviders.add(provider);
      }
    }
  }

  private void selectProviderByUserDefined(String userProvider){
    if (getLocationManager() != null && getLocationManager().getAllProviders().contains(userProvider)){
      selectedLocationProviders.add(userProvider);
    }
  }

  private void startUpdateHeading(){
    if (internalHeadingListener == null){
      internalHeadingListener = new InternalHeadingListener();
    }

    SensorManager manager = getSensorManager();

    manager.registerListener(
        internalHeadingListener,
        manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
        SensorManager.SENSOR_DELAY_UI);
    manager.registerListener(
        internalHeadingListener,
        manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED),
        SensorManager.SENSOR_DELAY_UI);
    manager.registerListener(
        internalHeadingListener,
        manager.getDefaultSensor(Sensor.TYPE_GRAVITY),
        SensorManager.SENSOR_DELAY_UI);
    manager.registerListener(internalHeadingListener,
        manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
        SensorManager.SENSOR_DELAY_UI);
  }

  private void stopUpdateHeading(){
    getSensorManager().unregisterListener(internalHeadingListener);
    if (internalHeadingListener != null){
      internalHeadingListener = null;
    }

    updateHeading(Double.NaN);
  }

  private class InternalLocationListener implements LocationListener {
    private android.location.Location innerAndroidLocation = null;

    @Override
    public void onLocationChanged(android.location.Location location) {
      updateEsriLocation(location, false);
      innerAndroidLocation = location;
    }

    @Override
    public void onProviderEnabled(String provider) {
      // Re-register the enabled provider
      if (selectedLocationProviders.contains(provider)){
        startLocationProviders();
      }
    }

    @Override
    public void onProviderDisabled(String provider) {
      // If only one provider is is selected and that provider is disabled then the last known location is used as
      // the current location
      if (selectedLocationProviders.contains(provider) && selectedLocationProviders.size() == 1) {
        updateEsriLocation(innerAndroidLocation, true);
      }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
      if (selectedLocationProviders.contains(provider)) {
        if (status == LocationProvider.AVAILABLE) {
          startLocationProviders();
        } else {
          // Out of service or temporarily unavailable
          updateEsriLocation(innerAndroidLocation, true);
        }
      }
    }
  }

  private class InternalHeadingListener implements SensorEventListener {
    private float[] gravity = new float[3];
    private float[] geomagnetic = new float[3];
    private float[] rotationMatrixR = new float[9];
    private float[] rotationMatrixI = new float[9];
    private float[] orientation = new float[3];
    private float heading = 0f;

    @Override
    public void onSensorChanged(SensorEvent event) {
      int type = event.sensor.getType();

      if (type == Sensor.TYPE_GRAVITY || type == Sensor.TYPE_ACCELEROMETER_UNCALIBRATED || type == Sensor.TYPE_ACCELEROMETER){
        float[] temp_grav = new float[3];
        System.arraycopy(event.values, 0, temp_grav, 0, 3);
        gravity = lowPassFilter(temp_grav, gravity);
      } else if (type == Sensor.TYPE_MAGNETIC_FIELD){
        geomagnetic = lowPassFilter(event.values.clone(), geomagnetic);
      }

      Boolean success = SensorManager.getRotationMatrix(rotationMatrixR, rotationMatrixI, gravity, geomagnetic);

      if (success){
        SensorManager.getOrientation(rotationMatrixR, orientation);
        this.heading = toDegrees(orientation[0]);

        if (this.heading < 0) {
          heading += 360f;
        }

        updateHeading(heading);
      }
    }

    /**
     * Function to apply low pass filter to smooth out sensor readings. Based upon implementation here:
     * https://www.built.io/blog/applying-low-pass-filter-to-android-sensor-s-readings
     *
     * @since 100.6.0
     */
    private float[] lowPassFilter(float[] input, float[] output){
      if (output == null){
        return input;
      }
      for (int i = 0; i < input.length; i++){
        output[i] = output[i] + 0.1f * (input[i] - output[i]);
      }
      return output;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
  }

  private class InternalNmeaListener implements GpsStatus.NmeaListener {

    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
      if (nmea.startsWith("$GPGGA") || nmea.startsWith("$GNGNS") || nmea.startsWith("$GNGGA")){
        String[] messageParts = nmea.split(",");
        if (messageParts.length < 10){
          return; // Not enough parts
        }

        String mslAltitude = messageParts[9];

        if (mslAltitude.isEmpty()) {
          return;
        }

        Double altitudeParsed = null;

        try {
          altitudeParsed = Double.parseDouble(mslAltitude);
        } catch (NumberFormatException e){
          //
          return;
        }

        if (altitudeParsed != null){
          lastNmeaHeight = altitudeParsed;
          lastNmeaUpdateTimestamp = timestamp;
        }
      }
    }
  }

  private double getOffsetAltitude(double longitude, double latitude, double altitude){
    switch (getAltitudeAdjustmentMode()){
      case NMEA_PARSED_MSL:
        if (lastNmeaUpdateTimestamp > 0 && lastNmeaHeight != Double.NaN){
          return lastNmeaHeight + getManualOffset();
        } else {
          return altitude + getManualOffset();
        }
      case GPS_RAW_ELLIPSOID:
        return altitude + getManualOffset();
      case LOCALLY_ADJUSTED_WITH_EGM2008:
        throw new IllegalStateException("Geoid adjustment not implemented");
    }
    return altitude;
  }

  private LocationDataSource.Location toEsriLocation(android.location.Location location, Boolean lastKnown){
    Point position;
    if (location.hasAltitude()){
      double offsetAltitude = getOffsetAltitude(location.getLongitude(), location.getLatitude(), location.getAltitude());
      position = new Point(
          location.getLongitude(),
          location.getLatitude(),
          offsetAltitude,
          SpatialReferences.getWgs84());
    } else {
      position = new Point(location.getLongitude(), location.getLatitude(), SpatialReferences.getWgs84());
    }

    java.util.Calendar timeStamp = createCalendarFromTimeInMillis(location.getTime());

    double verticalAccuracy = Double.NaN;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
      verticalAccuracy = location.getVerticalAccuracyMeters();
    }

    return new LocationDataSource.Location(
        position,
        (double)location.getAccuracy(),
        verticalAccuracy,
        (double)location.getSpeed(),
        (double)location.getBearing(),
        lastKnown,
        timeStamp);
  }

  private Calendar createCalendarFromTimeInMillis(long timeInMillis){
    Calendar returnValue = new GregorianCalendar(TimeZone.getTimeZone("UTC"), ENGLISH);
    returnValue.setTimeInMillis(timeInMillis);
    return returnValue;
  }

  /**
   * Converts an angle measured in radians to an approximately
   * equivalent angle measured in degrees.  The conversion from
   * radians to degrees is generally inexact.
   *
   * @since 100.6.0
   */
  private float toDegrees(float input){
    return input * 180f / (float)Math.PI;
  }
} 
