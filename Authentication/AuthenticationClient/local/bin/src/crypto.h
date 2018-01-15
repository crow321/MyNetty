/*------------------------------------------------------*/
/* Trace functions                                      */
/*                                                      */
/* Crypto.h                                    */
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

#include "QkeyDefines.h"

#ifndef Q_CRYPTO_H
#define Q_CRYPTO_H

typedef enum CryptTypeEm{
	CRYPTO_NULL = 0,
	CRYPTO_AES_CBC,
	CRYPTO_MAX
}CryptTypeE;


int Encrypt(CryptTypeE eType, BYTE *InSource, int InSourceLen, BYTE *OutDest, int *OutDestLen, BYTE *InKey, int InKeyLen);

int Decrypt(CryptTypeE eType, BYTE *InSource, int InSourceLen, BYTE *OutDest, int *OutDestLen, BYTE *InKey, int InKeyLen);

#endif
