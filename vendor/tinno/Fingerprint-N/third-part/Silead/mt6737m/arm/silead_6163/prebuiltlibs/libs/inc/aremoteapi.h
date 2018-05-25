/*  ====================================================================================================
**
**
**  ---------------------------------------------------------------------------------------------------
**
**	File Name:  	
**	
**	Description:	This file contains the interface for the Buffer Control.
**
**					this is kernal code of SW framework.
**					It contributes one of functionalities of SW Platform. 
**
**  ---------------------------------------------------------------------------------------------------
**
**  Author:			Warren Zhao
**
** -------------------------------------------------------------------------
**
**	Change History:
**	
**	Initial revision
**
**====================================================================================================*/

#ifndef _AREMOTEAPI_H_
#define _AREMOTEAPI_H_

#include <assert.h>

#include "metaconsts.h"
#include "metatypes.h"
#include "aostypes.h"
#include "arapi.h"
#include "alog.h"

typedef struct{
	char m_name[OSTAGName_Len+1];
} AInfIPCNameStr_t;

typedef struct{
	long  m_clientid;
} AInfClientInfo_t;

typedef struct{
	char m_RApiClientSideRelayerName[OSTAGName_Len+1];
	long m_clientid;
	long m_context;
} AInfCallContext_t;

/*for pointer, you must input arg as follows:
int ARemoteApiRelayer::onFwdCall(
	char* func_name,
	char* func_args
	)
{
	char* t_pargs = func_args;

	if( strcmp(func_name,"Foo") == 0 )
	{
		int index;
		Foo_t foo, *pfoo;

		ARAPI_MEM_TO_ARGS(t_pargs,int,index);
		ARAPI_MEM_TO_ARGS(t_pargs,Foo_t,foo); or ARAPI_MEM_TO_PTR_ARGS(t_pargs,Foo_t,pfoo) //later one is faster

		Foo(index,&foo); or Foo(index,pfoo); //later one is faster
	}
}
int AInfFpsvcClient::IdentifyCredential(
    int     index,
	Foo_t*  pfoo
    )
{	
	char* t_pargs = RegArgsBuff("IdentifyCredential");

	ARAPI_ARGS_TO_MEM(t_pargs,int,index);
	ARAPI_ARGS_TO_MEM(t_pargs,Foo_t,(*pfoo)); or ARAPI_PTR_ARGS_TO_MEM(t_pargs,Foo_t,pfoo)//both are same preformance

	return Call();
}

*/

//no use externally
#define WZ_TPAD(x)	(((sizeof(x)+7)/8)*8)
#define WZ_LRND(x)	(((x)/8)*8)
#define __ARAPI_MEM_TO_ARGS(porg,len,p,type,arg)	\
	{											\
		volatile void* volatile mem = (p);		\
		assert( ((char*)(p) + WZ_TPAD(type)) < ((char*)(porg) + (len)) );	\
		(arg) = *((type*)mem);					\
		(p) = ((char*)mem) + WZ_TPAD(type);		\
	}

#define __ARAPI_ARGS_TO_MEM(porg,len,p,type,arg)	\
	{											\
		volatile void* volatile mem = (p);		\
		assert( ((char*)(p) + WZ_TPAD(type)) < ((char*)(porg) + (len)) );	\
		*((type*)mem) = (arg);					\
		(p) = ((char*)mem) + WZ_TPAD(type);		\
	}

#define __ARAPI_MEM_TO_PTR_ARGS(porg,len,p,type,ptr)	\
	{										    \
		volatile void* volatile mem = (p);		\
		volatile void* volatile tptr;			\
		assert( ((char*)(p) + WZ_TPAD(type) + WZ_TPAD(unsigned long)) < ((char*)(porg) + (len)) );	\
		(tptr) = (type*)(*((unsigned long*)mem));			\
		(p) = ((char*)mem) + WZ_TPAD(unsigned long);	\
		mem = (p);								\
		if(tptr)									\
		{											\
			(ptr) = (type*)mem;						\
		}											\
		(p) = ((char*)mem) + WZ_TPAD(type);		\
	}

#define __ARAPI_MEM_TO_PTR_ARGS_ONRETURN(porg,len,p,type,ptr)	\
	{										    \
		volatile void* volatile mem = (p);		\
		volatile void* volatile tptr;			\
		assert( ((char*)(p) + WZ_TPAD(type) + WZ_TPAD(unsigned long)) < ((char*)(porg) + (len)) );	\
		(tptr) = (type*)(*((unsigned long*)mem));			\
		(p) = ((char*)mem) + WZ_TPAD(unsigned long);	\
		mem = (p);								\
		if(tptr)									\
		{											\
			*((type*)(ptr)) = *((type*)mem);		\
		}											\
		(p) = ((char*)mem) + WZ_TPAD(type);		\
	}

