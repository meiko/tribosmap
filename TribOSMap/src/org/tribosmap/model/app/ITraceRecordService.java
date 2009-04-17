/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/meiko/tribosmap/workspace/TribOSMap/src/org/tribosmap/model/app/ITraceRecordService.aidl
 */
package org.tribosmap.model.app;
import java.lang.String;
import android.os.RemoteException;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Binder;
import android.os.Parcel;
public interface ITraceRecordService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.tribosmap.model.app.ITraceRecordService
{
private static final java.lang.String DESCRIPTOR = "org.tribosmap.model.app.ITraceRecordService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an ITraceRecordService interface,
 * generating a proxy if needed.
 */
public static org.tribosmap.model.app.ITraceRecordService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof org.tribosmap.model.app.ITraceRecordService))) {
return ((org.tribosmap.model.app.ITraceRecordService)iin);
}
return new org.tribosmap.model.app.ITraceRecordService.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_startRecord:
{
data.enforceInterface(DESCRIPTOR);
this.startRecord();
reply.writeNoException();
return true;
}
case TRANSACTION_stopRecord:
{
data.enforceInterface(DESCRIPTOR);
this.stopRecord();
reply.writeNoException();
return true;
}
case TRANSACTION_newMarker:
{
data.enforceInterface(DESCRIPTOR);
this.newMarker();
reply.writeNoException();
return true;
}
case TRANSACTION_isRecording:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isRecording();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_actualTraceId:
{
data.enforceInterface(DESCRIPTOR);
long _result = this.actualTraceId();
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_getPointCount:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getPointCount();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getActualDistanceInMeter:
{
data.enforceInterface(DESCRIPTOR);
double _result = this.getActualDistanceInMeter();
reply.writeNoException();
reply.writeDouble(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.tribosmap.model.app.ITraceRecordService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public void startRecord() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_startRecord, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void stopRecord() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_stopRecord, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void newMarker() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_newMarker, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public boolean isRecording() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isRecording, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public long actualTraceId() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_actualTraceId, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public int getPointCount() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getPointCount, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public double getActualDistanceInMeter() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
double _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getActualDistanceInMeter, _data, _reply, 0);
_reply.readException();
_result = _reply.readDouble();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_startRecord = (IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_stopRecord = (IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_newMarker = (IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_isRecording = (IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_actualTraceId = (IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_getPointCount = (IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_getActualDistanceInMeter = (IBinder.FIRST_CALL_TRANSACTION + 6);
}
public void startRecord() throws android.os.RemoteException;
public void stopRecord() throws android.os.RemoteException;
public void newMarker() throws android.os.RemoteException;
public boolean isRecording() throws android.os.RemoteException;
public long actualTraceId() throws android.os.RemoteException;
public int getPointCount() throws android.os.RemoteException;
public double getActualDistanceInMeter() throws android.os.RemoteException;
}
