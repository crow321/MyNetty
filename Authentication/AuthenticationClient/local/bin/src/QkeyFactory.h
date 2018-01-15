/*------------------------------------------------------*/
/* Trace functions                                      */
/*                                                      */
/* QkeyFactory.h                                    */
/*                                                      */
/* Copyright (C) QTEC Inc.                              */
/* All rights reserved                                  */
/*                                                      */
/* Author                                               */
/*    pengwb (pengwb@qtec.cn)                           */
/*                                                      */
/* History                                              */
/*    2017/10/18  Create                                */
/*                                                      */
/*------------------------------------------------------*/
#ifndef Q_KEY_FACTORY_H
#define Q_KEY_FACTORY_H
#ifdef __cplusplus
extern "C" {
#endif
/***********************************************
***函数名称：InitQuantumKeys
***函数功能：初始化密钥信息-仅安全模式下可调用
***入口参数：根密钥Rootkey(48字节)、量子密钥QuantumKey(32字节)
***出口参数：NULL
***返回值：0表示成功
***备    注：NULL
************************************************/
int InitQuantumKeys(const unsigned char* Rootkey, const unsigned char *QuantumKey);

/***********************************************
***函数名称：Reset
***函数功能：恢复usb-key下载态-仅安全模式下可调用
***入口参数：NULL
***返回值：0表示成功
***备    注：NULL
************************************************/
int ResetLoad();


/***********************************************
***函数名称：GetDeviceCount
***函数功能：获取usb-key数量
***入口参数：NULL
***出口参数：NULL
***返回值：0表示无usb设备
***备    注：NULL
************************************************/
int GetDeviceCount();
	
/***********************************************
***函数名称：GetDeviceInfo
***函数功能：根据设备序列号获取设备信息
***入口参数：
				1,nIndex设备序列
				2,pDeviceId的buffer长度pDevLen
***出口参数：
				 1,pVersion设备版本号
				 2,pDevLen设备id长度
				 3,pDeviceId设备id
				 4,pVendorId供应商id
				 5,pProductId产品id
***返回值：0表示成功
***备    注：NULL
************************************************/
int GetDeviceInfo(int nIndex, unsigned short *pVersion, unsigned short *pDevLen, 
			unsigned char *pDeviceId, unsigned short *pVendorId, unsigned short *pProductId);


/***********************************************
***函数名称：InitQuantumKeysByIndex
***函数功能：根据设备序列号初始化密钥
***入口参数：根密钥Rootkey(48字节)、量子密钥QuantumKey(32字节)、序列号nIndex
***出口参数：NULL
***返回值：0表示成功
***备    注：NULL
************************************************/
int InitQuantumKeysByIndex(const int nIndex, const unsigned char* Rootkey, const unsigned char *QuantumKey);


/***********************************************
***函数名称：DecryptQuantumKeysByIndex
***函数功能：根据设备序列号解密密钥
***入口参数：
				1,加密信息InEncryptedData
				2,加密信息长度InEncryptedDatalen
				3,DecryptedKeyInfo的buffer长度DecryptedKeyInfoLen
				4,nIndex使用第几个设备进行解密
***出口参数：
				1,解密后内容信息DecryptedKeyInfo(key_id+key_value,key_id+key_value...)
				2,解密后内容信息长度DecryptedKeyInfoLen
***返回值：0表示成功
***备    注：NULL
************************************************/
int DecryptQuantumKeysByIndex(const int nIndex, const unsigned char* InEncryptedData, 
				const unsigned int InEncryptedDatalen, unsigned char* DecryptedKeyInfo, unsigned int *DecryptedKeyInfoLen);

/***********************************************
***函数名称：RestartByIndex
***函数功能：根据设备序列号重启设备
***入口参数：NULL
***出口参数：NULL
***返回值：0表示成功
***备    注：NULL
************************************************/
int RestartByIndex(int nIndex);


/***********************************************
***函数名称：ResetPINByIndex
***函数功能：根据设备序列号重置PIN码
***入口参数：NULL
***出口参数：NULL
***返回值：0表示成功
***备    注：NULL
************************************************/
int ResetPINByIndex(int nIndex);


#ifdef __cplusplus
}
#endif

#endif
