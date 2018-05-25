/*  ====================================================================================================
**
**
**  ---------------------------------------------------------------------------------------------------
**
**	File Name:  	metatypes.h
**	
**	Description:	This file contains the meta types definitions.
**
**					this is kernal code of SW framework.
**					It contributes one of functionalities of SW Platform. 
**					If the checkin is CR not PR, to add change History to this file head part 
**					will be appreciated.
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

#ifndef _META_TYPES_H_
#define _META_TYPES_H_

typedef unsigned char	UInt8;
typedef unsigned short	UInt16;
typedef signed   char	Int8;
typedef signed   short	Int16;
typedef unsigned char	Boolean;
typedef char			INT8;
typedef short			INT16;
typedef unsigned int	UInt32;
typedef signed   int	Int32;
typedef unsigned int	BitField;
typedef int				INT32;
typedef unsigned long	   PTR_T;
typedef unsigned long long UInt64;
typedef signed long long   Int64;
typedef signed long long   INT64;


//#ifdef __GCC_BUILTIN_ARCH64__ sizeof(long) == 8
//#else
//#endif

#define ATrue  1
#define AFalse 0

#ifndef TRUE
#define TRUE  1
#endif
#ifndef FALSE
#define FALSE 0
#endif

#ifdef NULL
#undef					NULL
#endif
#define NULL 0
//unify the meta types......
#ifdef BOOL
#undef					BOOL
#endif

#ifdef CHAR
#undef					CHAR
#endif

#ifdef SHORT
#undef					SHORT
#endif

#ifdef LONG
#undef					LONG
#endif

#ifdef	VOID
#undef					VOID
#endif

#ifndef	INLINE
#define INLINE			__inline
#endif

typedef UInt16 wchar;

#define	TRUSTOS_TBASE		0
#define	TRUSTOS_WATCH		1
#define	TRUSTOS_BEANPOD		2
#define	TRUSTOS_QSEE		3
#define	TRUSTOS_HISI		4


#define	STRING_LEN	255

typedef struct
{
    char content[STRING_LEN+1];
}__attribute__((packed))   String_t;

#endif

