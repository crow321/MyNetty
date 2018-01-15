/*------------------------------------------------------*/
/* Trace functions                                      */
/*                                                      */
/* HidTrans.h                                    */
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

#ifndef HID_TRANS_H
#define HID_TRANS_H

#include "hidapi.h"
#include "QkeyError.h"
#include "QkeyDefines.h"
#include <string.h>
#include <stdlib.h>
#include <stdio.h>

#ifdef __cplusplus
extern "C" {
#endif

/*Define len*/
#define DEFAULT_DEV_ID_LEN		8
#define DEFAULT_KEY_ID_LEN		16
#define DEFAULT_KEY_LEN			32
#define MAX_BUFF_LEN			1024
#define MAX_DEVICE_COUNT		127
#define DEFAULT_TRANS_LEN		255
#define MAX_TIME_OUT			1000	//1000ms
#define DATA_HEAD_LEN			6	//2Bit FC,2Bit LEN,2Bit Sta.
#define HEAD_STATE_LEN			2
#define MAX_PATH_LEN			32
#define DEFAULT_REPORT_ID		0x00
#define DEFAULT_REPORT_ID_LEN	1
#define QKEY_PRODUCT_ID			0x1234		
#define QKEY_VENDOR_ID			0x925
#define DEFAULT_FILE_VERSION			1
#define DEFAULT_RANDOM_LEN				32
#define DEFAULT_TOKEN_LEN				48
#define DEFAULT_PIN_LEN					32
#define MAX_USER_NAME_LEN				32
#define DEFAULT_KEY_INFO_LEN			(DEFAULT_KEY_ID_LEN + DEFAULT_KEY_LEN)

/*Define get head info*/
#define LW_FC(pApduBuff)		(*((pApduBuff)))
#define HI_FC(pApduBuff)		(*((pApduBuff)+1))
#define DC(pApduBuff)			(HI_FC((pApduBuff)) & 0xFF)
#define MD(pApduBuff)			(((pApduBuff)>>8)&0xFE)
#define LEN(pApduBuff)			(((*((pApduBuff)+3)) << 8) + (*((pApduBuff)+2)))
#define RC(pApduBuff)			(((*((pApduBuff)+5)) << 8) + (*((pApduBuff)+4)))


/*Define direction*/
#define DC_CLIENT_TO_QSHELL		0x00
#define DC_QSHELL_TO_CLIENT		0x01

/*Define Funcode*/
typedef enum EQkFunCodeT{
	FC_RESERVED = 0x0000,
	FC_INIT_QUANTUMKEYS,
	FC_GEN_STARTINFO,
	FC_GEN_AUTHINFO,
	FC_CHECK_AUTHINFO,
	FC_ENCRYPT_QUANTUMKEYS,
	FC_DECRYPT_QUANTUMKEYS,
	FC_UPDATE_ROOTKEY,
	FC_CONFIRM_ROOTKEY,
	FC_RESET_PIN = 0x000C,
	FC_RESTART,
	FC_GET_DEVICE_INFO,
	FC_RESET_LOAD,
	FC_LOGIN_SHOCK = 0x0201,
	FC_LOGIN_RESPONSE,
	FC_MODIFY_PIN_CODE,
	FC_SET_USER_NAME,
	FC_GET_USER_NAME,
	FC_UPDATE_FILE_KEYS,
	FC_GET_FILE_KEYS,
	FC_GET_FILES_ACK,
	FC_GET_FILES_NUMBER,
	FC_DECRYPT_FILE_KEY,
	FC_DECRYPT_DELETE_KEY
}EQkFunCode;

void FuncSwapShort(WORD* p_value);

int PrintData(unsigned char *pName, unsigned char *pData, int nLen);

int HidTransGetCount();

int FcLittleEndianAddShortToData(BYTE *pData, const WORD wLen);

int HidTransSendRec(const EQkFunCode eFunCode, int nIndex, const BYTE *pSendData, const UINT nSendLen, BYTE *pRecvData, UINT *pRecvLen);

#ifdef __cplusplus
}
#endif

#endif
