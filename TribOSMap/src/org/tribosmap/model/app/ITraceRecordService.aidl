package org.tribosmap.model.app;

interface ITraceRecordService {
  
  void startRecord();
  
  void stopRecord();
  
  void newMarker();
  
  boolean isRecording();
  
  long actualTraceId();
  
  int getPointCount();
  
  double getActualDistanceInMeter();
}

