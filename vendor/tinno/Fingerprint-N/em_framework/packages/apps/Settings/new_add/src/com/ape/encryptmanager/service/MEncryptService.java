/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: packages/apps/Settings/src/com/ape/encryptmanager/service/MEncryptService.aidl
 */
package com.ape.encryptmanager.service;
public interface MEncryptService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.ape.encryptmanager.service.MEncryptService
{
private static final java.lang.String DESCRIPTOR = "com.ape.encryptmanager.service.MEncryptService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.ape.encryptmanager.service.MEncryptService interface,
 * generating a proxy if needed.
 */
public static com.ape.encryptmanager.service.MEncryptService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.ape.encryptmanager.service.MEncryptService))) {
return ((com.ape.encryptmanager.service.MEncryptService)iin);
}
return new com.ape.encryptmanager.service.MEncryptService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_isMustSetPassword:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isMustSetPassword();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_checkToken:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.checkToken(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_checkPassword:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _result = this.checkPassword(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getPasswordType:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getPasswordType();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setNumberPassword:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _result = this.setNumberPassword(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_setPatternPassword:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _result = this.setPatternPassword(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getToken:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getToken();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getPasswordQuestionID:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getPasswordQuestionID();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getQuestionList:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String[] _result = this.getQuestionList();
reply.writeNoException();
reply.writeStringArray(_result);
return true;
}
case TRANSACTION_checkPasswordQuestion:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.checkPasswordQuestion(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_changePatternPassword:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _result = this.changePatternPassword(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_changeNumberPassword:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _result = this.changeNumberPassword(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_changePasswordQuestion:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _result = this.changePasswordQuestion(_arg0, _arg1);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getPasswordLength:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getPasswordLength();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getAppFingerprint:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.getAppFingerprint();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getFileFingerprintLock:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.getFileFingerprintLock();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getGalleryFingerprintLock:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.getGalleryFingerprintLock();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getLeftScrollSwitch:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.getLeftScrollSwitch();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getRightScrollSwitch:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.getRightScrollSwitch();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getDownScrollSwitch:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.getDownScrollSwitch();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_geClickSwitch:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.geClickSwitch();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getLongClickSwitch:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.getLongClickSwitch();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getDoubleClickSwitch:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.getDoubleClickSwitch();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getLongClickData:
{
data.enforceInterface(DESCRIPTOR);
java.util.Map _result = this.getLongClickData();
reply.writeNoException();
reply.writeMap(_result);
return true;
}
case TRANSACTION_registerCallback:
{
data.enforceInterface(DESCRIPTOR);
com.ape.encryptmanager.service.ITinnoFigurePrintCallback _arg0;
_arg0 = com.ape.encryptmanager.service.ITinnoFigurePrintCallback.Stub.asInterface(data.readStrongBinder());
boolean _result = this.registerCallback(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_unregisterCallback:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.unregisterCallback();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_verifyStart:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.verifyStart();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_isSupportFingerprint:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isSupportFingerprint();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getQuickBootAppDatas:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<com.ape.encryptmanager.service.AppData> _result = this.getQuickBootAppDatas();
reply.writeNoException();
reply.writeTypedList(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.ape.encryptmanager.service.MEncryptService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/******Password Interface********/
@Override public boolean isMustSetPassword() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isMustSetPassword, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean checkToken(java.lang.String token) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(token);
mRemote.transact(Stub.TRANSACTION_checkToken, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String checkPassword(java.lang.String password) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(password);
mRemote.transact(Stub.TRANSACTION_checkPassword, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getPasswordType() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getPasswordType, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String setNumberPassword(java.lang.String password, int question, java.lang.String answer) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(password);
_data.writeInt(question);
_data.writeString(answer);
mRemote.transact(Stub.TRANSACTION_setNumberPassword, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String setPatternPassword(java.lang.String password, int question, java.lang.String answer) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(password);
_data.writeInt(question);
_data.writeString(answer);
mRemote.transact(Stub.TRANSACTION_setPatternPassword, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getToken() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getToken, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getPasswordQuestionID() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getPasswordQuestionID, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String[] getQuestionList() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getQuestionList, _data, _reply, 0);
_reply.readException();
_result = _reply.createStringArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean checkPasswordQuestion(java.lang.String security) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(security);
mRemote.transact(Stub.TRANSACTION_checkPasswordQuestion, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String changePatternPassword(java.lang.String password) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(password);
mRemote.transact(Stub.TRANSACTION_changePatternPassword, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String changeNumberPassword(java.lang.String password) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(password);
mRemote.transact(Stub.TRANSACTION_changeNumberPassword, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String changePasswordQuestion(int question, java.lang.String answer) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(question);
_data.writeString(answer);
mRemote.transact(Stub.TRANSACTION_changePasswordQuestion, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getPasswordLength() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getPasswordLength, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/******Switch Interface********/
@Override public boolean getAppFingerprint() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getAppFingerprint, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean getFileFingerprintLock() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getFileFingerprintLock, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean getGalleryFingerprintLock() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getGalleryFingerprintLock, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean getLeftScrollSwitch() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getLeftScrollSwitch, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean getRightScrollSwitch() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getRightScrollSwitch, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean getDownScrollSwitch() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getDownScrollSwitch, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean geClickSwitch() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_geClickSwitch, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean getLongClickSwitch() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getLongClickSwitch, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean getDoubleClickSwitch() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getDoubleClickSwitch, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.util.Map getLongClickData() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.Map _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getLongClickData, _data, _reply, 0);
_reply.readException();
java.lang.ClassLoader cl = (java.lang.ClassLoader)this.getClass().getClassLoader();
_result = _reply.readHashMap(cl);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
// KEY include	 "PRE_APP_NAME","PRE_PACKAGE_NAME", "PRE_CLASS_NAME"
/******Fingerprint  verify Interface********/
@Override public boolean registerCallback(com.ape.encryptmanager.service.ITinnoFigurePrintCallback Callback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((Callback!=null))?(Callback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerCallback, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean unregisterCallback() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_unregisterCallback, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean verifyStart() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_verifyStart, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isSupportFingerprint() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isSupportFingerprint, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.util.List<com.ape.encryptmanager.service.AppData> getQuickBootAppDatas() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List<com.ape.encryptmanager.service.AppData> _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getQuickBootAppDatas, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArrayList(com.ape.encryptmanager.service.AppData.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_isMustSetPassword = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_checkToken = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_checkPassword = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getPasswordType = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_setNumberPassword = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_setPatternPassword = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_getToken = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_getPasswordQuestionID = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_getQuestionList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_checkPasswordQuestion = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_changePatternPassword = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_changeNumberPassword = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_changePasswordQuestion = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_getPasswordLength = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_getAppFingerprint = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
static final int TRANSACTION_getFileFingerprintLock = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
static final int TRANSACTION_getGalleryFingerprintLock = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
static final int TRANSACTION_getLeftScrollSwitch = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
static final int TRANSACTION_getRightScrollSwitch = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
static final int TRANSACTION_getDownScrollSwitch = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
static final int TRANSACTION_geClickSwitch = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
static final int TRANSACTION_getLongClickSwitch = (android.os.IBinder.FIRST_CALL_TRANSACTION + 21);
static final int TRANSACTION_getDoubleClickSwitch = (android.os.IBinder.FIRST_CALL_TRANSACTION + 22);
static final int TRANSACTION_getLongClickData = (android.os.IBinder.FIRST_CALL_TRANSACTION + 23);
static final int TRANSACTION_registerCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 24);
static final int TRANSACTION_unregisterCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 25);
static final int TRANSACTION_verifyStart = (android.os.IBinder.FIRST_CALL_TRANSACTION + 26);
static final int TRANSACTION_isSupportFingerprint = (android.os.IBinder.FIRST_CALL_TRANSACTION + 27);
static final int TRANSACTION_getQuickBootAppDatas = (android.os.IBinder.FIRST_CALL_TRANSACTION + 28);
}
/******Password Interface********/
public boolean isMustSetPassword() throws android.os.RemoteException;
public boolean checkToken(java.lang.String token) throws android.os.RemoteException;
public java.lang.String checkPassword(java.lang.String password) throws android.os.RemoteException;
public int getPasswordType() throws android.os.RemoteException;
public java.lang.String setNumberPassword(java.lang.String password, int question, java.lang.String answer) throws android.os.RemoteException;
public java.lang.String setPatternPassword(java.lang.String password, int question, java.lang.String answer) throws android.os.RemoteException;
public java.lang.String getToken() throws android.os.RemoteException;
public int getPasswordQuestionID() throws android.os.RemoteException;
public java.lang.String[] getQuestionList() throws android.os.RemoteException;
public boolean checkPasswordQuestion(java.lang.String security) throws android.os.RemoteException;
public java.lang.String changePatternPassword(java.lang.String password) throws android.os.RemoteException;
public java.lang.String changeNumberPassword(java.lang.String password) throws android.os.RemoteException;
public java.lang.String changePasswordQuestion(int question, java.lang.String answer) throws android.os.RemoteException;
public int getPasswordLength() throws android.os.RemoteException;
/******Switch Interface********/
public boolean getAppFingerprint() throws android.os.RemoteException;
public boolean getFileFingerprintLock() throws android.os.RemoteException;
public boolean getGalleryFingerprintLock() throws android.os.RemoteException;
public boolean getLeftScrollSwitch() throws android.os.RemoteException;
public boolean getRightScrollSwitch() throws android.os.RemoteException;
public boolean getDownScrollSwitch() throws android.os.RemoteException;
public boolean geClickSwitch() throws android.os.RemoteException;
public boolean getLongClickSwitch() throws android.os.RemoteException;
public boolean getDoubleClickSwitch() throws android.os.RemoteException;
public java.util.Map getLongClickData() throws android.os.RemoteException;
// KEY include	 "PRE_APP_NAME","PRE_PACKAGE_NAME", "PRE_CLASS_NAME"
/******Fingerprint  verify Interface********/
public boolean registerCallback(com.ape.encryptmanager.service.ITinnoFigurePrintCallback Callback) throws android.os.RemoteException;
public boolean unregisterCallback() throws android.os.RemoteException;
public boolean verifyStart() throws android.os.RemoteException;
public boolean isSupportFingerprint() throws android.os.RemoteException;
public java.util.List<com.ape.encryptmanager.service.AppData> getQuickBootAppDatas() throws android.os.RemoteException;
}