#define __ARAPI_PTR_ARGS_TO_MEM(porg,len,p,type,ptr)	\
	{										    \
		volatile void* volatile mem = (p);		\
		assert( ((char*)(p) + WZ_TPAD(type) + WZ_TPAD(unsigned long)) < ((char*)(porg) + (len)) );	\
		*((unsigned long*)mem) = (unsigned long)(ptr);	\
		(p) = ((char*)mem) + WZ_TPAD(unsigned long);	\
		mem = (p);									\
		if((ptr))									\
		{										\
			*((type*)mem) = *((type*)(ptr));	\
		}										\
		(p) = ((char*)mem) + WZ_TPAD(type);	\
	}

#define __ARAPI_SKIP_ARGS_INMEM(porg,len,p,type)	\
	{											\
		volatile void* volatile mem = (p);		\
		assert( ((char*)(p) + WZ_TPAD(type)) < ((char*)(porg) + (len)) );	\
		(p) = ((char*)mem) + WZ_TPAD(type);		\
	}

#define __ARAPI_SKIP_PTR_ARGS_INMEM(porg,len,p,type)	\
	{											\
		volatile void* volatile mem = (p);		\
		assert( ((char*)(p) + WZ_TPAD(type) + WZ_TPAD(unsigned long)) < ((char*)(porg) + (len)) );	\
		(p) = ((char*)mem) + WZ_TPAD(type) + WZ_TPAD(unsigned long);		\
	}

///for user call
#define ARAPI_USER_AGRS_LEN						WZ_LRND( OSTAGArgs_Len )

#define ARAPI_MEM_TO_ARGS(p,type,arg)			__ARAPI_MEM_TO_ARGS(m_ptmpOrgArgs,ARAPI_USER_AGRS_LEN,p,type,arg)	

#define ARAPI_ARGS_TO_MEM(p,type,arg)			__ARAPI_ARGS_TO_MEM(m_ptmpOrgArgs,ARAPI_USER_AGRS_LEN,p,type,arg)	

#define ARAPI_MEM_TO_PTR_ARGS(p,type,ptr)		__ARAPI_MEM_TO_PTR_ARGS(m_ptmpOrgArgs,ARAPI_USER_AGRS_LEN,p,type,ptr)	

#define ARAPI_PTR_ARGS_TO_MEM(p,type,ptr)		__ARAPI_PTR_ARGS_TO_MEM(m_ptmpOrgArgs,ARAPI_USER_AGRS_LEN,p,type,ptr)	

#define ARAPI_MEM_TO_PTR_ARGS_ONRETURN(p,type,ptr)		__ARAPI_MEM_TO_PTR_ARGS_ONRETURN(m_ptmpOrgArgs,ARAPI_USER_AGRS_LEN,p,type,ptr)	


#define ARAPI_SKIP_ARGS_INMEM(p,type)			__ARAPI_SKIP_ARGS_INMEM(m_ptmpOrgArgs,ARAPI_USER_AGRS_LEN,p,type)	

#define ARAPI_SKIP_PTR_ARGS_INMEM(p,type)			__ARAPI_SKIP_PTR_ARGS_INMEM(m_ptmpOrgArgs,ARAPI_USER_AGRS_LEN,p,type)	



class AInterface;

class ARemoteApi
{
public:

protected:
	char						m_sRelayerName[OSTAGName_Len+1];
	ARemoteApiClient_t			m_ClientHandle;
	AInterface*					m_pAttachedInf;
	//static ARemoteApi*		s_pRemoteApiRelayer;

	//for internal protection check purpose, pls do not use it outside.
	char*						m_ptmpOrgArgs;

private:

public:
	virtual						~ARemoteApi();

	//static const ARemoteApi*  GetRelayer();
	const char*					GetRelayerName();
	void						SetOrgArgsPtr(char* in_pOrgArgsPtr);

	//return 0 == succ
	virtual int					PingRelayer();
	virtual int					Dump();

	virtual const ARemoteApi*   Client();
	virtual const ARemoteApi*   Relayer();

	Boolean						IsInstanceClient();

protected:
								ARemoteApi(
									const char* inName,
									AInterface* inAttachedInf
									);

	//return 0 == succ
	virtual int					onPingRelayer();

	virtual int					ClientConnect(
		AInfIPCNameStr_t*		inRApiClientSideRelayerName,
		AInfClientInfo_t*		inInfClientInfo
		);
	virtual int					ClientDisConnect();

	virtual int					OnClientConnect(
		AInfIPCNameStr_t*		inRApiClientSideRelayerName,
		AInfClientInfo_t*		inInfClientInfo
		);
	virtual int					OnClientDisConnect();

private:
};

inline const char*       ARemoteApi::GetRelayerName()  { return m_sRelayerName; }
//inline const ARemoteApi* ARemoteApi::GetRelayer()     { return s_pRemoteApiRelayer; }
inline Boolean           ARemoteApi::IsInstanceClient(){ return m_ClientHandle? TRUE:FALSE; }
inline void              ARemoteApi::SetOrgArgsPtr(char* in_pOrgArgsPtr)  { m_ptmpOrgArgs = in_pOrgArgsPtr; }

#endif 
