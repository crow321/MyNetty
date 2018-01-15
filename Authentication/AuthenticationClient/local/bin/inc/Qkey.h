/*------------------------------------------------------*/
/* Trace functions                                      */
/*                                                      */
/* Qkey.h                                    */
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
#ifndef Q_KEY_H
#define Q_KEY_H
#ifdef __cplusplus
extern "C" {
#endif
/***********************************************
***函数名称：GenStartInfo
***函数功能：生成启动接入认证信息
***入口参数：
				1,OutDevideId的buffer长度OutDeviceIdLen
				2,OutStartAuthInfod的buffer长度OutStartAuthLen
***出口参数：
				1,设备标志OutDevideId
				2,设备标志长度OutDeviceIdLen
				3,启动认证信息OutStartAuthInfo
				4,启动认证信息长度OutStartAuthLen
***返回值：0表示成功
***备    注：NULL
************************************************/
int GenStartInfo(unsigned char *OutDevideId, unsigned char *OutDeviceIdLen, 
				unsigned char *OutStartAuthInfo, unsigned char *OutStartAuthLen);

/***********************************************
***函数名称：GenAuthInfo
***函数功能：生成接入认证信息
***入口参数：
				1,挑战信息InChallengeInfo
				2,挑战信息长度InChallengeLen
				3,OutReqAuthInfo的buffer长度OutReqAuthLen
***出口参数：
				1,启动认证信息OutReqAuthInfo
				2,启动认证信息长度OutReqAuthLen
***返回值：0表示成功
***备    注：NULL
************************************************/
int GenAuthInfo(const unsigned char * InChallengeInfo, const unsigned char InChallengeLen, 
				unsigned char *OutReqAuthInfo, unsigned char *OutReqAuthLen);

/***********************************************
***函数名称：CheckAuthInfo
***函数功能：校验服务器端认证信息
***入口参数：
				1,服务端认证信息InAuthInfo
				2,服务端认证信息长度InAuthInfoLen
				3,OutSessionKey的buffer长度OutSessionKeyLen
***出口参数：
				1,会话密钥标志OutSessionId、16字节
				2,会话密钥OutSessionKey
				3,会话密钥长度OutSessionKeyLen
***返回值：0表示成功
***备    注：NULL
************************************************/
int CheckAuthInfo(const unsigned char* InAuthInfo, const unsigned char InAuthInfoLen, 
				unsigned char* OutSessionId, unsigned char *OutSessionKey, unsigned char *OutSessionKeyLen);

/***********************************************
***函数名称：EncryptQuantumKeys
***函数功能：使用根密钥加密数据
***入口参数：
				1,加密信息InputData
				2,加密信息长度InputDataLen
				3,EncryptedData的buffer长度EncryptedDataLen
***出口参数：
				1,加密后内容信息EncryptedData
				2,加密后内容信息长度EncryptedDataLen
***返回值：0表示成功
***备    注：NULL
************************************************/
int EncryptQuantumKeys(const unsigned char* InputData, const unsigned int InputDataLen, 
				unsigned char* EncryptedData, unsigned int *EncryptedDataLen);

/***********************************************
***函数名称：DecryptQuantumKeys
***函数功能：使用根密钥解密应用密钥
***入口参数：
				1,加密信息InEncryptedData
				2,加密信息长度InEncryptedDatalen
				3,DecryptedKeyInfo的buffer长度DecryptedKeyInfoLen
***出口参数：
				1,解密后内容信息DecryptedKeyInfo(key_id+key_value,key_id+key_value...)
				2,解密后内容信息长度DecryptedKeyInfoLen
***返回值：0表示成功
***备    注：NULL
************************************************/
int DecryptQuantumKeys(const unsigned char* InEncryptedData, const unsigned int InEncryptedDatalen, 
				unsigned char* DecryptedKeyInfo, unsigned int *DecryptedKeyInfoLen);

/***********************************************
***函数名称：UpdateRootKey
***函数功能：更新根密钥
***入口参数：
				1,RootKey的buffer长度RootKeylen
***出口参数：
				1,更新根密钥信息RootKey
				2,更新根密钥信息长度RootKeylen
***返回值：0表示成功
***备    注：NULL
************************************************/
int UpdateRootKey(unsigned char* RootKey, unsigned char *RootKeylen);

/***********************************************
***函数名称：ConfirmRootKey
***函数功能：确认更新根密钥
***入口参数：
				1,确认更新根密钥信息RootKey
				2,确认更新根密钥信息长度RootKeyLen
***返回值：0表示成功
***备    注：NULL
************************************************/
int ConfirmRootKey(const unsigned char* RootKey, const unsigned char RootKeyLen);
#ifdef __cplusplus
}
#endif

#endif
