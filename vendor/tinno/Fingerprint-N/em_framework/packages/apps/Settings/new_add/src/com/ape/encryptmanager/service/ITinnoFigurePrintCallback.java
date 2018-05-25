/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: packages/apps/Settings/src/com/ape/encryptmanager/service/ITinnoFigurePrintCallback.aidl
 */
package com.ape.encryptmanager.service;
/** {@hide} */
public interface ITinnoFigurePrintCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.ape.encryptmanager.service.ITinnoFigurePrintCallback
{
private static final java.lang.String DESCRIPTOR = "com.ape.encryptmanager.service.ITinnoFigurePrintCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.ape.encryptmanager.service.ITinnoFigurePrintCallback interface,
 * generating a proxy if needed.
 */
public static com.ape.encryptmanager.service.ITinnoFigurePrintCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.ape.encryptmanager.service.ITinnoFigurePrintCallback))) {
return ((com.ape.encryptmanager.service.ITinnoFigurePrintCallback)iin);
}
return new com.ape.encryptmanager.service.ITinnoFigurePrintCallback.Stub.Proxy(obj);
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
case TRANSACTION_onIdentifyRsp:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
this.onIdentifyRsp(_arg0, _arg1);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.ape.encryptmanager.service.ITinnoFigurePrintCallback
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
@Override public void onIdentifyRsp(int result, int fingerid) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(result);
_data.writeInt(fingerid);
mRemote.transact(Stub.TRANSACTION_onIdentifyRsp, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_onIdentifyRsp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void onIdentifyRsp(int result, int fingerid) throws android.os.RemoteException;
}
