/*------------------------------------------------------*/
/* Trace functions                                      */
/*                                                      */
/* QkeyFile.h                                    */
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
#ifndef Q_KEY_FILE_H
#define Q_KEY_FILE_H
#ifdef __cplusplus
extern "C" {
#endif
/***********************************************
***函数名称：LoginShock
***函数功能：登录冲击发起
***入口参数：
				1.InIndex设备序列号(必须大于等于 0)
				2.InDevideLen设备id长度
				3.InDeviceId设备id
 				4.InRandom输入的随机数(32Bits)
 ***出口参数：
 				1.OutRandom输出的随机数(32Bits)
***返回值：0表示成功
***备    注：NULL
************************************************/
int LoginShock(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
					unsigned char *InRandom, unsigned char *OutRandom);

/***********************************************
***函数名称：LoginResponse
***函数功能：登录冲击反馈
***入口参数：
				1.InIndex设备序列号(必须大于等于 0)
				2.InDevideLen设备id长度
				3.InDeviceId设备id
 				4.InLoginResp输入的冲击数据(32Bits)
 ***出口参数：
 				1.OutLoginToken输出的冲击反馈的Token数据48Bits
***返回值：0表示成功
***备    注：NULL
************************************************/
int LoginResponse(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						 unsigned char *InLoginResp,  unsigned char *OutLoginToken);

/***********************************************
***函数名称：ModifyPinCode
***函数功能：修改PIN码
***入口参数：
				1.InIndex设备序列号(必须大于等于 0)
				2.InDevideLen设备id长度
				3.InDeviceId设备id
				4.InLoginToken输入的Token数据(48Bits)
				5.InOldPin旧的PIN码(32Bits)
				6.InNewPin新的PIN码(32Bits)
***出口参数：NULL
***返回值：0表示成功
***备    注：NULL
************************************************/
int ModifyPinCode(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *InLoginToken, unsigned char *InOldPin, unsigned char *InNewPin);

/***********************************************
***函数名称：SetUserName
***函数功能：设置用户名
***入口参数：
				1.InIndex设备序列号(必须大于等于 0)
				2.InDevideLen设备id长度
				3.InDeviceId设备id
				4.InLoginToken输入的Token数据(48Bits)
				5.InUserNameLen用户名长度(必须小于等于31Bits)
				6.InUserName用户名
***出口参数：NULL
***返回值：0表示成功
***备    注：NULL
************************************************/
int SetUserName(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *InLoginToken, unsigned char InUserNameLen, unsigned char *InUserName);


/***********************************************
***函数名称：GetUserName
***函数功能：获取用户名称
***入口参数：
				1.InIndex设备序列号(必须大于等于 0)
				2.InDevideLen设备id长度
				3.InDeviceId设备id
				4.OutUserName的buffer长度OutUserNameLen
***出口参数：
				1.OutUserNameLen输出用户名长度(小于等于31Bits)
				2.OutUserName用户名称
***返回值：0表示成功
***备    注：NULL
************************************************/
int GetUserName(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *OutUserNameLen, unsigned char *OutUserName);

/***********************************************
***函数名称：UpdateFileKeys
***函数功能：更新文件加密密钥
***入口参数：
				1.InIndex设备序列号(必须大于等于 0)
				2.InDevideLen设备id长度
				3.InDeviceId设备id
				4.InLoginToken输入的Token数据(48Bits)
				5.InKeyCount输入密钥个数(最多2个密钥)
				6.InKeyInfo输入的密钥信息(长度为InKeyCount x 48)
***出口参数：NULL
***返回值：0表示成功
***备    注：NULL
************************************************/
int UpdateFileKeys(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *InLoginToken, unsigned char InKeyCount, unsigned char *InKeyInfo);

/***********************************************
***函数名称：GetFileKeys
***函数功能：获取文件加密密钥
***入口参数：
				1.InIndex设备序列号(必须大于等于 0)
				2.InDevideLen设备id长度
				3.InDeviceId设备id
				4.InLoginToken输入的Token数据(48Bits)
***出口参数：
				1.OutKeyId获取的文件加密密钥ID (16Bits)
				2.OutKey获取的文件加密密钥(32Bits)
				3.OutEncKey根密钥加密的密钥信息(64Bits)
***返回值：0表示成功
***备    注：NULL
************************************************/
int GetFileKeys(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *InLoginToken, unsigned char *OutKeyId, 
						unsigned char *OutKey, unsigned char *OutEncKey);

/***********************************************
***函数名称：GetFileKeysAck
***函数功能：获取文件加密密钥反馈
***入口参数：
				1.InIndex设备序列号(必须大于等于 0)
				2.InDevideLen设备id长度
				3.InDeviceId设备id
				4.InLoginToken输入的Token数据(48Bits)
				5.InKeyId输入的密钥ID(16Bits)
***出口参数：NULL
***返回值：0表示成功
***备    注：NULL
************************************************/
int GetFileKeysAck(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *InLoginToken, unsigned char *InKeyId);

/***********************************************
***函数名称：GetFileKeysNumber
***函数功能：获取文件加密密钥数量
***入口参数：
				1.InIndex设备序列号(必须大于等于 0)
				2.InDevideLen设备id长度
				3.InDeviceId设备id
				4.InLoginToken输入的Token数据(48Bits)
***出口参数：
				1.OutFileKeyCount输出的密钥个数
***返回值：0表示成功
***备    注：NULL
************************************************/
int GetFileKeysNumber(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *InLoginToken, unsigned short *OutFileKeyCount);

/***********************************************
***函数名称：DecryptFileKey
***函数功能：解密根密钥加密的密钥
***入口参数：
				1.InIndex设备序列号(必须大于等于 0)
				2.InDevideLen设备id长度
				3.InDeviceId设备id
				4.InLoginToken输入的Token数据(48Bits)
				5.InEncKey输入的加密密钥(64Bits)
***出口参数：
				1.OutKeyId输出的解密密钥ID(16Bits)
				2.OutKey输出的解密密钥(32Bits)
***返回值：0表示成功
***备    注：NULL
************************************************/
int DecryptFileKey(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *InLoginToken, unsigned char *InEncKey,
						unsigned char *OutKeyId, unsigned char *OutKey);

/***********************************************
***函数名称：DeleteFileKey
***函数功能：删除密钥
***入口参数：
				1.InIndex设备序列号(必须大于等于 0)
				2.InDevideLen设备id长度
				3.InDeviceId设备id
				4.InLoginToken输入的Token数据(48Bits)
				5.wCount输入的删除密钥个数
***出口参数：NULL
***返回值：0表示成功
***备    注：NULL
************************************************/
int DeleteFileKey(int InIndex, unsigned char InDevideLen, unsigned char *InDeviceId, 
						unsigned char *InLoginToken, unsigned short wCount);



#ifdef __cplusplus
}
#endif

#endif
