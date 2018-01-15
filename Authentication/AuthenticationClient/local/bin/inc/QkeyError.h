/*------------------------------------------------------*/
/* Trace functions                                      */
/*                                                      */
/* QkeyError.h                                    */
/*                                                      */
/* Copyright (C) QTEC Inc.                              */
/* All rights reserved                                  */
/*                                                      */
/* Author                                               */
/*    pengwb (pengwb@qtec.cn)                           */
/*                                                      */
/* History                                              */
/*    2017/10/08  Create                                */
/*                                                      */
/*------------------------------------------------------*/


#ifndef QKEY_ERROR_H
#define QKEY_ERROR_H

/*Define return type*/
#define QK_SUCCESS                              			0x0000
#define QK_ERROR_TIMEOUT									0x0101
#define QK_ERROR_FOUND										0x0102
#define QK_ERROR_INITIALIZED								0x0103
#define QK_ERROR_MALLOC										0x0104
#define QK_ERROR_ARG										0x0105
#define QK_ERROR_TRANSFER									0x0106
#define QK_ERROR_CHECK										0x0107
#define QK_ERROR_PACK										0x0108
#define QK_ERROR_COMMUNICATION                              0x0401
#define QK_ERROR_DATA										0x0402
#define	QK_ERROR_PROCESS									0x0403
#define QK_ERROR_UNDEFINED									0x0404
#define QK_ERROR_HARDWARE									0x0405
#define QK_ERROR_UNINITIALIZED								0x0406
#define QK_ERROR_DATA_OVERFLOW								0x0407
#define QK_ERROR_DEVICE_ID									0x0408
#define QK_ERROR_KEY_ERROR									0x0409
#define	QK_ERROR_SESSION									0x040a
#define QK_ERROR_VERSION									0x040b
#define QK_ERROR_LOGOUT										0x040c
#define QK_ERROR_PIN_CODE									0x040d
#define QK_ERROR_FILE_KEY_FULL								0x040e
#define QK_ERROR_FILE_KEY_EMPTY								0x040f
#define QK_ERROR_TOKEN_ERRROR								0x0410

#endif
