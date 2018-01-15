/*------------------------------------------------------*/
/* Trace functions                                      */
/*                                                      */
/* QkeyDefines.h                                    */
/*                                                      */
/* Copyright (C) QTEC Inc.                              */
/* All rights reserved                                  */
/*                                                      */
/* Author                                               */
/*    pengwb (pengwb@qtec.cn)                           */
/*                                                      */
/* History                                              */
/*    2017/11/09  Create                                */
/*                                                      */
/*------------------------------------------------------*/

#ifndef Q_QKEY_DEFINE_H
#define Q_QKEY_DEFINE_H
#include <stdio.h>
/*Define debug_print*/
#if	1	/* And it was asked to emit this code... */
#define	QKEY_DEBUG(fmt, ...)	do {			\
		fprintf(stderr, "[Qkey_SDK]");		\
		fprintf(stderr, fmt,  ##__VA_ARGS__);		\
		fprintf(stderr, " (%s:%d)\n",		\
			__FILE__, __LINE__);		\
	} while(0)
#else
static void QKEY_DEBUG(const char *fmt, ...) { (void)fmt; }
#endif

#if 0
#define	QKEY_DEBUG_INFO(fmt, ...)	do {			\
		fprintf(stderr, "[Qkey_SDK]");		\
		fprintf(stderr, fmt, ##__VA_ARGS__);		\
		fprintf(stderr, " \n"); 	\
	} while(0)
#else
static void QKEY_DEBUG_INFO(const char *fmt, ...) { (void)fmt; }
#endif


/*Define abbreviation*/
typedef char				  	CHAR;
typedef unsigned char			BYTE;
typedef unsigned short			WORD;
typedef int						INT;
typedef unsigned int			UINT;

/*Define some operation*/
#define LOWSHORT(sData)			((sData) & 0xFF)
#define HIGHSHORT(sData)		(((sData) >> 8) & 0xFF)
#define MALLOC(len, type)       (type*)malloc(len)
#define GETSHORT(pApduBuff)		(((*((pApduBuff)+1)) << 8) + (*(pApduBuff)))

#endif
